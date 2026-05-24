package com.eu.habbo.habbohotel.users;

import com.eu.habbo.habbohotel.navigation.DisplayMode;
import com.eu.habbo.habbohotel.navigation.ListMode;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/users/HabboNavigatorPersonalDisplayMode.class */
public class HabboNavigatorPersonalDisplayMode {
    public ListMode listMode;
    public DisplayMode displayMode;

    public HabboNavigatorPersonalDisplayMode(ListMode listMode, DisplayMode displayMode) {
        this.listMode = listMode;
        this.displayMode = displayMode;
    }

    public HabboNavigatorPersonalDisplayMode(ResultSet resultSet) throws SQLException {
        this.listMode = resultSet.getString("list_type").equals("thumbnails") ? ListMode.THUMBNAILS : ListMode.LIST;
        this.displayMode = DisplayMode.valueOf(resultSet.getString("display").toUpperCase());
    }
}
