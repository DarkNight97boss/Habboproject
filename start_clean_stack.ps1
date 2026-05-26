# ============================================================
#  ASTERIA CORE — Avvio stack di sviluppo
#  - MariaDB (XAMPP)        : 3306   (DB: ms)
#  - Asteria Core 3.5.5     : 3000 game / 3001 rcon / 2096 WS(nitro)
#    [motore: Arcturus Morningstar 3.5.5, GPL-3.0]
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

# --- Server Asteria Core ---
# La directory $EMU resta "Arcturus-MS" perche' e' il working dir storico
# del build artifact (Habbo-3.5.5-jar-with-dependencies.jar). Rinominare la
# dir richiederebbe spostare i log esistenti, config.ini, dumps, plugin.
if (Test-Port 2096) { Write-Host "[OK] Server Asteria Core gia' attivo (porta 2096)" }
else {
    Write-Host "[..] Avvio Asteria Core 3.5.5 (JDK 11)..."
    # JVM tuning production-ready (target: >5k utenti).
    # Razionale di ogni flag in EMU/Dockerfile; di seguito i piu' importanti:
    #   -Xms=-Xmx                 : niente resize pause (alloca subito tutto)
    #   G1GC + MaxGCPauseMillis    : low-pause GC per real-time game
    #   UseStringDeduplication     : G1 dedupa string duplicate (chat ripetute)
    #   AlwaysPreTouch             : touch pages all'avvio (boot piu' lento,
    #                                 ma pause GC piu' prevedibili dopo)
    #   ExitOnOutOfMemoryError     : crash subito invece di degradare zombico
    #   HeapDumpOnOutOfMemoryError : dump per post-mortem
    #   leakDetection=disabled     : Netty leak detector OFF in prod (1-2% CPU)
    #   allocator=pooled           : ridondante con la config Java in Server.java
    #                                 ma rinforza il default anche se config viene
    #                                 letta dopo Netty init.
    #
    # Heap default: 2-4GB. Aumenta a -Xmx6g/8g se hai >32GB di RAM e >3k utenti
    # contemporanei. Lascia almeno 25% RAM all'OS + MariaDB + altri servizi.
    $dumpDir = "$EMU\dumps"
    if (-not (Test-Path $dumpDir)) { New-Item -ItemType Directory -Path $dumpDir | Out-Null }
    $jvmArgs = @(
        '-Xms2g','-Xmx4g',
        '-XX:+UseG1GC','-XX:MaxGCPauseMillis=100',
        '-XX:+UseStringDeduplication','-XX:+AlwaysPreTouch',
        '-XX:MaxMetaspaceSize=256m',
        '-XX:+HeapDumpOnOutOfMemoryError', "-XX:HeapDumpPath=$dumpDir",
        '-XX:+ExitOnOutOfMemoryError',
        '-Dfile.encoding=UTF-8','-Djava.net.preferIPv4Stack=true',
        '-Dio.netty.allocator.type=pooled',
        '-Dio.netty.leakDetection.level=disabled',
        '-jar','Habbo-3.5.5-jar-with-dependencies.jar'
    )
    Start-Process -FilePath $JAVA -ArgumentList $jvmArgs -WorkingDirectory $EMU -RedirectStandardOutput "$EMU\emu.out.log" -RedirectStandardError "$EMU\emu.err.log" -WindowStyle Hidden
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
