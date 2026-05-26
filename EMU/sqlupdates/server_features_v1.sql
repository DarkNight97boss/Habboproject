-- Server-only feature wave: HotelEvents + XP multiplier + DbCleanup config keys.
-- (Chat spam guard config lives in chat_spam_guard.sql, applied separately.)

INSERT INTO `emulator_settings` (`key`, `value`) VALUES
  -- Hotel Events scheduler
  ('hotel.events.enabled', '1'),
  ('hotel.events.interval.minutes', '30'),
  ('hotel.events.initial.delay.seconds', '120'),
  ('hotel.events.free_credits.amount', '50'),
  ('hotel.events.free_pixels.amount', '100'),
  ('hotel.events.double_xp.factor', '2.0'),
  ('hotel.events.double_xp.minutes', '60'),

  -- Time-based XP multipliers (battle pass)
  ('xp.multiplier.weekend', '1.3'),
  ('xp.multiplier.night', '1.5'),

  -- DB auto-cleanup
  ('db.cleanup.enabled', '1'),
  ('db.cleanup.interval.hours', '24'),
  ('db.cleanup.initial.delay.seconds', '300'),
  ('db.cleanup.max_rows_per_table', '5000')
ON DUPLICATE KEY UPDATE `key` = `key`;
