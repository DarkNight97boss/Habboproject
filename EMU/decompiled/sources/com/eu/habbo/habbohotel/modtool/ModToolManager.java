package com.eu.habbo.habbohotel.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomState;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboManager;
import com.eu.habbo.messages.ClientMessage;
import com.eu.habbo.messages.outgoing.modtool.ModToolIssueHandledComposer;
import com.eu.habbo.messages.outgoing.modtool.ModToolIssueInfoComposer;
import com.eu.habbo.messages.outgoing.modtool.ModToolUserInfoComposer;
import com.eu.habbo.plugin.events.support.SupportRoomActionEvent;
import com.eu.habbo.plugin.events.support.SupportTicketEvent;
import com.eu.habbo.plugin.events.support.SupportTicketStatusChangedEvent;
import com.eu.habbo.plugin.events.support.SupportUserAlertedEvent;
import com.eu.habbo.plugin.events.support.SupportUserAlertedReason;
import com.eu.habbo.plugin.events.support.SupportUserBannedEvent;
import com.eu.habbo.threading.runnables.InsertModToolIssue;
import gnu.trove.TCollections;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.hash.THashSet;
import io.netty.channel.Channel;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/modtool/ModToolManager.class */
public class ModToolManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModToolManager.class);
    private final TIntObjectMap<ModToolCategory> category;
    private final THashMap<String, THashSet<String>> presets;
    private final THashMap<Integer, ModToolIssue> tickets;
    private final TIntObjectMap<CfhCategory> cfhCategories;

    public ModToolManager() {
        long jCurrentTimeMillis = System.currentTimeMillis();
        this.category = TCollections.synchronizedMap(new TIntObjectHashMap());
        this.presets = new THashMap<>();
        this.tickets = new THashMap<>();
        this.cfhCategories = new TIntObjectHashMap();
        loadModTool();
        LOGGER.info("ModTool Manager -> Loaded! (" + (System.currentTimeMillis() - jCurrentTimeMillis) + " MS)");
    }

    public static void requestUserInfo(GameClient gameClient, ClientMessage clientMessage) {
        int iIntValue = clientMessage.readInt().intValue();
        if (iIntValue <= 0) {
            return;
        }
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT users.*, users_settings.*, permissions.rank_name, permissions.acc_hide_mail AS hide_mail, permissions.id AS rank_id FROM users INNER JOIN users_settings ON users.id = users_settings.user_id INNER JOIN permissions ON permissions.id = users.rank WHERE users.id = ? LIMIT 1");
                try {
                    preparedStatementPrepareStatement.setInt(1, iIntValue);
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            gameClient.sendResponse(new ModToolUserInfoComposer(resultSetExecuteQuery));
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
            } catch (Throwable th5) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Throwable th6) {
                        th5.addSuppressed(th6);
                    }
                }
                throw th5;
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        } catch (Exception e2) {
            LOGGER.error("Caught exception", e2);
        }
    }

    public synchronized void loadModTool() {
        this.category.clear();
        this.presets.clear();
        this.cfhCategories.clear();
        this.presets.put("user", new THashSet());
        this.presets.put("room", new THashSet());
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                loadCategory(connection);
                loadPresets(connection);
                loadTickets(connection);
                loadCfhCategories(connection);
                if (connection != null) {
                    connection.close();
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }

    private void loadCategory(Connection connection) {
        try {
            Statement statementCreateStatement = connection.createStatement();
            try {
                ResultSet resultSetExecuteQuery = statementCreateStatement.executeQuery("SELECT * FROM support_issue_categories");
                while (resultSetExecuteQuery.next()) {
                    try {
                        this.category.put(resultSetExecuteQuery.getInt("id"), new ModToolCategory(resultSetExecuteQuery.getString("name")));
                        PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM support_issue_presets WHERE category = ?");
                        try {
                            preparedStatementPrepareStatement.setInt(1, resultSetExecuteQuery.getInt("id"));
                            ResultSet resultSetExecuteQuery2 = preparedStatementPrepareStatement.executeQuery();
                            while (resultSetExecuteQuery2.next()) {
                                try {
                                    ((ModToolCategory) this.category.get(resultSetExecuteQuery.getInt("id"))).addPreset(new ModToolPreset(resultSetExecuteQuery2));
                                } catch (Throwable th) {
                                    if (resultSetExecuteQuery2 != null) {
                                        try {
                                            resultSetExecuteQuery2.close();
                                        } catch (Throwable th2) {
                                            th.addSuppressed(th2);
                                        }
                                    }
                                    throw th;
                                }
                            }
                            if (resultSetExecuteQuery2 != null) {
                                resultSetExecuteQuery2.close();
                            }
                            if (preparedStatementPrepareStatement != null) {
                                preparedStatementPrepareStatement.close();
                            }
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
                    } catch (Throwable th5) {
                        if (resultSetExecuteQuery != null) {
                            try {
                                resultSetExecuteQuery.close();
                            } catch (Throwable th6) {
                                th5.addSuppressed(th6);
                            }
                        }
                        throw th5;
                    }
                }
                if (resultSetExecuteQuery != null) {
                    resultSetExecuteQuery.close();
                }
                if (statementCreateStatement != null) {
                    statementCreateStatement.close();
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }

    private void loadPresets(Connection connection) {
        Statement statementCreateStatement;
        synchronized (this.presets) {
            try {
                statementCreateStatement = connection.createStatement();
            } catch (SQLException e) {
                LOGGER.error("Caught SQL exception", e);
            }
            try {
                ResultSet resultSetExecuteQuery = statementCreateStatement.executeQuery("SELECT * FROM support_presets");
                while (resultSetExecuteQuery.next()) {
                    try {
                        ((THashSet) this.presets.get(resultSetExecuteQuery.getString("type"))).add(resultSetExecuteQuery.getString("preset"));
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
                if (statementCreateStatement != null) {
                    statementCreateStatement.close();
                }
            } catch (Throwable th3) {
                if (statementCreateStatement != null) {
                    try {
                        statementCreateStatement.close();
                    } catch (Throwable th4) {
                        th3.addSuppressed(th4);
                    }
                }
                throw th3;
            }
        }
    }

    private void loadTickets(Connection connection) {
        synchronized (this.tickets) {
            try {
                Statement statementCreateStatement = connection.createStatement();
                try {
                    ResultSet resultSetExecuteQuery = statementCreateStatement.executeQuery("SELECT S.username as sender_username, R.username AS reported_username, M.username as mod_username, support_tickets.* FROM support_tickets INNER JOIN users as S ON S.id = sender_id INNER JOIN users AS R ON R.id = reported_id INNER JOIN users AS M ON M.id = mod_id WHERE state != 0");
                    while (resultSetExecuteQuery.next()) {
                        try {
                            this.tickets.put(Integer.valueOf(resultSetExecuteQuery.getInt("id")), new ModToolIssue(resultSetExecuteQuery));
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
                    if (statementCreateStatement != null) {
                        statementCreateStatement.close();
                    }
                } catch (Throwable th3) {
                    if (statementCreateStatement != null) {
                        try {
                            statementCreateStatement.close();
                        } catch (Throwable th4) {
                            th3.addSuppressed(th4);
                        }
                    }
                    throw th3;
                }
            } catch (SQLException e) {
                LOGGER.error("Caught SQL exception", e);
            }
        }
    }

    private void loadCfhCategories(Connection connection) {
        try {
            Statement statementCreateStatement = connection.createStatement();
            try {
                ResultSet resultSetExecuteQuery = statementCreateStatement.executeQuery("SELECT support_cfh_topics.id, support_cfh_topics.category_id, support_cfh_topics.name_internal, support_cfh_topics.action, support_cfh_topics.auto_reply,support_cfh_topics.ignore_target, support_cfh_categories.name_internal AS category_name_internal, support_cfh_categories.id AS support_cfh_category_id, support_cfh_topics.default_sanction AS default_sanction FROM support_cfh_topics LEFT JOIN support_cfh_categories ON support_cfh_categories.id = support_cfh_topics.category_id");
                while (resultSetExecuteQuery.next()) {
                    try {
                        if (!this.cfhCategories.containsKey(resultSetExecuteQuery.getInt("support_cfh_category_id"))) {
                            this.cfhCategories.put(resultSetExecuteQuery.getInt("support_cfh_category_id"), new CfhCategory(resultSetExecuteQuery.getInt("id"), resultSetExecuteQuery.getString("category_name_internal")));
                        }
                        ((CfhCategory) this.cfhCategories.get(resultSetExecuteQuery.getInt("support_cfh_category_id"))).addTopic(new CfhTopic(resultSetExecuteQuery, getIssuePreset(resultSetExecuteQuery.getInt("default_sanction"))));
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
                if (statementCreateStatement != null) {
                    statementCreateStatement.close();
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }

    public CfhTopic getCfhTopic(int i) {
        Iterator it = getCfhCategories().valueCollection().iterator();
        while (it.hasNext()) {
            for (CfhTopic cfhTopic : ((CfhCategory) it.next()).getTopics().valueCollection()) {
                if (cfhTopic.id == i) {
                    return cfhTopic;
                }
            }
        }
        return null;
    }

    public ModToolPreset getIssuePreset(final int i) {
        if (i == 0) {
            return null;
        }
        final ModToolPreset[] modToolPresetArr = {null};
        this.category.forEachValue(new TObjectProcedure<ModToolCategory>() { // from class: com.eu.habbo.habbohotel.modtool.ModToolManager.1
            public boolean execute(ModToolCategory modToolCategory) {
                modToolPresetArr[0] = (ModToolPreset) modToolCategory.getPresets().get(i);
                return modToolPresetArr[0] == null;
            }
        });
        return modToolPresetArr[0];
    }

    public void quickTicket(Habbo habbo, String str, String str2) {
        ModToolIssue modToolIssue = new ModToolIssue(0, str, habbo.getHabboInfo().getId(), habbo.getHabboInfo().getUsername(), 0, str2, ModToolTicketType.AUTOMATIC);
        Emulator.getGameEnvironment().getModToolManager().addTicket(modToolIssue);
        Emulator.getGameEnvironment().getModToolManager().updateTicketToMods(modToolIssue);
    }

    public ArrayList<ModToolChatLog> getRoomChatlog(int i) {
        ArrayList<ModToolChatLog> arrayList = new ArrayList<>();
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT users.username, users.id, chatlogs_room.* FROM chatlogs_room INNER JOIN users ON users.id = chatlogs_room.user_from_id WHERE room_id = ? ORDER BY timestamp DESC LIMIT 150");
                try {
                    preparedStatementPrepareStatement.setInt(1, i);
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            arrayList.add(new ModToolChatLog(resultSetExecuteQuery.getInt("timestamp"), resultSetExecuteQuery.getInt("id"), resultSetExecuteQuery.getString("username"), resultSetExecuteQuery.getString("message")));
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
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        return arrayList;
    }

    public ArrayList<ModToolChatLog> getUserChatlog(int i) {
        ArrayList<ModToolChatLog> arrayList = new ArrayList<>();
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT users.username, users.id, chatlogs_room.* FROM chatlogs_room INNER JOIN users ON users.id = chatlogs_room.user_from_id WHERE user_from_id = ? ORDER BY chatlogs_room.timestamp DESC LIMIT 150");
                try {
                    preparedStatementPrepareStatement.setInt(1, i);
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            arrayList.add(new ModToolChatLog(resultSetExecuteQuery.getInt("timestamp"), resultSetExecuteQuery.getInt("id"), resultSetExecuteQuery.getString("username"), resultSetExecuteQuery.getString("message")));
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
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        return arrayList;
    }

    public ArrayList<ModToolChatLog> getMessengerChatlog(int i, int i2) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        ArrayList<ModToolChatLog> arrayList = new ArrayList<>();
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("SELECT users.username, chatlogs_private.* FROM chatlogs_private INNER JOIN users ON users.id = user_from_id WHERE (user_from_id = ? AND user_to_id = ?) OR (user_from_id = ? AND user_to_id = ?) ORDER BY chatlogs_private.timestamp DESC LIMIT 50");
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setInt(1, i);
            preparedStatementPrepareStatement.setInt(2, i2);
            preparedStatementPrepareStatement.setInt(3, i2);
            preparedStatementPrepareStatement.setInt(4, i);
            ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
            while (resultSetExecuteQuery.next()) {
                try {
                    arrayList.add(new ModToolChatLog(resultSetExecuteQuery.getInt("timestamp"), resultSetExecuteQuery.getInt("chatlogs_private.user_from_id"), resultSetExecuteQuery.getString("users.username"), resultSetExecuteQuery.getString("message")));
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

    public ArrayList<ModToolRoomVisit> getUserRoomVisitsAndChatlogs(int i) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        ArrayList<ModToolRoomVisit> arrayList = new ArrayList<>();
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("SELECT rooms.name, users.username, room_enter_log.timestamp AS enter_timestamp, room_enter_log.exit_timestamp, chatlogs_room.* FROM room_enter_log INNER JOIN rooms ON room_enter_log.room_id = rooms.id INNER JOIN users ON room_enter_log.user_id = users.id LEFT JOIN chatlogs_room ON room_enter_log.user_id = chatlogs_room.user_from_id AND room_enter_log.room_id = chatlogs_room.room_id AND chatlogs_room.timestamp >= room_enter_log.timestamp AND chatlogs_room.timestamp < room_enter_log.exit_timestamp WHERE chatlogs_room.user_from_id = ? ORDER BY room_enter_log.timestamp DESC LIMIT 500");
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setInt(1, i);
            ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
            int i2 = 0;
            String string = "unknown";
            while (resultSetExecuteQuery.next()) {
                try {
                    ModToolRoomVisit modToolRoomVisit = null;
                    for (ModToolRoomVisit modToolRoomVisit2 : arrayList) {
                        if (modToolRoomVisit2.timestamp == resultSetExecuteQuery.getInt("enter_timestamp") && modToolRoomVisit2.exitTimestamp == resultSetExecuteQuery.getInt("exit_timestamp")) {
                            modToolRoomVisit = modToolRoomVisit2;
                        }
                    }
                    if (modToolRoomVisit == null) {
                        modToolRoomVisit = new ModToolRoomVisit(resultSetExecuteQuery.getInt("room_id"), resultSetExecuteQuery.getString("name"), resultSetExecuteQuery.getInt("enter_timestamp"), resultSetExecuteQuery.getInt("exit_timestamp"));
                        arrayList.add(modToolRoomVisit);
                    }
                    modToolRoomVisit.chat.add(new ModToolChatLog(resultSetExecuteQuery.getInt("timestamp"), resultSetExecuteQuery.getInt("user_from_id"), resultSetExecuteQuery.getString("username"), resultSetExecuteQuery.getString("message")));
                    if (i2 == 0) {
                        i2 = resultSetExecuteQuery.getInt("user_from_id");
                    }
                    if (string.equals("unknown")) {
                        string = resultSetExecuteQuery.getString("username");
                    }
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

    public THashSet<ModToolRoomVisit> getUserRoomVisits(int i) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        THashSet<ModToolRoomVisit> tHashSet = new THashSet<>();
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("SELECT rooms.name, room_enter_log.* FROM room_enter_log INNER JOIN rooms ON rooms.id = room_enter_log.room_id WHERE user_id = ? ORDER BY timestamp DESC LIMIT 50");
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setInt(1, i);
            ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
            while (resultSetExecuteQuery.next()) {
                try {
                    tHashSet.add(new ModToolRoomVisit(resultSetExecuteQuery));
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
            return tHashSet;
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

    public THashSet<ModToolRoomVisit> getVisitsForRoom(Room room, int i, boolean z, int i2, int i3) {
        return getVisitsForRoom(room, i, z, i2, i3, Emulator.PREVIEW);
    }

    public THashSet<ModToolRoomVisit> getVisitsForRoom(Room room, int i, boolean z, int i2, int i3, String str) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        THashSet<ModToolRoomVisit> tHashSet = new THashSet<>();
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM (SELECT users.username as name, room_enter_log.* FROM`room_enter_log` INNER JOIN users ON users.id = room_enter_log.user_id WHERE room_enter_log.room_id = ? " + (i2 > 0 ? "AND timestamp >= ? " : Emulator.PREVIEW) + (i3 > 0 ? "AND exit_timestamp <= ? " : Emulator.PREVIEW) + "AND users.username != ? ORDER BY timestamp DESC LIMIT ?) x " + (z ? "GROUP BY user_id" : Emulator.PREVIEW) + ";");
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setInt(1, room.getId());
            if (i2 > 0) {
                preparedStatementPrepareStatement.setInt(2, i2);
            }
            if (i3 > 0) {
                preparedStatementPrepareStatement.setInt(i2 > 0 ? 3 : 2, i3);
            }
            preparedStatementPrepareStatement.setString(i3 > 0 ? i2 > 0 ? 4 : 3 : 2, str);
            int i4 = 3;
            if (i2 > 0) {
                i4 = 3 + 1;
            }
            if (i3 > 0) {
                i4++;
            }
            preparedStatementPrepareStatement.setInt(i4, i);
            ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
            while (resultSetExecuteQuery.next()) {
                try {
                    tHashSet.add(new ModToolRoomVisit(resultSetExecuteQuery));
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
            return tHashSet;
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

    public ModToolBan createOfflineUserBan(int i, int i2, int i3, String str, ModToolBanType modToolBanType) {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO bans (user_id, ip, machine_id, user_staff_id, ban_expire, ban_reason, type) VALUES (?, (SELECT ip_current FROM users WHERE id = ?), (SELECT machine_id FROM users WHERE id = ?), ?, ?, ?, ?)", 1);
                try {
                    preparedStatementPrepareStatement.setInt(1, i);
                    preparedStatementPrepareStatement.setInt(2, i);
                    preparedStatementPrepareStatement.setInt(3, i);
                    preparedStatementPrepareStatement.setInt(4, i2);
                    preparedStatementPrepareStatement.setInt(5, Emulator.getIntUnixTimestamp() + i3);
                    preparedStatementPrepareStatement.setString(6, str);
                    preparedStatementPrepareStatement.setString(7, modToolBanType.getType());
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    try {
                        if (resultSetExecuteQuery.next()) {
                            PreparedStatement preparedStatementPrepareStatement2 = connection.prepareStatement("SELECT * FROM bans WHERE id = ? LIMIT 1");
                            try {
                                preparedStatementPrepareStatement2.setInt(1, resultSetExecuteQuery.getInt(1));
                                ResultSet resultSetExecuteQuery2 = preparedStatementPrepareStatement2.executeQuery();
                                try {
                                    if (resultSetExecuteQuery2.next()) {
                                        ModToolBan modToolBan = new ModToolBan(resultSetExecuteQuery2);
                                        if (resultSetExecuteQuery2 != null) {
                                            resultSetExecuteQuery2.close();
                                        }
                                        if (preparedStatementPrepareStatement2 != null) {
                                            preparedStatementPrepareStatement2.close();
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
                                        return modToolBan;
                                    }
                                    if (resultSetExecuteQuery2 != null) {
                                        resultSetExecuteQuery2.close();
                                    }
                                    if (preparedStatementPrepareStatement2 != null) {
                                        preparedStatementPrepareStatement2.close();
                                    }
                                } catch (Throwable th) {
                                    if (resultSetExecuteQuery2 != null) {
                                        try {
                                            resultSetExecuteQuery2.close();
                                        } catch (Throwable th2) {
                                            th.addSuppressed(th2);
                                        }
                                    }
                                    throw th;
                                }
                            } catch (Throwable th3) {
                                if (preparedStatementPrepareStatement2 != null) {
                                    try {
                                        preparedStatementPrepareStatement2.close();
                                    } catch (Throwable th4) {
                                        th3.addSuppressed(th4);
                                    }
                                }
                                throw th3;
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
                        return null;
                    } catch (Throwable th5) {
                        if (resultSetExecuteQuery != null) {
                            try {
                                resultSetExecuteQuery.close();
                            } catch (Throwable th6) {
                                th5.addSuppressed(th6);
                            }
                        }
                        throw th5;
                    }
                } catch (Throwable th7) {
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th8) {
                            th7.addSuppressed(th8);
                        }
                    }
                    throw th7;
                }
            } catch (Throwable th9) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Throwable th10) {
                        th9.addSuppressed(th10);
                    }
                }
                throw th9;
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
            return null;
        }
    }

    public void alert(Habbo habbo, Habbo habbo2, String str) {
        alert(habbo, habbo2, str, SupportUserAlertedReason.ALERT);
    }

    public void alert(Habbo habbo, Habbo habbo2, String str, SupportUserAlertedReason supportUserAlertedReason) {
        if (!habbo.hasPermission(Permission.ACC_SUPPORTTOOL)) {
            ScripterManager.scripterDetected(habbo.getClient(), Emulator.getTexts().getValue("scripter.warning.modtools.alert").replace("%username%", habbo.getHabboInfo().getUsername()).replace("%message%", str));
            return;
        }
        SupportUserAlertedEvent supportUserAlertedEvent = new SupportUserAlertedEvent(habbo, habbo2, str, supportUserAlertedReason);
        if (((SupportUserAlertedEvent) Emulator.getPluginManager().fireEvent(supportUserAlertedEvent)).isCancelled() || habbo2 == null) {
            return;
        }
        supportUserAlertedEvent.target.getClient().sendResponse(new ModToolIssueHandledComposer(supportUserAlertedEvent.message));
    }

    public void kick(Habbo habbo, Habbo habbo2, String str) {
        if (!habbo.hasPermission(Permission.ACC_SUPPORTTOOL) || habbo2.hasPermission(Permission.ACC_UNKICKABLE)) {
            return;
        }
        if (habbo2.getHabboInfo().getCurrentRoom() != null) {
            Emulator.getGameEnvironment().getRoomManager().leaveRoom(habbo2, habbo2.getHabboInfo().getCurrentRoom());
        }
        alert(habbo, habbo2, str, SupportUserAlertedReason.KICKED);
    }

    public List<ModToolBan> ban(int i, Habbo habbo, String str, int i2, ModToolBanType modToolBanType, int i3) {
        if (habbo == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        Habbo habbo2 = Emulator.getGameEnvironment().getHabboManager().getHabbo(i);
        HabboInfo habboInfo = habbo2 != null ? habbo2.getHabboInfo() : HabboManager.getOfflineHabboInfo(i);
        if (habbo.getHabboInfo().getRank().getId() < habboInfo.getRank().getId()) {
            return arrayList;
        }
        if ((modToolBanType == ModToolBanType.MACHINE || modToolBanType == ModToolBanType.SUPER) && (habboInfo == null || habboInfo.getMachineID().isEmpty())) {
            modToolBanType = ModToolBanType.IP;
        }
        if ((modToolBanType == ModToolBanType.IP || modToolBanType == ModToolBanType.SUPER) && (habboInfo == null || habboInfo.getIpLogin().isEmpty())) {
            modToolBanType = ModToolBanType.ACCOUNT;
        }
        ModToolBan modToolBan = new ModToolBan(i, habboInfo != null ? habboInfo.getIpLogin() : "offline", habboInfo != null ? habboInfo.getMachineID() : "offline", habbo.getHabboInfo().getId(), Emulator.getIntUnixTimestamp() + i2, str, modToolBanType, i3);
        Emulator.getPluginManager().fireEvent(new SupportUserBannedEvent(habbo, habbo2, modToolBan));
        Emulator.getThreading().run(modToolBan);
        arrayList.add(modToolBan);
        if (habbo2 != null) {
            Emulator.getGameServer().getGameClientManager().disposeClient(habbo2.getClient());
        }
        if ((modToolBanType == ModToolBanType.IP || modToolBanType == ModToolBanType.SUPER) && habbo2 != null && !modToolBan.ip.equals("offline")) {
            Iterator<Habbo> it = Emulator.getGameServer().getGameClientManager().getHabbosWithIP(modToolBan.ip).iterator();
            while (it.hasNext()) {
                Habbo next = it.next();
                if (next.getHabboInfo().getRank().getId() < habbo.getHabboInfo().getRank().getId()) {
                    modToolBan = new ModToolBan(next.getHabboInfo().getId(), next != null ? next.getHabboInfo().getIpLogin() : "offline", next != null ? next.getClient().getMachineId() : "offline", habbo.getHabboInfo().getId(), Emulator.getIntUnixTimestamp() + i2, str, modToolBanType, i3);
                    Emulator.getPluginManager().fireEvent(new SupportUserBannedEvent(habbo, next, modToolBan));
                    Emulator.getThreading().run(modToolBan);
                    arrayList.add(modToolBan);
                    Emulator.getGameServer().getGameClientManager().disposeClient(next.getClient());
                }
            }
        }
        if ((modToolBanType == ModToolBanType.MACHINE || modToolBanType == ModToolBanType.SUPER) && habbo2 != null && !modToolBan.machineId.equals("offline")) {
            Iterator<Habbo> it2 = Emulator.getGameServer().getGameClientManager().getHabbosWithMachineId(modToolBan.machineId).iterator();
            while (it2.hasNext()) {
                Habbo next2 = it2.next();
                if (next2.getHabboInfo().getRank().getId() < habbo.getHabboInfo().getRank().getId()) {
                    ModToolBan modToolBan2 = new ModToolBan(next2.getHabboInfo().getId(), next2 != null ? next2.getHabboInfo().getIpLogin() : "offline", next2 != null ? next2.getClient().getMachineId() : "offline", habbo.getHabboInfo().getId(), Emulator.getIntUnixTimestamp() + i2, str, modToolBanType, i3);
                    Emulator.getPluginManager().fireEvent(new SupportUserBannedEvent(habbo, next2, modToolBan2));
                    Emulator.getThreading().run(modToolBan2);
                    arrayList.add(modToolBan2);
                    Emulator.getGameServer().getGameClientManager().disposeClient(next2.getClient());
                }
            }
        }
        return arrayList;
    }

    public void roomAction(Room room, Habbo habbo, boolean z, boolean z2, boolean z3) {
        SupportRoomActionEvent supportRoomActionEvent = new SupportRoomActionEvent(habbo, room, z, z2, z3);
        Emulator.getPluginManager().fireEvent(supportRoomActionEvent);
        if (supportRoomActionEvent.changeTitle) {
            room.setName(Emulator.getTexts().getValue("hotel.room.inappropriate.title"));
            room.setNeedsUpdate(true);
        }
        if (supportRoomActionEvent.lockDoor) {
            room.setState(RoomState.LOCKED);
            room.setNeedsUpdate(true);
        }
        if (supportRoomActionEvent.kickUsers) {
            for (Habbo habbo2 : room.getHabbos()) {
                if (!habbo2.hasPermission(Permission.ACC_UNKICKABLE) && !habbo2.hasPermission(Permission.ACC_SUPPORTTOOL) && !room.isOwner(habbo2)) {
                    room.kickHabbo(habbo2, false);
                }
            }
        }
    }

    public ModToolBan checkForBan(int i) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        ModToolBan modToolBan = null;
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM bans WHERE user_id = ? AND ban_expire >= ? AND (type = 'account' OR type = 'super') ORDER BY timestamp LIMIT 1");
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setInt(1, i);
            preparedStatementPrepareStatement.setInt(2, Emulator.getIntUnixTimestamp());
            ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
            try {
                if (resultSetExecuteQuery.next()) {
                    modToolBan = new ModToolBan(resultSetExecuteQuery);
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
                return modToolBan;
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

    @Deprecated
    public boolean hasIPBan(Channel channel) {
        if (channel == null || channel.remoteAddress() == null || ((InetSocketAddress) channel.remoteAddress()).getAddress() == null) {
            return false;
        }
        return hasIPBan(((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress());
    }

    public boolean hasIPBan(String str) {
        boolean z = false;
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM bans WHERE ip = ? AND (type = 'ip' OR type = 'super')  AND ban_expire > ? LIMIT 1");
                try {
                    preparedStatementPrepareStatement.setString(1, str);
                    preparedStatementPrepareStatement.setInt(2, Emulator.getIntUnixTimestamp());
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    try {
                        if (resultSetExecuteQuery.next()) {
                            z = true;
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
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        return z;
    }

    public boolean hasMACBan(GameClient gameClient) {
        if (gameClient == null || gameClient.getMachineId().isEmpty()) {
            return false;
        }
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM bans WHERE machine_id = ? AND (type = 'machine' OR type = 'super') AND ban_expire > ? LIMIT 1");
                try {
                    preparedStatementPrepareStatement.setString(1, gameClient.getMachineId());
                    preparedStatementPrepareStatement.setInt(2, Emulator.getIntUnixTimestamp());
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    try {
                        if (resultSetExecuteQuery.next()) {
                            if (resultSetExecuteQuery != null) {
                                resultSetExecuteQuery.close();
                            }
                            if (preparedStatementPrepareStatement != null) {
                                preparedStatementPrepareStatement.close();
                            }
                            if (connection != null) {
                                connection.close();
                            }
                            return true;
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
                        return false;
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
            } catch (Throwable th5) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Throwable th6) {
                        th5.addSuppressed(th6);
                    }
                }
                throw th5;
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
            return false;
        }
    }

    public boolean unban(String str) {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE bans INNER JOIN users ON bans.user_id = users.id SET ban_expire = ?, ban_reason = CONCAT('" + Emulator.getTexts().getValue("unbanned") + ": ', ban_reason) WHERE users.username LIKE ? AND ban_expire > ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, Emulator.getIntUnixTimestamp());
                    preparedStatementPrepareStatement.setString(2, str);
                    preparedStatementPrepareStatement.setInt(3, Emulator.getIntUnixTimestamp());
                    preparedStatementPrepareStatement.execute();
                    boolean z = preparedStatementPrepareStatement.getUpdateCount() > 0;
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                    return z;
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
            } catch (Throwable th3) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Throwable th4) {
                        th3.addSuppressed(th4);
                    }
                }
                throw th3;
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
            return false;
        }
    }

    public void pickTicket(ModToolIssue modToolIssue, Habbo habbo) {
        modToolIssue.modId = habbo.getHabboInfo().getId();
        modToolIssue.modName = habbo.getHabboInfo().getUsername();
        modToolIssue.state = ModToolTicketState.PICKED;
        updateTicketToMods(modToolIssue);
        modToolIssue.updateInDatabase();
    }

    public void updateTicketToMods(ModToolIssue modToolIssue) {
        if (((SupportTicketStatusChangedEvent) Emulator.getPluginManager().fireEvent(new SupportTicketStatusChangedEvent(null, modToolIssue))).isCancelled()) {
            return;
        }
        Emulator.getGameEnvironment().getHabboManager().sendPacketToHabbosWithPermission(new ModToolIssueInfoComposer(modToolIssue).compose(), Permission.ACC_SUPPORTTOOL);
    }

    public void addTicket(ModToolIssue modToolIssue) {
        if (((SupportTicketEvent) Emulator.getPluginManager().fireEvent(new SupportTicketEvent(null, modToolIssue))).isCancelled()) {
            return;
        }
        if (modToolIssue.id == 0) {
            new InsertModToolIssue(modToolIssue).run();
        }
        synchronized (this.tickets) {
            this.tickets.put(Integer.valueOf(modToolIssue.id), modToolIssue);
        }
    }

    public void removeTicket(ModToolIssue modToolIssue) {
        removeTicket(modToolIssue.id);
    }

    public void removeTicket(int i) {
        synchronized (this.tickets) {
            this.tickets.remove(Integer.valueOf(i));
        }
    }

    public void closeTicketAsUseless(ModToolIssue modToolIssue, Habbo habbo) {
        modToolIssue.state = ModToolTicketState.CLOSED;
        modToolIssue.updateInDatabase();
        if (habbo != null) {
            habbo.getClient().sendResponse(new ModToolIssueHandledComposer(1));
        }
        updateTicketToMods(modToolIssue);
        removeTicket(modToolIssue);
    }

    public void closeTicketAsAbusive(ModToolIssue modToolIssue, Habbo habbo) {
        modToolIssue.state = ModToolTicketState.CLOSED;
        modToolIssue.updateInDatabase();
        if (habbo != null) {
            habbo.getClient().sendResponse(new ModToolIssueHandledComposer(2));
        }
        updateTicketToMods(modToolIssue);
        removeTicket(modToolIssue);
    }

    public void closeTicketAsHandled(ModToolIssue modToolIssue, Habbo habbo) {
        modToolIssue.state = ModToolTicketState.CLOSED;
        modToolIssue.updateInDatabase();
        if (habbo != null) {
            habbo.getClient().sendResponse(new ModToolIssueHandledComposer(0));
        }
        updateTicketToMods(modToolIssue);
        removeTicket(modToolIssue);
    }

    public boolean hasPendingTickets(int i) {
        synchronized (this.tickets) {
            Iterator it = this.tickets.entrySet().iterator();
            while (it.hasNext()) {
                if (((ModToolIssue) ((Map.Entry) it.next()).getValue()).senderId == i) {
                    return true;
                }
            }
            return false;
        }
    }

    public int totalBans(int i) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        int i2 = 0;
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("SELECT COUNT(*) as total FROM bans WHERE user_id = ?");
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setInt(1, i);
            ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
            try {
                if (resultSetExecuteQuery.next()) {
                    i2 = resultSetExecuteQuery.getInt("total");
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
                return i2;
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

    public TIntObjectMap<ModToolCategory> getCategory() {
        return this.category;
    }

    public ModToolCategory getCategory(int i) {
        return (ModToolCategory) this.category.get(i);
    }

    public THashMap<String, THashSet<String>> getPresets() {
        return this.presets;
    }

    public THashMap<Integer, ModToolIssue> getTickets() {
        return this.tickets;
    }

    public ModToolIssue getTicket(int i) {
        return (ModToolIssue) this.tickets.get(Integer.valueOf(i));
    }

    public TIntObjectMap<CfhCategory> getCfhCategories() {
        return this.cfhCategories;
    }

    public List<ModToolIssue> openTicketsForHabbo(final Habbo habbo) {
        final ArrayList arrayList = new ArrayList();
        synchronized (this.tickets) {
            this.tickets.forEachValue(new TObjectProcedure<ModToolIssue>() { // from class: com.eu.habbo.habbohotel.modtool.ModToolManager.2
                public boolean execute(ModToolIssue modToolIssue) {
                    if (modToolIssue.senderId != habbo.getHabboInfo().getId() || modToolIssue.state != ModToolTicketState.OPEN) {
                        return true;
                    }
                    arrayList.add(modToolIssue);
                    return true;
                }
            });
        }
        return arrayList;
    }
}
