// EMU load/resilience flood — SICURO, usa-e-getta. Flood di CONNESSIONI TCP grezze
// + frame piccoli e OVERSIZE (esercita ConnectionLimiter, reaper pre-login e il cap
// di framing) e misura se l'EMU RESTA VIVO. NON contiene exploit "armati" (repo
// pubblico): il kill guild-badge (#222) si verifica a codice.
//
// Uso:  EMU_HOST=127.0.0.1 EMU_PORT=13000 node flood.mjs --conns 400 --seconds 15
// Guardia: solo host locali/privati.
import net from 'node:net';

const args = process.argv.slice(2);
const opt = (n, d) => { const i = args.indexOf(n); return i >= 0 ? args[i + 1] : d; };
const HOST = process.env.EMU_HOST || '127.0.0.1';
const PORT = parseInt(process.env.EMU_PORT || '13000', 10);
const CONNS = parseInt(opt('--conns', '300'), 10);
const SECONDS = parseInt(opt('--seconds', '15'), 10);

const okHost = ['localhost', '127.0.0.1', '::1', '0.0.0.0'].includes(HOST) || /^(10\.|192\.168\.|172\.(1[6-9]|2\d|3[01])\.)/.test(HOST);
if (!okHost) { console.error(`RIFIUTATO: '${HOST}' non è locale/privato. Solo istanze usa-e-getta.`); process.exit(2); }

const frame = (header, bodyLen) => { const n = Math.max(0, bodyLen); const b = Buffer.alloc(6 + n); b.writeInt32BE(2 + n, 0); b.writeInt16BE(header & 0xffff, 4); return b; };
const alive = () => new Promise(res => { const s = net.connect({ host: HOST, port: PORT }); s.setTimeout(3000); s.on('connect', () => { s.destroy(); res(true); }); s.on('error', () => res(false)); s.on('timeout', () => { s.destroy(); res(false); }); });

console.log(`[flood] target ${HOST}:${PORT} — ${CONNS} conn TCP per ${SECONDS}s`);
console.log(`[flood] socket vivo PRIMA: ${await alive() ? 'sì' : 'NO'}`);
let opened = 0, err = 0, closed = 0; const socks = [];
for (let i = 0; i < CONNS; i++) {
  const s = net.connect({ host: HOST, port: PORT });
  s.on('connect', () => { opened++; try { s.write(frame(4000, 8)); s.write(frame(4000, 5 * 1024 * 1024)); } catch {} });
  s.on('error', () => { err++; }); s.on('close', () => { closed++; });
  socks.push(s);
  if (i % 50 === 0) await new Promise(r => setTimeout(r, 15));
}
const end = Date.now() + SECONDS * 1000;
while (Date.now() < end) { for (const s of socks) { try { if (!s.destroyed) s.write(frame(4000, 16)); } catch {} } await new Promise(r => setTimeout(r, 1000)); }
for (const s of socks) { try { s.destroy(); } catch {} }
await new Promise(r => setTimeout(r, 800));
console.log(`[flood] aperte=${opened} errori/chiuse-dal-server=${err} chiuse=${closed}`);
const after = await alive();
console.log(`[flood] socket vivo DOPO: ${after ? 'sì' : 'NO'}`);
console.log(after ? '[flood] ✅ EMU SOPRAVVISSUTO al flood (framing/connessioni gestiti).'
                  : '[flood] ⚠️ EMU non accetta più connessioni: annota conns/secondi e indaga.');
