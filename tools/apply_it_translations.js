#!/usr/bin/env node
/**
 * apply_it_translations.js
 *
 * Applica le traduzioni italiane (client-patches/nitro-react/gamedata/)
 * ai file gamedata del client Nitro live (CMS/react/gamedata/).
 *
 * Strategia:
 *   - UITexts.json   -> SOSTITUZIONE TOTALE col file IT (52 chiavi).
 *   - ExternalTexts.json -> MERGE: parte da EN come base e sovrascrive
 *     SOLO le ~2022 chiavi del subset IT. Le restanti ~38k restano EN
 *     (fallback automatico del Nitro renderer).
 *
 * Idempotente: rieseguire e' safe. Crea backup .bak.en al primo run.
 *
 * Uso:
 *   node tools/apply_it_translations.js
 *
 * Verifica:
 *   - Hard-refresh del browser (Ctrl+Shift+R) per ricaricare i JSON cached.
 */

const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const PATCH_DIR = path.join(ROOT, 'client-patches', 'nitro-react', 'gamedata');
const LIVE_DIR  = path.join(ROOT, 'CMS', 'react', 'gamedata');

function loadJson(p) {
    return JSON.parse(fs.readFileSync(p, 'utf-8'));
}
function saveJson(p, obj) {
    fs.writeFileSync(p, JSON.stringify(obj, null, 2), 'utf-8');
}
function backupOnce(p) {
    const bak = p + '.bak.en';
    if (!fs.existsSync(bak)) {
        fs.copyFileSync(p, bak);
        console.log(`  Backup creato: ${path.basename(bak)}`);
    }
}

console.log('=== Applicazione traduzioni IT ===\n');

// ----- UITexts: sostituzione totale -----
const uiLive  = path.join(LIVE_DIR,  'UITexts.json');
const uiPatch = path.join(PATCH_DIR, 'UITexts.it.json');
if (!fs.existsSync(uiLive))  { console.error('Manca:', uiLive);  process.exit(1); }
if (!fs.existsSync(uiPatch)) { console.error('Manca:', uiPatch); process.exit(1); }

backupOnce(uiLive);
const uiIt = loadJson(uiPatch);
saveJson(uiLive, uiIt);
console.log(`UITexts.json: scritte ${Object.keys(uiIt).length} chiavi IT.\n`);

// ----- ExternalTexts: merge selettivo -----
const extLive  = path.join(LIVE_DIR,  'ExternalTexts.json');
const extPatch = path.join(PATCH_DIR, 'ExternalTexts.it.subset.json');
if (!fs.existsSync(extLive))  { console.error('Manca:', extLive);  process.exit(1); }
if (!fs.existsSync(extPatch)) { console.error('Manca:', extPatch); process.exit(1); }

backupOnce(extLive);
const enBase = loadJson(extLive);
const itSubset = loadJson(extPatch);

let updated = 0, added = 0;
for (const [k, v] of Object.entries(itSubset)) {
    if (k in enBase) updated++;
    else added++;
    enBase[k] = v;
}

saveJson(extLive, enBase);
console.log(`ExternalTexts.json:`);
console.log(`  Chiavi totali finali  : ${Object.keys(enBase).length}`);
console.log(`  Aggiornate da subset  : ${updated}`);
console.log(`  Aggiunte ex novo      : ${added}`);
console.log(`  Rimaste in EN (fallb.): ${Object.keys(enBase).length - updated - added}\n`);

console.log('Fatto. Ricarica il browser con Ctrl+Shift+R per vedere le UI in italiano.');
