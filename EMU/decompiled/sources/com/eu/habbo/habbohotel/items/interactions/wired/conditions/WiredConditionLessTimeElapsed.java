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

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/conditions/WiredConditionLessTimeElapsed.class */
public class WiredConditionLessTimeElapsed extends InteractionWiredCondition {
    public static final WiredConditionType type = WiredConditionType.TIME_LESS_THAN;
    private int cycles;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/conditions/WiredConditionLessTimeElapsed$JsonData.class */
    static class JsonData {
        int cycles;

        public JsonData(int i) {
            this.cycles = i;
        }
    }

    public WiredConditionLessTimeElapsed(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public WiredConditionLessTimeElapsed(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public boolean execute(RoomUnit roomUnit, Room room, Object[] objArr) {
        return ((double) (Emulator.getIntUnixTimestamp() - room.getLastTimerReset())) / 0.5d < ((double) this.cycles);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.cycles));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void loadWiredData(ResultSet resultSet, Room room) throws SQLException {
        String string = resultSet.getString("wired_data");
        try {
            if (string.startsWith("{")) {
                this.cycles = ((JsonData) WiredHandler.getGsonBuilder().create().fromJson(string, JsonData.class)).cycles;
            } else if (!string.equals(Emulator.PREVIEW)) {
                this.cycles = Integer.parseInt(string);
            }
        } catch (Exception e) {
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void onPickUp() {
        this.cycles = 0;
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
        serverMessage.appendInt((Integer) 1);
        serverMessage.appendInt(Integer.valueOf(this.cycles));
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(getType().code));
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt((Integer) 0);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition
    public boolean saveData(WiredSettings wiredSettings) {
        if (wiredSettings.getIntParams().length < 1) {
            return false;
        }
        this.cycles = wiredSettings.getIntParams()[0];
        return true;
    }
}
