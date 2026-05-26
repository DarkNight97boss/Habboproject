# gen_rcon_token.ps1
# Genera un token RCON criptograficamente sicuro (64 hex chars = 256 bit).
# Lo stesso valore va messo in EMU/config.ini (rcon.token) E in API/config.php
# (RCON shared secret) — senza match l'API non riesce a parlare con l'EMU.
#
# Uso:
#   .\tools\gen_rcon_token.ps1
# poi copia/incolla l'output nei due file di config.

$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
$hex = ($bytes | ForEach-Object { '{0:x2}' -f $_ }) -join ''

Write-Host ''
Write-Host '=== RCON token (256 bit, hex) ==='
Write-Host $hex
Write-Host ''
Write-Host 'Aggiorna:'
Write-Host '  EMU/config.ini       -> rcon.token=' -NoNewline; Write-Host $hex
Write-Host '  API/config.php       -> $RCON_TOKEN = ''' -NoNewline; Write-Host -NoNewline $hex; Write-Host ''';'
Write-Host ''
Write-Host 'Riavvia l''emulatore e l''API dopo aver applicato.'
