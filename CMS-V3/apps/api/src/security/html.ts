import DOMPurify from 'isomorphic-dompurify';

/**
 * Sanitizzazione HTML per contenuti "ricchi" scritti dallo staff (articoli news).
 *
 * Perche' (security audit P1.2): `body_html` veniva salvato RAW e reso con
 * `dangerouslySetInnerHTML` su ogni pagina pubblica dell'articolo e nell'anteprima
 * del pannello admin. `isomorphic-dompurify` era in package.json ma mai usato:
 * un account staff compromesso/phishato poteva iniettare `<img onerror=...>` ed
 * eseguire JS nel browser di tutti i visitatori (XSS stored) e degli admin
 * (escalation staff→admin). Sanitizziamo SERVER-SIDE IN SCRITTURA, cosi' il DB
 * contiene solo markup sicuro qualunque sia il client che lo rende.
 *
 * Politica: allowlist esplicita di tag e attributi (niente script/style/iframe/
 * form/svg, nessun handler on*, nessun href/src `javascript:` o `data:` —
 * DOMPurify li scarta gia' con ALLOWED_URI_REGEXP di default).
 */
const ALLOWED_TAGS = [
    'p', 'br', 'hr', 'strong', 'b', 'em', 'i', 'u', 's', 'span', 'div',
    'a', 'ul', 'ol', 'li', 'h2', 'h3', 'h4', 'blockquote', 'img',
    'table', 'thead', 'tbody', 'tr', 'th', 'td', 'code', 'pre'
];
const ALLOWED_ATTR = [
    'href', 'src', 'alt', 'title', 'target', 'rel', 'class',
    'width', 'height', 'colspan', 'rowspan'
];

export function sanitizeNewsHtml(html: string): string
{
    return DOMPurify.sanitize(html, {
        ALLOWED_TAGS,
        ALLOWED_ATTR,
        FORBID_TAGS: ['script', 'style', 'iframe', 'object', 'embed', 'form', 'input', 'svg', 'math'],
        // I link aperti in nuova scheda non devono poter manipolare la pagina
        // di origine (tabnabbing): forziamo rel="noopener noreferrer".
        ADD_ATTR: [],
        KEEP_CONTENT: true
    }).replace(/<a\s([^>]*?)target="_blank"([^>]*)>/gi, (_m, pre: string, post: string) =>
        /rel=/i.test(pre + post) ? `<a ${pre}target="_blank"${post}>` : `<a ${pre}target="_blank" rel="noopener noreferrer"${post}>`
    );
}
