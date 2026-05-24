package com.eu.habbo.habbohotel.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.navigation.DisplayMode;
import com.eu.habbo.habbohotel.navigation.ListMode;
import gnu.trove.map.hash.THashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/users/HabboNavigatorWindowSettings.class */
public class HabboNavigatorWindowSettings {
    private static final Logger LOGGER = LoggerFactory.getLogger(HabboNavigatorWindowSettings.class);
    public final THashMap<String, HabboNavigatorPersonalDisplayMode> displayModes;
    private final int userId;
    public int x;
    public int y;
    public int width;
    public int height;
    public boolean openSearches;
    public int unknown;

    public HabboNavigatorWindowSettings(int i) {
        this.displayModes = new THashMap<>(2);
        this.x = 100;
        this.y = 100;
        this.width = 425;
        this.height = 535;
        this.openSearches = false;
        this.unknown = 0;
        this.userId = i;
    }

    public HabboNavigatorWindowSettings(ResultSet resultSet) throws SQLException {
        this.displayModes = new THashMap<>(2);
        this.x = 100;
        this.y = 100;
        this.width = 425;
        this.height = 535;
        this.openSearches = false;
        this.unknown = 0;
        this.userId = resultSet.getInt("user_id");
        this.x = resultSet.getInt("x");
        this.y = resultSet.getInt("y");
        this.width = resultSet.getInt("width");
        this.height = resultSet.getInt("height");
        this.openSearches = resultSet.getBoolean("open_searches");
        this.unknown = 0;
    }

    public void addDisplayMode(String str, HabboNavigatorPersonalDisplayMode habboNavigatorPersonalDisplayMode) {
        this.displayModes.put(str, habboNavigatorPersonalDisplayMode);
    }

    public boolean hasDisplayMode(String str) {
        return this.displayModes.containsKey(str);
    }

    public void insertDisplayMode(String str, ListMode listMode, DisplayMode displayMode) {
        if (this.displayModes.containsKey(str)) {
            return;
        }
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO users_navigator_settings (user_id, caption, list_type, display) VALUES (?, ?, ?, ?)");
                try {
                    preparedStatementPrepareStatement.setInt(1, this.userId);
                    preparedStatementPrepareStatement.setString(2, str);
                    preparedStatementPrepareStatement.setString(3, listMode.name().toLowerCase());
                    preparedStatementPrepareStatement.setString(4, displayMode.name().toLowerCase());
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
        this.displayModes.put(str, new HabboNavigatorPersonalDisplayMode(listMode, displayMode));
    }

    public void setDisplayMode(String str, DisplayMode displayMode) {
        HabboNavigatorPersonalDisplayMode habboNavigatorPersonalDisplayMode = (HabboNavigatorPersonalDisplayMode) this.displayModes.get(str);
        if (habboNavigatorPersonalDisplayMode != null) {
            habboNavigatorPersonalDisplayMode.displayMode = displayMode;
        } else {
            insertDisplayMode(str, ListMode.LIST, displayMode);
        }
    }

    public void setListMode(String str, ListMode listMode) {
        HabboNavigatorPersonalDisplayMode habboNavigatorPersonalDisplayMode = (HabboNavigatorPersonalDisplayMode) this.displayModes.get(str);
        if (habboNavigatorPersonalDisplayMode != null) {
            habboNavigatorPersonalDisplayMode.listMode = listMode;
        } else {
            insertDisplayMode(str, listMode, DisplayMode.VISIBLE);
        }
    }

    public DisplayMode getDisplayModeForCategory(String str) {
        return getDisplayModeForCategory(str, DisplayMode.VISIBLE);
    }

    public DisplayMode getDisplayModeForCategory(String str, DisplayMode displayMode) {
        return this.displayModes.containsKey(str) ? ((HabboNavigatorPersonalDisplayMode) this.displayModes.get(str)).displayMode : displayMode;
    }

    public ListMode getListModeForCategory(String str) {
        return getListModeForCategory(str, ListMode.LIST);
    }

    public ListMode getListModeForCategory(String str, ListMode listMode) {
        return this.displayModes.containsKey(str) ? ((HabboNavigatorPersonalDisplayMode) this.displayModes.get(str)).listMode : listMode;
    }

    public void save(Connection connection) {
        try {
            PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE users_navigator_settings SET list_type = ?, display = ? WHERE user_id = ? AND caption = ? LIMIT 1");
            try {
                for (Map.Entry entry : this.displayModes.entrySet()) {
                    preparedStatementPrepareStatement.setString(1, ((HabboNavigatorPersonalDisplayMode) entry.getValue()).listMode.name().toLowerCase());
                    preparedStatementPrepareStatement.setString(2, ((HabboNavigatorPersonalDisplayMode) entry.getValue()).displayMode.name().toLowerCase());
                    preparedStatementPrepareStatement.setInt(3, this.userId);
                    preparedStatementPrepareStatement.setString(4, (String) entry.getKey());
                    preparedStatementPrepareStatement.execute();
                }
                if (preparedStatementPrepareStatement != null) {
                    preparedStatementPrepareStatement.close();
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }
}
