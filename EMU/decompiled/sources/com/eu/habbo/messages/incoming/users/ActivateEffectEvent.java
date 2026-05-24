package com.eu.habbo.messages.incoming.users;

import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/users/ActivateEffectEvent.class */
public class ActivateEffectEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        if (this.client.getHabbo().getInventory().getEffectsComponent().ownsEffect(iIntValue)) {
            this.client.getHabbo().getInventory().getEffectsComponent().activateEffect(iIntValue);
        }
    }
}
