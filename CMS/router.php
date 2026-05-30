<?php
// Local dev router for PHP built-in server (replaces IIS/Helicon .htaccess rewrites)
$base = __DIR__;
$uri  = urldecode(parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH));
$path = trim($uri, '/');
$seg  = $path === '' ? array() : explode('/', $path);

// Permissive CORS for /react/* (Nitro asset/gamedata path) so dev clients on
// other ports (e.g. 8091 Nitro-V3) can fetch cross-origin without browser blocks.
if (strncmp($path, 'react/', 6) === 0) {
    header('Access-Control-Allow-Origin: *');
    header('Access-Control-Allow-Methods: GET, HEAD, OPTIONS');
    header('Access-Control-Allow-Headers: *');
    if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') { http_response_code(204); exit; }
}

if ($path !== '') {
    $target = $base . DIRECTORY_SEPARATOR . str_replace('/', DIRECTORY_SEPARATOR, $path);

    // Existing static file -> let the built-in server serve it
    if (is_file($target)) {
        if (strtolower(pathinfo($target, PATHINFO_EXTENSION)) === 'php') {
            chdir(dirname($target));
            require $target;
            return true;
        }
        // For /react/* we MUST serve manually to preserve CORS headers
        // (PHP -S's internal static-file handler drops any headers set in router.php
        // when we `return false`). PHP 5.6 compatible: no null-coalescing operator.
        if (strncmp($path, 'react/', 6) === 0) {
            $ext = strtolower(pathinfo($target, PATHINFO_EXTENSION));
            $mimeMap = array(
                'json' => 'application/json',
                'js'   => 'application/javascript',
                'css'  => 'text/css',
                'png'  => 'image/png',
                'jpg'  => 'image/jpeg',
                'jpeg' => 'image/jpeg',
                'gif'  => 'image/gif',
                'svg'  => 'image/svg+xml',
                'mp3'  => 'audio/mpeg',
                'html' => 'text/html',
                'nitro'=> 'application/octet-stream',
            );
            $mime = isset($mimeMap[$ext]) ? $mimeMap[$ext] : 'application/octet-stream';
            header('Content-Type: ' . $mime);
            header('Content-Length: ' . filesize($target));
            readfile($target);
            return true;
        }
        return false;
    }

    // Subdirectory app with its own index.php (e.g. /ase housekeeping, /findretros)
    if (is_dir($target) && is_file($target . DIRECTORY_SEPARATOR . 'index.php')) {
        chdir($target);
        if (isset($seg[1])) { $_GET['url'] = $seg[1]; }
        if (isset($seg[2])) { $_GET['id']  = $seg[2]; }
        require $target . DIRECTORY_SEPARATOR . 'index.php';
        return true;
    }

    // First segment is a directory but request points deeper to a real file
    $first = $base . DIRECTORY_SEPARATOR . $seg[0];
    if (is_dir($first)) {
        // remainder under a dir app: route to that dir's index.php with url
        if (is_file($first . DIRECTORY_SEPARATOR . 'index.php')) {
            chdir($first);
            $_GET['url'] = isset($seg[1]) ? $seg[1] : '';
            if (isset($seg[2])) { $_GET['id'] = $seg[2]; }
            require $first . DIRECTORY_SEPARATOR . 'index.php';
            return true;
        }
    }
}

// Missing files under /react/ (Nitro client assets) MUST return 404,
// otherwise the CMS HTML page is served with 200 and the client tries to
// parse it as a .nitro/image -> "Offset outside bounds of DataView"/decode errors.
if (isset($seg[0]) && $seg[0] === 'react') {
    http_response_code(404);
    header('Content-Type: text/plain');
    echo 'Not Found';
    return true;
}

// The Nitro client (vite build + nitro runtime) uses ROOT-absolute paths for
// EVERYTHING: bundles, configs, gamedata, bundled assets, etc. The files live
// under react/, not at the CMS document root, so without this re-route the CMS
// HTML page would be served (200) and the client would parse HTML as JS / JSON
// / .nitro -> "Configuration Failed" / decode errors.
$nitroRootPaths = array(
    'assets',     // vite bundles (index-*.js, vendor-*.js, ...)
    'src',        // vite raw assets (fonts/css/svg)
    'bundled',    // .nitro asset libraries
    'gamedata',   // FigureMap, FurnitureData, ExternalTexts, ...
    'c_images',   // badges and album textures
    'dcr',        // furni icons
    'sounds',
    'images',
);
$nitroRootFiles = array(
    'renderer-config.json',
    'ui-config.json',
    'site.webmanifest',
    'browserconfig.xml',
    'safari-pinned-tab.svg',
);
if (isset($seg[0]) && (in_array($seg[0], $nitroRootPaths, true) || in_array($seg[0], $nitroRootFiles, true))) {
    $target = $base . DIRECTORY_SEPARATOR . 'react' . str_replace('/', DIRECTORY_SEPARATOR, $uri);
    if (is_file($target)) {
        $ext = strtolower(pathinfo($target, PATHINFO_EXTENSION));
        $mime = array(
            'js'    => 'text/javascript; charset=UTF-8',
            'mjs'   => 'text/javascript; charset=UTF-8',
            'css'   => 'text/css; charset=UTF-8',
            'json'  => 'application/json; charset=UTF-8',
            'svg'   => 'image/svg+xml',
            'map'   => 'application/json; charset=UTF-8',
            'png'   => 'image/png',
            'jpg'   => 'image/jpeg',
            'jpeg'  => 'image/jpeg',
            'gif'   => 'image/gif',
            'woff'  => 'font/woff',
            'woff2' => 'font/woff2',
            'ttf'   => 'font/ttf',
            'mp3'   => 'audio/mpeg',
            'nitro' => 'application/octet-stream',
            'xml'   => 'application/xml; charset=UTF-8',
        );
        if (isset($mime[$ext])) header('Content-Type: ' . $mime[$ext]);
        readfile($target);
        return true;
    }
    http_response_code(404);
    header('Content-Type: text/plain');
    echo 'Not Found';
    return true;
}

// Pretty routes handled by the main CMS index.php
$s0 = isset($seg[0]) ? $seg[0] : '';
$_GET['url'] = $s0;
if ($s0 === 'home'  && isset($seg[1])) { $_GET['user'] = $seg[1]; }
if ($s0 === 'news'  && isset($seg[1])) { $_GET['id']   = $seg[1]; }
if ($s0 === 'staff' && isset($seg[1])) { $_GET['rank'] = $seg[1]; }

chdir($base);
require $base . DIRECTORY_SEPARATOR . 'index.php';
