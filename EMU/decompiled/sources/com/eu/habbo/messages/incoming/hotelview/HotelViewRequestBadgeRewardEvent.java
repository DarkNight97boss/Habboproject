package com.eu.habbo.messages.incoming.hotelview;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.hotelview.HotelViewBadgeButtonConfigComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/hotelview/HotelViewRequestBadgeRewardEvent.class */
public class HotelViewRequestBadgeRewardEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        String string = this.packet.readString();
        this.client.sendResponse(new HotelViewBadgeButtonConfigComposer(Emulator.getConfig().getValue("hotelview.badgereward." + string + ".badge"), Emulator.getConfig().getBoolean("hotelview.badgereward." + string + ".enabled")));
    }
}
