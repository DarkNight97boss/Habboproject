import type { MiddlewareHandler } from 'hono';

/**
 * Rate limiter sliding-window in-memory (no Redis per ora).
 * Per produzione multi-istanza serve Redis o un KV condiviso.
 */
interface Bucket
{
    times: number[];
    windowMs: number;
    limit: number;
}

const buckets = new Map<string, Bucket>();

function getKey(req: Request, scope: string): string
{
    const xff = req.headers.get('x-forwarded-for');
    const ip = (xff?.split(',')[0]?.trim()) ?? req.headers.get('x-real-ip') ?? 'unknown';
    return `${scope}:${ip}`;
}

export function makeRateLimit(
    scope: string,
    limit: number,
    windowMs: number
): MiddlewareHandler
{
    return async (c, next) =>
    {
        const key = getKey(c.req.raw, scope);
        const now = Date.now();
        const bucket = buckets.get(key) ?? { times: [], windowMs, limit };
        // Scarta gli hit fuori finestra.
        bucket.times = bucket.times.filter(t => now - t < windowMs);

        if(bucket.times.length >= limit)
        {
            c.header('Retry-After', String(Math.ceil(windowMs / 1000)));
            return c.json({ error: 'rate_limited', message: 'Troppe richieste. Riprova più tardi.' }, 429);
        }

        bucket.times.push(now);
        buckets.set(key, bucket);

        await next();
    };
}

// Cleanup periodico per evitare memory leak su IP visti una volta.
setInterval(() =>
{
    const now = Date.now();
    for(const [key, bucket] of buckets.entries())
    {
        bucket.times = bucket.times.filter(t => now - t < bucket.windowMs);
        if(bucket.times.length === 0) buckets.delete(key);
    }
}, 60_000).unref();
