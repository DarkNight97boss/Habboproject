package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionExternalImage;
import com.eu.habbo.habbohotel.items.interactions.InteractionPostIt;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.items.RemoveWallItemComposer;
import com.eu.habbo.threading.runnables.QueryDeleteHabboItem;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/PostItDeleteEvent.class */
public class PostItDeleteEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null) {
            return;
        }
        HabboItem habboItem = currentRoom.getHabboItem(iIntValue);
        if ((habboItem instanceof InteractionPostIt) || (habboItem instanceof InteractionExternalImage)) {
            if (habboItem.getUserId() == this.client.getHabbo().getHabboInfo().getId() || this.client.getHabbo().hasPermission(Permission.ACC_ANYROOMOWNER)) {
                habboItem.setRoomId(0);
                currentRoom.removeHabboItem(habboItem);
                currentRoom.sendComposer(new RemoveWallItemComposer(habboItem).compose());
                Emulator.getThreading().run(new QueryDeleteHabboItem(habboItem.getId()));
            }
        }
    }
}
