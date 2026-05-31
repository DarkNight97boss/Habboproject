# ============================================================
#  Build del client Nitro UPSTREAM (billsonnn/nitro-react)
#  Stessa versione (2.1.1 / renderer 1.6.6) del build duckietm
#  ma con i fix recenti (feb 2026), incluso il fix img onload.
#
#  Installa in CMS\react\ (servito su http://127.0.0.1:8080/react/)
#  PRESERVA gli asset gia' presenti (gamedata/ bundled/ images/ sounds/).
#
#  Uso:  powershell -ExecutionPolicy Bypass -File build_billsonnn.ps1
# ============================================================

$ErrorActionPreference = 'Stop'

$SRC      = 'C:\Users\vincenzokatrielgiuva\Desktop\Github\nitro-react'
$CMSREACT = 'C:\Users\vincenzokatrielgiuva\Desktop\Github\Habboproject\CMS\react'
$NODE     = 'C:\Program Files\nodejs'

$env:Path = "$NODE;$env:Path"
Write-Host "== Node: $(node --version)  npm: $(npm --version)"

Set-Location $SRC

Write-Host "== Abilito Yarn (corepack)..."
corepack enable

Write-Host "== yarn install (scarica @nitrots/nitro-renderer da npm; alcuni minuti)..."
corepack yarn install

Write-Host "== Build di produzione con base=/react/ ..."
corepack yarn build --base=/react/

if (-not (Test-Path "$SRC\dist\index.html")) { throw "Build fallita: dist\index.html non creato" }

# Rimuovo SOLO i vecchi artefatti client (NON gli asset)
Write-Host "== Pulizia vecchi file client in CMS\react (mantengo gli asset)..."
foreach ($p in 'assets','src','index.html') {
    $tp = Join-Path $CMSREACT $p
    if (Test-Path $tp) { Remove-Item $tp -Recurse -Force }
}

Write-Host "== Copio il nuovo build in $CMSREACT (gamedata/bundled/images/sounds restano) ..."
New-Item -ItemType Directory -Force -Path $CMSREACT | Out-Null
Copy-Item "$SRC\dist\*" "$CMSREACT\" -Recurse -Force

# Riassicuro i config corretti (gia' in public/, ma li ribadisco)
Copy-Item "$SRC\public\renderer-config.json" "$CMSREACT\renderer-config.json" -Force
Copy-Item "$SRC\public\ui-config.json"       "$CMSREACT\ui-config.json"       -Force

Write-Host ""
Write-Host "================= NITRO (billsonnn) INSTALLATO ================="
Write-Host "Client: http://127.0.0.1:8080/react/   (login sul CMS -> /client)"
Write-Host "Asset preservati in: $CMSREACT\gamedata , \bundled , \images , \sounds"
Write-Host "Versione: nitro-react upstream (renderer 1.6.6) con fix recenti"
Write-Host "==============================================================="
