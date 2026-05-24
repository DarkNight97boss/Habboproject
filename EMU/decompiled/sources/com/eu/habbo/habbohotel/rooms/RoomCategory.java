package com.eu.habbo.habbohotel.rooms;

import com.eu.habbo.habbohotel.navigation.ListMode;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/rooms/RoomCategory.class */
public class RoomCategory implements Comparable<RoomCategory> {
    private int id;
    private int minRank;
    private String caption;
    private String captionSave;
    private boolean canTrade;
    private int maxUserCount;
    private boolean official;
    private ListMode displayMode;
    private int order;

    public RoomCategory(ResultSet resultSet) throws SQLException {
        this.id = resultSet.getInt("id");
        this.minRank = resultSet.getInt("min_rank");
        this.caption = resultSet.getString("caption");
        this.captionSave = resultSet.getString("caption_save");
        this.canTrade = resultSet.getBoolean("can_trade");
        this.maxUserCount = resultSet.getInt("max_user_count");
        this.official = resultSet.getString("public").equals("1");
        this.displayMode = ListMode.fromType(resultSet.getInt("list_type"));
        this.order = resultSet.getInt("order_num");
    }

    public int getId() {
        return this.id;
    }

    public int getMinRank() {
        return this.minRank;
    }

    public String getCaption() {
        return this.caption;
    }

    public String getCaptionSave() {
        return this.captionSave;
    }

    public boolean isCanTrade() {
        return this.canTrade;
    }

    public int getMaxUserCount() {
        return this.maxUserCount;
    }

    public boolean isPublic() {
        return this.official;
    }

    public ListMode getDisplayMode() {
        return this.displayMode;
    }

    public int getOrder() {
        return this.order;
    }

    @Override // java.lang.Comparable
    public int compareTo(RoomCategory roomCategory) {
        return roomCategory.getId() - getId();
    }
}
