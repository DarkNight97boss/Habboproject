# CMS Refactor — Habbo-style React 19 App

## TL;DR

Sostituiamo il CMS PHP `ZabboME` con un'app React 19 nuova (`CMS-V3`) che imita
visivamente habbo.it ufficiale. Il backend PHP viene **mantenuto** ma esposto
come API JSON sotto `/api/v2/*`. La logica di business attuale (login, register,
SSO ticket, profile) viene incapsulata in endpoint REST → la nuova UI li consuma.

Migrazione **graduale**: ogni pagina può essere convertita a React indipendente,
il vecchio CMS continua a girare in parallelo finché non è tutto migrato.

---

## A. Stato attuale CMS (audit completato)

| Componente | Tecnologia | Note |
|---|---|---|
| Backend | PHP 5.6 + `mysql_*` deprecato | 60 pagine in `app/tpl/skins/ZabboME/` |
| Frontend | jQuery 3 + Bootstrap 3.4 (CDN) | CSS plain, nessun build step |
| Templating | Sistema proprietario `class.template.php` | Sostituzione `{var}` semplice |
| Routing | `router.php` + `index.php` switch | Pretty URL via Apache rewrite |
| Auth | Session-based PHP, cookie PHPSESSID | IP-locked, no JWT, no CSRF |
| SSO | `users.auth_ticket` hash, generato al login | Letto da Nitro-V3 via `?sso=` |
| DB | MariaDB tramite `mysql_*` API (legacy) | Tabelle: users, friendships, bans, cms_settings, ... |
| Skin attivo | `ZabboME` (unico) | `app/management/config.php` |
| API JSON | **NESSUNA** | Tutto server-rendered |

**Problemi noti:**
- PHP 5.6 EOL da anni, vulnerabilità non patchate
- `mysql_query()` deprecato — vulnerabile SQL injection se non sanitizzato
- CSRF tokens assenti
- Bootstrap 3 obsoleto (responsive zoppo, accessibility scarsa)
- Nessuna typescript safety, nessun test
- Stile attuale (ZabboME) molto differente da habbo.it ufficiale

---

## B. Analisi visiva habbo.it ufficiale

### Palette colori (estratta via DOM scan)

| Ruolo | Hex | Uso |
|---|---|---|
| Background pagina | `#0c3a65` | Sfondo blu scuro principale |
| Background header | `#0a3b6e` | Top bar logo + login chip |
| Background nav | `#e8f0f5` | Bar bianca menu HOME/COMMUNITY/SHOP |
| Background sub-nav | `#0a1620` | Bar nera sotto-menu (FOTO/STANZE/...) |
| Card background | `#102f4d` | Card "ULTIME NOTIZIE", "STANZE" |
| Card header | `#2a6495` | Header card chiaro |
| Sidebar tip | `#1c4575` | "CONSIGLI DI SICUREZZA" box |
| Testo primario | `#7ECAEE` (azzurro chiaro) | Body text su sfondo blu |
| Testo header | `#FFFFFF` | Titoli "ULTIME NOTIZIE" |
| Accent giallo | `#FFB900` / `#FFEA00` | Logo Habbo, accenti |
| Accent verde "GIOCA" | `#0F7DBC` BG + `#FFDE00` border | CTA principale |
| Accent arancio | `#FF8F3A` BG + `#FFDE00` border | "Gioca ora!" |
| Border standard | `#A1B5C8` | Separatori |

### Tipografia
- **Font primario**: `Ubuntu, "Trebuchet MS", "Lucida Grande", sans-serif`
- **Titoli sezione**: UPPERCASE bold (es. "ULTIME NOTIZIE", "FOTO DA HABBO")
- **Sotto-titoli**: UPPERCASE thin + separatori `|`
- **Body**: 14-16px regular

### Pattern di layout

1. **Header (74px)** — logo Habbo centrato, login chip top-right
2. **Main nav (60px)** — bar bianca chiara, icone + UPPERCASE links (HOME, COMMUNITY, SHOP, IL MONDO DI HABBO, COLLEZIONABILI), "GIOCA" CTA verde a destra
3. **Sub-nav (40px)** — bar nera con sotto-categorie separate da `|`
4. **Hero stripe** — banner pixel art tipico Habbo con personaggi/scenari
5. **Content grid** — 2 colonne: 70% main + 30% sidebar (sticky)
6. **Card stile** — `border-radius: 0` (nessuno!), bordo sottile, ombra leggera, header colorato con titolo UPPERCASE
7. **Bottoni** — `border-radius: 10px`, bordo spesso 4px del colore complementare (giallo/rosa/etc.), testo bianco bold

### Componenti UI rilevati

- `HabboLogo` — il logo retro yellow/red
- `UserChip` — top-right: avatar mini + username + dropdown menu
- `NavBar` — bar bianca con 5 link principali
- `SubNav` — bar nera con breadcrumb categorie
- `HeroBanner` — banner pixel-art con titolo overlay
- `NewsCard` — card grande con immagine + titolo + body
- `SidebarTipBox` — "CONSIGLI DI SICUREZZA" box
- `PlayButton` (`GIOCA`) — CTA verde a forma di freccia/etichetta
- `PageTitle` — "FOTO DA HABBO" / "GALLERIA STANZE" UPPERCASE + sotto descrizione
- `RoomCard` — thumb stanza isometrica + nome + owner avatar
- `ProfileBanner` — header con avatar full-body + username

---

## C. Architettura proposta CMS-V3

### Stack

```
CMS-V3/
├── apps/web/          # React 19 frontend (Vite + Tailwind + React Router 7)
│   ├── src/
│   │   ├── pages/     # Route components (Home, Profile, Settings, ...)
│   │   ├── components/  # NavBar, Card, Button, ...
│   │   ├── api/       # fetch wrappers per /api/v2/*
│   │   ├── hooks/     # useAuth, useUserData, ...
│   │   ├── styles/    # Tailwind + design tokens
│   │   └── i18n/      # IT strings
│   └── vite.config.ts
├── apps/api/          # PHP 8 REST API (refactor del legacy)
│   ├── public/
│   │   └── index.php  # PSR-7 router (es. Slim 4)
│   ├── src/
│   │   ├── Controllers/
│   │   ├── Services/
│   │   ├── Repositories/  # PDO/Doctrine
│   │   └── Auth/      # JWT + CSRF
│   └── composer.json
└── packages/
    ├── ui-kit/        # Componenti React condivisi
    └── design-tokens/ # Palette, tipografia (TS)
```

### Tecnologie

| Layer | Scelta | Motivazione |
|---|---|---|
| Build | **Vite 8** | Coerente con Nitro-V3 |
| Frontend | **React 19.2** + **TypeScript 5** | Coerente con Nitro-V3 |
| Styling | **Tailwind 4** + **CSS variables** per design tokens | Hot-swap temi |
| Router | **React Router 7** (data routes) | SSR-ready se serve |
| Data | **TanStack Query 5** | Coerente con Nitro-V3 |
| State | **Zustand** | Coerente con Nitro-V3 |
| Forms | **React Hook Form** + **Zod** | Validation type-safe |
| Backend | **PHP 8.3** + **Slim 4** | Migrazione graduale, riusa logica esistente |
| ORM | **Doctrine DBAL** (no full ORM) | Lightweight, sicurezza prep-stmt |
| Auth | **JWT** (firma HS256) + refresh token | Stateless, scalabile |
| Tests | **Vitest** + **Playwright** + **PHPUnit** | Coerente con Nitro-V3 |

### API JSON (esempi)

```
POST   /api/v2/auth/login        { username, password } → { token, user }
POST   /api/v2/auth/register     { ... } → { token, user }
POST   /api/v2/auth/logout
POST   /api/v2/auth/refresh
GET    /api/v2/auth/sso          → { ticket }       (consumed by Nitro)

GET    /api/v2/me                → user profile
GET    /api/v2/users/:username   → public profile
PATCH  /api/v2/me                → update motto/look/email

GET    /api/v2/news              → list of news articles
GET    /api/v2/news/:slug        → single article

GET    /api/v2/rooms/popular     → top rooms
GET    /api/v2/rooms/:id         → room details

GET    /api/v2/staff             → staff list
GET    /api/v2/leaderboards      → ranks

POST   /api/v2/oracolo/requests  → submit feature request (integrazione Bot Oracolo!)
GET    /api/v2/oracolo/requests  → list (sortable)
POST   /api/v2/oracolo/requests/:id/vote
```

### Design system

**`packages/design-tokens/index.ts`**
```ts
export const habboPalette = {
  page:    { bg: '#0c3a65', text: '#7ECAEE' },
  header:  { bg: '#0a3b6e', text: '#ffffff' },
  navTop:  { bg: '#e8f0f5', text: '#0a3b6e', accent: '#2a6495' },
  navSub:  { bg: '#0a1620', text: '#7ECAEE' },
  card:    { bg: '#102f4d', headerBg: '#2a6495' },
  sidebar: { tipBg: '#1c4575' },
  accents: {
    yellow:  '#FFB900',
    yellowL: '#FFEA00',
    green:   '#8EDA55',
    orange:  '#FF8F3A',
    red:     '#923E3A',
    blue:    '#0F7DBC',
  },
  border:  '#A1B5C8',
};

export const habboType = {
  family: '"Ubuntu", "Trebuchet MS", "Lucida Grande", sans-serif',
  sizes:  { xs:12, sm:14, md:16, lg:18, xl:24, hero:36 },
  weights:{ regular:400, bold:700 },
};
```

**Tailwind config estende questi token** → tutti i componenti usano `bg-habbo-card` ecc.

### Routing client

```tsx
<Routes>
  <Route element={<AppShell />}>
    <Route path="/"                   element={<HomePage />} />
    <Route path="/community">
      <Route path="photos"            element={<PhotosPage />} />
      <Route path="rooms"             element={<RoomsPage />} />
      <Route path="fansite"           element={<FansitePage />} />
      <Route path="news"              element={<NewsPage />} />
    </Route>
    <Route path="/shop"               element={<ShopPage />} />
    <Route path="/world"              element={<WorldPage />} />
    <Route path="/collectibles"       element={<CollectiblesPage />} />
    <Route path="/profile/:username"  element={<ProfilePage />} />
    <Route path="/settings/*"         element={<SettingsPage />} />
    <Route path="/client"             element={<ClientPage />} />  {/* Nitro-V3 iframe */}
    <Route path="/oracolo"            element={<OracoloPage />} />  {/* Bot Oracolo bacheca */}
  </Route>
  <Route path="/login"                element={<LoginPage />} />
  <Route path="/register"             element={<RegisterPage />} />
</Routes>
```

### Componenti UI base (ui-kit)

```
<HabboLogo />             # SVG logo originale
<AppShell>                # Layout completo header + navs + sidebar
<NavBar items={...} />    # Bar bianca con 5 link + GIOCA CTA
<SubNav crumbs={...} />   # Bar nera sotto
<HeroBanner img title />  # Hero pixel-art
<Card title>...</Card>    # Card stile Habbo
<TipBox color>            # Sidebar yellow/blue tip box
<HabboButton variant>     # Variants: primary, secondary, danger, success
<UserChip />              # Top-right login chip
<RoomTile />              # Card stanza
<NewsTile />              # Card news
<ProfileBanner />         # Banner profilo
<AvatarImage size />      # Avatar full/head
```

---

## D. Roadmap migrazione (3 fasi)

### Fase 1 — Foundation (1 settimana, ~30h)
- [ ] Scaffold `CMS-V3/` con Vite + React + Tailwind
- [ ] Design tokens + UI kit base (HabboLogo, NavBar, SubNav, Card, Button, ...)
- [ ] PHP 8 API skeleton (Slim 4) — endpoint `/auth/login`, `/auth/sso`, `/me`
- [ ] Pagine: Home, Login, Register, Client iframe
- [ ] Deploy in parallelo: vecchio `/` PHP, nuovo `/v3/` React

### Fase 2 — Core pages (2 settimane, ~60h)
- [ ] Profile pubblico (`/profile/:username`)
- [ ] Settings (privacy, password, email, 2FA, personaggi)
- [ ] Community (photos, rooms, fansite, news)
- [ ] Shop UI (catalogo, valute, transazioni)
- [ ] Bot Oracolo bacheca (`/oracolo`)
- [ ] Mobile responsive

### Fase 3 — Switch & cleanup (1 settimana, ~20h)
- [ ] Migrate housekeeping/admin pages (ase/)
- [ ] Cutover: `/v3/` diventa `/`
- [ ] Vecchio PHP rimane come fallback su `/legacy/` per 30 giorni
- [ ] Cleanup + monitoring + bugfix

**Totale stimato**: **~110h** lavoro effettivo

---

## E. Cosa posso fare subito

1. **Scaffold del repo** `CMS-V3/` con tutto il boilerplate (Vite, Tailwind config, design tokens, UI kit base)
2. **Implementare la HomePage** con NavBar + Hero + News list (consumando i dati dal PHP legacy via un quick proxy endpoint)
3. **Mockare le 4-5 pagine più importanti** in HTML/CSS che imita habbo.it pixel-perfect

Per partire mi serve sapere:

- **Vuoi che il CMS-V3 viva in un nuovo repo separato** (es. `Github/CMS-V3/`) **o dentro Habboproject/CMS-V3/**?
- **Backend PHP**: rifaccio in PHP 8 + Slim **o** preferisci Node/Bun? (PHP 8 è più veloce da migrare perché posso riusare la logica esistente)
- **Approccio cutover**: full rewrite tutto-in-una-volta, oppure **strangler pattern** (sostituisco pagina per pagina mantenendo il vecchio working)?

Quando rispondi parto subito con la Fase 1.
