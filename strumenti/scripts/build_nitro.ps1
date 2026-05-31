# ============================================================
#  Build del client Nitro (ESEGUI TU QUESTO, in un terminale tuo)
#  Questi comandi eseguono tooling esterno (yarn) che l'agente
#  non puo' lanciare per via del classificatore di sicurezza.
#
#  Il CMS incorpora Nitro da {swfurl}/react/index.html?sso=...
#  quindi il client va servito su http://127.0.0.1:8080/react/
#  cioe' nella cartella  CMS\react\
#
#  Uso:  powershell -ExecutionPolicy Bypass -File build_nitro.ps1
# ============================================================

$ErrorActionPreference = 'Stop'

$NITROSRC = 'C:\Users\vincenzokatrielgiuva\Desktop\Github\Nitro-Default'
$CMSREACT = 'C:\Users\vincenzokatrielgiuva\Desktop\Github\Habboproject\CMS\react'
$NODE     = 'C:\Program Files\nodejs'

$env:Path = "$NODE;$env:Path"

Write-Host "== Node:  $(node --version)"
Write-Host "== npm:   $(npm --version)"

Set-Location $NITROSRC

Write-Host "== Abilito Yarn via corepack..."
corepack enable

Write-Host "== yarn install (puo' richiedere diversi minuti)..."
corepack yarn install

Write-Host "== Build di produzione con base=/react/ ..."
corepack yarn build --base=/react/

if (-not (Test-Path "$NITROSRC\dist")) { throw "Build fallita: cartella dist non creata" }

Write-Host "== Copio il client buildato in $CMSREACT ..."
New-Item -ItemType Directory -Force -Path $CMSREACT | Out-Null
Copy-Item "$NITROSRC\dist\*" "$CMSREACT\" -Recurse -Force
# i config vanno serviti su /react/renderer-config.json e /react/ui-config.json
Copy-Item "$NITROSRC\public\renderer-config.json" "$CMSREACT\renderer-config.json" -Force
Copy-Item "$NITROSRC\public\ui-config.json"       "$CMSREACT\ui-config.json"       -Force

Write-Host ""
Write-Host "================= CLIENT NITRO INSTALLATO ================="
Write-Host "Client servito su:   http://127.0.0.1:8080/react/  (dal CMS)"
Write-Host "Config:              $CMSREACT\renderer-config.json / ui-config.json"
Write-Host ""
Write-Host "MANCANO ANCORA GLI ASSET (.nitro + gamedata)."
Write-Host "Generali con un converter e copiali sotto $CMSREACT :"
Write-Host "   $CMSREACT\gamedata\   (FigureData.json, FurnitureData.json, ...)"
Write-Host "   $CMSREACT\bundled\    (figure\ effect\ furniture\ pet\ generic\)"
Write-Host "   $CMSREACT\images\     $CMSREACT\c_images\     $CMSREACT\dcr\"
Write-Host ""
Write-Host "Converter consigliato: https://github.com/duckietm/all-in-1-converter"
Write-Host "Poi accedi dal sito: login -> /client  (carica Nitro con l'SSO in automatico)"
Write-Host "=========================================================="
