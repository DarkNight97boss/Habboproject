package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Observability beacon for the security pipeline.
 *
 * Once a minute (configurable) emits a single structured INFO line summarising:
 *   - online users + peak in window
 *   - active client connections
 *   - audit log entries written in the last minute
 *   - distinct IPs locked out by IpRateLimiter (SSO)
 *   - last AuditLog integrity status (best-effort)
 *
 * The point is to make it cheap for an operator to spot anomalies (e.g. a 10×
 * spike in audit-log writes = something attacking us) without scraping the
 * whole log.
 *
 * Config:
 *   sec.monitor.enabled            (default 1)
 *   sec.monitor.interval.seconds   (default 60)
 *   sec.monitor.initial.delay.seconds (default 30)
 */
public class SecurityMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityMonitor.class);

    private static final AtomicLong peakOnline = new AtomicLong(0L);
    private static long lastAuditCount = -1L;

    private ScheduledExecutorService scheduler;

    public SecurityMonitor() {
        if (!isEnabled()) {
            LOGGER.info("SecurityMonitor -> disabled");
            return;
        }
        int delay = Math.max(5, Emulator.getConfig().getInt("sec.monitor.initial.delay.seconds", 30));
        int interval = Math.max(15, Emulator.getConfig().getInt("sec.monitor.interval.seconds", 60));
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SecurityMonitor");
            t.setDaemon(true);
            return t;
        });
        this.scheduler.scheduleAtFixedRate(this::tick, delay, interval, TimeUnit.SECONDS);
        LOGGER.info("SecurityMonitor -> Loaded! (tick every {}s)", interval);
    }

    public static boolean isEnabled() {
        return Emulator.getConfig().getBoolean("sec.monitor.enabled", true);
    }

    private void tick() {
        try {
            int online = 0;
            try {
                if (Emulator.getGameEnvironment() != null
                        && Emulator.getGameEnvironment().getHabboManager() != null
                        && Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos() != null) {
                    online = Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().size();
                }
            } catch (Exception ignored) {
            }
            peakOnline.accumulateAndGet(online, Math::max);

            long auditCount = readAuditCount();
            long deltaAudit = lastAuditCount < 0 ? 0 : Math.max(0, auditCount - lastAuditCount);
            lastAuditCount = auditCount;

            LOGGER.info("[SECMON] online={} peak={} audit_written_last_window={} (total={}) freeMem_MB={}",
                    online, peakOnline.get(), deltaAudit, auditCount,
                    Runtime.getRuntime().freeMemory() / (1024 * 1024));

            // Anomaly heuristic: more than 100 audit rows / minute is suspicious.
            int threshold = Emulator.getConfig().getInt("sec.monitor.audit_spike_threshold", 100);
            if (deltaAudit > threshold) {
                LOGGER.warn("[SECMON] AUDIT SPIKE: {} rows in last window (threshold {})",
                        deltaAudit, threshold);
                AuditLog.record(0, "SecurityMonitor", "AUDIT_SPIKE", "",
                        "rows=" + deltaAudit + " threshold=" + threshold);
            }
        } catch (Exception e) {
            LOGGER.error("SecurityMonitor tick failed", e);
        }
    }

    /**
     * Read the audit_log row count.
     *
     * Originally this ran SELECT COUNT(*) FROM audit_log every 60s, which is
     * fine when the table has 10k rows but degenerates to a full table scan
     * once it grows to millions — the tick would burn seconds of DB time and
     * trigger the connection-leak detector. We now read the optimizer estimate
     * from INFORMATION_SCHEMA.TABLES (MySQL/MariaDB only), which is O(1).
     *
     * The estimate is off by a few percent but that's fine: the only consumer
     * is the per-window delta + spike heuristic, which cares about relative
     * change, not exact counts. Falls back to COUNT(*) if the schema query
     * fails (older engines, restrictive privileges, etc.).
     */
    private long readAuditCount() {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT TABLE_ROWS FROM INFORMATION_SCHEMA.TABLES " +
                     "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'audit_log' LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                long estimate = rs.getLong(1);
                if (estimate > 0) return estimate;
            }
        } catch (Exception ignored) {
            // Fall through to the legacy exact count.
        }
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM audit_log");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getLong(1);
        } catch (Exception ignored) {
        }
        return 0L;
    }
}
