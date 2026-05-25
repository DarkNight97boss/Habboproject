-- Tamper-evident audit log (hash-chain) for sensitive/privileged actions.
-- Applied to DB `ms`. See com.eu.habbo.core.AuditLog.
CREATE TABLE IF NOT EXISTS audit_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  timestamp INT NOT NULL,
  actor_id INT NOT NULL,
  actor_name VARCHAR(64) NOT NULL DEFAULT '',
  action VARCHAR(64) NOT NULL,
  target VARCHAR(128) NOT NULL DEFAULT '',
  detail VARCHAR(512) NOT NULL DEFAULT '',
  prev_hash CHAR(64) NOT NULL DEFAULT '',
  hash CHAR(64) NOT NULL,
  INDEX idx_actor (actor_id),
  INDEX idx_action (action),
  INDEX idx_ts (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
