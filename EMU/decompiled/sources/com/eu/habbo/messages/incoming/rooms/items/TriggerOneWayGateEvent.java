package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.habbohotel.items.interactions.InteractionOneWayGate;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/TriggerOneWayGateEvent.class */
public class TriggerOneWayGateEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (this.client.getHabbo().getHabboInfo().getCurrentRoom() == null) {
            return;
        }
        HabboItem habboItem = this.client.getHabbo().getHabboInfo().getCurrentRoom().getHabboItem(this.packet.readInt().intValue());
        if (habboItem != null && (habboItem instanceof InteractionOneWayGate) && habboItem.getExtradata().equals("0") && !this.client.getHabbo().getRoomUnit().isTeleporting) {
            habboItem.onClick(this.client, this.client.getHabbo().getHabboInfo().getCurrentRoom(), null);
        }
    }
}
