package com.eu.habbo.messages.incoming.catalog;

import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.catalog.ClubGiftsComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/catalog/RequestClubGiftsEvent.class */
public class RequestClubGiftsEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        this.client.sendResponse(new ClubGiftsComposer((int) Math.floor(((double) this.client.getHabbo().getHabboStats().getTimeTillNextClubGift()) / 86400.0d), this.client.getHabbo().getHabboStats().getRemainingClubGifts(), (int) Math.floor(((double) this.client.getHabbo().getHabboStats().getPastTimeAsClub()) / 86400.0d)));
    }
}
