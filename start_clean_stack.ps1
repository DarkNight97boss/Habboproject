# ============================================================
#  AVVIO STACK PULITO
#  - MariaDB (XAMPP)        : 3306   (DB: ms)
#  - Arcturus MS 3.5.5      : 3000 game / 3001 rcon / 2096 WS(nitro)
#  - Client Nitro (statico) : 8090   (cartella client-dist)
#
#  Uso:  powershell -ExecutionPolicy Bypass -File start_clean_stack.ps1
#  Poi:  http://localhost:8090/index.html?sso=<auth_ticket>&room=<id>
# ============================================================
$ErrorActionPreference = 'Continue'
$G    = 'C:\Users\vincenzokatrielgiuva\Desktop\Github'
$JAVA = 'C:\Program Files\Microsoft\jdk-11.0.31.11-hotspot\bin\java.exe'
$PHP  = 'C:\xampp\php\php.exe'
$MYSQLD = 'C:\xampp\mysql\bin\mysqld.exe'
$EMU  = "$G\Arcturus-MS"
$DIST = "$G\client-dist"

function Test-Port($p){ [bool](Get-NetTCPConnection -State Listen -LocalPort $p -ErrorAction SilentlyContinue) }

# --- MariaDB ---
if (Test-Port 3306) { Write-Host "[OK] MariaDB gia' attivo" }
else { Write-Host "[..] Avvio MariaDB..."; Start-Process -FilePath $MYSQLD -ArgumentList '--defaults-file=C:\xampp\mysql\bin\my.ini','--standalone' -WindowStyle Hidden; Start-Sleep 6 }

# --- Emulatore Arcturus MS ---
if (Test-Port 2096) { Write-Host "[OK] Emulatore gia' attivo (2096)" }
else {
    Write-Host "[..] Avvio Arcturus MS 3.5.5 (JDK 11)..."
    # JVM tuning per molti utenti: heap 512MB-2GB (alza -Xmx in base alla RAM) + G1GC a bassa latenza
    Start-Process -FilePath $JAVA -ArgumentList '-Xms512m','-Xmx2g','-XX:+UseG1GC','-XX:MaxGCPauseMillis=100','-jar','Habbo-3.5.5-jar-with-dependencies.jar' -WorkingDirectory $EMU -RedirectStandardOutput "$EMU\emu.out.log" -RedirectStandardError "$EMU\emu.err.log" -WindowStyle Hidden
    Start-Sleep 28
}

# --- Client Nitro (server statico) ---
if (Test-Port 8090) { Write-Host "[OK] Client gia' servito (8090)" }
else {
    Write-Host "[..] Servo il client su :8090..."
    Start-Process -FilePath $PHP -ArgumentList '-S','0.0.0.0:8090','-t',$DIST -WorkingDirectory $DIST -WindowStyle Hidden
    Start-Sleep 2
}

Write-Host ""
Write-Host "==================== STATO ===================="
foreach($p in 3306,3000,3001,2096,8090){ $s = if(Test-Port $p){'ONLINE '}else{'offline'}; Write-Host (" porta {0,-5}: {1}" -f $p,$s) }
Write-Host "Client:  http://localhost:8090/index.html?sso=<auth_ticket>&room=<id>"
Write-Host "Utente test (DB ms): imposta auth_ticket e usalo come ?sso="
Write-Host "  mysql -u root ms -e \"UPDATE users SET auth_ticket='MIASSO', online='0' WHERE username='test';\""
Write-Host "  poi apri: http://localhost:8090/index.html?sso=MIASSO&room=57"
Write-Host "==============================================="
