<?php
// CORS-friendly static router for serving the Nitro-V3 build via PHP -S.
// Adds permissive CORS headers so the client (on :8091) can fetch
// gamedata / bundled assets from the CMS (:8080) without browser blocks.
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: *');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') { http_response_code(204); exit; }

$path = parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH);
$file = __DIR__ . $path;

// Existing file? Let PHP serve it (returns false to delegate).
if ($path !== '/' && file_exists($file) && !is_dir($file)) {
    return false;
}

// SPA fallback: serve index.html so client-side routing works.
if (file_exists(__DIR__ . '/index.html')) {
    readfile(__DIR__ . '/index.html');
    return true;
}

http_response_code(404);
echo 'Not found';
return true;
