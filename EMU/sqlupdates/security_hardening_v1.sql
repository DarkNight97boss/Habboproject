-- Hardening pass: per-IP rate-limit on SSO + SecurityMonitor observability.
-- See com.eu.habbo.core.IpRateLimiter / SecurityMonitor.

INSERT INTO `emulator_settings` (`key`, `value`) VALUES
  -- Per-IP SSO brute-force protection.
  ('sec.ip.sso.max_failures', '10'),
  ('sec.ip.sso.window_seconds', '300'),
  ('sec.ip.sso.lockout_seconds', '900'),

  -- Security observability beacon.
  ('sec.monitor.enabled', '1'),
  ('sec.monitor.interval.seconds', '60'),
  ('sec.monitor.initial.delay.seconds', '30'),
  ('sec.monitor.audit_spike_threshold', '100')
ON DUPLICATE KEY UPDATE `key` = `key`;
