package com.eu.habbo.messages.incoming.hotelview;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.inventory.BadgesComponent;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.users.AddUserBadgeComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/hotelview/HotelViewClaimBadgeRewardEvent.class */
public class HotelViewClaimBadgeRewardEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        String string = this.packet.readString();
        if (Emulator.getConfig().getBoolean("hotelview.badgereward." + string + ".enabled")) {
            String value = Emulator.getConfig().getValue("hotelview.badgereward." + string + "badge");
            if (value.isEmpty() || this.client.getHabbo().getInventory().getBadgesComponent().hasBadge(value)) {
                return;
            }
            this.client.sendResponse(new AddUserBadgeComposer(BadgesComponent.createBadge(value, this.client.getHabbo())));
        }
    }
}
