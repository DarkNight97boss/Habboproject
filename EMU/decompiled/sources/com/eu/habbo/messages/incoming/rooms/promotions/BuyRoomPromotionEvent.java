package com.eu.habbo.messages.incoming.rooms.promotions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.CatalogItem;
import com.eu.habbo.habbohotel.catalog.CatalogPage;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomRightLevels;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.catalog.AlertPurchaseFailedComposer;
import com.eu.habbo.messages.outgoing.catalog.PurchaseOKComposer;
import com.eu.habbo.messages.outgoing.navigator.NewNavigatorEventCategoriesComposer;
import com.eu.habbo.messages.outgoing.rooms.promotions.RoomPromotionMessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/promotions/BuyRoomPromotionEvent.class */
public class BuyRoomPromotionEvent extends MessageHandler {
    public static String ROOM_PROMOTION_BADGE = "RADZZ";

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        CatalogPage catalogPage;
        CatalogItem catalogItem;
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        int iIntValue3 = this.packet.readInt().intValue();
        String string = this.packet.readString();
        this.packet.readBoolean();
        String string2 = this.packet.readString();
        int iIntValue4 = this.packet.readInt().intValue();
        if (NewNavigatorEventCategoriesComposer.CATEGORIES.stream().noneMatch(eventCategory -> {
            return eventCategory.getId() == iIntValue4;
        }) || (catalogPage = Emulator.getGameEnvironment().getCatalogManager().getCatalogPage(iIntValue)) == null || !catalogPage.getLayout().equals("roomads") || (catalogItem = catalogPage.getCatalogItem(iIntValue2)) == null || !this.client.getHabbo().getHabboInfo().canBuy(catalogItem)) {
            return;
        }
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(iIntValue3);
        if (room.isOwner(this.client.getHabbo()) || room.hasRights(this.client.getHabbo()) || room.getGuildRightLevel(this.client.getHabbo()).equals(RoomRightLevels.GUILD_ADMIN)) {
            if (room.isPromoted()) {
                room.getPromotion().addEndTimestamp(7200);
            } else {
                room.createPromotion(string, string2, iIntValue4);
            }
            if (!room.isPromoted()) {
                this.client.sendResponse(new AlertPurchaseFailedComposer(0));
                return;
            }
            if (!this.client.getHabbo().hasPermission(Permission.ACC_INFINITE_CREDITS)) {
                this.client.getHabbo().giveCredits(-catalogItem.getCredits());
            }
            if (!this.client.getHabbo().hasPermission(Permission.ACC_INFINITE_POINTS)) {
                this.client.getHabbo().givePoints(catalogItem.getPointsType(), -catalogItem.getPoints());
            }
            this.client.sendResponse(new PurchaseOKComposer());
            room.sendComposer(new RoomPromotionMessageComposer(room, room.getPromotion()).compose());
            if (this.client.getHabbo().getInventory().getBadgesComponent().hasBadge(ROOM_PROMOTION_BADGE)) {
                return;
            }
            this.client.getHabbo().addBadge(ROOM_PROMOTION_BADGE);
        }
    }
}
