package com.eu.habbo.habbohotel.items.interactions.wired.triggers;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.messages.ServerMessage;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/triggers/WiredTriggerScoreAchieved.class */
public class WiredTriggerScoreAchieved extends InteractionWiredTrigger {
    private static final WiredTriggerType type = WiredTriggerType.SCORE_ACHIEVED;
    private int score;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/triggers/WiredTriggerScoreAchieved$JsonData.class */
    static class JsonData {
        int score;

        public JsonData(int i) {
            this.score = i;
        }
    }

    public WiredTriggerScoreAchieved(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.score = 0;
    }

    public WiredTriggerScoreAchieved(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.score = 0;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public boolean execute(RoomUnit roomUnit, Room room, Object[] objArr) {
        if (objArr.length < 2) {
            return false;
        }
        int iIntValue = ((Integer) objArr[0]).intValue();
        return iIntValue - ((Integer) objArr[1]).intValue() < this.score && iIntValue >= this.score;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.score));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void loadWiredData(ResultSet resultSet, Room room) throws SQLException {
        String string = resultSet.getString("wired_data");
        if (string.startsWith("{")) {
            this.score = ((JsonData) WiredHandler.getGsonBuilder().create().fromJson(string, JsonData.class)).score;
        } else {
            try {
                this.score = Integer.parseInt(string);
            } catch (Exception e) {
            }
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void onPickUp() {
        this.score = 0;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger
    public WiredTriggerType getType() {
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
        serverMessage.appendInt(Integer.valueOf(this.score));
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(getType().code));
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt((Integer) 0);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger
    public boolean saveData(WiredSettings wiredSettings) {
        if (wiredSettings.getIntParams().length < 1) {
            return false;
        }
        this.score = wiredSettings.getIntParams()[0];
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger
    public boolean isTriggeredByRoomUnit() {
        return true;
    }
}
