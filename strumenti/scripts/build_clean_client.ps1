# ============================================================
#  BUILD CLIENT NITRO (riproducibile, un comando)
#  Stack canonico: Arcturus MS 3.5.5 + nitro-react + ms-websockets
#  Asset: harrydev44/nitro-assets (set completo, formato compatibile col renderer)
#  Output: Github\client-dist  (servito su http://localhost:8090)
#
#  Uso:  powershell -ExecutionPolicy Bypass -File build_clean_client.ps1
# ============================================================
$ErrorActionPreference = 'Stop'
$G    = 'C:\Users\vincenzokatrielgiuva\Desktop\Github'
$NODE = 'C:\Program Files\nodejs'
$env:Path = "$NODE;$env:Path"
Write-Host "Node $(node --version)"
corepack enable

# ---------- 1) nitro-react (client) ----------
if (-not (Test-Path "$G\nitro-react")) {
    git clone https://github.com/billsonnn/nitro-react.git "$G\nitro-react"
}
Set-Location "$G\nitro-react"
# config client (socket emulatore + asset su :8090) se non gia' presenti
if (-not (Test-Path "$G\nitro-react\public\renderer-config.json")) {
    Copy-Item "public\renderer-config.json.example" "public\renderer-config.json"
    Copy-Item "public\ui-config.json.example" "public\ui-config.json"
    (Get-Content "public\renderer-config.json") `
        -replace '"socket\.url":\s*"[^"]*"','"socket.url": "ws://127.0.0.1:2096"' `
        -replace '"asset\.url":\s*"[^"]*"','"asset.url": "."' `
        -replace '"image\.library\.url":\s*"[^"]*"','"image.library.url": "/c_images/"' `
        -replace '"hof\.furni\.url":\s*"[^"]*"','"hof.furni.url": "/dcr/hof_furni"' `
        -replace '"avatar\.mandatory\.effect\.libraries":\s*\[[^\]]*\]','"avatar.mandatory.effect.libraries": []' |
        Set-Content "public\renderer-config.json"
}
# config.urls -> root (servito a / non /react)
(Get-Content "index.html") -replace '/react/renderer-config.json','/renderer-config.json' -replace '/react/ui-config.json','/ui-config.json' | Set-Content "index.html"

Write-Host "== nitro-react: yarn install + build ..."
corepack yarn install
corepack yarn build
if (-not (Test-Path "$G\nitro-react\dist\index.html")) { throw "nitro-react: build fallita" }

# ---------- 2) asset (harrydev44/nitro-assets) ----------
if (-not (Test-Path "$G\nitro-assets\gamedata\FigureData.json")) {
    git clone --depth 1 https://github.com/harrydev44/nitro-assets.git "$G\nitro-assets"
}

# ---------- 3) assembla client-dist ----------
$DIST = "$G\client-dist"
Write-Host "== Assemblo $DIST ..."
Remove-Item $DIST -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force $DIST | Out-Null
Copy-Item "$G\nitro-react\dist\*" $DIST -Recurse -Force
Copy-Item "$G\nitro-assets\bundled"  "$DIST\bundled"  -Recurse -Force
Copy-Item "$G\nitro-assets\gamedata" "$DIST\gamedata" -Recurse -Force
if (Test-Path "$G\nitro-assets\images") { Copy-Item "$G\nitro-assets\images" "$DIST\images" -Recurse -Force }
if (Test-Path "$G\nitro-assets\sounds") { Copy-Item "$G\nitro-assets\sounds" "$DIST\sounds" -Recurse -Force }
# fix difensivo config.urls anche nel dist
(Get-Content "$DIST\index.html") -replace '/react/renderer-config.json','/renderer-config.json' -replace '/react/ui-config.json','/ui-config.json' | Set-Content "$DIST\index.html"

Write-Host ""
Write-Host "================= CLIENT PRONTO ================="
Write-Host "Servi con:  start_stack.ps1  (oppure php -S 0.0.0.0:8090 -t client-dist)"
Write-Host "Apri:       http://localhost:8090/index.html?sso=<auth_ticket>&room=<id>"
Write-Host "================================================="
