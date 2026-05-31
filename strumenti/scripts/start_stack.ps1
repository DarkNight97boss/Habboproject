# ============================================================
#  Habboproject - avvio completo dello stack (Windows ARM64)
#  Avvia: MariaDB, Emulatore Arcturus, CMS, API, (Nitro)
#  Uso:  powershell -ExecutionPolicy Bypass -File start_stack.ps1
# ============================================================

$ErrorActionPreference = 'Continue'

$ROOT     = 'C:\Users\vincenzokatrielgiuva\Desktop\Github\Habboproject'
$XAMPP    = 'C:\xampp'
$JAVA11   = 'C:\Program Files\Microsoft\jdk-11.0.31.11-hotspot\bin\java.exe'
$PHP56    = 'C:\php56\php.exe'
$PHP56INI = 'C:\php56\php.ini'
$PHP8     = "$XAMPP\php\php.exe"
$NITROWEB = 'C:\HabboNitro'   # web root del client Nitro (creato dopo la build)

function Test-Port($port) {
    return [bool](Get-NetTCPConnection -State Listen -LocalPort $port -ErrorAction SilentlyContinue)
}

# --- 1) MariaDB (3306) ---
if (Test-Port 3306) { Write-Host "[OK] MariaDB gia' in ascolto su 3306" }
else {
    Write-Host "[..] Avvio MariaDB..."
    Start-Process -FilePath "$XAMPP\mysql\bin\mysqld.exe" -ArgumentList "--defaults-file=$XAMPP\mysql\bin\my.ini","--standalone" -WindowStyle Hidden
    Start-Sleep -Seconds 6
    if (Test-Port 3306) { Write-Host "[OK] MariaDB avviato" } else { Write-Host "[!!] MariaDB non risponde" }
}

# --- 2) Emulatore Arcturus (game 3000, RCON 3001, Nitro WS 2096) - richiede JDK 11 ---
if (Test-Port 3000) { Write-Host "[OK] Emulatore gia' in ascolto su 3000" }
else {
    Write-Host "[..] Avvio Emulatore (JDK 11)..."
    Start-Process -FilePath $JAVA11 -ArgumentList '-jar','target\Habbo-3.5.5-jar-with-dependencies.jar' `
        -WorkingDirectory "$ROOT\EMU" -RedirectStandardOutput "$ROOT\EMU\emu.log" -RedirectStandardError "$ROOT\EMU\emu.err.log" -WindowStyle Hidden
    Start-Sleep -Seconds 25
    if (Test-Port 3000) { Write-Host "[OK] Emulatore avviato (3000/3001/2096)" } else { Write-Host "[!!] Emulatore non risponde - vedi EMU\emu.log" }
}

# --- 3) CMS (PHP 5.6) su 8080 ---
if (Test-Port 8080) { Write-Host "[OK] CMS gia' in ascolto su 8080" }
else {
    Write-Host "[..] Avvio CMS (PHP 5.6)..."
    Start-Process -FilePath $PHP56 -ArgumentList '-c',$PHP56INI,'-S','0.0.0.0:8080','-t',"$ROOT\CMS","$ROOT\CMS\router.php" `
        -WorkingDirectory "$ROOT\CMS" -RedirectStandardOutput "$ROOT\CMS\cms_server.log" -RedirectStandardError "$ROOT\CMS\cms_server.err.log" -WindowStyle Hidden
    Start-Sleep -Seconds 3
    if (Test-Port 8080) { Write-Host "[OK] CMS su http://localhost:8080" } else { Write-Host "[!!] CMS non risponde" }
}

# --- 4) API (PHP 8) su 8081 ---
if (Test-Port 8081) { Write-Host "[OK] API gia' in ascolto su 8081" }
else {
    Write-Host "[..] Avvio API (PHP 8)..."
    Start-Process -FilePath $PHP8 -ArgumentList '-S','0.0.0.0:8081','-t',"$ROOT\API" `
        -WorkingDirectory "$ROOT\API" -RedirectStandardOutput "$ROOT\API\api_server.log" -RedirectStandardError "$ROOT\API\api_server.err.log" -WindowStyle Hidden
    Start-Sleep -Seconds 3
    if (Test-Port 8081) { Write-Host "[OK] API su http://localhost:8081" } else { Write-Host "[!!] API non risponde" }
}

# --- 5) Nitro: servito dal CMS stesso su /react/ (cartella CMS\react) ---
if (Test-Path "$ROOT\CMS\react\index.html") {
    Write-Host "[OK] Nitro presente in CMS\react -> http://127.0.0.1:8080/react/  (login -> /client)"
} else {
    Write-Host "[--] Nitro non ancora buildato. Esegui build_nitro.ps1 (crea CMS\react) + asset."
}

Write-Host ""
Write-Host "==================== STATO ===================="
foreach ($p in 3306,3000,3001,2096,8080,8081) {
    $s = if (Test-Port $p) { 'ONLINE ' } else { 'offline' }
    Write-Host (" porta {0,-5} : {1}" -f $p, $s)
}
Write-Host "CMS:  http://127.0.0.1:8080   |  API: http://127.0.0.1:8081"
Write-Host "Login test: utente 'test'  password 'test1234'  (poi /client per Nitro)"
Write-Host "==============================================="
