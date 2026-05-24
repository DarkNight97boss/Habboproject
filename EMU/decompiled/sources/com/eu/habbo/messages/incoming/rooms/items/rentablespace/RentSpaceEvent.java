package com.eu.habbo.messages.incoming.rooms.items.rentablespace;

import com.eu.habbo.habbohotel.items.interactions.InteractionRentableSpace;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/rentablespace/RentSpaceEvent.class */
public class RentSpaceEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null) {
            return;
        }
        HabboItem habboItem = currentRoom.getHabboItem(iIntValue);
        if (habboItem instanceof InteractionRentableSpace) {
            ((InteractionRentableSpace) habboItem).rent(this.client.getHabbo());
            currentRoom.updateItem(habboItem);
            ((InteractionRentableSpace) habboItem).sendRentWidget(this.client.getHabbo());
        }
    }
}
