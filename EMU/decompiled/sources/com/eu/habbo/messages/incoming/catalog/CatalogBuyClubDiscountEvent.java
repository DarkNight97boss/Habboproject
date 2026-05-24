package com.eu.habbo.messages.incoming.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.ClubOffer;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.users.subscriptions.Subscription;
import com.eu.habbo.habbohotel.users.subscriptions.SubscriptionHabboClub;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.catalog.AlertPurchaseFailedComposer;
import com.eu.habbo.messages.outgoing.catalog.PurchaseOKComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/catalog/CatalogBuyClubDiscountEvent.class */
public class CatalogBuyClubDiscountEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogBuyClubDiscountEvent.class);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        ClubOffer clubOffer;
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
        }).findAny().orElse(null)) == null || Emulator.getGameEnvironment().getCatalogManager().getClubOffers().stream().filter(clubOffer2 -> {
            return clubOffer2.getDays() == clubOffer.getDays();
        }).findAny().orElse(null) == null) {
            return;
        }
        int days = clubOffer.getDays();
        int credits = clubOffer.getCredits();
        int points = clubOffer.getPoints();
        if (days <= 0 || this.client.getHabbo().getHabboInfo().getCurrencyAmount(clubOffer.getPointsType()) < points || this.client.getHabbo().getHabboInfo().getCredits() < credits) {
            return;
        }
        if (!this.client.getHabbo().hasPermission(Permission.ACC_INFINITE_CREDITS)) {
            this.client.getHabbo().giveCredits(-credits);
        }
        if (!this.client.getHabbo().hasPermission(Permission.ACC_INFINITE_POINTS)) {
            this.client.getHabbo().givePoints(clubOffer.getPointsType(), -points);
        }
        if (this.client.getHabbo().getHabboStats().createSubscription(Subscription.HABBO_CLUB, days * 86400) == null) {
            this.client.sendResponse(new AlertPurchaseFailedComposer(0).compose());
            throw new Exception("Unable to create or extend subscription");
        }
        this.client.sendResponse(new PurchaseOKComposer(null));
        this.client.sendResponse(new InventoryRefreshComposer());
        this.client.getHabbo().getHabboStats().run();
    }
}
