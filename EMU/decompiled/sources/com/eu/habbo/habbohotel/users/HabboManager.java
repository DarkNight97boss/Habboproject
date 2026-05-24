package com.eu.habbo.habbohotel.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.permissions.Rank;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.catalog.CatalogModeComposer;
import com.eu.habbo.messages.outgoing.catalog.CatalogUpdatedComposer;
import com.eu.habbo.messages.outgoing.catalog.DiscountComposer;
import com.eu.habbo.messages.outgoing.catalog.GiftConfigurationComposer;
import com.eu.habbo.messages.outgoing.catalog.RecyclerLogicComposer;
import com.eu.habbo.messages.outgoing.catalog.marketplace.MarketplaceConfigComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.GenericAlertComposer;
import com.eu.habbo.messages.outgoing.modtool.ModToolComposer;
import com.eu.habbo.messages.outgoing.users.UserPerksComposer;
import com.eu.habbo.messages.outgoing.users.UserPermissionsComposer;
import com.eu.habbo.plugin.events.users.UserRankChangedEvent;
import com.eu.habbo.plugin.events.users.UserRegisteredEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/users/HabboManager.class */
public class HabboManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(HabboManager.class);
    public static String WELCOME_MESSAGE = Emulator.PREVIEW;
    public static boolean NAMECHANGE_ENABLED = false;
    private final ConcurrentHashMap<Integer, Habbo> onlineHabbos;

    public HabboManager() {
        long jCurrentTimeMillis = System.currentTimeMillis();
        this.onlineHabbos = new ConcurrentHashMap<>();
        LOGGER.info("Habbo Manager -> Loaded! (" + (System.currentTimeMillis() - jCurrentTimeMillis) + " MS)");
    }

    public static HabboInfo getOfflineHabboInfo(int i) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        ResultSet resultSetExecuteQuery;
        HabboInfo habboInfo = null;
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM users WHERE id = ? LIMIT 1");
                try {
                    preparedStatementPrepareStatement.setInt(1, i);
                    resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                } catch (Throwable th) {
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                    }
                    throw th;
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            if (resultSetExecuteQuery.next()) {
                habboInfo = new HabboInfo(resultSetExecuteQuery);
            }
            if (resultSetExecuteQuery != null) {
                resultSetExecuteQuery.close();
            }
            if (preparedStatementPrepareStatement != null) {
                preparedStatementPrepareStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
            return habboInfo;
        } catch (Throwable th3) {
            if (resultSetExecuteQuery != null) {
                try {
                    resultSetExecuteQuery.close();
                } catch (Throwable th4) {
                    th3.addSuppressed(th4);
                }
            }
            throw th3;
        }
    }

    public static HabboInfo getOfflineHabboInfo(String str) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        ResultSet resultSetExecuteQuery;
        HabboInfo habboInfo = null;
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM users WHERE username = ? LIMIT 1");
                try {
                    preparedStatementPrepareStatement.setString(1, str);
                    resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                } catch (Throwable th) {
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                    }
                    throw th;
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            if (resultSetExecuteQuery.next()) {
                habboInfo = new HabboInfo(resultSetExecuteQuery);
            }
            if (resultSetExecuteQuery != null) {
                resultSetExecuteQuery.close();
            }
            if (preparedStatementPrepareStatement != null) {
                preparedStatementPrepareStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
            return habboInfo;
        } catch (Throwable th3) {
            if (resultSetExecuteQuery != null) {
                try {
                    resultSetExecuteQuery.close();
                } catch (Throwable th4) {
                    th3.addSuppressed(th4);
                }
            }
            throw th3;
        }
    }

    public void addHabbo(Habbo habbo) {
        this.onlineHabbos.put(Integer.valueOf(habbo.getHabboInfo().getId()), habbo);
    }

    public void removeHabbo(Habbo habbo) {
        this.onlineHabbos.remove(Integer.valueOf(habbo.getHabboInfo().getId()));
    }

    public Habbo getHabbo(int i) {
        return this.onlineHabbos.get(Integer.valueOf(i));
    }

    public Habbo getHabbo(String str) {
        synchronized (this.onlineHabbos) {
            for (Map.Entry<Integer, Habbo> entry : this.onlineHabbos.entrySet()) {
                if (entry.getValue().getHabboInfo().getUsername().equalsIgnoreCase(str)) {
                    return entry.getValue();
                }
            }
            return null;
        }
    }

    public Habbo loadHabbo(String str) {
        Connection connection;
        int i = 0;
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT id FROM users WHERE auth_ticket = ? LIMIT 1");
            try {
                preparedStatementPrepareStatement.setString(1, str);
                ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                try {
                    if (resultSetExecuteQuery.next()) {
                        i = resultSetExecuteQuery.getInt("id");
                    }
                    if (resultSetExecuteQuery != null) {
                        resultSetExecuteQuery.close();
                    }
                    preparedStatementPrepareStatement.close();
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                    Habbo habboCloneCheck = cloneCheck(i);
                    if (habboCloneCheck != null) {
                        habboCloneCheck.alert(Emulator.getTexts().getValue("loggedin.elsewhere"));
                        Emulator.getGameServer().getGameClientManager().disposeClient(habboCloneCheck.getClient());
                        habboCloneCheck = null;
                    }
                    if (Emulator.getGameEnvironment().getModToolManager().checkForBan(i) != null) {
                        return null;
                    }
                    try {
                        Connection connection2 = Emulator.getDatabase().getDataSource().getConnection();
                        try {
                            preparedStatementPrepareStatement = connection2.prepareStatement("SELECT * FROM users WHERE auth_ticket = ? LIMIT 1");
                            try {
                                preparedStatementPrepareStatement.setString(1, str);
                                resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                                try {
                                    if (resultSetExecuteQuery.next()) {
                                        habboCloneCheck = new Habbo(resultSetExecuteQuery);
                                        if (habboCloneCheck.getHabboInfo().firstVisit) {
                                            Emulator.getPluginManager().fireEvent(new UserRegisteredEvent(habboCloneCheck));
                                        }
                                        if (!Emulator.debugging) {
                                            try {
                                                PreparedStatement preparedStatementPrepareStatement2 = connection2.prepareStatement("UPDATE users SET auth_ticket = ? WHERE id = ? LIMIT 1");
                                                try {
                                                    preparedStatementPrepareStatement2.setString(1, Emulator.PREVIEW);
                                                    preparedStatementPrepareStatement2.setInt(2, habboCloneCheck.getHabboInfo().getId());
                                                    preparedStatementPrepareStatement2.execute();
                                                    if (preparedStatementPrepareStatement2 != null) {
                                                        preparedStatementPrepareStatement2.close();
                                                    }
                                                } finally {
                                                }
                                            } catch (SQLException e2) {
                                                LOGGER.error("Caught SQL exception", e2);
                                            }
                                        }
                                    }
                                    if (resultSetExecuteQuery != null) {
                                        resultSetExecuteQuery.close();
                                    }
                                    if (preparedStatementPrepareStatement != null) {
                                        preparedStatementPrepareStatement.close();
                                    }
                                    if (connection2 != null) {
                                        connection2.close();
                                    }
                                } finally {
                                }
                            } finally {
                                if (preparedStatementPrepareStatement != null) {
                                    try {
                                        preparedStatementPrepareStatement.close();
                                    } catch (Throwable th) {
                                        th.addSuppressed(th);
                                    }
                                }
                            }
                        } catch (Throwable th2) {
                            if (connection2 != null) {
                                try {
                                    connection2.close();
                                } catch (Throwable th3) {
                                    th2.addSuppressed(th3);
                                }
                            }
                            throw th2;
                        }
                    } catch (SQLException e3) {
                        LOGGER.error("Caught SQL exception", e3);
                    } catch (Exception e4) {
                        LOGGER.error("Caught exception", e4);
                    }
                    return habboCloneCheck;
                } finally {
                }
            } finally {
            }
        } finally {
        }
    }

    public HabboInfo getHabboInfo(int i) {
        return getHabbo(i) == null ? getOfflineHabboInfo(i) : getHabbo(i).getHabboInfo();
    }

    public int getOnlineCount() {
        return this.onlineHabbos.size();
    }

    public Habbo cloneCheck(int i) {
        return Emulator.getGameServer().getGameClientManager().getHabbo(i);
    }

    public void sendPacketToHabbosWithPermission(ServerMessage serverMessage, String str) {
        synchronized (this.onlineHabbos) {
            for (Habbo habbo : this.onlineHabbos.values()) {
                if (habbo.hasPermission(str)) {
                    habbo.getClient().sendResponse(serverMessage);
                }
            }
        }
    }

    public ConcurrentHashMap<Integer, Habbo> getOnlineHabbos() {
        return this.onlineHabbos;
    }

    public synchronized void dispose() {
        LOGGER.info("Habbo Manager -> Disposed!");
    }

    public ArrayList<HabboInfo> getCloneAccounts(Habbo habbo, int i) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        ArrayList<HabboInfo> arrayList = new ArrayList<>();
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM users WHERE ip_register = ? OR ip_current = ? AND id != ? ORDER BY id DESC LIMIT ?");
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setString(1, habbo.getHabboInfo().getIpRegister());
            preparedStatementPrepareStatement.setString(2, habbo.getHabboInfo().getIpLogin());
            preparedStatementPrepareStatement.setInt(3, habbo.getHabboInfo().getId());
            preparedStatementPrepareStatement.setInt(4, i);
            ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
            while (resultSetExecuteQuery.next()) {
                try {
                    arrayList.add(new HabboInfo(resultSetExecuteQuery));
                } catch (Throwable th) {
                    if (resultSetExecuteQuery != null) {
                        try {
                            resultSetExecuteQuery.close();
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                    }
                    throw th;
                }
            }
            if (resultSetExecuteQuery != null) {
                resultSetExecuteQuery.close();
            }
            if (preparedStatementPrepareStatement != null) {
                preparedStatementPrepareStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
            return arrayList;
        } catch (Throwable th3) {
            if (preparedStatementPrepareStatement != null) {
                try {
                    preparedStatementPrepareStatement.close();
                } catch (Throwable th4) {
                    th3.addSuppressed(th4);
                }
            }
            throw th3;
        }
    }

    public List<Map.Entry<Integer, String>> getNameChanges(int i, int i2) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        ArrayList arrayList = new ArrayList();
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("SELECT timestamp, new_name FROM namechange_log WHERE user_id = ? ORDER by timestamp DESC LIMIT ?");
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setInt(1, i);
            preparedStatementPrepareStatement.setInt(2, i2);
            ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
            while (resultSetExecuteQuery.next()) {
                try {
                    arrayList.add(new AbstractMap.SimpleEntry(Integer.valueOf(resultSetExecuteQuery.getInt("timestamp")), resultSetExecuteQuery.getString("new_name")));
                } catch (Throwable th) {
                    if (resultSetExecuteQuery != null) {
                        try {
                            resultSetExecuteQuery.close();
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                    }
                    throw th;
                }
            }
            if (resultSetExecuteQuery != null) {
                resultSetExecuteQuery.close();
            }
            if (preparedStatementPrepareStatement != null) {
                preparedStatementPrepareStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
            return arrayList;
        } catch (Throwable th3) {
            if (preparedStatementPrepareStatement != null) {
                try {
                    preparedStatementPrepareStatement.close();
                } catch (Throwable th4) {
                    th3.addSuppressed(th4);
                }
            }
            throw th3;
        }
    }

    public void setRank(int i, int i2) throws Exception {
        Habbo habbo = getHabbo(i);
        if (!Emulator.getGameEnvironment().getPermissionsManager().rankExists(i2)) {
            throw new Exception("Rank ID (" + i2 + ") does not exist");
        }
        Rank rank = Emulator.getGameEnvironment().getPermissionsManager().getRank(i2);
        if (habbo == null || habbo.getHabboStats() == null) {
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE users SET `rank` = ? WHERE id = ? LIMIT 1");
                    try {
                        preparedStatementPrepareStatement.setInt(1, i2);
                        preparedStatementPrepareStatement.setInt(2, i);
                        preparedStatementPrepareStatement.execute();
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
                        }
                        if (connection != null) {
                            connection.close();
                        }
                    } catch (Throwable th) {
                        if (preparedStatementPrepareStatement != null) {
                            try {
                                preparedStatementPrepareStatement.close();
                            } catch (Throwable th2) {
                                th.addSuppressed(th2);
                            }
                        }
                        throw th;
                    }
                } finally {
                }
            } catch (SQLException e) {
                LOGGER.error("Caught SQL exception", e);
            }
        } else {
            Rank rank2 = habbo.getHabboInfo().getRank();
            if (!rank2.getBadge().isEmpty()) {
                habbo.deleteBadge(habbo.getInventory().getBadgesComponent().getBadge(rank2.getBadge()));
            }
            if (rank2.getRoomEffect() > 0) {
                habbo.getInventory().getEffectsComponent().effects.remove(Integer.valueOf(rank2.getRoomEffect()));
            }
            habbo.getHabboInfo().setRank(rank);
            if (!rank.getBadge().isEmpty()) {
                habbo.addBadge(rank.getBadge());
            }
            if (rank.getRoomEffect() > 0) {
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
            habbo.alert(Emulator.getTexts().getValue("commands.generic.cmd_give_rank.new_rank").replace("id", rank.getName()));
        }
        Emulator.getPluginManager().fireEvent(new UserRankChangedEvent(habbo));
    }

    public void giveCredits(int i, int i2) {
        Habbo habbo = getHabbo(i);
        if (habbo != null) {
            habbo.giveCredits(i2);
            return;
        }
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE users SET credits = credits + ? WHERE id = ? LIMIT 1");
                try {
                    preparedStatementPrepareStatement.setInt(1, i2);
                    preparedStatementPrepareStatement.setInt(2, i);
                    preparedStatementPrepareStatement.execute();
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Throwable th) {
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                    }
                    throw th;
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }

    public void staffAlert(String str) {
        Emulator.getGameEnvironment().getHabboManager().sendPacketToHabbosWithPermission(new GenericAlertComposer(Emulator.getTexts().getValue("commands.generic.cmd_staffalert.title") + "\r\n" + str).compose(), "cmd_staffalert");
    }
}
