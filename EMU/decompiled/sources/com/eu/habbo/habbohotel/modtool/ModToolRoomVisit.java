package com.eu.habbo.habbohotel.modtool;

import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/modtool/ModToolRoomVisit.class */
public class ModToolRoomVisit implements Comparable<ModToolRoomVisit> {
    public int roomId;
    public String roomName;
    public int timestamp;
    public int exitTimestamp;
    public THashSet<ModToolChatLog> chat;

    public ModToolRoomVisit(ResultSet resultSet) throws SQLException {
        this.roomId = resultSet.getInt("room_id");
        this.roomName = resultSet.getString("name");
        this.timestamp = resultSet.getInt("timestamp");
    }

    public ModToolRoomVisit(int i, String str, int i2, int i3) {
        this.roomId = i;
        this.roomName = str;
        this.timestamp = i2;
        this.exitTimestamp = i3;
        this.chat = new THashSet<>();
    }

    @Override // java.lang.Comparable
    public int compareTo(ModToolRoomVisit modToolRoomVisit) {
        return modToolRoomVisit.timestamp - this.timestamp;
    }
}
