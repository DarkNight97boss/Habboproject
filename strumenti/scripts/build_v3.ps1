# ============================================================
#  Build Nitro V3 (duckietm/Nitro-V3 + Nitro_Render_V3)
#  Abbinato al converter all-in-1 (formato V3).
#  Installer non-interattivo con gli URL del setup LOCALE.
#  Output -> CMS\react\  (preserva bundled/ gamedata/ images/ sounds/)
#
#  Uso:  powershell -ExecutionPolicy Bypass -File build_v3.ps1
# ============================================================
$ErrorActionPreference = 'Stop'

$SRC      = 'C:\Users\vincenzokatrielgiuva\Desktop\Github\Nitro-V3'
$CMSREACT = 'C:\Users\vincenzokatrielgiuva\Desktop\Github\Habboproject\CMS\react'
$NODE     = 'C:\Program Files\nodejs'
$env:Path = "$NODE;$env:Path"

# V3 usa base RELATIVA (./) di default: funziona servito da /react/ senza
# rompere il loader rolldown. NON forziamo VITE_BASE.
if ($env:VITE_BASE) { Remove-Item Env:\VITE_BASE -ErrorAction SilentlyContinue }

Write-Host "== Node: $(node --version)"
Set-Location $SRC
corepack enable

Write-Host "== Eseguo l'installer V3 (clona renderer + install + config + build)..."
node install.mjs `
  --non-interactive --skip-prompts `
  --json-mode=legacy `
  --socket-url=ws://127.0.0.1:2096 `
  --api-url=http://127.0.0.1:2096 `
  --asset-url=http://127.0.0.1:8080/react/bundled `
  --image-library-url=http://127.0.0.1:8080/react/c_images/ `
  --hof-furni-url=http://127.0.0.1:8080/react/c_images/dcr/hof_furni `
  --plain-config-base-url=http://127.0.0.1:8080/react/configuration `
  --plain-gamedata-base-url=http://127.0.0.1:8080/react/gamedata `
  --camera-url=http://127.0.0.1:8081/camera `
  --thumbnails-url=http://127.0.0.1:8081/camera/thumbnail `
  --habbopages-url=/gamedata/habbopages `
  --api-base-url=http://127.0.0.1:8081 `
  --url-prefix=

if (-not (Test-Path "$SRC\dist\index.html")) { throw "Build V3 fallita: dist\index.html assente. Incolla l'output dell'installer." }

Write-Host "== Pulizia vecchi artefatti client (mantengo gli asset dati)..."
foreach ($p in 'assets','src','index.html','configuration','plugins','text_translate') {
    $tp = Join-Path $CMSREACT $p
    if (Test-Path $tp) { Remove-Item $tp -Recurse -Force }
}

Write-Host "== Copio dist V3 in $CMSREACT (bundled/gamedata/images/sounds restano)..."
New-Item -ItemType Directory -Force -Path $CMSREACT | Out-Null
Copy-Item "$SRC\dist\*" "$CMSREACT\" -Recurse -Force

# L'installer lascia gamedata.url/images.url ai default d'esempio: li correggo ai percorsi locali
Write-Host "== Patch renderer-config (gamedata/images/effetti/pong) ..."
$cfg = Join-Path $CMSREACT 'configuration\renderer-config.json'
$c = Get-Content $cfg -Raw
$c = $c -replace '"gamedata\.url":"[^"]*"', '"gamedata.url":"http://127.0.0.1:8080/react/gamedata"'
$c = $c -replace '"images\.url":"[^"]*"', '"images.url":"http://127.0.0.1:8080/react/images"'
$c = $c -replace '"avatar\.mandatory\.effect\.libraries":\[[^\]]*\]', '"avatar.mandatory.effect.libraries":[]'
$c = $c -replace '"system\.pong\.manually":true', '"system.pong.manually":false'
Set-Content $cfg $c -Encoding UTF8

Write-Host ""
Write-Host "================= NITRO V3 INSTALLATO ================="
Write-Host "Client: http://127.0.0.1:8080/react/   (login CMS -> /client)"
Write-Host "Config: $CMSREACT\configuration   |  Gamedata: $CMSREACT\gamedata (legacy/flat)"
Write-Host "Asset:  $CMSREACT\bundled (figure/generic/furniture)"
Write-Host "Se compaiono errori in console, incollameli per la verifica connessione emulatore."
Write-Host "======================================================"
