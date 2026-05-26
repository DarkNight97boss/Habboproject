-- Two new lifetime-based achievements that integrate with existing features.
-- The client (nitro) already renders any achievement defined in this table, so
-- no protocol change is needed.
--
-- Hook points (in Java):
--   - DailyStreak.claim()   -> +1 to ACH_LifetimeLogins
--   - RoomTrade.tradeItems()-> +1 to ACH_LifetimeTrades for both sides

INSERT INTO `achievements` (`name`, `category`, `level`, `reward_amount`, `reward_type`, `points`, `progress_needed`) VALUES
  ('ACH_LifetimeLogins',  'identity', 1,  100, 0, 5,   7),
  ('ACH_LifetimeLogins',  'identity', 2,  250, 0, 10,  30),
  ('ACH_LifetimeLogins',  'identity', 3,  500, 0, 25,  100),
  ('ACH_LifetimeLogins',  'identity', 4, 1500, 0, 100, 365),

  ('ACH_LifetimeTrades',  'social',   1,  100, 0, 5,   5),
  ('ACH_LifetimeTrades',  'social',   2,  250, 0, 10,  25),
  ('ACH_LifetimeTrades',  'social',   3,  500, 0, 25,  100),
  ('ACH_LifetimeTrades',  'social',   4, 1500, 0, 100, 500)
ON DUPLICATE KEY UPDATE `progress_needed` = VALUES(`progress_needed`);
