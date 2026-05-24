package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.habbohotel.items.interactions.InteractionPostIt;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.items.PostItDataComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/PostItRequestDataEvent.class */
public class PostItRequestDataEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom != null) {
            HabboItem habboItem = currentRoom.getHabboItem(iIntValue);
            if (habboItem instanceof InteractionPostIt) {
                this.client.sendResponse(new PostItDataComposer((InteractionPostIt) habboItem));
            }
        }
    }
}
