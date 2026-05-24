package com.eu.habbo.messages.incoming.rooms.pets;

import com.eu.habbo.habbohotel.items.interactions.pets.InteractionPetBreedingNest;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/pets/StopBreedingEvent.class */
public class StopBreedingEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        HabboItem habboItem = this.client.getHabbo().getHabboInfo().getCurrentRoom().getHabboItem(this.packet.readInt().intValue());
        if (habboItem instanceof InteractionPetBreedingNest) {
            ((InteractionPetBreedingNest) habboItem).stopBreeding(this.client.getHabbo());
        }
    }
}
