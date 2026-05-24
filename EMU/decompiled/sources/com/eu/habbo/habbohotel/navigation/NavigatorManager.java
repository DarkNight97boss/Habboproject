package com.eu.habbo.habbohotel.navigation;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import gnu.trove.map.hash.THashMap;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/navigation/NavigatorManager.class */
public class NavigatorManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(NavigatorManager.class);
    public static int MAXIMUM_RESULTS_PER_PAGE = 10;
    public static boolean CATEGORY_SORT_USING_ORDER_NUM = false;
    public final THashMap<Integer, NavigatorPublicCategory> publicCategories = new THashMap<>();
    public final ConcurrentHashMap<String, NavigatorFilterField> filterSettings = new ConcurrentHashMap<>();
    public final THashMap<String, NavigatorFilter> filters = new THashMap<>();

    public NavigatorManager() {
        long jCurrentTimeMillis = System.currentTimeMillis();
        this.filters.put(NavigatorPublicFilter.name, new NavigatorPublicFilter());
        this.filters.put(NavigatorHotelFilter.name, new NavigatorHotelFilter());
        this.filters.put(NavigatorRoomAdsFilter.name, new NavigatorRoomAdsFilter());
        this.filters.put(NavigatorUserFilter.name, new NavigatorUserFilter());
        this.filters.put(NavigatorFavoriteFilter.name, new NavigatorFavoriteFilter());
        LOGGER.info("Navigator Manager -> Loaded! (" + (System.currentTimeMillis() - jCurrentTimeMillis) + " MS)");
    }

    public void loadNavigator() {
        Statement statementCreateStatement;
        ResultSet resultSetExecuteQuery;
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                synchronized (this.publicCategories) {
                    this.publicCategories.clear();
                    try {
                        statementCreateStatement = connection.createStatement();
                        try {
                            resultSetExecuteQuery = statementCreateStatement.executeQuery("SELECT * FROM navigator_publiccats WHERE visible = '1' ORDER BY order_num DESC");
                            while (resultSetExecuteQuery.next()) {
                                try {
                                    this.publicCategories.put(Integer.valueOf(resultSetExecuteQuery.getInt("id")), new NavigatorPublicCategory(resultSetExecuteQuery));
                                } finally {
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
                    try {
                        Statement statementCreateStatement2 = connection.createStatement();
                        try {
                            resultSetExecuteQuery = statementCreateStatement2.executeQuery("SELECT * FROM navigator_publics WHERE visible = '1'");
                            while (resultSetExecuteQuery.next()) {
                                try {
                                    NavigatorPublicCategory navigatorPublicCategory = (NavigatorPublicCategory) this.publicCategories.get(Integer.valueOf(resultSetExecuteQuery.getInt("public_cat_id")));
                                    if (navigatorPublicCategory != null) {
                                        Room roomLoadRoom = Emulator.getGameEnvironment().getRoomManager().loadRoom(resultSetExecuteQuery.getInt("room_id"));
                                        if (roomLoadRoom != null) {
                                            navigatorPublicCategory.addRoom(roomLoadRoom);
                                        } else {
                                            LOGGER.error("Public room (ID: {} defined in navigator_publics does not exist!", Integer.valueOf(resultSetExecuteQuery.getInt("room_id")));
                                        }
                                    }
                                } finally {
                                }
                            }
                            if (resultSetExecuteQuery != null) {
                                resultSetExecuteQuery.close();
                            }
                            if (statementCreateStatement2 != null) {
                                statementCreateStatement2.close();
                            }
                        } finally {
                        }
                    } catch (SQLException e2) {
                        LOGGER.error("Caught SQL exception", e2);
                    }
                }
                synchronized (this.filterSettings) {
                    try {
                        statementCreateStatement = connection.createStatement();
                    } catch (SQLException e3) {
                        LOGGER.error("Caught SQL exception", e3);
                    }
                    try {
                        ResultSet resultSetExecuteQuery2 = statementCreateStatement.executeQuery("SELECT * FROM navigator_filter");
                        while (resultSetExecuteQuery2.next()) {
                            try {
                                Method declaredMethod = null;
                                Class<?> returnType = Room.class;
                                if (resultSetExecuteQuery2.getString("field").contains(".")) {
                                    for (String str : resultSetExecuteQuery2.getString("field").split("\\.")) {
                                        try {
                                            declaredMethod = returnType.getDeclaredMethod(str, new Class[0]);
                                            returnType = declaredMethod.getReturnType();
                                        } catch (Exception e4) {
                                            LOGGER.error("Caught exception", e4);
                                        }
                                    }
                                } else {
                                    try {
                                        declaredMethod = returnType.getDeclaredMethod(resultSetExecuteQuery2.getString("field"), new Class[0]);
                                    } catch (Exception e5) {
                                        LOGGER.error("Caught exception", e5);
                                    }
                                }
                                if (declaredMethod != null) {
                                    this.filterSettings.put(resultSetExecuteQuery2.getString("key"), new NavigatorFilterField(resultSetExecuteQuery2.getString("key"), declaredMethod, resultSetExecuteQuery2.getString("database_query"), NavigatorFilterComparator.valueOf(resultSetExecuteQuery2.getString("compare").toUpperCase())));
                                }
                            } finally {
                                if (resultSetExecuteQuery2 != null) {
                                    try {
                                        resultSetExecuteQuery2.close();
                                    } catch (Throwable th) {
                                        th.addSuppressed(th);
                                    }
                                }
                            }
                        }
                        if (resultSetExecuteQuery2 != null) {
                            resultSetExecuteQuery2.close();
                        }
                        if (statementCreateStatement != null) {
                            statementCreateStatement.close();
                        }
                    } finally {
                        if (statementCreateStatement != null) {
                            try {
                                statementCreateStatement.close();
                            } catch (Throwable th2) {
                                th.addSuppressed(th2);
                            }
                        }
                    }
                }
                if (connection != null) {
                    connection.close();
                }
            } finally {
            }
        } catch (SQLException e6) {
            LOGGER.error("Caught SQL exception", e6);
        }
        Iterator<Room> it = Emulator.getGameEnvironment().getRoomManager().getRoomsStaffPromoted().iterator();
        while (it.hasNext()) {
            ((NavigatorPublicCategory) this.publicCategories.get(Integer.valueOf(Emulator.getConfig().getInt("hotel.navigator.staffpicks.categoryid")))).addRoom(it.next());
        }
    }

    public NavigatorFilterComparator comperatorForField(Method method) {
        for (Map.Entry<String, NavigatorFilterField> entry : this.filterSettings.entrySet()) {
            if (entry.getValue().field == method) {
                return entry.getValue().comparator;
            }
        }
        return null;
    }

    public List<Room> getRoomsForCategory(String str, Habbo habbo) {
        List<Room> topRatedRooms;
        new ArrayList();
        switch (str) {
            case "my":
                topRatedRooms = Emulator.getGameEnvironment().getRoomManager().getRoomsForHabbo(habbo);
                break;
            case "favorites":
                topRatedRooms = Emulator.getGameEnvironment().getRoomManager().getRoomsFavourite(habbo);
                break;
            case "history_freq":
                topRatedRooms = Emulator.getGameEnvironment().getRoomManager().getRoomsVisited(habbo, false, 10);
                break;
            case "my_groups":
                topRatedRooms = Emulator.getGameEnvironment().getRoomManager().getGroupRooms(habbo, 25);
                break;
            case "with_rights":
                topRatedRooms = Emulator.getGameEnvironment().getRoomManager().getRoomsWithRights(habbo);
                break;
            case "official-root":
                topRatedRooms = Emulator.getGameEnvironment().getRoomManager().getPublicRooms();
                break;
            case "popular":
                topRatedRooms = Emulator.getGameEnvironment().getRoomManager().getPopularRooms(Emulator.getConfig().getInt("hotel.navigator.popular.amount"));
                break;
            case "categories":
                topRatedRooms = Emulator.getGameEnvironment().getRoomManager().getRoomsPromoted();
                break;
            case "with_friends":
                topRatedRooms = Emulator.getGameEnvironment().getRoomManager().getRoomsWithFriendsIn(habbo, 25);
                break;
            case "highest_score":
                topRatedRooms = Emulator.getGameEnvironment().getRoomManager().getTopRatedRooms(25);
                break;
            default:
                return null;
        }
        Collections.sort(topRatedRooms);
        return topRatedRooms;
    }
}
