package com.eu.habbo.habbohotel.navigation;

import com.eu.habbo.habbohotel.rooms.Room;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/navigation/NavigatorPublicCategory.class */
public class NavigatorPublicCategory {
    public final int id;
    public final String name;
    public final List<Room> rooms;
    public final ListMode image;
    public final int order;

    public NavigatorPublicCategory(ResultSet resultSet) throws SQLException {
        this.id = resultSet.getInt("id");
        this.name = resultSet.getString("name");
        this.image = resultSet.getString("image").equals("1") ? ListMode.THUMBNAILS : ListMode.LIST;
        this.order = resultSet.getInt("order_num");
        this.rooms = new ArrayList();
    }

    public void addRoom(Room room) {
        room.preventUncaching = true;
        this.rooms.add(room);
    }

    public void removeRoom(Room room) {
        this.rooms.remove(room);
        room.preventUncaching = room.isPublicRoom();
    }
}
