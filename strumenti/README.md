# Strumenti

Snapshot leggero (solo source, **no build artifacts**) di tutto il materiale
ausiliario al progetto Habbo retro:

- fork dei tool della community (Nitro, converter, websocket plugin, ecc.)
- script di build/deploy
- documentazione di setup

## Cosa NON è incluso

Per tenere il repo leggero, sono ESCLUSI:

| Pattern | Motivo |
|---|---|
| `node_modules/`, `target/`, `dist/`, `build/`, `.vite/`, `.cache/` | Rigenerati da `yarn install` / `mvn` / `vite build` |
| `*.log`, `*.jar`, `*.tar.gz`, `*.zip` | Build output / log |
| `.git/` di ogni sotto-repo | Tieni snapshot, non sub-repo aggiornabili |
| `Arcturus-MS/` | Working copy 1.8 GB — già coperta da `EMU/` nel root |
| `client-dist/`, `downloads/` | Output di build temporanei |
| Eclipse `.metadata/`, `.classpath`, `.project`, `.settings/` | IDE local junk |

Per ricostruire qualsiasi cosa: `cd strumenti/<nome>` → `yarn install` o
`mvn clean package` a seconda dello stack.

## Contenuto

| Cartella | Origine | Cosa fa |
|---|---|---|
| `Arcturus-Community/` | git.krews.org/morningstar/Arcturus-Community | Sorgenti upstream EMU Arcturus (riferimento) |
| `Nitro-Default/` | duckietm/Nitro-Default | Asset di default Nitro (chat bubbles, icons) |
| `Nitro-V3/` | DarkNight97boss/Nitro-V3 (fork) | Client React 19 (sorgenti) |
| `Nitro_Render_V3/` | DarkNight97boss/Nitro_Render_V3 (fork) | Renderer SDK PixiJS v8 |
| `all-in-1-converter/` | duckietm/all-in-1-converter | Convertitore SWF→nitro |
| `ms-websockets/` | DarkNight97boss/ms-websockets | Plugin EMU WebSocket |
| `nitro-assets/` | harrydev44/nitro-assets | Asset library Nitro |
| `nitro-converter/` | billsonnn/nitro-converter | Converter ufficiale |
| `nitro-react/` | DarkNight97boss/nitro-react | Vecchio client React (legacy) |
| `_scratch_translate/`, `_translate_work/` | (locale) | Scratch traduzioni |
| `scripts/` | — | `.ps1` build/deploy + `SETUP.md` |

## Setup tipico per uno strumento

```sh
cd Habboproject/strumenti/Nitro-V3
yarn install
yarn dev
```

oppure per i Java:

```sh
cd Habboproject/strumenti/ms-websockets
mvn clean package -DskipTests
```
