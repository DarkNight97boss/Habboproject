-- Wave 18 audit fixes: add UNIQUE keys to defeat dupe/spam vectors.
-- Each ALTER is preceded by an idempotent dedup so the migration is safe even
-- if duplicate rows already exist (would otherwise block UNIQUE creation).

-- 1) calendar_rewards_claimed: prevent two parallel claims persisting twice.
-- The table has no surrogate `id`, so dedup via snapshot-truncate-reinsert.
CREATE TEMPORARY TABLE IF NOT EXISTS _crc_distinct AS
  SELECT user_id, campaign_id, day, MIN(reward_id) AS reward_id, MIN(timestamp) AS timestamp
  FROM calendar_rewards_claimed
  GROUP BY user_id, campaign_id, day;
DELETE FROM calendar_rewards_claimed;
INSERT INTO calendar_rewards_claimed (user_id, campaign_id, day, reward_id, timestamp)
  SELECT user_id, campaign_id, day, reward_id, timestamp FROM _crc_distinct;
DROP TEMPORARY TABLE _crc_distinct;

SET @idx := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'calendar_rewards_claimed'
    AND INDEX_NAME = 'uq_claim');
SET @sql := IF(@idx = 0,
  'ALTER TABLE calendar_rewards_claimed ADD UNIQUE KEY uq_claim (user_id, campaign_id, day)',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2) messenger_friendrequests: prevent duplicate pending requests per pair.
DELETE r1 FROM messenger_friendrequests r1
  JOIN messenger_friendrequests r2
    ON r1.user_from_id = r2.user_from_id AND r1.user_to_id = r2.user_to_id
   AND r1.id > r2.id;

SET @idx := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'messenger_friendrequests'
    AND INDEX_NAME = 'uq_pair');
SET @sql := IF(@idx = 0,
  'ALTER TABLE messenger_friendrequests ADD UNIQUE KEY uq_pair (user_from_id, user_to_id)',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 3) room_votes: prevent vote inflation (table had no PRIMARY/UNIQUE).
-- Strategy: snapshot distinct pairs, truncate the table, re-insert distincts.
-- Safe because losing the few orphan duplicates is harmless and the UNIQUE
-- below will hold from now on.
CREATE TEMPORARY TABLE IF NOT EXISTS _rv_distinct
  AS SELECT DISTINCT user_id, room_id FROM room_votes;
DELETE FROM room_votes;
INSERT INTO room_votes (user_id, room_id) SELECT user_id, room_id FROM _rv_distinct;
DROP TEMPORARY TABLE _rv_distinct;

SET @idx := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'room_votes'
    AND INDEX_NAME = 'uq_vote');
SET @sql := IF(@idx = 0,
  'ALTER TABLE room_votes ADD UNIQUE KEY uq_vote (user_id, room_id)',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Wave 18 config keys
INSERT INTO `emulator_settings` (`key`, `value`) VALUES
  ('hotel.marketplace.max_active_offers', '50'),
  ('hotel.room.users_max.hard_cap', '250')
ON DUPLICATE KEY UPDATE `key` = `key`;
