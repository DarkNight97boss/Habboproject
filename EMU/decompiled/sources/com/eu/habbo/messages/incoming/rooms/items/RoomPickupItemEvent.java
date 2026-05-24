package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.habbohotel.items.interactions.InteractionPostIt;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/RoomPickupItemEvent.class */
public class RoomPickupItemEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        HabboItem habboItem;
        this.packet.readInt().intValue();
        int iIntValue = this.packet.readInt().intValue();
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null || (habboItem = currentRoom.getHabboItem(iIntValue)) == null || (habboItem instanceof InteractionPostIt)) {
            return;
        }
        if (habboItem.getUserId() == this.client.getHabbo().getHabboInfo().getId()) {
            currentRoom.pickUpItem(habboItem, this.client.getHabbo());
            return;
        }
        if (currentRoom.hasRights(this.client.getHabbo())) {
            if (this.client.getHabbo().hasPermission(Permission.ACC_ANYROOMOWNER)) {
                habboItem.setUserId(this.client.getHabbo().getHabboInfo().getId());
            } else if (this.client.getHabbo().getHabboInfo().getId() != currentRoom.getOwnerId() && habboItem.getUserId() == currentRoom.getOwnerId()) {
                return;
            }
            currentRoom.ejectUserItem(habboItem);
        }
    }
}
