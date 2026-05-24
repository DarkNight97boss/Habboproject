package com.eu.habbo.habbohotel.items.interactions.wired.triggers;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.ICycleable;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredTriggerReset;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.procedure.TObjectProcedure;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/triggers/WiredTriggerRepeaterLong.class */
public class WiredTriggerRepeaterLong extends InteractionWiredTrigger implements ICycleable, WiredTriggerReset {
    public static final int DEFAULT_DELAY = 50000;
    private static final WiredTriggerType type = WiredTriggerType.PERIODICALLY_LONG;
    private int repeatTime;
    private int counter;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/triggers/WiredTriggerRepeaterLong$JsonData.class */
    static class JsonData {
        int repeatTime;

        public JsonData(int i) {
            this.repeatTime = i;
        }
    }

    public WiredTriggerRepeaterLong(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.repeatTime = DEFAULT_DELAY;
        this.counter = 0;
    }

    public WiredTriggerRepeaterLong(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.repeatTime = DEFAULT_DELAY;
        this.counter = 0;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public boolean execute(RoomUnit roomUnit, Room room, Object[] objArr) {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.repeatTime));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void loadWiredData(ResultSet resultSet, Room room) throws SQLException {
        String string = resultSet.getString("wired_data");
        if (string.startsWith("{")) {
            this.repeatTime = ((JsonData) WiredHandler.getGsonBuilder().create().fromJson(string, JsonData.class)).repeatTime;
        } else if (string.length() >= 1) {
            this.repeatTime = Integer.valueOf(string).intValue();
        }
        if (this.repeatTime < 5000) {
            this.repeatTime = 100000;
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void onPickUp() {
        this.repeatTime = DEFAULT_DELAY;
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
        serverMessage.appendInt(Integer.valueOf(this.repeatTime / 5000));
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(getType().code));
        if (isTriggeredByRoomUnit()) {
            serverMessage.appendInt((Integer) 0);
            return;
        }
        final ArrayList arrayList = new ArrayList();
        room.getRoomSpecialTypes().getEffects(getX(), getY()).forEach(new TObjectProcedure<InteractionWiredEffect>() { // from class: com.eu.habbo.habbohotel.items.interactions.wired.triggers.WiredTriggerRepeaterLong.1
            public boolean execute(InteractionWiredEffect interactionWiredEffect) {
                if (!interactionWiredEffect.requiresTriggeringUser()) {
                    return true;
                }
                arrayList.add(Integer.valueOf(interactionWiredEffect.getBaseItem().getSpriteId()));
                return true;
            }
        });
        serverMessage.appendInt(Integer.valueOf(arrayList.size()));
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            serverMessage.appendInt((Integer) it.next());
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger
    public boolean saveData(WiredSettings wiredSettings) {
        if (wiredSettings.getIntParams().length < 1) {
            return false;
        }
        this.repeatTime = wiredSettings.getIntParams()[0] * 5000;
        this.counter = 0;
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.ICycleable
    public void cycle(Room room) {
        this.counter += 500;
        if (this.counter >= this.repeatTime) {
            this.counter = 0;
            if (getRoomId() == 0 || !room.isLoaded()) {
                return;
            }
            WiredHandler.handle(this, (RoomUnit) null, room, new Object[]{this});
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.wired.WiredTriggerReset
    public void resetTimer() {
        Room room;
        this.counter = 0;
        if (getRoomId() == 0 || (room = Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId())) == null || !room.isLoaded()) {
            return;
        }
        WiredHandler.handle(this, (RoomUnit) null, room, new Object[]{this});
    }
}
