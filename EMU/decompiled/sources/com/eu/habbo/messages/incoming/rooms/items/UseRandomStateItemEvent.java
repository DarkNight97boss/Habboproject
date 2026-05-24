package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.habbohotel.items.interactions.InteractionRandomState;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/UseRandomStateItemEvent.class */
public class UseRandomStateItemEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(UseRandomStateItemEvent.class);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        try {
            int iIntValue = this.packet.readInt().intValue();
            this.packet.readInt().intValue();
            Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
            HabboItem habboItem = currentRoom.getHabboItem(iIntValue);
            if (habboItem == null || !(habboItem instanceof InteractionRandomState)) {
                return;
            }
            ((InteractionRandomState) habboItem).onRandomStateClick(this.client, currentRoom);
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
        }
    }
}
