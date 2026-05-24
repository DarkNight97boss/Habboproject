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

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/triggers/WiredTriggerBotReachedHabbo.class */
public class WiredTriggerBotReachedHabbo extends InteractionWiredTrigger {
    public static final WiredTriggerType type = WiredTriggerType.BOT_REACHED_AVTR;
    private String botName;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/triggers/WiredTriggerBotReachedHabbo$JsonData.class */
    static class JsonData {
        String botName;

        public JsonData(String str) {
            this.botName = str;
        }
    }

    public WiredTriggerBotReachedHabbo(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.botName = Emulator.PREVIEW;
    }

    public WiredTriggerBotReachedHabbo(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.botName = Emulator.PREVIEW;
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
        serverMessage.appendString(this.botName);
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(getType().code));
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt((Integer) 0);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger
    public boolean saveData(WiredSettings wiredSettings) {
        this.botName = wiredSettings.getStringParam();
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public boolean execute(RoomUnit roomUnit, Room room, Object[] objArr) {
        return room.getBots(this.botName).stream().anyMatch(bot -> {
            return bot.getRoomUnit() == roomUnit;
        });
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.botName));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void loadWiredData(ResultSet resultSet, Room room) throws SQLException {
        String string = resultSet.getString("wired_data");
        if (string.startsWith("{")) {
            this.botName = ((JsonData) WiredHandler.getGsonBuilder().create().fromJson(string, JsonData.class)).botName;
        } else {
            this.botName = string;
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void onPickUp() {
        this.botName = Emulator.PREVIEW;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger
    public boolean isTriggeredByRoomUnit() {
        return true;
    }
}
