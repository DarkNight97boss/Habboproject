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

public class BuyRoomPromotionEvent extends MessageHandler {
    public static String ROOM_PROMOTION_BADGE = "RADZZ";

    @Override
    public void handle() throws Exception {
        int pageId = this.packet.readInt();
        int itemId = this.packet.readInt();
        int roomId = this.packet.readInt();
        String title = this.packet.readString();
        boolean extendedPromotion = this.packet.readBoolean();
        String description = this.packet.readString();
        int categoryId = this.packet.readInt();

        if (NewNavigatorEventCategoriesComposer.CATEGORIES.stream().noneMatch(c -> c.getId() == categoryId))
            return;

        CatalogPage page = Emulator.getGameEnvironment().getCatalogManager().getCatalogPage(pageId);

        if (page == null || !page.getLayout().equals("roomads"))
            return;

        CatalogItem item = page.getCatalogItem(itemId);
        if (item == null) return;

        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(roomId);
        if (room == null) return; // null-check: previously could NPE.

        // Atomic price + promotion update. Two concurrent buys with just-enough
        // balance would otherwise both pass canBuy(), both extend the promotion,
        // and only one pay (the second giveCredits(-cost) would clamp at 0).
        synchronized (room) {
            if (!(room.isOwner(this.client.getHabbo()) || room.hasRights(this.client.getHabbo()) || room.getGuildRightLevel(this.client.getHabbo()).equals(RoomRightLevels.GUILD_ADMIN))) {
                return;
            }
            // Re-check canBuy INSIDE the lock so we can charge atomically.
            if (!this.client.getHabbo().getHabboInfo().canBuy(item)) {
                this.client.sendResponse(new AlertPurchaseFailedComposer(AlertPurchaseFailedComposer.SERVER_ERROR));
                return;
            }

            // Pay FIRST, then apply promotion. This way, if any spending step is
            // bypassed (perm), the promo still applies, but we never apply a promo
            // without having charged the user.
            if (!this.client.getHabbo().hasPermission(Permission.ACC_INFINITE_CREDITS)) {
                this.client.getHabbo().giveCredits(-item.getCredits());
            }
            if (!this.client.getHabbo().hasPermission(Permission.ACC_INFINITE_POINTS)) {
                this.client.getHabbo().givePoints(item.getPointsType(), -item.getPoints());
            }

            if (room.isPromoted()) {
                room.getPromotion().addEndTimestamp(120 * 60);
            } else {
                room.createPromotion(title, description, categoryId);
            }

            if (room.isPromoted()) {
                this.client.sendResponse(new PurchaseOKComposer());
                room.sendComposer(new RoomPromotionMessageComposer(room, room.getPromotion()).compose());

                if (!this.client.getHabbo().getInventory().getBadgesComponent().hasBadge(BuyRoomPromotionEvent.ROOM_PROMOTION_BADGE)) {
                    this.client.getHabbo().addBadge(BuyRoomPromotionEvent.ROOM_PROMOTION_BADGE);
                }
            } else {
                this.client.sendResponse(new AlertPurchaseFailedComposer(AlertPurchaseFailedComposer.SERVER_ERROR));
            }
        }
    }
}
