package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.map.hash.THashMap;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionCustomValues.class */
public abstract class InteractionCustomValues extends HabboItem {
    public final THashMap<String, String> values;

    public InteractionCustomValues(ResultSet resultSet, Item item, THashMap<String, String> tHashMap) throws SQLException {
        super(resultSet, item);
        this.values = new THashMap<>();
        this.values.putAll(tHashMap);
        for (String str : resultSet.getString("extra_data").split(";")) {
            String[] strArrSplit = str.split("=");
            if (strArrSplit.length == 2) {
                this.values.put(strArrSplit[0], strArrSplit[1]);
            }
        }
    }

    public InteractionCustomValues(int i, int i2, Item item, String str, int i3, int i4, THashMap<String, String> tHashMap) {
        super(i, i2, item, str, i3, i4);
        this.values = new THashMap<>();
        this.values.putAll(tHashMap);
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

    @Override // com.eu.habbo.habbohotel.users.HabboItem, java.lang.Runnable
    public void run() {
        setExtradata(toExtraData());
        super.run();
    }

    public String toExtraData() {
        StringBuilder sb = new StringBuilder();
        synchronized (this.values) {
            for (Map.Entry entry : this.values.entrySet()) {
                sb.append((String) entry.getKey()).append("=").append((String) entry.getValue()).append(";");
            }
        }
        return sb.toString();
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void serializeExtradata(ServerMessage serverMessage) {
        serverMessage.appendInt(Integer.valueOf(1 + (isLimited() ? 256 : 0)));
        serverMessage.appendInt(Integer.valueOf(this.values.size()));
        for (Map.Entry entry : this.values.entrySet()) {
            serverMessage.appendString((String) entry.getKey());
            serverMessage.appendString((String) entry.getValue());
        }
        super.serializeExtradata(serverMessage);
    }

    public void onCustomValuesSaved(Room room, GameClient gameClient, THashMap<String, String> tHashMap) {
    }
}
