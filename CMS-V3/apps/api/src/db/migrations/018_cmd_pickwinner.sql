-- 018_cmd_pickwinner.sql — Comando :pickwinner (raffle/giveaway).
--
-- Registra permesso + testi sull'EMU (tabelle Arcturus `permissions` +
-- `emulator_texts`, DB `ms`). Applicata PRIMA del restart EMU dal deploy.
-- Permesso a ROOM_OWNER ('2') per ogni rank → usabile dall'host nella propria
-- stanza (oltre allo staff). NB: l'ALTER gira una sola volta (tracker).

ALTER TABLE `permissions` ADD COLUMN `cmd_pickwinner` ENUM('0','1','2') NOT NULL DEFAULT '0';

-- ROOM_OWNER: usabile da chi ha i diritti sulla stanza (host/proprietario).
UPDATE `permissions` SET `cmd_pickwinner` = '2';

INSERT INTO `emulator_texts` (`key`, `value`) VALUES
    ('commands.keys.cmd_pickwinner',     'pickwinner;vincitore;raffle'),
    ('commands.error.cmd_pickwinner.empty', 'Nessun partecipante in stanza.'),
    ('commands.succes.cmd_pickwinner',   '🎉 Il vincitore e'' %user%!')
ON DUPLICATE KEY UPDATE `value` = VALUES(`value`);
