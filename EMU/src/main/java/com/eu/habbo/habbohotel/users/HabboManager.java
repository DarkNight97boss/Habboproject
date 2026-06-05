package com.eu.habbo.habbohotel.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ModToolBan;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.permissions.Rank;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.catalog.*;
import com.eu.habbo.messages.outgoing.catalog.marketplace.MarketplaceConfigComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.GenericAlertComposer;
import com.eu.habbo.messages.outgoing.modtool.ModToolComposer;
import com.eu.habbo.messages.outgoing.users.UserPerksComposer;
import com.eu.habbo.messages.outgoing.users.UserPermissionsComposer;
import com.eu.habbo.plugin.events.users.UserRankChangedEvent;
import com.eu.habbo.plugin.events.users.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HabboManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(HabboManager.class);

    //Configuration. Loaded from database & updated accordingly.
    public static String WELCOME_MESSAGE = "";
    public static boolean NAMECHANGE_ENABLED = false;

    private final ConcurrentHashMap<Integer, Habbo> onlineHabbos;

    public HabboManager() {
        long millis = System.currentTimeMillis();

        this.onlineHabbos = new ConcurrentHashMap<>();

        LOGGER.info("Habbo Manager -> Caricato! ({} MS)", System.currentTimeMillis() - millis);
    }

    public static HabboInfo getOfflineHabboInfo(int id) {
        HabboInfo info = null;
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE id = ? LIMIT 1")) {
            statement.setInt(1, id);
            try (ResultSet set = statement.executeQuery()) {
                if (set.next()) {
                    info = new HabboInfo(set);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Eccezione SQL intercettata", e);
        }

        return info;
    }

    public static HabboInfo getOfflineHabboInfo(String username) {
        HabboInfo info = null;

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE username = ? LIMIT 1")) {
            statement.setString(1, username);

            try (ResultSet set = statement.executeQuery()) {
                if (set.next()) {
                    info = new HabboInfo(set);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Eccezione SQL intercettata", e);
        }

        return info;
    }

    public void addHabbo(Habbo habbo) {
        this.onlineHabbos.put(habbo.getHabboInfo().getId(), habbo);
    }

    public void removeHabbo(Habbo habbo) {
        this.onlineHabbos.remove(habbo.getHabboInfo().getId());
    }

    public Habbo getHabbo(int id) {
        return this.onlineHabbos.get(id);
    }

    public Habbo getHabbo(String username) {
        synchronized (this.onlineHabbos) {
            for (Map.Entry<Integer, Habbo> map : this.onlineHabbos.entrySet()) {
                if (map.getValue().getHabboInfo().getUsername().equalsIgnoreCase(username))
                    return map.getValue();
            }
        }

        return null;
    }

    /** @deprecated usare {@link #loadHabbo(String, String)} per abilitare il binding IP. */
    @Deprecated
    public Habbo loadHabbo(String sso) {
        return loadHabbo(sso, null);
    }

    /**
     * Carica un Habbo a partire dal ticket SSO emesso dal CMS-V3.
     *
     * Controlli di sicurezza applicati (in ordine):
     *   1. TTL: ticket emesso da più di `sso.ticket.ttl.seconds` (default 60s) → rifiuto.
     *   2. IP binding: se `sso.ticket.bind_ip.enabled=true` (default true) e la
     *      colonna `auth_ticket_bound_ip` è popolata, deve coincidere con
     *      `peerIp` (IP del WebSocket peer). Mismatch → rifiuto.
     *   3. Single-use atomico: UPDATE con WHERE auth_ticket=? LIMIT 1.
     *      Solo un worker concorrente vince (rowsAffected==1).
     *   4. SELECT del profilo per id (ticket già consumato).
     *
     * @param sso     valore del ticket inviato dal client al SecureLoginEvent
     * @param peerIp  IP del peer WebSocket (può essere null per backward-compat con
     *                chiamate vecchie; il binding IP è effettivo solo se non-null)
     */
    public Habbo loadHabbo(String sso, String peerIp) {
        Habbo habbo;
        int userId = 0;

        // SSO TTL + IP binding: leggiamo issued_at e bound_ip in un'unica query.
        // Se il CMS non ha ancora popolato bound_ip (deploy intermedio), il check
        // IP è skippato (bound_ip == "") per evitare lock-out durante rollout.
        boolean ipBindEnabled = Emulator.getConfig().getBoolean("sso.ticket.bind_ip.enabled", true);
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, auth_ticket_issued_at, auth_ticket_bound_ip, auth_ticket_bound_ip_alt FROM users WHERE auth_ticket = ? LIMIT 1")) {
            statement.setString(1, sso);
            try (ResultSet s = statement.executeQuery()) {
                if (s.next()) {
                    int issuedAt;
                    String boundIp;
                    try {
                        issuedAt = s.getInt("auth_ticket_issued_at");
                    } catch (SQLException missingColumn) {
                        issuedAt = 0;
                    }
                    try {
                        boundIp = s.getString("auth_ticket_bound_ip");
                        if (boundIp == null) boundIp = "";
                    } catch (SQLException missingColumn) {
                        boundIp = "";
                    }
                    String boundIpAlt;
                    try {
                        boundIpAlt = s.getString("auth_ticket_bound_ip_alt");
                        if (boundIpAlt == null) boundIpAlt = "";
                    } catch (SQLException missingColumn) {
                        boundIpAlt = "";
                    }
                    int ttl = Emulator.getConfig().getInt("sso.ticket.ttl.seconds", 60);
                    if (issuedAt > 0 && ttl > 0 && Emulator.getIntUnixTimestamp() - issuedAt > ttl) {
                        LOGGER.warn("Ticket SSO scaduto rifiutato per l'utente {} (età {}s > ttl {}s)",
                                s.getInt("id"), Emulator.getIntUnixTimestamp() - issuedAt, ttl);
                        return null;
                    }
                    // IP binding check (TOFU dual-stack):
                    // 1. Se peerIp matcha boundIp (primary) → OK
                    // 2. Else se boundIpAlt è popolata e matcha peerIp → OK (sealed)
                    // 3. Else se boundIpAlt è vuota → TOFU: prova a sigillare atomicamente
                    //    boundIpAlt = peerIp. Se UPDATE rowsAffected==1 → OK.
                    //    Razionale: il browser può usare IPv4 per /api/v2/auth/play e
                    //    IPv6 per il WebSocket (happy-eyeballs RFC 8305). Il primo
                    //    connect riempie l'alt slot atomically; successive connessioni
                    //    devono matchare uno dei due IP. Single-use ticket + TTL 300s
                    //    limitano la finestra di abuso.
                    // 4. Else (entrambi popolati e nessun match) → rifiuto.
                    if (ipBindEnabled && !boundIp.isEmpty() && peerIp != null && !peerIp.equals(boundIp)) {
                        if (!boundIpAlt.isEmpty()) {
                            if (!peerIp.equals(boundIpAlt)) {
                                LOGGER.warn("Ticket SSO IP-mismatch per l'utente {} (bound={}, alt={}, peer={}) — rifiutato",
                                        s.getInt("id"), boundIp, boundIpAlt, peerIp);
                                return null;
                            }
                            // peerIp matcha alt — OK
                        } else {
                            // TOFU: sigilla atomicamente alt = peerIp
                            try (PreparedStatement tofu = connection.prepareStatement(
                                    "UPDATE users SET auth_ticket_bound_ip_alt = ? WHERE auth_ticket = ? AND (auth_ticket_bound_ip_alt IS NULL OR auth_ticket_bound_ip_alt = '') LIMIT 1")) {
                                tofu.setString(1, peerIp);
                                tofu.setString(2, sso);
                                int sealed = tofu.executeUpdate();
                                if (sealed != 1) {
                                    LOGGER.warn("Ticket SSO TOFU race lost per l'utente {} (bound={}, peer={}) — rifiutato",
                                            s.getInt("id"), boundIp, peerIp);
                                    return null;
                                }
                                LOGGER.info("Ticket SSO TOFU dual-stack sealed per l'utente {} (bound={}, sealed_alt={})",
                                        s.getInt("id"), boundIp, peerIp);
                            }
                        }
                    }
                    userId = s.getInt("id");
                }
            }
            statement.close();
        } catch (SQLException e) {
            // Fall back to the legacy query if the new columns do not yet exist
            // (DBA hasn't run sqlupdates/sso_ticket_binding.sql). Logging at DEBUG only
            // — la migration è idempotente e raccomandata, ma il flusso resta funzionante.
            try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT id FROM users WHERE auth_ticket = ? LIMIT 1")) {
                statement.setString(1, sso);
                try (ResultSet s = statement.executeQuery()) {
                    if (s.next()) userId = s.getInt("id");
                }
            } catch (SQLException ex) {
                LOGGER.error("Eccezione SQL intercettata", ex);
            }
        }

        habbo = this.cloneCheck(userId);
        if (habbo != null) {
            habbo.alert(Emulator.getTexts().getValue("loggedin.elsewhere"));
            Emulator.getGameServer().getGameClientManager().disposeClient(habbo.getClient());
            habbo = null;
        }

        ModToolBan ban = Emulator.getGameEnvironment().getModToolManager().checkForBan(userId);
        if (ban != null) {
            return null;
        }


        // ATOMIC SINGLE-USE: blank the ticket FIRST and check rowsAffected.
        // Solo un worker concorrente può vincere questa UPDATE per un dato
        // valore di auth_ticket (vincolo InnoDB row-level lock + WHERE filter).
        // Il vecchio flusso SELECT * → UPDATE permetteva un TOCTOU: due login
        // paralleli con lo stesso ticket potevano entrambi superare la SELECT
        // prima che il blank avvenisse. Ora il primo UPDATE consuma il ticket
        // atomicamente; il secondo vede rowsAffected=0 e ritorna null.
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {
            int rowsClaimed;
            try (PreparedStatement claim = connection.prepareStatement(
                    "UPDATE users SET auth_ticket = '', auth_ticket_issued_at = 0 WHERE auth_ticket = ? LIMIT 1")) {
                claim.setString(1, sso);
                rowsClaimed = claim.executeUpdate();
            } catch (SQLException missingColumn) {
                // Fallback se la colonna auth_ticket_issued_at non è ancora stata creata.
                try (PreparedStatement claim = connection.prepareStatement(
                        "UPDATE users SET auth_ticket = '' WHERE auth_ticket = ? LIMIT 1")) {
                    claim.setString(1, sso);
                    rowsClaimed = claim.executeUpdate();
                }
            }
            if (rowsClaimed != 1) {
                // Race persa o ticket inesistente / già consumato.
                LOGGER.warn("Ticket SSO non disponibile (consumato in parallelo o inesistente)");
                return null;
            }

            // Ora carichiamo il profilo per id — il ticket è già stato azzerato
            // quindi nessun secondo worker può raggiungere questo punto.
            try (PreparedStatement select = connection.prepareStatement("SELECT * FROM users WHERE id = ? LIMIT 1")) {
                select.setInt(1, userId);
                try (ResultSet set = select.executeQuery()) {
                    if (set.next()) {
                        habbo = new Habbo(set);

                        if (habbo.getHabboInfo().firstVisit) {
                            Emulator.getPluginManager().fireEvent(new UserRegisteredEvent(habbo));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Eccezione SQL intercettata", e);
        } catch (Exception ex) {
            LOGGER.error("Eccezione intercettata", ex);
        }

        return habbo;
    }

    public HabboInfo getHabboInfo(int id) {
        if (this.getHabbo(id) == null) {
            return getOfflineHabboInfo(id);
        }
        return this.getHabbo(id).getHabboInfo();
    }

    public int getOnlineCount() {
        return this.onlineHabbos.size();
    }

    public Habbo cloneCheck(int id) {
        return Emulator.getGameServer().getGameClientManager().getHabbo(id);
    }

    public void sendPacketToHabbosWithPermission(ServerMessage message, String perm) {
        synchronized (this.onlineHabbos) {
            for (Habbo habbo : this.onlineHabbos.values()) {
                if (habbo.hasPermission(perm)) {
                    habbo.getClient().sendResponse(message);
                }
            }
        }
    }

    public ConcurrentHashMap<Integer, Habbo> getOnlineHabbos() {
        return this.onlineHabbos;
    }

    public synchronized void dispose() {


//


        LOGGER.info("Habbo Manager -> Disposed!");
    }

    public ArrayList<HabboInfo> getCloneAccounts(Habbo habbo, int limit) {
        ArrayList<HabboInfo> habboInfo = new ArrayList<>();

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE ip_register = ? OR ip_current = ? AND id != ? ORDER BY id DESC LIMIT ?")) {
            statement.setString(1, habbo.getHabboInfo().getIpRegister());
            statement.setString(2, habbo.getHabboInfo().getIpLogin());
            statement.setInt(3, habbo.getHabboInfo().getId());
            statement.setInt(4, limit);

            try (ResultSet set = statement.executeQuery()) {
                while (set.next()) {
                    habboInfo.add(new HabboInfo(set));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Eccezione SQL intercettata", e);
        }

        return habboInfo;
    }

    public List<Map.Entry<Integer, String>> getNameChanges(int userId, int limit) {
        List<Map.Entry<Integer, String>> nameChanges = new ArrayList<>();

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT timestamp, new_name FROM namechange_log WHERE user_id = ? ORDER by timestamp DESC LIMIT ?")) {
            statement.setInt(1, userId);
            statement.setInt(2, limit);
            try (ResultSet set = statement.executeQuery()) {
                while (set.next()) {
                    nameChanges.add(new AbstractMap.SimpleEntry<>(set.getInt("timestamp"), set.getString("new_name")));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Eccezione SQL intercettata", e);
        }

        return nameChanges;
    }


    public void setRank(int userId, int rankId) throws Exception {
        Habbo habbo = this.getHabbo(userId);

        if (!Emulator.getGameEnvironment().getPermissionsManager().rankExists(rankId)) {
            throw new Exception("Il Rank ID (" + rankId + ") non esiste");
        }
        Rank newRank = Emulator.getGameEnvironment().getPermissionsManager().getRank(rankId);
        if (habbo != null && habbo.getHabboStats() != null) {
            Rank oldRank = habbo.getHabboInfo().getRank();
            if (!oldRank.getBadge().isEmpty()) {
                habbo.deleteBadge(habbo.getInventory().getBadgesComponent().getBadge(oldRank.getBadge()));
            }
            if(oldRank.getRoomEffect() > 0) {
                habbo.getInventory().getEffectsComponent().effects.remove(oldRank.getRoomEffect());
            }

            habbo.getHabboInfo().setRank(newRank);

            if (!newRank.getBadge().isEmpty()) {
                habbo.addBadge(newRank.getBadge());
            }

            if(newRank.getRoomEffect() > 0) {
                habbo.getInventory().getEffectsComponent().createRankEffect(habbo.getHabboInfo().getRank().getRoomEffect());
            }

            habbo.getClient().sendResponse(new UserPermissionsComposer(habbo));
            habbo.getClient().sendResponse(new UserPerksComposer(habbo));

            if (habbo.hasPermission(Permission.ACC_SUPPORTTOOL)) {
                habbo.getClient().sendResponse(new ModToolComposer(habbo));
            }
            habbo.getHabboInfo().run();

            habbo.getClient().sendResponse(new CatalogUpdatedComposer());
            habbo.getClient().sendResponse(new CatalogModeComposer(0));
            habbo.getClient().sendResponse(new DiscountComposer());
            habbo.getClient().sendResponse(new MarketplaceConfigComposer());
            habbo.getClient().sendResponse(new GiftConfigurationComposer());
            habbo.getClient().sendResponse(new RecyclerLogicComposer());
            habbo.alert(Emulator.getTexts().getValue("commands.generic.cmd_give_rank.new_rank").replace("id", newRank.getName()));
        } else {
            try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("UPDATE users SET `rank` = ? WHERE id = ? LIMIT 1")) {
                statement.setInt(1, rankId);
                statement.setInt(2, userId);
                statement.execute();
            } catch (SQLException e) {
                LOGGER.error("Eccezione SQL intercettata", e);
            }
        }

        Emulator.getPluginManager().fireEvent(new UserRankChangedEvent(habbo));
    }

    public void giveCredits(int userId, int credits) {
        Habbo habbo = this.getHabbo(userId);
        if (habbo != null) {
            habbo.giveCredits(credits);
        } else {
            try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("UPDATE users SET credits = credits + ? WHERE id = ? LIMIT 1")) {
                statement.setInt(1, credits);
                statement.setInt(2, userId);
                statement.execute();
            } catch (SQLException e) {
                LOGGER.error("Eccezione SQL intercettata", e);
            }
        }
    }

    public void staffAlert(String message) {
        message = Emulator.getTexts().getValue("commands.generic.cmd_staffalert.title") + "\r\n" + message;
        ServerMessage msg = new GenericAlertComposer(message).compose();
        Emulator.getGameEnvironment().getHabboManager().sendPacketToHabbosWithPermission(msg, "cmd_staffalert");
    }
}
