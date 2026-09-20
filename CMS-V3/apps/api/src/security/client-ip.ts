/**
 * IP del client canonico dietro Cloudflare + nginx.
 *
 * `CF-Connecting-IP` è impostato da Cloudflare all'edge e NON è falsificabile dal
 * client: Cloudflare lo sovrascrive a ogni richiesta e l'origin accetta 80/443
 * SOLO dagli IP Cloudflare (ufw), quindi nessuno può raggiungere nginx con un
 * CF-Connecting-IP arbitrario. È presente sia in prod sia in dev.
 *
 * Pentest 2026-09-20: il vecchio codice prendeva il PRIMO valore di
 * `X-Forwarded-For`, che il client controlla (nginx-dev lo APPENDE) → chiave
 * rate-limit spoofabile e limiti login/register annullati. Fallback ordinati e
 * tutti "a monte" del client: X-Real-IP (impostato da nginx), poi l'ULTIMO hop
 * di XFF (aggiunto dal proxy fidato, non il primo), infine 'unknown'.
 */
export function clientIp(req: Request): string
{
    const cf = req.headers.get('cf-connecting-ip')?.trim();
    if(cf) return cf;

    const real = req.headers.get('x-real-ip')?.trim();
    if(real) return real;

    const xff = req.headers.get('x-forwarded-for');
    if(xff)
    {
        const parts = xff.split(',').map(s => s.trim()).filter(Boolean);
        if(parts.length) return parts[parts.length - 1]!;
    }
    return 'unknown';
}
