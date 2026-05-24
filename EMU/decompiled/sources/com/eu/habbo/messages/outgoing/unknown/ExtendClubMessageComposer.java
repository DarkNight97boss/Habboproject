package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.habbohotel.catalog.ClubOffer;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/ExtendClubMessageComposer.class */
public class ExtendClubMessageComposer extends MessageComposer {
    private final Habbo habbo;
    private final ClubOffer offer;
    private final int normalCreditCost;
    private final int normalPointsCost;
    private final int pointsType;
    private final int daysRemaining;

    public ExtendClubMessageComposer(Habbo habbo, ClubOffer clubOffer, int i, int i2, int i3, int i4) {
        this.habbo = habbo;
        this.offer = clubOffer;
        this.normalCreditCost = i;
        this.normalPointsCost = i2;
        this.pointsType = i3;
        this.daysRemaining = i4;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(3964);
        this.offer.serialize(this.response, this.habbo.getHabboStats().getClubExpireTimestamp());
        this.response.appendInt(Integer.valueOf(this.normalCreditCost));
        this.response.appendInt(Integer.valueOf(this.normalPointsCost));
        this.response.appendInt(Integer.valueOf(this.pointsType));
        this.response.appendInt(Integer.valueOf(this.daysRemaining));
        return this.response;
    }
}
