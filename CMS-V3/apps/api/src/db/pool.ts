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
    queueLimit: 0,
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
    const [rows] = await getDbPool().execute(sql, params);
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
    const [result] = await getDbPool().execute(sql, params);
    const r = result as { affectedRows: number; insertId: number };
    return { affectedRows: r.affectedRows, insertId: r.insertId };
}
