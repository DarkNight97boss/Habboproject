package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.threading.runnables.WiredResetTimers;
import gnu.trove.procedure.TObjectProcedure;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectResetTimers.class */
public class WiredEffectResetTimers extends InteractionWiredEffect {
    public static final WiredEffectType type = WiredEffectType.RESET_TIMERS;
    private int delay;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectResetTimers$JsonData.class */
    static class JsonData {
        int delay;

        public JsonData(int i) {
            this.delay = i;
        }
    }

    public WiredEffectResetTimers(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.delay = 0;
    }

    public WiredEffectResetTimers(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.delay = 0;
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
        serverMessage.appendInt(Integer.valueOf(getDelay()));
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(getType().code));
        serverMessage.appendInt(Integer.valueOf(getDelay()));
        if (!requiresTriggeringUser()) {
            serverMessage.appendInt((Integer) 0);
            return;
        }
        final ArrayList arrayList = new ArrayList();
        room.getRoomSpecialTypes().getTriggers(getX(), getY()).forEach(new TObjectProcedure<InteractionWiredTrigger>() { // from class: com.eu.habbo.habbohotel.items.interactions.wired.effects.WiredEffectResetTimers.1
            public boolean execute(InteractionWiredTrigger interactionWiredTrigger) {
                if (interactionWiredTrigger.isTriggeredByRoomUnit()) {
                    return true;
                }
                arrayList.add(Integer.valueOf(interactionWiredTrigger.getBaseItem().getSpriteId()));
                return true;
            }
        });
        serverMessage.appendInt(Integer.valueOf(arrayList.size()));
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            serverMessage.appendInt((Integer) it.next());
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect
    public boolean saveData(WiredSettings wiredSettings, GameClient gameClient) {
        setDelay(wiredSettings.getDelay());
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public boolean execute(RoomUnit roomUnit, Room room, Object[] objArr) {
        Emulator.getThreading().run(new WiredResetTimers(room), this.delay);
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.delay));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void loadWiredData(ResultSet resultSet, Room room) throws SQLException {
        String string = resultSet.getString("wired_data");
        if (string.startsWith("{")) {
            this.delay = ((JsonData) WiredHandler.getGsonBuilder().create().fromJson(string, JsonData.class)).delay;
        } else {
            try {
                if (!string.equals(Emulator.PREVIEW)) {
                    this.delay = Integer.parseInt(string);
                }
            } catch (Exception e) {
            }
        }
        setDelay(this.delay);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void onPickUp() {
        this.delay = 0;
        setDelay(0);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect
    public WiredEffectType getType() {
        return type;
    }
}
