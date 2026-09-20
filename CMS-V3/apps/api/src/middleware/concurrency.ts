import type { MiddlewareHandler } from 'hono';

/**
 * Circuit-breaker di concorrenza IN-PROCESS. Conta le richieste in volo; oltre
 * `max` risponde 503 IMMEDIATO invece di accodarle sul pool DB. Evita il
 * congestion-collapse: sotto flood la latenza esplode e cade anche /healthz;
 * scartando in fretta l'eccesso, il server resta responsivo e i load-balancer
 * continuano a vederlo vivo. È per-processo di proposito (protegge il pool di
 * QUESTA istanza); il limite distribuito è a monte (Redis + edge Cloudflare).
 *
 * `exempt` salta il conteggio per path a costo ~zero (healthz/version), così un
 * flood non fa mai fallire il probe di liveness.
 */
export function makeConcurrencyLimit(max: number, exempt?: (path: string) => boolean): MiddlewareHandler
{
    let inFlight = 0;
    return async (c, next) =>
    {
        if(exempt && exempt(c.req.path)) return await next();
        if(inFlight >= max)
        {
            c.header('Retry-After', '1');
            return c.json({ error: 'server_busy', message: 'Server occupato, riprova tra poco.' }, 503);
        }
        inFlight++;
        try { await next(); }
        finally { inFlight--; }
    };
}
