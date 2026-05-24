package com.eu.habbo.messages.incoming.users;

import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/users/EnableEffectEvent.class */
public class EnableEffectEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        if (iIntValue > 0) {
            if (this.client.getHabbo().getInventory().getEffectsComponent().ownsEffect(iIntValue)) {
                this.client.getHabbo().getInventory().getEffectsComponent().enableEffect(iIntValue);
            }
        } else {
            this.client.getHabbo().getInventory().getEffectsComponent().activatedEffect = 0;
            if (this.client.getHabbo().getHabboInfo().getCurrentRoom() != null) {
                this.client.getHabbo().getHabboInfo().getCurrentRoom().giveEffect(this.client.getHabbo().getRoomUnit(), 0, -1);
            }
        }
    }
}
