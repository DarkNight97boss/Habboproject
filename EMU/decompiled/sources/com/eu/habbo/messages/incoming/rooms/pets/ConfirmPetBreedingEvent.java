package com.eu.habbo.messages.incoming.rooms.pets;

import com.eu.habbo.habbohotel.items.interactions.pets.InteractionPetBreedingNest;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/pets/ConfirmPetBreedingEvent.class */
public class ConfirmPetBreedingEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        String string = this.packet.readString();
        int iIntValue2 = this.packet.readInt().intValue();
        int iIntValue3 = this.packet.readInt().intValue();
        HabboItem habboItem = this.client.getHabbo().getHabboInfo().getCurrentRoom().getHabboItem(iIntValue);
        if (habboItem instanceof InteractionPetBreedingNest) {
            ((InteractionPetBreedingNest) habboItem).breed(this.client.getHabbo(), string, iIntValue2, iIntValue3);
        }
    }
}
