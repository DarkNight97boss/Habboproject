package com.eu.habbo.habbohotel.bots;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.RoomChatMessage;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.plugin.events.bots.BotServerItemEvent;
import com.eu.habbo.threading.runnables.RoomUnitGiveHanditem;
import com.eu.habbo.threading.runnables.RoomUnitWalkToRoomUnit;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/bots/ButlerBot.class */
public class ButlerBot extends Bot {
    private static final Logger LOGGER = LoggerFactory.getLogger(ButlerBot.class);
    public static THashMap<THashSet<String>, Integer> serveItems = new THashMap<>();

    public ButlerBot(ResultSet resultSet) throws SQLException {
        super(resultSet);
    }

    public ButlerBot(Bot bot) {
        super(bot);
    }

    public static void initialise() {
        if (serveItems == null) {
            serveItems = new THashMap<>();
        }
        serveItems.clear();
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                Statement statementCreateStatement = connection.createStatement();
                try {
                    ResultSet resultSetExecuteQuery = statementCreateStatement.executeQuery("SELECT * FROM bot_serves");
                    while (resultSetExecuteQuery.next()) {
                        try {
                            String[] strArrSplit = resultSetExecuteQuery.getString("keys").split(";");
                            THashSet tHashSet = new THashSet();
                            Collections.addAll(tHashSet, strArrSplit);
                            serveItems.put(tHashSet, Integer.valueOf(resultSetExecuteQuery.getInt("item")));
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
                    if (connection != null) {
                        connection.close();
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
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }

    public static void dispose() {
        serveItems.clear();
    }

    @Override // com.eu.habbo.habbohotel.bots.Bot
    public void onUserSay(RoomChatMessage roomChatMessage) {
        if (getRoomUnit().hasStatus(RoomUnitStatus.MOVE) || getRoom() == null) {
            return;
        }
        double dDistance = getRoomUnit().getCurrentLocation().distance(roomChatMessage.getHabbo().getRoomUnit().getCurrentLocation());
        if (dDistance > Emulator.getConfig().getInt("hotel.bot.butler.commanddistance") || roomChatMessage.getUnfilteredMessage() == null) {
            return;
        }
        for (Map.Entry entry : serveItems.entrySet()) {
            TObjectHashIterator it = ((THashSet) entry.getKey()).iterator();
            while (it.hasNext()) {
                String str = (String) it.next();
                if (roomChatMessage.getUnfilteredMessage().toLowerCase().matches("\\b" + str + "\\b")) {
                    BotServerItemEvent botServerItemEvent = new BotServerItemEvent(this, roomChatMessage.getHabbo(), ((Integer) entry.getValue()).intValue());
                    if (((BotServerItemEvent) Emulator.getPluginManager().fireEvent(botServerItemEvent)).isCancelled()) {
                        return;
                    }
                    if (!getRoomUnit().canWalk()) {
                        if (getRoom() != null) {
                            getRoom().giveHandItem(botServerItemEvent.habbo, botServerItemEvent.itemId);
                            String strReplace = Emulator.getTexts().getValue("bots.butler.given").replace("%key%", str).replace("%username%", botServerItemEvent.habbo.getHabboInfo().getUsername());
                            if (WiredHandler.handle(WiredTriggerType.SAY_SOMETHING, getRoomUnit(), getRoom(), new Object[]{strReplace})) {
                                return;
                            }
                            talk(strReplace);
                            return;
                        }
                        return;
                    }
                    lookAt(botServerItemEvent.habbo);
                    ArrayList arrayList = new ArrayList();
                    arrayList.add(new RoomUnitGiveHanditem(botServerItemEvent.habbo.getRoomUnit(), botServerItemEvent.habbo.getHabboInfo().getCurrentRoom(), botServerItemEvent.itemId));
                    arrayList.add(new RoomUnitGiveHanditem(getRoomUnit(), botServerItemEvent.habbo.getHabboInfo().getCurrentRoom(), 0));
                    arrayList.add(() -> {
                        if (getRoom() != null) {
                            String strReplace2 = Emulator.getTexts().getValue("bots.butler.given").replace("%key%", str).replace("%username%", botServerItemEvent.habbo.getHabboInfo().getUsername());
                            if (WiredHandler.handle(WiredTriggerType.SAY_SOMETHING, getRoomUnit(), getRoom(), new Object[]{strReplace2})) {
                                return;
                            }
                            this.talk(strReplace2);
                        }
                    });
                    ArrayList arrayList2 = new ArrayList();
                    arrayList2.add(() -> {
                        if (dDistance <= Emulator.getConfig().getInt("hotel.bot.butler.servedistance", 8)) {
                            Iterator it2 = arrayList.iterator();
                            while (it2.hasNext()) {
                                ((Runnable) it2.next()).run();
                            }
                        }
                    });
                    Emulator.getThreading().run(new RoomUnitGiveHanditem(getRoomUnit(), botServerItemEvent.habbo.getHabboInfo().getCurrentRoom(), botServerItemEvent.itemId));
                    if (dDistance > Emulator.getConfig().getInt("hotel.bot.butler.reachdistance", 3)) {
                        Emulator.getThreading().run(new RoomUnitWalkToRoomUnit(getRoomUnit(), botServerItemEvent.habbo.getRoomUnit(), botServerItemEvent.habbo.getHabboInfo().getCurrentRoom(), arrayList, arrayList2, Emulator.getConfig().getInt("hotel.bot.butler.reachdistance", 3)));
                        return;
                    } else {
                        Emulator.getThreading().run((Runnable) arrayList2.get(0), 1000L);
                        return;
                    }
                }
            }
        }
    }
}
