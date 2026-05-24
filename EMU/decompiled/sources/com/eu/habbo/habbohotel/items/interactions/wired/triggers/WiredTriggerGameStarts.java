package com.eu.habbo.habbohotel.items.interactions.wired.triggers;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.procedure.TObjectProcedure;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/triggers/WiredTriggerGameStarts.class */
public class WiredTriggerGameStarts extends InteractionWiredTrigger {
    private static final WiredTriggerType type = WiredTriggerType.GAME_STARTS;

    public WiredTriggerGameStarts(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public WiredTriggerGameStarts(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public boolean execute(RoomUnit roomUnit, Room room, Object[] objArr) {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public String getWiredData() {
        return Emulator.PREVIEW;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void loadWiredData(ResultSet resultSet, Room room) throws SQLException {
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void onPickUp() {
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
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(type.code));
        if (isTriggeredByRoomUnit()) {
            serverMessage.appendInt((Integer) 0);
            return;
        }
        final ArrayList arrayList = new ArrayList();
        room.getRoomSpecialTypes().getEffects(getX(), getY()).forEach(new TObjectProcedure<InteractionWiredEffect>() { // from class: com.eu.habbo.habbohotel.items.interactions.wired.triggers.WiredTriggerGameStarts.1
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
        return true;
    }
}
