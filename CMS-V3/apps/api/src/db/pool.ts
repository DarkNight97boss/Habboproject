import mysql, { Pool, PoolOptions } from 'mysql2/promise';
import { env } from '../env.js';

/**
 * Connection pool MariaDB.
 *
 * Security:
 * - SOLO query parametrizzate (via pool.execute() o prepare()), MAI string concat
 * - charset utf8mb4 per evitare bug encoding
 * - timeout connect per evitare hang
 * - multipleStatements DISABLED (default) per evitare stacked injection
 */
const poolOptions: PoolOptions = {
    host: env.DB_HOST,
    port: env.DB_PORT,
    user: env.DB_USER,
    password: env.DB_PASSWORD,
    database: env.DB_NAME,
    waitForConnections: true,
    connectionLimit: env.DB_POOL_MAX,
    maxIdle: env.DB_POOL_MAX,
    // DoS (pentest 2026-09-20): coda FINITA. Con 0 (illimitata) query lente
    // accodavano richieste all'infinito -> heap + API non responsiva anche su
    // /healthz. Oltre la coda: errore immediato (fail-fast) invece di hang.
    queueLimit: 256,
    connectTimeout: env.DB_CONNECT_TIMEOUT_MS,
    charset: 'utf8mb4',
    multipleStatements: false,
    namedPlaceholders: true,
    dateStrings: false,
    timezone: 'Z'
};

let _pool: Pool | null = null;

export function getDbPool(): Pool
{
    if(_pool === null)
    {
        _pool = mysql.createPool(poolOptions);
        // Crash-resistance: mysql2 emette 'error' sul pool; senza listener l'EventEmitter
        // rilancia -> crash del processo a ogni blip del DB. Logghiamo e proseguiamo.
        _pool.on('error', () => { /* connessione persa: il pool la rimpiazza */ });
    }
    return _pool;
}

export async function closeDbPool(): Promise<void>
{
    if(_pool !== null)
    {
        await _pool.end();
        _pool = null;
    }
}

/**
 * Helper per query SELECT con prepared statement.
 * Type generic = riga risultato attesa.
 */
export async function dbQuery<T = unknown>(
    sql: string,
    params: Record<string, unknown> | unknown[] = {}
): Promise<T[]>
{
    // mysql2 tipizza `values` come ExecuteValues: l'unione object|array del nostro
    // helper non combacia con un singolo overload → cast interno all'adapter.
    const [rows] = await getDbPool().execute(sql, params as never);
    return rows as T[];
}

/**
 * Helper per query INSERT/UPDATE/DELETE.
 * Ritorna affectedRows + insertId.
 */
export async function dbExecute(
    sql: string,
    params: Record<string, unknown> | unknown[] = {}
): Promise<{ affectedRows: number; insertId: number }>
{
    const [result] = await getDbPool().execute(sql, params as never);
    const r = result as { affectedRows: number; insertId: number };
    return { affectedRows: r.affectedRows, insertId: r.insertId };
}
