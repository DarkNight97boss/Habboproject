package com.eu.habbo.messages.incoming.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.NewUserGift;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.plugin.events.users.UserPickGiftEvent;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/users/PickNewUserGiftEvent.class */
public class PickNewUserGiftEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        this.packet.readInt().intValue();
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        int iIntValue3 = this.packet.readInt().intValue();
        if (((UserPickGiftEvent) Emulator.getPluginManager().fireEvent(new UserPickGiftEvent(this.client.getHabbo(), iIntValue, iIntValue2, iIntValue3))).isCancelled() || this.client.getHabbo().getHabboStats().nuxReward || !Emulator.getConfig().getBoolean("hotel.nux.gifts.enabled")) {
            return;
        }
        this.client.getHabbo().getHabboStats().nuxReward = true;
        NewUserGift newUserGift = Emulator.getGameEnvironment().getItemManager().getNewUserGift(iIntValue3 + 1);
        if (newUserGift != null) {
            newUserGift.give(this.client.getHabbo());
        }
    }
}
