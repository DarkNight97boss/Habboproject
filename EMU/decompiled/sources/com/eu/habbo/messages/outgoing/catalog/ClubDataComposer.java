package com.eu.habbo.messages.outgoing.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.ClubOffer;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/catalog/ClubDataComposer.class */
public class ClubDataComposer extends MessageComposer {
    private final int windowId;
    private final Habbo habbo;

    public ClubDataComposer(Habbo habbo, int i) {
        this.habbo = habbo;
        this.windowId = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.ClubDataComposer);
        List<ClubOffer> clubOffers = Emulator.getGameEnvironment().getCatalogManager().getClubOffers();
        this.response.appendInt(Integer.valueOf(clubOffers.size()));
        Iterator<ClubOffer> it = clubOffers.iterator();
        while (it.hasNext()) {
            it.next().serialize(this.response, this.habbo.getHabboStats().getClubExpireTimestamp());
        }
        this.response.appendInt(Integer.valueOf(this.windowId));
        return this.response;
    }
}
