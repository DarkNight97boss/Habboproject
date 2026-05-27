# Localizzazione client Nitro — Italiano

I file in questa directory contengono le traduzioni italiane delle
stringhe **client-side** del Nitro renderer.

## File

| File | Contenuto | Chiavi |
|---|---|---|
| `UITexts.it.json` | Traduzione completa di `UITexts.json` (~52 chiavi) | 52 |
| `ExternalTexts.it.subset.json` | Subset prioritario UI di `ExternalTexts.json` (navigator, catalog, widget, modtools, profile, infostand, friendlist, avatar editor, ecc.) | 2022 |

## Strategia

`ExternalTexts.json` originale ha **~40.000+ chiavi**, di cui:
- ~15.000 **badge** name (identifier-like, lasciati in inglese)
- ~4.500 **landing pages** (specifico contenuto promozionale legacy)
- ~4.000 **quests** (achievement system upstream)
- ~2.000 **UI labels** (questi sono i nostri target)

Tradurre tutti i 40k è di basso valore: badge/landing/quests sono o
identifier o contenuto specifico Habbo originale che non si vede
all'utente medio. Il subset UI (2022 chiavi) copre **~95% di quello
che l'utente vede** nelle prime ore di gioco.

## Come applicare (post-build Nitro)

I file vivono qui (versionati su git). Dopo aver fatto `yarn build`
del client Nitro, **sovrascrivi** i file generati con le versioni italiane:

```powershell
# Esempio Windows (dopo yarn build su nitro-react)
$src = "C:\Users\<you>\Desktop\Github\Habboproject\client-patches\nitro-react\gamedata"
$dst = "C:\Users\<you>\Desktop\Github\Habboproject\CMS\react\gamedata"

# Sovrascrivi UITexts.json (traduzione totale)
Copy-Item "$src\UITexts.it.json" "$dst\UITexts.json" -Force

# Merge ExternalTexts subset NELL'originale (preserva i 38k restanti EN)
$en = Get-Content "$dst\ExternalTexts.json" -Raw | ConvertFrom-Json
$it = Get-Content "$src\ExternalTexts.it.subset.json" -Raw | ConvertFrom-Json
foreach ($k in $it.PSObject.Properties.Name) {
    $en | Add-Member -Name $k -Value $it.$k -MemberType NoteProperty -Force
}
$en | ConvertTo-Json -Depth 5 -Compress | Set-Content "$dst\ExternalTexts.json"
```

Linux/macOS equivalente con `jq`:
```bash
jq -s '.[0] * .[1]' \
    "$dst/ExternalTexts.json" \
    "$src/ExternalTexts.it.subset.json" \
    > "$dst/ExternalTexts.json.new"
mv "$dst/ExternalTexts.json.new" "$dst/ExternalTexts.json"

cp "$src/UITexts.it.json" "$dst/UITexts.json"
```

## Estendere

Per tradurre **altre** chiavi (es. badge piu' visti, quests popolari),
aggiungi entry al rispettivo file `*.it.json`. Le chiavi mancanti
restano automaticamente in EN dal file upstream.

Lavoro futuro candidato:
- `quests.*` (~4000 entry) — quest names + step descriptions
- `landing.*` (~4500) — homepage promo (basso valore se il CMS gestisce homepage)
- `targeted.*` (~395) — pop-up promozionali

## Glossario applicato

Vedi `client-patches/nitro-react/gamedata/UITexts.it.json` per il
glossario canonico usato (rooms=stanze, friends=amici, credits=Crediti,
duckets=Duckets, pixels=Pixel, diamonds=Diamanti, badge=distintivo,
achievement=obiettivo, ecc.). Mantieni la consistenza se aggiungi entry.
