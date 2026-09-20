// EMU load/resilience flood — SICURO e usa-e-getta.
// Uso:
//   EMU_WS=ws://localhost:13000 node flood.mjs --conns 300 --seconds 30
//   EMU_HEALTH=http://localhost:19090 node flood.mjs --check
//
// Cosa fa: apre N connessioni WebSocket concorrenti verso l'EMU, invia frame
// piccoli e frame OVERSIZE (per esercitare il cap di framing e il reaper
// pre-login), e misura quante connessioni l'EMU accetta/rifiuta e se resta
// VIVO (via /readyz) prima e dopo. NON contiene exploit "armati" (es. il kill
// guild-badge): è uno strumento di resilienza, non un'arma — e questo è un repo
// pubblico. Il fix del guild-badge è la PR #222; verificarlo si fa a livello di
// codice o manualmente in un ambiente dev con una sessione autenticata.
//
// GUARDIA: rifiuta qualsiasi target non locale/privato.

const args = process.argv.slice(2);
const opt = (name, def) => { const i = args.indexOf(name); return i >= 0 ? args[i + 1] : def; };
const has = (name) => args.includes(name);

const WS = process.env.EMU_WS || 'ws://localhost:13000';
const HEALTH = process.env.EMU_HEALTH || 'http://localhost:19090';
const CONNS = parseInt(opt('--conns', '200'), 10);
const SECONDS = parseInt(opt('--seconds', '20'), 10);

function assertLocal(u) {
  const host = new URL(u).hostname;
  const ok = ['localhost', '127.0.0.1', '::1', '0.0.0.0'].includes(host)
    || /^(10\.|192\.168\.|172\.(1[6-9]|2\d|3[01])\.)/.test(host);
  if (!ok) { console.error(`RIFIUTATO: '${host}' non è locale/privato. Solo istanze usa-e-getta.`); process.exit(2); }
}
assertLocal(WS); assertLocal(HEALTH);

async function readyz() {
  try { const r = await fetch(`${HEALTH}/readyz`, { signal: AbortSignal.timeout(4000) }); return `${r.status}`; }
  catch (e) { return `DOWN (${e.name})`; }
}

// Framing Habbo: [int32 length][int16 header][body]. Frame oversize per testare il cap.
function framed(header, bodyLen) {
  const body = Buffer.alloc(Math.max(0, bodyLen));
  const buf = Buffer.alloc(4 + 2 + body.length);
  buf.writeInt32BE(2 + body.length, 0);
  buf.writeInt16BE(header & 0xffff, 4);
  body.copy(buf, 6);
  return buf;
}

async function runCheck() {
  console.log(`[check] EMU health @ ${HEALTH}/readyz → ${await readyz()}`);
}

async function runFlood() {
  console.log(`[flood] target ${WS} — ${CONNS} conn per ${SECONDS}s`);
  console.log(`[flood] readyz PRIMA: ${await readyz()}`);
  let opened = 0, refused = 0, closed = 0;
  const sockets = [];
  const deadline = Date.now() + SECONDS * 1000;

  for (let i = 0; i < CONNS; i++) {
    try {
      const ws = new WebSocket(WS);
      ws.binaryType = 'arraybuffer';
      ws.onopen = () => {
        opened++;
        // piccolo frame "release version" + un frame oversize (cap framing).
        try { ws.send(framed(4000, 8)); } catch {}
        try { ws.send(framed(4000, 5 * 1024 * 1024)); } catch {} // 5MB: deve essere droppato dal cap
      };
      ws.onerror = () => { refused++; };
      ws.onclose = () => { closed++; };
      sockets.push(ws);
    } catch { refused++; }
    if (i % 50 === 0) await new Promise(r => setTimeout(r, 20));
  }

  while (Date.now() < deadline) {
    for (const ws of sockets) { try { if (ws.readyState === 1) ws.send(framed(4000, 16)); } catch {} }
    await new Promise(r => setTimeout(r, 1000));
  }
  for (const ws of sockets) { try { ws.close(); } catch {} }
  await new Promise(r => setTimeout(r, 1000));
  console.log(`[flood] aperte=${opened} rifiutate=${refused} chiuse=${closed}`);
  const after = await readyz();
  console.log(`[flood] readyz DOPO: ${after}`);
  console.log(after.startsWith('200')
    ? '[flood] ✅ EMU SOPRAVVISSUTO al flood di connessioni/framing.'
    : '[flood] ⚠️ EMU non risponde 200 su /readyz: annota conns/secondi e indaga.');
}

if (has('--check')) runCheck();
else runFlood();
