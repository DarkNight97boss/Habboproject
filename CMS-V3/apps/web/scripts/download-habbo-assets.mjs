/**
 * Mirror locale di tutti gli asset chrome del sito ufficiale habbo.it
 * (CSS + sprite + background + favicon). Output sotto
 * `apps/web/public/assets/habbo/` così Vite li serve a `/assets/habbo/*`.
 *
 * Perché esiste questo script:
 *  - Prima il `index.html` puntava direttamente a images.habbo.com per CSS
 *    e prefetch sprite. Se Sulake cambia una versione (URL diventa
 *    `app.XXXXXXXX.css`), il nostro CMS si rompe silenziosamente.
 *  - Spostiamo tutto in locale, taggato + diffabile + sotto controllo
 *    versione → integrità preservata.
 *
 * Cosa fa:
 *  1. scarica `app.a8ea7435.css` da images.habbo.com
 *  2. estrae TUTTE le `url(...)` referenziate (sia relative che assolute
 *     verso images.habbo.com)
 *  3. scarica ogni asset preservando il path relativo
 *  4. riscrive le url() assolute → path locali (`/assets/habbo/...`),
 *     lascia intatte le relative (risolvono già contro la CSS locale)
 *  5. scarica favicon + sprite 1x/2x + background hero che linkiamo dall
 *     `index.html`
 *
 * Run: `yarn workspace @cms-v3/web download-assets` (vedi package.json).
 */
import { mkdir, readFile, writeFile } from 'node:fs/promises';
import { existsSync } from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const CDN_BASE = 'https://images.habbo.com/habbo-web/america/it/';
const CSS_VERSION = 'app.a8ea7435.css';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const OUT_ROOT = path.resolve(__dirname, '../public/assets/habbo');

function log(msg) { console.log(`[mirror] ${msg}`); }

async function fetchBuffer(url)
{
    const r = await fetch(url);
    if(!r.ok) throw new Error(`HTTP ${r.status} on ${url}`);
    return Buffer.from(await r.arrayBuffer());
}

async function fetchText(url)
{
    const r = await fetch(url);
    if(!r.ok) throw new Error(`HTTP ${r.status} on ${url}`);
    return r.text();
}

/**
 * Scarica `relPath` (resolved against CDN_BASE) in `OUT_ROOT/relPath`.
 * Skip se file già esiste (idempotente).
 */
async function downloadAsset(relPath, force = false)
{
    const url = new URL(relPath, CDN_BASE).toString();
    const dest = path.join(OUT_ROOT, relPath);
    if(!force && existsSync(dest))
    {
        log(`skip (exists): ${relPath}`);
        return;
    }
    await mkdir(path.dirname(dest), { recursive: true });
    const buf = await fetchBuffer(url);
    await writeFile(dest, buf);
    log(`fetched ${relPath} (${buf.length} bytes)`);
}

/**
 * Estrae tutte le `url(...)` da CSS. Restituisce un Set di URL stringhe
 * "raw" (così come compaiono nella sorgente, possono essere relative o
 * assolute, possono avere ?query).
 */
function extractCssUrls(css)
{
    const urls = new Set();
    const re = /url\(\s*(['"]?)([^'")]+)\1\s*\)/g;
    let m;
    while((m = re.exec(css)) !== null)
    {
        const raw = m[2].trim();
        if(raw.startsWith('data:')) continue;
        urls.add(raw);
    }
    return urls;
}

/**
 * Risolve `raw` (può essere relativa o assoluta) rispetto a CDN_BASE.
 * Ritorna { absoluteUrl, relPath } dove relPath è il path da usare
 * sia per il download sia per il riferimento locale (`/assets/habbo/...`).
 * Ritorna null se l'URL punta fuori dal dominio habbo (es. fonts.googleapis,
 * cloudflare turnstile, ecc — non li mirroriamo).
 */
function resolveAsset(raw)
{
    let absolute;
    try { absolute = new URL(raw, CDN_BASE); } catch { return null; }
    if(absolute.hostname !== 'images.habbo.com') return null;
    // strip query string per il path locale (tipo `sprite.png?v=123`)
    const url = new URL(absolute.toString());
    url.search = '';
    // path locale = pathname relativo a /habbo-web/america/it/
    const cdnPath = new URL(CDN_BASE).pathname; // "/habbo-web/america/it/"
    let p = url.pathname;
    if(p.startsWith(cdnPath)) p = p.slice(cdnPath.length);
    return { absoluteUrl: absolute.toString(), relPath: p };
}

async function main()
{
    await mkdir(OUT_ROOT, { recursive: true });

    // 1) scarica CSS sorgente
    log(`fetching CSS ${CSS_VERSION}`);
    const cssSrc = await fetchText(new URL(CSS_VERSION, CDN_BASE).toString());
    log(`CSS size: ${cssSrc.length} bytes`);

    // 2) estrai url()
    const rawUrls = extractCssUrls(cssSrc);
    log(`extracted ${rawUrls.size} url() refs from CSS`);

    // 3) risolvi + download asset CSS-referenced
    const downloaded = [];
    const failed = [];
    for(const raw of rawUrls)
    {
        const res = resolveAsset(raw);
        if(!res)
        {
            log(`skip non-habbo URL: ${raw}`);
            continue;
        }
        try
        {
            await downloadAsset(res.relPath);
            downloaded.push(res.relPath);
        }
        catch(e)
        {
            log(`FAILED ${res.relPath}: ${e.message}`);
            failed.push({ relPath: res.relPath, error: String(e.message) });
        }
    }

    // 4) riscrivi CSS: replace url("https://images.habbo.com/habbo-web/america/it/...")
    //    → url("...") (path relativo al CSS locale).
    //    Lascio intatte le url() relative perché già risolvono correttamente
    //    rispetto al CSS locale (stessa folder structure).
    const cdnPrefix = CDN_BASE;
    const cssRewritten = cssSrc.replace(
        /url\(\s*(['"]?)https?:\/\/images\.habbo\.com\/habbo-web\/america\/it\/([^'")]+)\1\s*\)/g,
        (_match, q, rel) => `url(${q}${rel}${q})`
    );

    await writeFile(path.join(OUT_ROOT, 'app.css'), cssRewritten);
    log(`wrote app.css (${cssRewritten.length} bytes)`);

    // 5) asset extra linkati dall'index.html (favicon + sprite prefetch + hotel bg)
    const EXTRA = [
        'assets/images/favicon.08c747be.ico',
        'assets/images/sprite.275ed2fd.png',
        'assets/images/sprite@2x.f98bb62b.png',
        'assets/images/backgrounds/hotel.c5bc8f85.png'
    ];
    for(const rel of EXTRA)
    {
        try { await downloadAsset(rel); downloaded.push(rel); }
        catch(e) { failed.push({ relPath: rel, error: String(e.message) }); }
    }

    // 6) JS bundle del CDN base (Angular vendor + scripts del sito ufficiale).
    //    Nominati con hash della versione corrente — quando Sulake rilascia
    //    una nuova build cambiano gli hex e questo script va aggiornato.
    //    NB: NON usiamo questi script runtime (il nostro CMS è React) ma li
    //    mirroriamo per future referenze (debug, parity check, eventuale
    //    embedding di pezzi del client Angular).
    const EXTRA_JS = [
        'vendor.accb8962.js',
        'scripts.b823e390.js'
    ];
    for(const rel of EXTRA_JS)
    {
        try
        {
            await downloadAsset(rel);
            downloaded.push(rel);
            // Recursive scan: parse il JS appena scaricato per pattern
            // sub-chunk relativi al CDN base (lazy-loaded chunks Angular
            // tipo `require.ensure('./foo.XXX.js')`). Catch i casi più
            // comuni; se Sulake aggiunge code-splitting in futuro, estendi
            // questa regex.
            const js = await readFile(path.join(OUT_ROOT, rel), 'utf8');
            const subChunks = new Set();
            // import dinamici: import("foo.123.js") o require("./bar.456.js")
            for(const m of js.matchAll(/["']\.?\.?\/?([\w./-]+\.js)["']/g))
            {
                const chunk = m[1];
                // skip i file già presenti
                if(chunk === rel) continue;
                // skip path absoluti non habbo
                if(chunk.startsWith('http')) continue;
                subChunks.add(chunk);
            }
            if(subChunks.size > 0)
            {
                log(`  ↳ ${rel} referenzia ${subChunks.size} chunk JS — tento mirror`);
                for(const chunk of subChunks)
                {
                    try { await downloadAsset(chunk); downloaded.push(chunk); }
                    catch { /* 404 sui chunk inesistenti è atteso, skip silent */ }
                }
            }
        }
        catch(e)
        {
            failed.push({ relPath: rel, error: String(e.message) });
        }
    }

    // 6) sommario
    log(`\n=== DONE ===`);
    log(`OK   : ${downloaded.length} file`);
    log(`FAIL : ${failed.length} file`);
    if(failed.length)
    {
        for(const f of failed) console.log(`  - ${f.relPath}: ${f.error}`);
        process.exit(1);
    }
}

main().catch(e =>
{
    console.error('[mirror] FATAL', e);
    process.exit(1);
});
