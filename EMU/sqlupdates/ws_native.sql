-- WebSocket nativo (sostituisce il plugin NitroWebsockets-3.1.jar). OFF di default:
-- il deploy non cambia nulla finché l'operatore non rimuove il jar e imposta ws.enabled=1.
-- Idempotente.
INSERT IGNORE INTO `emulator_settings` (`key`, `value`) VALUES
('ws.enabled', '0'),
('ws.host', '127.0.0.1'),
('ws.port', '2096'),
('ws.ip.header', 'X-Real-IP'),
('ws.whitelist', 'asteriacore.online,*.asteriacore.online,localhost');
