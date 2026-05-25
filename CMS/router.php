<?php
// Local dev router for PHP built-in server (replaces IIS/Helicon .htaccess rewrites)
$base = __DIR__;
$uri  = urldecode(parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH));
$path = trim($uri, '/');
$seg  = $path === '' ? array() : explode('/', $path);

if ($path !== '') {
    $target = $base . DIRECTORY_SEPARATOR . str_replace('/', DIRECTORY_SEPARATOR, $path);

    // Existing static file -> let the built-in server serve it
    if (is_file($target)) {
        if (strtolower(pathinfo($target, PATHINFO_EXTENSION)) === 'php') {
            chdir(dirname($target));
            require $target;
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

// Pretty routes handled by the main CMS index.php
$s0 = isset($seg[0]) ? $seg[0] : '';
$_GET['url'] = $s0;
if ($s0 === 'home'  && isset($seg[1])) { $_GET['user'] = $seg[1]; }
if ($s0 === 'news'  && isset($seg[1])) { $_GET['id']   = $seg[1]; }
if ($s0 === 'staff' && isset($seg[1])) { $_GET['rank'] = $seg[1]; }

chdir($base);
require $base . DIRECTORY_SEPARATOR . 'index.php';
