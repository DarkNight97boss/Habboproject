-- 016_cmd_roll.sql — Comando :roll (dado per giochi/raffle).
--
-- Registra permesso + testi del comando sull'EMU (tabelle Arcturus `permissions`
-- + `emulator_texts`, DB `ms`). Applicata PRIMA del restart EMU dal deploy.
-- Permesso concesso a TUTTI i rank (comando di gioco innocuo, con cooldown).
-- NB: l'ALTER gira una sola volta (tracker schema_migrations).

ALTER TABLE `permissions` ADD COLUMN `cmd_roll` ENUM('0','1') NOT NULL DEFAULT '0';

-- Usabile da tutti i giocatori.
UPDATE `permissions` SET `cmd_roll` = '1';

INSERT INTO `emulator_texts` (`key`, `value`) VALUES
    ('commands.keys.cmd_roll', 'roll;dado;dice')
ON DUPLICATE KEY UPDATE `value` = VALUES(`value`);
