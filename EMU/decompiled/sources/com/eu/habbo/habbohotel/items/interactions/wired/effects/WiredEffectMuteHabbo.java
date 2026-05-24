package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessage;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.wired.WiredSaveException;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserWhisperComposer;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectMuteHabbo.class */
public class WiredEffectMuteHabbo extends InteractionWiredEffect {
    private static final WiredEffectType type = WiredEffectType.MUTE_TRIGGER;
    private int length;
    private String message;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectMuteHabbo$JsonData.class */
    static class JsonData {
        int delay;
        int length;
        String message;

        public JsonData(int i, int i2, String str) {
            this.delay = i;
            this.length = i2;
            this.message = str;
        }
    }

    public WiredEffectMuteHabbo(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.length = 5;
        this.message = Emulator.PREVIEW;
    }

    public WiredEffectMuteHabbo(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.length = 5;
        this.message = Emulator.PREVIEW;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void serializeWiredData(ServerMessage serverMessage, Room room) {
        serverMessage.appendBoolean(false);
        serverMessage.appendInt((Integer) 5);
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(getBaseItem().getSpriteId()));
        serverMessage.appendInt(Integer.valueOf(getId()));
        serverMessage.appendString(this.message);
        serverMessage.appendInt((Integer) 1);
        serverMessage.appendInt(Integer.valueOf(this.length));
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(getType().code));
        serverMessage.appendInt(Integer.valueOf(getDelay()));
        serverMessage.appendInt((Integer) 0);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect
    public boolean saveData(WiredSettings wiredSettings, GameClient gameClient) throws WiredSaveException {
        if (wiredSettings.getIntParams().length < 1) {
            throw new WiredSaveException("invalid data");
        }
        this.length = wiredSettings.getIntParams()[0];
        this.message = wiredSettings.getStringParam();
        setDelay(wiredSettings.getDelay());
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public boolean execute(RoomUnit roomUnit, Room room, Object[] objArr) {
        Habbo habbo;
        if (roomUnit == null || (habbo = room.getHabbo(roomUnit)) == null) {
            return true;
        }
        if (room.hasRights(habbo)) {
            return false;
        }
        room.muteHabbo(habbo, 60);
        habbo.getClient().sendResponse(new RoomUserWhisperComposer(new RoomChatMessage(this.message.replace("%user%", habbo.getHabboInfo().getUsername()).replace("%online_count%", Emulator.getGameEnvironment().getHabboManager().getOnlineCount() + Emulator.PREVIEW).replace("%room_count%", Emulator.getGameEnvironment().getRoomManager().getActiveRooms().size() + Emulator.PREVIEW), habbo, habbo, RoomChatMessageBubbles.WIRED)));
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(getDelay(), this.length, this.message));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void loadWiredData(ResultSet resultSet, Room room) throws SQLException {
        String string = resultSet.getString("wired_data");
        if (string.startsWith("{")) {
            JsonData jsonData = (JsonData) WiredHandler.getGsonBuilder().create().fromJson(string, JsonData.class);
            setDelay(jsonData.delay);
            this.length = jsonData.length;
            this.message = jsonData.message;
            return;
        }
        String[] strArrSplit = string.split("\t");
        if (strArrSplit.length >= 3) {
            try {
                setDelay(Integer.valueOf(strArrSplit[0]).intValue());
                this.length = Integer.valueOf(strArrSplit[1]).intValue();
                this.message = strArrSplit[2];
            } catch (Exception e) {
            }
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void onPickUp() {
        setDelay(0);
        this.message = Emulator.PREVIEW;
        this.length = 0;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect
    public WiredEffectType getType() {
        return type;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect
    public boolean requiresTriggeringUser() {
        return true;
    }
}
