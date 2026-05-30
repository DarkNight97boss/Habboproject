package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Periodic DB cleanup of orphan rows produced by deleted accounts.
 *
 * Each tick (default once/day) it runs a small set of targeted DELETEs guarded
 * by a deletion cap (so a misconfiguration / mass deletion can't nuke the DB).
 * Every delete count is recorded in {@link AuditLog} (action=DB_CLEANUP_*).
 *
 * Config (emulator_settings):
 *   db.cleanup.enabled                 (default 1)
 *   db.cleanup.interval.hours          (default 24)
 *   db.cleanup.initial.delay.seconds   (default 300)
 *   db.cleanup.max_rows_per_table      (default 5000)
 *
 * Tables cleaned (only rows whose user_id no longer exists in `users`):
 *   - items
 *   - room_rights
 *   - users_settings
 *   - users_subscriptions
 *   - users_currency
 *
 * Safe defaults: items get user_id=0 (= leaves the item in the world rather
 * than deleting hard) so we never silently destroy furniture; the other tables
 * are pure per-user metadata and are deleted hard.
 */
public class DbCleanup {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbCleanup.class);

    private ScheduledExecutorService scheduler;

    public DbCleanup() {
        if (!isEnabled()) {
            LOGGER.info("DbCleanup -> disabilitato");
            return;
        }
        int initialDelay = Emulator.getConfig().getInt("db.cleanup.initial.delay.seconds", 300);
        int intervalHours = Math.max(1, Emulator.getConfig().getInt("db.cleanup.interval.hours", 24));
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "DbCleanup");
            t.setDaemon(true);
            return t;
        });
        this.scheduler.scheduleAtFixedRate(this::tick, initialDelay, intervalHours * 3600L, TimeUnit.SECONDS);
        LOGGER.info("DbCleanup -> Caricato! (sweep ogni {}h)", intervalHours);
    }

    public static boolean isEnabled() {
        return Emulator.getConfig().getBoolean("db.cleanup.enabled", true);
    }

    private void tick() {
        int cap = Math.max(1, Emulator.getConfig().getInt("db.cleanup.max_rows_per_table", 5000));
        long totalChanges = 0;
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {
            // 1) Items belonging to a user that no longer exists: keep the furni
            //    in the world by setting user_id=0 (= "hotel-owned"), so admins
            //    can decide what to do with it. Capped.
            totalChanges += sweep(connection, "ITEMS_DETACH",
                    "UPDATE items SET user_id = 0 WHERE id IN (" +
                            "SELECT id FROM (SELECT i.id FROM items i LEFT JOIN users u ON i.user_id = u.id " +
                            "WHERE i.user_id > 0 AND u.id IS NULL LIMIT ?) AS x)",
                    cap);

            // 2) Hard-delete per-user metadata for missing users.
            String[][] hardDeletes = new String[][] {
                    {"ROOM_RIGHTS", "room_rights"},
                    {"USERS_SETTINGS", "users_settings"},
                    {"USERS_SUBSCRIPTIONS", "users_subscriptions"},
                    {"USERS_CURRENCY", "users_currency"}
            };
            for (String[] td : hardDeletes) {
                String label = td[0];
                String table = td[1];
                totalChanges += sweep(connection, label,
                        "DELETE FROM " + table + " WHERE user_id IN (" +
                                "SELECT id FROM (SELECT t.user_id AS id FROM " + table + " t LEFT JOIN users u ON t.user_id = u.id " +
                                "WHERE u.id IS NULL LIMIT ?) AS x)",
                        cap);
            }
        } catch (Exception e) {
            LOGGER.error("Tick DbCleanup fallito", e);
            AuditLog.record(0, "DbCleanup", "DB_CLEANUP_FAIL", "", e.getMessage() == null ? "" : e.getMessage());
            return;
        }
        if (totalChanges > 0) {
            LOGGER.info("DbCleanup -> {} orphan rows reconciled", totalChanges);
        } else {
            LOGGER.info("DbCleanup -> niente da pulire");
        }
    }

    private long sweep(Connection connection, String label, String sql, int cap) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, cap);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                AuditLog.record(0, "DbCleanup", "DB_CLEANUP_" + label, "", "deleted=" + affected);
            }
            return affected;
        } catch (Exception e) {
            // Table may not exist in this build (e.g. users_subscriptions on older DBs)
            // — log and continue with the next table.
            String msg = e.getMessage() == null ? "" : e.getMessage();
            if (msg.contains("doesn't exist") || msg.contains("Unknown table")) {
                return 0;
            }
            LOGGER.warn("Sweep DbCleanup '{}' fallito: {}", label, msg);
            return 0;
        }
    }
}
