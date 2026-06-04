-- 015_cmd_bringroom.sql — Comando staff :bringroom (#14 teleport di gruppo).
--
-- Registra il permesso e i testi del nuovo comando sull'EMU (tabelle Arcturus
-- `permissions` + `emulator_texts`, stesso DB `ms`). Il deploy applica questa
-- migrazione PRIMA del restart EMU, così al riavvio PermissionsManager e
-- TextsManager caricano colonna+testi e CommandHandler registra il comando.
--
-- NB: l'ALTER gira una sola volta (tracker schema_migrations).

-- Permesso (enum '0'/'1' come gli altri cmd_*).
ALTER TABLE `permissions` ADD COLUMN `cmd_bringroom` ENUM('0','1') NOT NULL DEFAULT '0';

-- Concedi agli stessi rank che hanno gia' :summonrank (mass-summon per rank).
UPDATE `permissions` SET `cmd_bringroom` = `cmd_summonrank`;

-- Trigger + messaggi (italiano). ON DUPLICATE per idempotenza sulla chiave.
INSERT INTO `emulator_texts` (`key`, `value`) VALUES
    ('commands.keys.cmd_bringroom',         'bringroom;portastanza'),
    ('commands.error.cmd_bringroom.usage',  'Uso: :bringroom <id stanza> — porta qui gli occupanti di quella stanza.'),
    ('commands.error.cmd_bringroom.same',   'Sono gia'' in questa stanza.'),
    ('commands.succes.cmd_bringroom.done',  'Spostati %count% utenti in questa stanza.')
ON DUPLICATE KEY UPDATE `value` = VALUES(`value`);
