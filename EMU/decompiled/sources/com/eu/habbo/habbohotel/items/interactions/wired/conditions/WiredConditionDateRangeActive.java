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

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/conditions/WiredConditionDateRangeActive.class */
public class WiredConditionDateRangeActive extends InteractionWiredCondition {
    public static final WiredConditionType type = WiredConditionType.DATE_RANGE;
    private int startDate;
    private int endDate;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/conditions/WiredConditionDateRangeActive$JsonData.class */
    static class JsonData {
        int startDate;
        int endDate;

        public JsonData(int i, int i2) {
            this.startDate = i;
            this.endDate = i2;
        }
    }

    public WiredConditionDateRangeActive(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public WiredConditionDateRangeActive(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
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
        serverMessage.appendInt(Integer.valueOf(this.startDate));
        serverMessage.appendInt(Integer.valueOf(this.endDate));
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(getType().code));
        serverMessage.appendInt(Integer.valueOf(this.startDate));
        serverMessage.appendInt(Integer.valueOf(this.endDate));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition
    public boolean saveData(WiredSettings wiredSettings) {
        if (wiredSettings.getIntParams().length < 2) {
            return false;
        }
        this.startDate = wiredSettings.getIntParams()[0];
        this.endDate = wiredSettings.getIntParams()[1];
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public boolean execute(RoomUnit roomUnit, Room room, Object[] objArr) {
        int intUnixTimestamp = Emulator.getIntUnixTimestamp();
        return this.startDate < intUnixTimestamp && this.endDate >= intUnixTimestamp;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.startDate, this.endDate));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void loadWiredData(ResultSet resultSet, Room room) throws SQLException {
        String string = resultSet.getString("wired_data");
        if (string.startsWith("{")) {
            JsonData jsonData = (JsonData) WiredHandler.getGsonBuilder().create().fromJson(string, JsonData.class);
            this.startDate = jsonData.startDate;
            this.endDate = jsonData.endDate;
        } else {
            String[] strArrSplit = string.split("\t");
            if (strArrSplit.length == 2) {
                try {
                    this.startDate = Integer.parseInt(strArrSplit[0]);
                    this.endDate = Integer.parseInt(strArrSplit[1]);
                } catch (Exception e) {
                }
            }
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void onPickUp() {
        this.startDate = 0;
        this.endDate = 0;
    }
}
