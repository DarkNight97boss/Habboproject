package com.eu.habbo.habbohotel.items.interactions.wired.conditions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.wired.WiredConditionType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ServerMessage;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/conditions/WiredConditionHabboCount.class */
public class WiredConditionHabboCount extends InteractionWiredCondition {
    public static final WiredConditionType type = WiredConditionType.USER_COUNT;
    private int lowerLimit;
    private int upperLimit;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/conditions/WiredConditionHabboCount$JsonData.class */
    static class JsonData {
        int lowerLimit;
        int upperLimit;

        public JsonData(int i, int i2) {
            this.lowerLimit = i;
            this.upperLimit = i2;
        }
    }

    public WiredConditionHabboCount(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.lowerLimit = 0;
        this.upperLimit = 50;
    }

    public WiredConditionHabboCount(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.lowerLimit = 0;
        this.upperLimit = 50;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public boolean execute(RoomUnit roomUnit, Room room, Object[] objArr) {
        int userCount = room.getUserCount();
        return userCount >= this.lowerLimit && userCount <= this.upperLimit;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.lowerLimit, this.upperLimit));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void loadWiredData(ResultSet resultSet, Room room) throws SQLException {
        String string = resultSet.getString("wired_data");
        if (string.startsWith("{")) {
            JsonData jsonData = (JsonData) WiredHandler.getGsonBuilder().create().fromJson(string, JsonData.class);
            this.lowerLimit = jsonData.lowerLimit;
            this.upperLimit = jsonData.upperLimit;
        } else {
            String[] strArrSplit = string.split(":");
            this.lowerLimit = Integer.parseInt(strArrSplit[0]);
            this.upperLimit = Integer.parseInt(strArrSplit[1]);
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void onPickUp() {
        this.lowerLimit = 0;
        this.upperLimit = 50;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition
    public WiredConditionType getType() {
        return type;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void serializeWiredData(ServerMessage serverMessage, Room room) {
        serverMessage.appendBoolean(false);
        serverMessage.appendInt((Integer) 5);
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(getBaseItem().getSpriteId()));
        serverMessage.appendInt(Integer.valueOf(getId()));
        serverMessage.appendString(Emulator.PREVIEW);
        serverMessage.appendInt((Integer) 2);
        serverMessage.appendInt(Integer.valueOf(this.lowerLimit));
        serverMessage.appendInt(Integer.valueOf(this.upperLimit));
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(getType().code));
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt((Integer) 0);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition
    public boolean saveData(WiredSettings wiredSettings) {
        if (wiredSettings.getIntParams().length < 2) {
            return false;
        }
        this.lowerLimit = wiredSettings.getIntParams()[0];
        this.upperLimit = wiredSettings.getIntParams()[1];
        return true;
    }
}
