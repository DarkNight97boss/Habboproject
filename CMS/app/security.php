<?php
/**
 * Central security helpers for the CMS (Habboproject).
 * - Cloudflare-aware client IP resolution (anti X-Forwarded-For spoofing)
 * - Security response headers
 * - CSRF protection (token helper + Origin/Referer verification for POST)
 *
 * Loaded early from global.php. Safe to include more than once.
 */

if (!defined('IN_INDEX')) { die('Sorry, you cannot access this file.'); }

if (!function_exists('hp_ip_in_cidr')) {
    /** Return true if $ip is inside the $cidr range (IPv4 or IPv6). */
    function hp_ip_in_cidr($ip, $cidr)
    {
        if (strpos($cidr, '/') === false) { return $ip === $cidr; }
        list($subnet, $bits) = explode('/', $cidr, 2);
        $bits = (int)$bits;

        $ipBin     = @inet_pton($ip);
        $subnetBin = @inet_pton($subnet);
        if ($ipBin === false || $subnetBin === false) { return false; }
        if (strlen($ipBin) !== strlen($subnetBin)) { return false; } // mixing v4/v6

        $bytes = intdiv($bits, 8);
        $rem   = $bits % 8;
        if ($bytes > 0 && substr($ipBin, 0, $bytes) !== substr($subnetBin, 0, $bytes)) { return false; }
        if ($rem === 0) { return true; }
        $mask = chr(0xff << (8 - $rem) & 0xff);
        return (ord($ipBin[$bytes]) & ord($mask)) === (ord($subnetBin[$bytes]) & ord($mask));
    }
}

if (!function_exists('hp_cloudflare_ranges')) {
    /** Cloudflare published edge IP ranges (https://www.cloudflare.com/ips/). */
    function hp_cloudflare_ranges()
    {
        return array(
            // IPv4
            '173.245.48.0/20', '103.21.244.0/22', '103.22.200.0/22', '103.31.4.0/22',
            '141.101.64.0/18', '108.162.192.0/18', '190.93.240.0/20', '188.114.96.0/20',
            '197.234.240.0/22', '198.41.128.0/17', '162.158.0.0/15', '104.16.0.0/13',
            '104.24.0.0/14', '172.64.0.0/13', '131.0.72.0/22',
            // IPv6
            '2400:cb00::/32', '2606:4700::/32', '2803:f800::/32', '2405:b500::/32',
            '2405:8100::/32', '2a06:98c0::/29', '2c0f:f248::/32',
        );
    }
}

if (!function_exists('hp_is_trusted_proxy')) {
    /** A request peer we trust to set X-Forwarded-For / CF-Connecting-IP. */
    function hp_is_trusted_proxy($ip)
    {
        if (in_array($ip, array('127.0.0.1', '::1'), true)) { return true; }
        foreach (hp_cloudflare_ranges() as $cidr) {
            if (hp_ip_in_cidr($ip, $cidr)) { return true; }
        }
        return false;
    }
}

if (!function_exists('hp_resolve_client_ip')) {
    /**
     * Resolve the real client IP into $_SERVER['REMOTE_ADDR'], but ONLY honor
     * proxy headers when the actual connection comes from a trusted proxy
     * (loopback reverse-proxy or a Cloudflare edge). Idempotent.
     */
    function hp_resolve_client_ip()
    {
        if (empty($_SERVER['REMOTE_ADDR'])) { return; }
        if (!hp_is_trusted_proxy($_SERVER['REMOTE_ADDR'])) { return; }

        if (isset($_SERVER['HTTP_CF_CONNECTING_IP']) && filter_var($_SERVER['HTTP_CF_CONNECTING_IP'], FILTER_VALIDATE_IP)) {
            $_SERVER['REMOTE_ADDR'] = $_SERVER['HTTP_CF_CONNECTING_IP'];
            return;
        }
        if (isset($_SERVER['HTTP_X_FORWARDED_FOR'])) {
            $first = trim(explode(',', $_SERVER['HTTP_X_FORWARDED_FOR'])[0]);
            if (filter_var($first, FILTER_VALIDATE_IP)) { $_SERVER['REMOTE_ADDR'] = $first; }
        }
    }
}

if (!function_exists('hp_is_https')) {
    /** True if the user<->edge connection is HTTPS (direct or via CF/proxy). */
    function hp_is_https()
    {
        if (!empty($_SERVER['HTTPS']) && strtolower($_SERVER['HTTPS']) !== 'off') { return true; }
        if (isset($_SERVER['HTTP_X_FORWARDED_PROTO']) && strtolower($_SERVER['HTTP_X_FORWARDED_PROTO']) === 'https') { return true; }
        if (isset($_SERVER['HTTP_CF_VISITOR']) && strpos($_SERVER['HTTP_CF_VISITOR'], 'https') !== false) { return true; }
        return false;
    }
}

if (!function_exists('hp_send_security_headers')) {
    /** Defense-in-depth response headers. */
    function hp_send_security_headers()
    {
        if (headers_sent()) { return; }
        header('X-Frame-Options: SAMEORIGIN');            // clickjacking
        header('X-Content-Type-Options: nosniff');        // MIME sniffing
        header('Referrer-Policy: strict-origin-when-cross-origin');
        header('X-XSS-Protection: 0');                    // rely on CSP/escaping, disable buggy legacy filter
        if (hp_is_https()) {
            header('Strict-Transport-Security: max-age=31536000; includeSubDomains');
        }
    }
}

if (!function_exists('hp_csrf_token')) {
    /** Get (or lazily create) the per-session CSRF token. */
    function hp_csrf_token()
    {
        if (empty($_SESSION['csrf_token'])) {
            $_SESSION['csrf_token'] = function_exists('random_bytes')
                ? bin2hex(random_bytes(32))
                : bin2hex(openssl_random_pseudo_bytes(32));
        }
        return $_SESSION['csrf_token'];
    }
}

if (!function_exists('hp_csrf_field')) {
    /** Hidden input to embed inside <form> for explicit per-form CSRF tokens. */
    function hp_csrf_field()
    {
        return '<input type="hidden" name="csrf_token" value="' . htmlspecialchars(hp_csrf_token(), ENT_QUOTES, 'UTF-8') . '" />';
    }
}

if (!function_exists('hp_verify_csrf')) {
    /**
     * CSRF protection for state-changing requests. Returns true if the request
     * is allowed. Strategy (defense-in-depth, on top of the SameSite=Lax cookie):
     *   1. A valid csrf_token field always passes.
     *   2. Otherwise the Origin/Referer header (when present) MUST match the host
     *      the request was sent to. A mismatch is blocked. Absent headers are
     *      allowed (real CSRF attacks always carry the attacker's Origin/Referer).
     */
    function hp_verify_csrf()
    {
        $method = isset($_SERVER['REQUEST_METHOD']) ? strtoupper($_SERVER['REQUEST_METHOD']) : 'GET';
        if (!in_array($method, array('POST', 'PUT', 'DELETE', 'PATCH'), true)) { return true; }

        // Explicit token wins.
        if (!empty($_POST['csrf_token']) && !empty($_SESSION['csrf_token'])
            && hash_equals($_SESSION['csrf_token'], (string)$_POST['csrf_token'])) {
            return true;
        }

        $host = isset($_SERVER['HTTP_HOST']) ? $_SERVER['HTTP_HOST'] : '';
        if ($host === '') { return true; }
        $allowed = array('http://' . $host, 'https://' . $host);

        if (!empty($_SERVER['HTTP_ORIGIN'])) {
            return in_array($_SERVER['HTTP_ORIGIN'], $allowed, true);
        }
        if (!empty($_SERVER['HTTP_REFERER'])) {
            foreach ($allowed as $base) {
                if (strpos($_SERVER['HTTP_REFERER'], $base . '/') === 0 || $_SERVER['HTTP_REFERER'] === $base) {
                    return true;
                }
            }
            return false;
        }
        return true; // no Origin and no Referer -> not a browser CSRF vector
    }
}
