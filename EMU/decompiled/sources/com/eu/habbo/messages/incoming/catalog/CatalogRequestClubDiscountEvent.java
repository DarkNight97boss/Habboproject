package com.eu.habbo.messages.incoming.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.ClubOffer;
import com.eu.habbo.habbohotel.users.subscriptions.Subscription;
import com.eu.habbo.habbohotel.users.subscriptions.SubscriptionHabboClub;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.unknown.ExtendClubMessageComposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/catalog/CatalogRequestClubDiscountEvent.class */
public class CatalogRequestClubDiscountEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogRequestClubDiscountEvent.class);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        ClubOffer clubOffer;
        ClubOffer clubOfferOrElse;
        Subscription subscription = this.client.getHabbo().getHabboStats().getSubscription(Subscription.HABBO_CLUB);
        int iFloor = 0;
        int remaining = 0;
        if (subscription != null) {
            remaining = subscription.getRemaining();
            iFloor = (int) Math.floor(((double) remaining) / 86400.0d);
            int iCeil = (int) Math.ceil(((double) remaining) / 60.0d);
            if (iFloor < 1 && iCeil > 0) {
                iFloor = 1;
            }
        }
        if (remaining <= 0 || !SubscriptionHabboClub.DISCOUNT_ENABLED || iFloor > SubscriptionHabboClub.DISCOUNT_DAYS_BEFORE_END || (clubOffer = (ClubOffer) Emulator.getGameEnvironment().getCatalogManager().clubOffers.values().stream().filter((v0) -> {
            return v0.isDeal();
        }).findAny().orElse(null)) == null || (clubOfferOrElse = Emulator.getGameEnvironment().getCatalogManager().getClubOffers().stream().filter(clubOffer2 -> {
            return clubOffer2.getDays() == clubOffer.getDays();
        }).findAny().orElse(null)) == null) {
            return;
        }
        this.client.sendResponse(new ExtendClubMessageComposer(this.client.getHabbo(), clubOffer, clubOfferOrElse.getCredits(), clubOfferOrElse.getPoints(), clubOfferOrElse.getPointsType(), Math.max(0, iFloor)));
    }
}
