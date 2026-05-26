-- Anti-Tanji / G-Earth defences v1.
--   * UnknownPacketGuard: kicks clients fuzzing unregistered packet headers
--   * Origin whitelist: stretto sui nostri host (CMS:8080 + client:8090)
--     (gia` applicato live; replicato qui per re-installs)

INSERT INTO `emulator_settings` (`key`, `value`) VALUES
  ('sec.unknown_packet.max', '5'),
  ('sec.unknown_packet.window_sec', '30'),
  ('sec.unknown_packet.kick', '1')
ON DUPLICATE KEY UPDATE `key` = `key`;

-- Restringi l'Origin whitelist del plugin nitro-websockets.
-- Aggiungi qui i nostri host legittimi separati da virgola. Un MitM
-- (Tanji/G-Earth) tipicamente ha Origin diverso o assente -> 403.
UPDATE `emulator_settings`
   SET value = '127.0.0.1,localhost,*127.0.0.1:8080,*localhost:8080,*127.0.0.1:8090,*localhost:8090'
 WHERE `key` = 'websockets.whitelist';
