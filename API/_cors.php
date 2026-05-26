<?php
/**
 * Centralized CORS handling for the API.
 *
 * Why this exists:
 *   - The previous "Access-Control-Allow-Origin: *" default leaks across
 *     origins when combined with cookies/Authorization (browsers refuse
 *     "*"+credentials but the absence of a tight default is itself a smell).
 *   - We want a single place to maintain the trusted-origin list so
 *     audio.php, extra.php, index.php, etc. don't drift apart.
 *
 * Behaviour:
 *   - If the request Origin header matches one of $CORS_ALLOWED_ORIGINS
 *     (exact match), echo it back as ACAO and allow credentials.
 *   - Otherwise emit "ACAO: null" — modern browsers treat "null" as a
 *     non-origin and reject the response for cross-origin reads. This is
 *     the safe default for an API that shouldn't be consumed by random
 *     sites.
 *   - OPTIONS preflight is responded to early (204) so the upstream
 *     handler doesn't see it.
 *
 * To add an origin: append to $CORS_ALLOWED_ORIGINS below. Keep it short.
 */

// Edit ONLY this array to widen/narrow the CORS surface.
$CORS_ALLOWED_ORIGINS = [
    'http://127.0.0.1:8080',
    'http://localhost:8080',
    // 'https://your-cms.example.com',  // <-- prod, uncomment when ready
];

$origin = isset($_SERVER['HTTP_ORIGIN']) ? $_SERVER['HTTP_ORIGIN'] : '';
if ($origin !== '' && in_array($origin, $CORS_ALLOWED_ORIGINS, true)) {
    header('Access-Control-Allow-Origin: ' . $origin);
    header('Access-Control-Allow-Credentials: true');
    header('Vary: Origin');
} else {
    // Safe default: reject cross-origin reads.
    header('Access-Control-Allow-Origin: null');
}
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization');
header('Access-Control-Max-Age: 3600');

// Short-circuit preflight before the include/route layers.
if (isset($_SERVER['REQUEST_METHOD']) && $_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(204);
    exit;
}
