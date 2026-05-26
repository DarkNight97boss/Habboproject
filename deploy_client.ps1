# ============================================================
#  DEPLOY CLIENT NITRO -> live (:8090) + CMS (:8080)
#
#  Builda il client nitro-react e copia i nuovi bundle (index.html
#  + assets/*) in ENTRAMBE le directory deployate:
#    - Github\client-dist          (php -S 0.0.0.0:8090, link diretto)
#    - Github\Habboproject\CMS\react (Zabbo CMS, iframe :8080/react/)
#
#  Preserva sempre: renderer-config.json, ui-config.json,
#  e le sottocartelle di asset (bundled/, gamedata/, sounds/, images/,
#  c_images/, dcr/) -- vengono SOLO sostituiti i file vite.
#
#  Uso:  powershell -File deploy_client.ps1
# ============================================================
$ErrorActionPreference = 'Stop'
$G       = 'C:\Users\vincenzokatrielgiuva\Desktop\Github'
$NITRO   = "$G\nitro-react"
$DIST    = "$NITRO\dist"
$LIVE    = "$G\client-dist"
$CMS_REACT = "$G\Habboproject\CMS\react"

# Path node nel PATH (per assicurare yarn build trovi node).
$NODE = 'C:\Program Files\nodejs'
if (Test-Path $NODE) { $env:Path = "$NODE;$env:Path" }

if (-not (Test-Path $NITRO)) { throw "nitro-react non trovato in $NITRO" }
if (-not (Test-Path $LIVE))      { throw "client-dist non trovato in $LIVE" }
if (-not (Test-Path $CMS_REACT)) { throw "CMS\react non trovato in $CMS_REACT" }

Write-Host '== 1. yarn build (vite) ...' -ForegroundColor Cyan
Set-Location $NITRO
& yarn build
if ($LASTEXITCODE -ne 0) { throw "yarn build failed (exit $LASTEXITCODE)" }
if (-not (Test-Path "$DIST\index.html")) { throw "build produsse nessun dist\index.html" }

function Deploy-Target([string]$target, [string]$label) {
    Write-Host "== 2. deploy -> $label ($target) ..." -ForegroundColor Cyan
    $assetsDir = Join-Path $target 'assets'
    if (-not (Test-Path $assetsDir)) { New-Item -ItemType Directory -Path $assetsDir | Out-Null }
    # Rimuovi solo i file (file-level), preserva eventuali sottocartelle.
    Get-ChildItem $assetsDir -File -ErrorAction SilentlyContinue | Remove-Item -Force
    Copy-Item "$DIST\assets\*" $assetsDir -Force
    Copy-Item "$DIST\index.html" (Join-Path $target 'index.html') -Force
    Get-ChildItem $assetsDir -File | ForEach-Object { Write-Host ("   + assets/" + $_.Name) }
    Write-Host ("   + index.html (" + (Get-Item (Join-Path $target 'index.html')).Length + " bytes)")
    # NB: NON tocchiamo renderer-config.json / ui-config.json / sottocartelle asset.
}

Deploy-Target $LIVE 'CLIENT-DIST (:8090 diretto)'
Deploy-Target $CMS_REACT 'CMS/react (:8080 iframe)'

Write-Host ''
Write-Host '== Deploy completo. Verifico HTTP ==' -ForegroundColor Green
foreach ($url in @('http://127.0.0.1:8090/index.html', 'http://127.0.0.1:8080/react/index.html')) {
    try {
        $r = Invoke-WebRequest -UseBasicParsing -Uri $url -TimeoutSec 5 -ErrorAction Stop
        Write-Host ("  {0,-55} HTTP {1} ({2} bytes)" -f $url, $r.StatusCode, $r.RawContentLength)
    } catch {
        Write-Host ("  {0,-55} {1}" -f $url, $_.Exception.Message) -ForegroundColor Yellow
    }
}
Write-Host ''
Write-Host 'Test: http://localhost:8090/index.html?sso=<ticket>&room=57   (diretto)'
Write-Host '      http://127.0.0.1:8080/client                            (via CMS, rigenera SSO)'
