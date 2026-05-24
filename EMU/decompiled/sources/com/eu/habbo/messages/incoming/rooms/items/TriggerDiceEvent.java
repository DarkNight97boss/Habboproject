package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.habbohotel.items.interactions.InteractionDice;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/TriggerDiceEvent.class */
public class TriggerDiceEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        HabboItem habboItem;
        int iIntValue = this.packet.readInt().intValue();
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom != null && (habboItem = currentRoom.getHabboItem(iIntValue)) != null && (habboItem instanceof InteractionDice) && RoomLayout.tilesAdjecent(currentRoom.getLayout().getTile(habboItem.getX(), habboItem.getY()), this.client.getHabbo().getRoomUnit().getCurrentLocation())) {
            habboItem.onClick(this.client, currentRoom, new Object[0]);
        }
    }
}
