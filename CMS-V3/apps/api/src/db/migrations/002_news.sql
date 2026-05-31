-- =========================================================================
--  CMS-V3 — gestione news.
--  Sostituisce l'array hardcoded NEWS in community.ts.
--  Schema modellato sul vecchio `cms_news` PHP ma con nomi più puliti e
--  prefisso cms_v3_ per non collidere col legacy CMS che vive nello stesso DB.
-- =========================================================================

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `cms_v3_news` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `slug` VARCHAR(80) NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `category` VARCHAR(40) NOT NULL DEFAULT 'aggiornamenti-su-habbo',
    `category_label` VARCHAR(60) NOT NULL DEFAULT 'Aggiornamenti su Habbo',
    `summary` VARCHAR(500) NOT NULL,
    `body_html` MEDIUMTEXT NOT NULL,
    `image` VARCHAR(255) NOT NULL DEFAULT '',
    `author_id` INT(11) NULL DEFAULT NULL,
    `published` TINYINT(1) NOT NULL DEFAULT 1,
    `created_at` INT(11) NOT NULL,
    `updated_at` INT(11) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_slug` (`slug`),
    KEY `idx_published_created` (`published`, `created_at` DESC),
    KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed: copia delle 4 news hardcoded che erano in community.ts.
-- INSERT IGNORE → idempotente, non duplica se già migrato.
INSERT IGNORE INTO `cms_v3_news` (
    slug, title, category, category_label, summary, body_html, image,
    author_id, published, created_at, updated_at
) VALUES
(
    'welcome',
    'BENVENUTO SU HABBO',
    'aggiornamenti-su-habbo',
    'Aggiornamenti su Habbo',
    'Il tuo hotel virtuale è pronto. Esplora le stanze, fai nuovi amici e personalizza il tuo Habbo. Clicca su GIOCA per iniziare!',
    '<h2>Benvenuto, nuovo Habbo!</h2><p>Habboproject è una community italiana dedicata al gioco originale, restaurata e curata per offrire la stessa esperienza pixel-art che amavamo nei primi 2000.</p><p>Cosa puoi fare:</p><ul><li>Crea il tuo avatar e personalizza il look</li><li>Visita le stanze pubbliche e fai nuove amicizie</li><li>Costruisci la tua stanza usando il furni del catalogo</li><li>Partecipa agli eventi staff-organizzati</li></ul><p>Clicca su <strong>GIOCA</strong> in alto a destra per entrare nell''hotel.</p>',
    '/assets/habbo/web_images/habbo-web-articles/lpromo_jonas_may26.png',
    NULL, 1, UNIX_TIMESTAMP('2026-05-31 12:00:00'), UNIX_TIMESTAMP('2026-05-31 12:00:00')
),
(
    'roller-disco',
    'LIVE ORA: Roller Disco!',
    'campagne-attivita',
    'Campagne & Attività',
    'La Pista Retrò ti sta chiamando. Entra e dai un''occhiata!',
    '<h2>Pattini, luci, musica anni ''70!</h2><p>La Pista Retrò è aperta nella stanza pubblica "Roller Disco". Indossa i pattini gratis che trovi nel guardaroba e lanciati sulla pista a tempo di musica.</p><p>Eventi schedulati ogni sera alle 21:00 con DJ Habbo dal vivo.</p>',
    '/assets/habbo/web_images/habbo-web-articles/lpromo_rollerdiscoFL_may26.png',
    NULL, 1, UNIX_TIMESTAMP('2026-05-01 12:00:00'), UNIX_TIMESTAMP('2026-05-01 12:00:00')
),
(
    'comandi-italiani',
    'COMANDI ITALIANI',
    'aggiornamenti-su-habbo',
    'Aggiornamenti su Habbo',
    'Ora puoi usare :bando, :tira, :spingi, :silenzia e altri 140 comandi in italiano.',
    '<h2>140 comandi tradotti</h2><p>Ora puoi usare i comandi in italiano in chat senza dover ricordare quelli in inglese:</p><ul><li><code>:bando &lt;nome&gt;</code> — banna un utente dalla tua stanza</li><li><code>:tira &lt;nome&gt;</code> — chiama un utente vicino a te</li><li><code>:spingi &lt;nome&gt;</code> — sposta un utente di una tile</li><li><code>:silenzia &lt;nome&gt; &lt;minuti&gt;</code> — mute temporaneo</li><li>e altri 140 comandi…</li></ul><p>Lista completa via <code>:comandi</code> in chat.</p>',
    '/assets/habbo/web_images/habbo-web-articles/lpromo_jonas_may26.png',
    NULL, 1, UNIX_TIMESTAMP('2026-05-29 12:00:00'), UNIX_TIMESTAMP('2026-05-29 12:00:00')
),
(
    'oracolo',
    'BOT ORACOLO ATTIVO',
    'nuove-funzionalita',
    'Nuove funzionalità',
    'Entra nella stanza Oracolo e proponi le tue idee per nuove feature.',
    '<h2>Cosa è Oracolo?</h2><p>Oracolo è un bot che vive nella stanza omonima e ascolta le proposte di feature che la community vorrebbe vedere implementate in Habboproject.</p><p>Come funziona:</p><ul><li>Entra nella stanza "Oracolo" (cerca su Navigator)</li><li>Scrivi la tua proposta normalmente in chat</li><li>Oracolo la salva e altri utenti possono votarla</li><li>Le top-10 proposte vengono valutate dallo staff per l''implementazione</li></ul><p>Comandi staff: <code>:oracolo lista</code>, <code>:oracolo accetta &lt;id&gt;</code>, <code>:oracolo rifiuta &lt;id&gt;</code>.</p>',
    '/assets/habbo/web_images/habbo-web-articles/lpromo_HabboPulse.png',
    NULL, 1, UNIX_TIMESTAMP('2026-05-30 12:00:00'), UNIX_TIMESTAMP('2026-05-30 12:00:00')
);
