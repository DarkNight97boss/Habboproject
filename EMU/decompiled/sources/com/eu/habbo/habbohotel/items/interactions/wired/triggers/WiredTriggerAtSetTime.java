package com.eu.habbo.habbohotel.items.interactions.wired.triggers;

import com.eu.habbo.Emulator;
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
import com.eu.habbo.threading.runnables.WiredExecuteTask;
import gnu.trove.procedure.TObjectProcedure;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/triggers/WiredTriggerAtSetTime.class */
public class WiredTriggerAtSetTime extends InteractionWiredTrigger implements WiredTriggerReset {
    public static final WiredTriggerType type = WiredTriggerType.AT_GIVEN_TIME;
    public int executeTime;
    public int taskId;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/triggers/WiredTriggerAtSetTime$JsonData.class */
    static class JsonData {
        int executeTime;

        public JsonData(int i) {
            this.executeTime = i;
        }
    }

    public WiredTriggerAtSetTime(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public WiredTriggerAtSetTime(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public boolean execute(RoomUnit roomUnit, Room room, Object[] objArr) {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.executeTime));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void loadWiredData(ResultSet resultSet, Room room) throws SQLException {
        String string = resultSet.getString("wired_data");
        if (string.startsWith("{")) {
            this.executeTime = ((JsonData) WiredHandler.getGsonBuilder().create().fromJson(string, JsonData.class)).executeTime;
        } else if (string.length() >= 1) {
            this.executeTime = Integer.parseInt(string);
        }
        if (this.executeTime < 500) {
            this.executeTime = 10000;
        }
        this.taskId = 1;
        Emulator.getThreading().run(new WiredExecuteTask(this, Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId())), this.executeTime);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void onPickUp() {
        this.executeTime = 0;
        this.taskId = 0;
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
        serverMessage.appendInt(Integer.valueOf(this.executeTime / 500));
        serverMessage.appendInt((Integer) 1);
        serverMessage.appendInt(Integer.valueOf(getType().code));
        if (isTriggeredByRoomUnit()) {
            serverMessage.appendInt((Integer) 0);
            return;
        }
        final ArrayList arrayList = new ArrayList();
        room.getRoomSpecialTypes().getEffects(getX(), getY()).forEach(new TObjectProcedure<InteractionWiredEffect>() { // from class: com.eu.habbo.habbohotel.items.interactions.wired.triggers.WiredTriggerAtSetTime.1
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
        this.executeTime = wiredSettings.getIntParams()[0] * 500;
        resetTimer();
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.wired.WiredTriggerReset
    public void resetTimer() {
        this.taskId++;
        Emulator.getThreading().run(new WiredExecuteTask(this, Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId())), this.executeTime);
    }
}
