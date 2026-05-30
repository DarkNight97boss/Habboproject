package com.eu.habbo.habbohotel.items.interactions.games.football;

import com.eu.habbo.Emulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-item runtime override of football kick mode (long shot vs 1-tile dribble).
 *
 * <p>Senza override esplicito la mode di una palla dipende dal suo {@code item_name}
 * di base (vedi {@link #defaultModeForItemName(String)}): le palle colorate
 * (red/blue/yellow) hanno il <b>tiro lungo</b> upstream Arcturus, le altre il
 * <b>dribble 1-casella</b> introdotto su richiesta utente.
 *
 * <p>L'admin puo' forzare la mode per il singolo item via il comando
 * {@code :ballmode}. L'override e' persistito nella tabella
 * {@code football_ball_modes} (chiave: item_id) e caricato all'avvio.
 */
public final class FootballBallModes {

    private static final Logger LOGGER = LoggerFactory.getLogger(FootballBallModes.class);

    public enum BallMode {
        /** Forza il "tiro lungo" upstream Arcturus (velocity 6, bounce 8-dir). */
        LONG,
        /** Forza il dribble 1-casella (velocity 1, bounce 180, canWalkOn blocca). */
        SHORT,
        /** Nessun override: usa il default per item_name. */
        DEFAULT
    }

    /**
     * Item names che, in assenza di override, usano il <b>tiro lungo</b>.
     * Scelta richiesta utente: le tre palle "colorate" (red/blue/yellow).
     * Tutte le altre (Game Ball bianca, Grand Final, Habbo Football, Beach Ball,
     * snow football) restano sul dribble 1-casella.
     */
    private static final java.util.Set<String> LONG_KICK_DEFAULTS = new java.util.HashSet<>(
            java.util.Arrays.asList("fball_ball2", "fball_ball3", "fball_ball4"));

    private static final Map<Integer, BallMode> overrides = new ConcurrentHashMap<>();
    private static volatile boolean loaded = false;

    private FootballBallModes() {
    }

    /** Default mode (no override) for a given base item name. */
    public static BallMode defaultModeForItemName(String itemName) {
        if (itemName != null && LONG_KICK_DEFAULTS.contains(itemName)) {
            return BallMode.LONG;
        }
        return BallMode.SHORT;
    }

    /** Resolved mode for a specific ball item id and base name. Override > default. */
    public static BallMode modeFor(int itemId, String itemName) {
        BallMode ov = overrides.get(itemId);
        if (ov != null && ov != BallMode.DEFAULT) {
            return ov;
        }
        return defaultModeForItemName(itemName);
    }

    /** Convenience: is this ball currently in long-kick mode? */
    public static boolean isLongKick(int itemId, String itemName) {
        return modeFor(itemId, itemName) == BallMode.LONG;
    }

    /**
     * Set an override for one item. DEFAULT means "remove override" (delete row).
     * Returns true if the call resulted in a DB write.
     */
    public static boolean setMode(int itemId, BallMode mode, String updatedBy) {
        if (mode == null) return false;
        if (mode == BallMode.DEFAULT) {
            overrides.remove(itemId);
            try (Connection c = Emulator.getDatabase().getDataSource().getConnection();
                 PreparedStatement ps = c.prepareStatement("DELETE FROM football_ball_modes WHERE item_id = ?")) {
                ps.setInt(1, itemId);
                ps.executeUpdate();
                return true;
            } catch (Exception e) {
                LOGGER.error("FootballBallModes: eliminazione dell'override per l'oggetto {} fallita", itemId, e);
                return false;
            }
        }
        overrides.put(itemId, mode);
        try (Connection c = Emulator.getDatabase().getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO football_ball_modes (item_id, mode, updated_at, updated_by) " +
                             "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE mode = VALUES(mode), " +
                             "updated_at = VALUES(updated_at), updated_by = VALUES(updated_by)")) {
            ps.setInt(1, itemId);
            ps.setString(2, mode.name());
            ps.setInt(3, Emulator.getIntUnixTimestamp());
            ps.setString(4, updatedBy != null ? updatedBy : "");
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            LOGGER.error("FootballBallModes: upsert dell'override per l'oggetto {} fallito", itemId, e);
            return false;
        }
    }

    /** One-shot load on startup (idempotent). */
    public static void loadAll() {
        if (loaded) return;
        synchronized (FootballBallModes.class) {
            if (loaded) return;
            int count = 0;
            try (Connection c = Emulator.getDatabase().getDataSource().getConnection();
                 PreparedStatement ps = c.prepareStatement("SELECT item_id, mode FROM football_ball_modes");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String m = rs.getString("mode");
                    try {
                        overrides.put(rs.getInt("item_id"), BallMode.valueOf(m));
                        count++;
                    } catch (IllegalArgumentException ignored) {
                        // unknown mode string in DB, skip
                    }
                }
            } catch (Exception e) {
                LOGGER.error("FootballBallModes: caricamento degli override fallito", e);
            }
            loaded = true;
            LOGGER.info("FootballBallModes: caricati {} override", count);
        }
    }
}
