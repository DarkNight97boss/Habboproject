-- Trade safety lock config: enforce a stable window after every item change
-- before accept can take effect (blocks last-second-swap scams), plus audit log
-- thresholds for flagging unusual trade patterns.
-- See com.eu.habbo.habbohotel.rooms.RoomTrade.

INSERT INTO `emulator_settings` (`key`, `value`) VALUES
  ('trade.safety.cooldown_ms', '3000'),
  ('trade.safety.audit_min_items', '10'),
  ('trade.safety.audit_min_ratio', '5')
ON DUPLICATE KEY UPDATE `key` = `key`;
