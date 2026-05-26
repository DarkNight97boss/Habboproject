# auto_deploy.ps1
# Push automatico verso GitHub.
#
# Strategia (workaround al fatto che il push diretto su `main` e' bloccato):
#  1. Controlla se main locale e' avanti rispetto a origin/main
#  2. Se si', spinge i nuovi commit su un branch fresco `auto-sync-<shortsha>`
#  3. Apre una PR (o riusa quella aperta da auto-sync verso main)
#  4. Stampa l'URL della PR cosi' la merge la fa l'utente con un click
#
# Puo' essere lanciato manualmente o richiamato dall'hook post-commit.

$ErrorActionPreference = 'Stop'

# Vai alla root del repo (la stessa dove vive questo script)
Set-Location -Path $PSScriptRoot

# Sync rapido col remoto (no merge, solo refs)
git fetch origin --quiet

# Helper: cattura output git, ritorna stringa vuota se null
function Git-Out { ((& git @args) | Out-String).Trim() }

# Branch corrente
$branch = Git-Out rev-parse --abbrev-ref HEAD

# Commits su HEAD ma non su origin/main
$aheadStr = Git-Out rev-list --count origin/main..HEAD
if ([string]::IsNullOrEmpty($aheadStr)) { $aheadStr = '0' }
if ([int]$aheadStr -eq 0) {
    Write-Host "[auto-deploy] Nessun commit nuovo da spingere. main e' allineato con origin/main."
    exit 0
}

# Sanity: lavoriamo solo da main
if ($branch -ne 'main') {
    Write-Host "[auto-deploy] Sei su '$branch', non su 'main'. Salto auto-push."
    exit 0
}

# Working tree pulito? Niente push se ci sono modifiche non committate
$dirty = Git-Out status --porcelain
if ($dirty) {
    Write-Host "[auto-deploy] Working tree non pulito. Committa o stash prima di rilanciare."
    exit 0
}

# Nome branch deterministico: usa lo sha breve del HEAD
$shortSha   = Git-Out rev-parse --short HEAD
$syncBranch = "auto-sync-$shortSha"

Write-Host "[auto-deploy] $aheadStr commit nuovi -> push su origin/$syncBranch"

# Push (no force: branch fresco). In PS 5.1 NON usare 2>&1 su native exe:
# git scrive info su stderr e PS le tratta come errori. Lasciamo che git
# stampi direttamente e ci affidiamo al solo $LASTEXITCODE.
git push origin "HEAD:refs/heads/$syncBranch"
$pushExit = $LASTEXITCODE
if ($pushExit -ne 0) {
    # Probabile: branch gia' presente. Verifichiamo via ls-remote.
    $remoteSha = (Git-Out ls-remote origin "refs/heads/$syncBranch") -split "`t" | Select-Object -First 1
    $localSha  = Git-Out rev-parse HEAD
    if ($remoteSha -eq $localSha) {
        Write-Host "[auto-deploy] $syncBranch e' gia' aggiornato su origin, procedo con PR."
    } else {
        Write-Host "[auto-deploy] ERRORE push (exit $pushExit). Vedi messaggi sopra."
        exit 1
    }
}

# Esiste gia' una PR aperta per questo branch?
$existing = gh pr list --repo DarkNight97boss/Habboproject --head $syncBranch --state open --json url --jq '.[0].url' 2>$null
if ($existing) {
    Write-Host "[auto-deploy] PR aperta: $existing"
    exit 0
}

# Componi messaggio PR dai commit ahead
$commitList = Git-Out log origin/main..HEAD --pretty=format:"- %s"
$title      = Git-Out log -1 --pretty=format:"%s"
if ($title.Length -gt 70) { $title = $title.Substring(0,67) + '...' }

$body = @"
Auto-deploy da locale (branch $syncBranch).

## Commit inclusi
$commitList

## Note
PR generata automaticamente da ``auto_deploy.ps1``. Verifica e mergia
quando sei pronto.
"@

# Salva body in temp file (gh pr create accetta --body-file)
$tmp = [System.IO.Path]::GetTempFileName()
$body | Out-File -FilePath $tmp -Encoding utf8

$prUrl = gh pr create --repo DarkNight97boss/Habboproject --base main --head $syncBranch --title $title --body-file $tmp
$prExit = $LASTEXITCODE
Remove-Item $tmp -ErrorAction SilentlyContinue

if ($prExit -ne 0) {
    Write-Host "[auto-deploy] ERRORE gh pr create (exit $prExit). Vedi messaggi sopra."
    exit 1
}

Write-Host "[auto-deploy] PR creata: $prUrl"
Write-Host "[auto-deploy] Clicca 'Merge pull request' per portare i commit su main."
