#!/usr/bin/env node
/**
 * merge_habbo_it_flash.js
 *
 * Mergia le traduzioni ufficiali Habbo Italia (formato Flash external_flash_texts,
 * 27k+ chiavi) nel file ExternalTexts.json live del client Nitro.
 *
 * Priority order (later overrides earlier):
 *   1. ExternalTexts.json originale EN (~31k chiavi)
 *   2. ExternalTexts.it.subset.json (mio, 2022 chiavi UI moderne)
 *   3. external_flash_texts_it.habbo.txt (ufficiale Habbo IT 2015, 27k chiavi)
 *
 * Cosi':
 *   - Tutte le chiavi che esistono in Habbo IT 2015 hanno traduzione ufficiale.
 *   - Le poche chiavi NUOVE Nitro (modtools modern, nux, talent.track) non
 *     presenti in 2015 mantengono la mia traduzione custom.
 *   - Le restanti EN restano come fallback.
 *
 * Skip:
 *   - Righe del flash file che sono placeholder `${...}` (non sono traduzioni
 *     reali, sono ref a un'altra chiave).
 *   - Righe vuote / commenti.
 *
 * Idempotente: rieseguibile senza danni (il backup .bak.en esiste gia').
 *
 * Uso:
 *   node tools/merge_habbo_it_flash.js
 *   # poi nel browser: Ctrl+Shift+R
 */

const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const PATCH = path.join(ROOT, 'client-patches', 'nitro-react', 'gamedata');
const LIVE  = path.join(ROOT, 'CMS', 'react', 'gamedata');

const enPath   = path.join(LIVE, 'ExternalTexts.json');
const enBak    = enPath + '.bak.en';
const subset   = path.join(PATCH, 'ExternalTexts.it.subset.json');
const flashTxt = path.join(PATCH, 'external_flash_texts_it.habbo.txt');

if (!fs.existsSync(enPath))   { console.error('Manca:', enPath);   process.exit(1); }
if (!fs.existsSync(subset))   { console.error('Manca:', subset);   process.exit(1); }
if (!fs.existsSync(flashTxt)) { console.error('Manca:', flashTxt); process.exit(1); }

// Lavora da .bak.en se esiste (cosi' partiamo SEMPRE dall'EN originale,
// senza accumulare merge precedenti). Se non c'e', lo creiamo ora.
let baseEn;
if (fs.existsSync(enBak)) {
    console.log('Lettura base EN da:', path.basename(enBak));
    baseEn = JSON.parse(fs.readFileSync(enBak, 'utf-8'));
} else {
    console.log('Backup non trovato — creo .bak.en da ExternalTexts.json corrente.');
    fs.copyFileSync(enPath, enBak);
    baseEn = JSON.parse(fs.readFileSync(enBak, 'utf-8'));
}
console.log(`Base EN: ${Object.keys(baseEn).length} chiavi.`);

// Layer 2: il mio subset (moderne Nitro)
const mySubset = JSON.parse(fs.readFileSync(subset, 'utf-8'));
console.log(`Layer subset IT custom: ${Object.keys(mySubset).length} chiavi.`);

// Layer 3: flash IT ufficiale Habbo
const flashRaw = fs.readFileSync(flashTxt, 'utf-8');
const habboIt = {};
let lineNo = 0, skipped = 0, parsed = 0;
for (const line of flashRaw.split(/\r?\n/)) {
    lineNo++;
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith('#')) continue;
    const eq = line.indexOf('=');
    if (eq <= 0) { skipped++; continue; }
    const key = line.substring(0, eq).trim();
    let val   = line.substring(eq + 1);
    // Skip placeholders ${...} (sono ref interne, non traduzioni vere)
    if (/^\$\{[^}]+\}$/.test(val.trim())) { skipped++; continue; }
    // Skip empty values
    if (val === '') { skipped++; continue; }
    habboIt[key] = val;
    parsed++;
}
console.log(`Layer Habbo IT flash: ${parsed} chiavi (skipped ${skipped} placeholder/empty).`);

// Merge in ordine
const merged = { ...baseEn };

let upd_subset = 0;
for (const k of Object.keys(mySubset)) {
    if (k in merged) upd_subset++;
    merged[k] = mySubset[k];
}

let upd_habbo = 0, new_habbo = 0;
for (const k of Object.keys(habboIt)) {
    if (k in merged) upd_habbo++; else new_habbo++;
    merged[k] = habboIt[k];
}

fs.writeFileSync(enPath, JSON.stringify(merged, null, 2), 'utf-8');

console.log('');
console.log('=== Risultato merge ===');
console.log(`  Chiavi finali totali       : ${Object.keys(merged).length}`);
console.log(`  Aggiornate da subset mio   : ${upd_subset}`);
console.log(`  Aggiornate da Habbo IT     : ${upd_habbo}`);
console.log(`  Nuove (solo Habbo IT)      : ${new_habbo}`);
console.log(`  Restanti EN (fallback)     : ${Object.keys(merged).length - upd_subset - upd_habbo - new_habbo} (approx)`);
console.log('');
console.log('Ricarica il browser con Ctrl+Shift+R.');
