# ============================================================
#  DEPLOY NITRO-V3 -> CMS (:8080) + standalone (:8091)
#
#  Builda Nitro-V3 e copia i file vite generati in:
#    - Github\Habboproject\CMS\react   (CMS iframe :8080/react/)
#    - Github\Nitro-V3\dist            (standalone PHP server :8091)
#
#  Preserva sempre, perche' shared con altre install:
#    bundled/  gamedata/  sounds/  images/  c_images/  dcr/
#  Sovrascrive solo: index.html, assets/, configuration/, src/,
#                    plugins/, .vite/ (entry-related, no asset shared)
#
#  Sostituisce il vecchio deploy_client.ps1 (che buildava nitro-react).
#  Il backup di nitro-react resta in CMS/react/_legacy-nitro-react/.
#
#  Uso:  powershell -File deploy_client_v3.ps1
# ============================================================
$ErrorActionPreference = 'Stop'
$G       = 'C:\Users\vincenzokatrielgiuva\Desktop\Github'
$NITRO   = "$G\Nitro-V3"
$DIST    = "$NITRO\dist"
$CMS_REACT = "$G\Habboproject\CMS\react"

# Path node nel PATH (per assicurare yarn build trovi node).
$NODE = 'C:\Program Files\nodejs'
if (Test-Path $NODE) { $env:Path = "$NODE;$env:Path" }

if (-not (Test-Path $NITRO)) { throw "Nitro-V3 non trovato in $NITRO" }
if (-not (Test-Path $CMS_REACT)) { throw "CMS\react non trovato in $CMS_REACT" }

Write-Host '== 1. yarn build (vite + rolldown) ...' -ForegroundColor Cyan
Set-Location $NITRO
& yarn build
if ($LASTEXITCODE -ne 0) { throw "yarn build failed (exit $LASTEXITCODE)" }
if (-not (Test-Path "$DIST\index.html")) { throw "build produsse nessun dist\index.html" }

# router.php (cors-friendly PHP -S) sopravvive ai rebuild perche' sta in
# Nitro-V3\public\router.php (vite lo copia in dist/), ma per sicurezza
# se manca lo ricreiamo dal public.
if ((-not (Test-Path "$DIST\router.php")) -and (Test-Path "$NITRO\public\router.php")) {
    Copy-Item "$NITRO\public\router.php" "$DIST\router.php" -Force
}

function Deploy-Target([string]$target, [string]$label) {
    Write-Host "== 2. deploy -> $label ($target) ..." -ForegroundColor Cyan

    # Per ognuno dei "blocchi" che il vite build produce: SE esiste nel dist,
    # sostituisci interamente in target (rimuovendo il vecchio).
    # NON tocchiamo bundled/, gamedata/, sounds/, images/, c_images/, dcr/
    # (sono asset condivisi, gestiti separatamente dal pipeline assets).
    $entries = @('index.html', 'assets', 'configuration', 'src', 'plugins', '.vite')
    foreach ($entry in $entries) {
        $src = Join-Path $DIST $entry
        $dst = Join-Path $target $entry
        if (-not (Test-Path $src)) { continue }
        if (Test-Path $dst) { Remove-Item $dst -Recurse -Force }
        Copy-Item $src $dst -Recurse -Force
        $kind = if ((Get-Item $src).PSIsContainer) { '/ (dir)' } else { '' }
        Write-Host ("   + $entry$kind")
    }
}

Deploy-Target $CMS_REACT 'CMS/react (:8080/react/ via iframe :8080/client)'

Write-Host ''
Write-Host '== Deploy completo. Verifico HTTP ==' -ForegroundColor Green
foreach ($url in @('http://127.0.0.1:8080/react/index.html', 'http://127.0.0.1:8080/react/.vite/manifest.json', 'http://127.0.0.1:8080/react/configuration/client-mode.json')) {
    try {
        $r = Invoke-WebRequest -UseBasicParsing -Uri $url -TimeoutSec 5 -ErrorAction Stop
        Write-Host ("  {0,-70} HTTP {1} ({2} bytes)" -f $url, $r.StatusCode, $r.RawContentLength)
    } catch {
        Write-Host ("  {0,-70} {1}" -f $url, $_.Exception.Message) -ForegroundColor Yellow
    }
}
Write-Host ''
Write-Host 'Test diretto:   http://127.0.0.1:8080/react/index.html?sso=<ticket>'
Write-Host 'Test via CMS:   http://127.0.0.1:8080/client   (login richiesto, rigenera SSO automaticamente)'
Write-Host ''
Write-Host 'Per rollback a nitro-react: mv CMS/react/_legacy-nitro-react/* CMS/react/' -ForegroundColor DarkGray
