package com.eu.habbo.habbohotel.items.interactions.wired.triggers;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.messages.ServerMessage;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/triggers/WiredTriggerHabboSaysKeyword.class */
public class WiredTriggerHabboSaysKeyword extends InteractionWiredTrigger {
    private static final WiredTriggerType type = WiredTriggerType.SAY_SOMETHING;
    private boolean ownerOnly;
    private String key;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/triggers/WiredTriggerHabboSaysKeyword$JsonData.class */
    static class JsonData {
        boolean ownerOnly;
        String key;

        public JsonData(boolean z, String str) {
            this.ownerOnly = z;
            this.key = str;
        }
    }

    public WiredTriggerHabboSaysKeyword(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.ownerOnly = false;
        this.key = Emulator.PREVIEW;
    }

    public WiredTriggerHabboSaysKeyword(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.ownerOnly = false;
        this.key = Emulator.PREVIEW;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public boolean execute(RoomUnit roomUnit, Room room, Object[] objArr) {
        if (this.key.length() <= 0 || !(objArr[0] instanceof String) || !((String) objArr[0]).toLowerCase().contains(this.key.toLowerCase())) {
            return false;
        }
        Habbo habbo = room.getHabbo(roomUnit);
        return !this.ownerOnly || (habbo != null && room.getOwnerId() == habbo.getHabboInfo().getId());
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.ownerOnly, this.key));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void loadWiredData(ResultSet resultSet, Room room) throws SQLException {
        String string = resultSet.getString("wired_data");
        if (string.startsWith("{")) {
            JsonData jsonData = (JsonData) WiredHandler.getGsonBuilder().create().fromJson(string, JsonData.class);
            this.ownerOnly = jsonData.ownerOnly;
            this.key = jsonData.key;
        } else {
            String[] strArrSplit = string.split("\t");
            if (strArrSplit.length == 2) {
                this.ownerOnly = strArrSplit[0].equalsIgnoreCase("1");
                this.key = strArrSplit[1];
            }
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void onPickUp() {
        this.ownerOnly = false;
        this.key = Emulator.PREVIEW;
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
        serverMessage.appendString(this.key);
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt((Integer) 1);
        serverMessage.appendInt(Integer.valueOf(getType().code));
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt((Integer) 0);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger
    public boolean saveData(WiredSettings wiredSettings) {
        if (wiredSettings.getIntParams().length < 1) {
            return false;
        }
        this.ownerOnly = wiredSettings.getIntParams()[0] == 1;
        this.key = wiredSettings.getStringParam();
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger
    public boolean isTriggeredByRoomUnit() {
        return true;
    }
}
