package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionMusicDisc.class */
public class InteractionMusicDisc extends HabboItem {
    private static final Logger LOGGER = LoggerFactory.getLogger(InteractionMusicDisc.class);
    private int songId;

    public InteractionMusicDisc(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        String[] strArrSplit = getExtradata().split("\n");
        if (strArrSplit.length < 7 || strArrSplit[6].isEmpty()) {
            return;
        }
        try {
            this.songId = Integer.valueOf(strArrSplit[6]).intValue();
        } catch (Exception e) {
            LOGGER.error("Warning: Item " + getId() + " has an invalid song id set for its music disk!");
        }
    }

    public InteractionMusicDisc(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        String[] strArrSplit = getExtradata().split("\n");
        if (strArrSplit.length < 7 || strArrSplit[6].isEmpty()) {
            return;
        }
        try {
            this.songId = Integer.valueOf(strArrSplit[6]).intValue();
        } catch (Exception e) {
            LOGGER.error("Warning: Item " + getId() + " has an invalid song id set for its music disk!");
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void serializeExtradata(ServerMessage serverMessage) {
        serverMessage.appendInt(Integer.valueOf(isLimited() ? 256 : 0));
        serverMessage.appendString(getExtradata());
        super.serializeExtradata(serverMessage);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        return false;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return false;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }

    public int getSongId() {
        return this.songId;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPlace(Room room) {
        super.onPlace(room);
        room.getTraxManager().sendUpdatedSongList();
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPickUp(Room room) {
        super.onPickUp(room);
        room.getTraxManager().sendUpdatedSongList();
    }
}
