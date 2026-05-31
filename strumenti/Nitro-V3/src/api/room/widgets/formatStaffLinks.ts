/**
 * Trasforma URL nelle bolle di chat dello staff in link cliccabili.
 *
 * Per i messaggi normali la chat resta testo plain (anti-phishing).
 * Solo se chi parla è un moderatore (IRoomUserData.isModerator === true)
 * i pattern http(s)://... vengono incartati in <a> aprendo in nuova tab
 * con rel=noopener per sicurezza.
 *
 * Il testo in ingresso è gia HTML pre-formattato dal server (puo' contenere
 * <img> per emoticon ecc.). Sostituiamo solo le sequenze URL letterali —
 * il regex non matcha dentro attributi HTML perche' richiede uno spazio
 * o inizio-stringa subito prima.
 */

// Match http:// o https:// seguito da caratteri URL validi.
// Lookbehind \s|^ evita di matchare dentro href="...".
const STAFF_LINK_REGEX = /(^|[\s(])((?:https?:\/\/)[^\s<>"'`)]+)/gi;

// Punteggiatura comune da staccare in coda (es. "url.").
const TRAIL_PUNCT = /[.,;:!?)]+$/;

export const formatStaffLinks = (html: string): string =>
{
    if(!html) return html;
    // Nessun URL? bypass.
    if(html.indexOf('http') === -1) return html;

    return html.replace(STAFF_LINK_REGEX, (_match, prefix: string, rawUrl: string) =>
    {
        // Stacca punteggiatura finale dal link (la rimettiamo dopo l'anchor).
        let url = rawUrl;
        let trail = '';
        const trailMatch = url.match(TRAIL_PUNCT);
        if(trailMatch)
        {
            trail = trailMatch[0];
            url = url.slice(0, -trail.length);
        }

        // Escape minimale dei caratteri pericolosi nell'attributo href.
        const safeUrl = url
            .replace(/&/g, '&amp;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');

        return `${ prefix }<a href="${ safeUrl }" target="_blank" rel="noopener noreferrer" class="staff-link">${ safeUrl }</a>${ trail }`;
    });
};
