package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.plugin.events.furniture.FurnitureToggleEvent;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/ToggleWallItemEvent.class */
public class ToggleWallItemEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null) {
            return;
        }
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        HabboItem habboItem = currentRoom.getHabboItem(iIntValue);
        if (habboItem == null) {
            return;
        }
        FurnitureToggleEvent furnitureToggleEvent = new FurnitureToggleEvent(habboItem, this.client.getHabbo(), iIntValue2);
        Emulator.getPluginManager().fireEvent(furnitureToggleEvent);
        if (furnitureToggleEvent.isCancelled() || habboItem.getBaseItem().getName().equalsIgnoreCase("poster")) {
            return;
        }
        habboItem.needsUpdate(true);
        habboItem.onClick(this.client, currentRoom, new Object[]{Integer.valueOf(iIntValue2)});
        currentRoom.updateItem(habboItem);
        Emulator.getThreading().run(habboItem);
    }
}
