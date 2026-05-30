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

/**
 * Anti-dupe / integrity watchdog.
 *
 * Periodically runs READ-ONLY scans over the database looking for anomalies that
 * indicate duplication, overflow or data-corruption, and records a tamper-evident
 * alert via {@link AuditLog} (action = WATCHDOG) plus a WARN log line for staff.
 *
 * It never modifies game state, so it cannot break gameplay. Tunables (config.ini
 * or emulator_settings): watchdog.enabled (default 1), watchdog.interval.minutes
 * (30), watchdog.initial.delay.seconds (60), watchdog.items.threshold (50000).
 */
public class Watchdog {

    private static final Logger LOGGER = LoggerFactory.getLogger(Watchdog.class);

    private ScheduledExecutorService scheduler;

    public Watchdog() {
        if ("0".equals(Emulator.getConfig().getValue("watchdog.enabled", "1"))) {
            LOGGER.info("Watchdog -> disabilitato");
            return;
        }

        int initialDelay = Emulator.getConfig().getInt("watchdog.initial.delay.seconds", 60);
        int intervalMinutes = Emulator.getConfig().getInt("watchdog.interval.minutes", 30);

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Watchdog");
            t.setDaemon(true);
            return t;
        });
        this.scheduler.scheduleAtFixedRate(this::scan, initialDelay, intervalMinutes * 60L, TimeUnit.SECONDS);

        LOGGER.info("Watchdog -> Caricato! (scansione ogni {} min)", intervalMinutes);
    }

    private void scan() {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {
            int anomalies = 0;

            // 1) Negative credit balances (impossible in normal play; overflow/exploit symptom).
            try (PreparedStatement ps = connection.prepareStatement("SELECT id, username, credits FROM users WHERE credits < 0 LIMIT 50");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    flag("NEGATIVE_CREDITS", "user:" + rs.getInt("id"), rs.getString("username") + " credits=" + rs.getInt("credits"));
                    anomalies++;
                }
            }

            // 2) Negative seasonal/duckets/diamonds balances.
            try (PreparedStatement ps = connection.prepareStatement("SELECT user_id, type, amount FROM users_currency WHERE amount < 0 LIMIT 50");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    flag("NEGATIVE_CURRENCY", "user:" + rs.getInt("user_id"), "type=" + rs.getInt("type") + " amount=" + rs.getInt("amount"));
                    anomalies++;
                }
            }

            // 3) Items owned by a non-existent user (corruption / failed transaction).
            try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM items i LEFT JOIN users u ON i.user_id = u.id WHERE i.user_id > 0 AND u.id IS NULL");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    flag("ORPHAN_ITEMS", "items", "count=" + rs.getInt(1));
                    anomalies++;
                }
            }

            // 4) Users holding an implausible number of items (possible dupe farm) - review only.
            int threshold = Emulator.getConfig().getInt("watchdog.items.threshold", 50000);
            try (PreparedStatement ps = connection.prepareStatement("SELECT user_id, COUNT(*) c FROM items WHERE user_id > 0 GROUP BY user_id HAVING c > ? ORDER BY c DESC LIMIT 20")) {
                ps.setInt(1, threshold);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        flag("EXCESSIVE_ITEMS", "user:" + rs.getInt("user_id"), "count=" + rs.getInt("c") + " (>" + threshold + ")");
                        anomalies++;
                    }
                }
            }

            if (anomalies == 0) {
                LOGGER.info("Watchdog -> scan OK (no anomalies)");
            } else {
                LOGGER.warn("Watchdog -> scansione ha trovato {} anomalie (vedi audit_log action=WATCHDOG)", anomalies);
            }
        } catch (Exception e) {
            LOGGER.error("Scansione Watchdog fallita", e);
        }
    }

    private void flag(String type, String target, String detail) {
        LOGGER.warn("[WATCHDOG] {} {} {}", type, target, detail);
        AuditLog.record(0, "watchdog", "WATCHDOG_" + type, target, detail);
    }
}
