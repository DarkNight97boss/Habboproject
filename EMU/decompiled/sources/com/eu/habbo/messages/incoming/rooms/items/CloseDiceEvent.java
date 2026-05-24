package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionDice;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/CloseDiceEvent.class */
public class CloseDiceEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        HabboItem habboItem;
        int iIntValue = this.packet.readInt().intValue();
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null || (habboItem = currentRoom.getHabboItem(iIntValue)) == null) {
            return;
        }
        if (!(habboItem instanceof InteractionDice)) {
            ScripterManager.scripterDetected(this.client, Emulator.getTexts().getValue("scripter.warning.packet.closedice").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()).replace("%id%", habboItem.getId() + Emulator.PREVIEW).replace("%itemname%", habboItem.getBaseItem().getName()));
            return;
        }
        if (!RoomLayout.tilesAdjecent(currentRoom.getLayout().getTile(habboItem.getX(), habboItem.getY()), this.client.getHabbo().getRoomUnit().getCurrentLocation()) || habboItem.getExtradata().equals("-1")) {
            return;
        }
        habboItem.setExtradata("0");
        habboItem.needsUpdate(true);
        Emulator.getThreading().run(habboItem);
        currentRoom.updateItem(habboItem);
    }
}
