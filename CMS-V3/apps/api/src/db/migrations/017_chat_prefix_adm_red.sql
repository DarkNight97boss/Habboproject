-- 017_chat_prefix_adm_red.sql — Prefisso chat: "[ADM]" → "ADM" rosso.
--
-- Due cambi:
--  1) Formato GLOBALE del prefisso chat: toglie le parentesi quadre.
--     (era '[<font color="%color%">%prefix%</font>] ')
--     NB: e' un setting GLOBALE → vale per OGNI rank con un prefisso, non solo ADM.
--  2) Colore del prefisso del rank ADM = rosso.
--
-- Il formato (`Room.PREFIX_FORMAT`) viene caricato al BOOT dell'EMU
-- (PluginManager legge `room.chat.prefix.format`), quindi questo PR tocca anche
-- l'EMU per forzare rebuild+restart e applicare tutto subito. `prefix_color` si
-- ricarica con le permissions al riavvio. Idempotente.

INSERT INTO `emulator_settings` (`key`, `value`) VALUES
    ('room.chat.prefix.format', '<font color="%color%">%prefix%</font> ')
ON DUPLICATE KEY UPDATE `value` = VALUES(`value`);

-- Rosso sul prefisso del rank ADM (colonna prefix_color, formato #RRGGBB).
UPDATE `permissions` SET `prefix_color` = '#FF3030' WHERE `prefix` = 'ADM';
