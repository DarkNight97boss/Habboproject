package com.eu.habbo.messages.incoming.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.bots.BotManager;
import com.eu.habbo.habbohotel.catalog.CatalogItem;
import com.eu.habbo.habbohotel.catalog.CatalogManager;
import com.eu.habbo.habbohotel.catalog.CatalogPage;
import com.eu.habbo.habbohotel.catalog.CatalogPageLayouts;
import com.eu.habbo.habbohotel.catalog.ClubOffer;
import com.eu.habbo.habbohotel.catalog.layouts.BotsLayout;
import com.eu.habbo.habbohotel.catalog.layouts.ClubBuyLayout;
import com.eu.habbo.habbohotel.catalog.layouts.PetsLayout;
import com.eu.habbo.habbohotel.catalog.layouts.RecentPurchasesLayout;
import com.eu.habbo.habbohotel.catalog.layouts.RoomBundleLayout;
import com.eu.habbo.habbohotel.catalog.layouts.VipBuyLayout;
import com.eu.habbo.habbohotel.items.FurnitureType;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.pets.PetManager;
import com.eu.habbo.habbohotel.rooms.RoomManager;
import com.eu.habbo.habbohotel.users.HabboBadge;
import com.eu.habbo.habbohotel.users.HabboInventory;
import com.eu.habbo.habbohotel.users.subscriptions.Subscription;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.catalog.AlertPurchaseFailedComposer;
import com.eu.habbo.messages.outgoing.catalog.AlertPurchaseUnavailableComposer;
import com.eu.habbo.messages.outgoing.catalog.PurchaseOKComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;
import com.eu.habbo.messages.outgoing.generic.alerts.HotelWillCloseInMinutesComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import com.eu.habbo.messages.outgoing.navigator.CanCreateRoomComposer;
import com.eu.habbo.messages.outgoing.users.AddUserBadgeComposer;
import com.eu.habbo.threading.runnables.ShutdownEmulator;
import gnu.trove.map.hash.THashMap;
import gnu.trove.procedure.TObjectProcedure;
import org.apache.commons.lang3.StringUtils;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/catalog/CatalogBuyItemEvent.class */
public class CatalogBuyItemEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (((long) Emulator.getIntUnixTimestamp()) - this.client.getHabbo().getHabboStats().lastPurchaseTimestamp < CatalogManager.PURCHASE_COOLDOWN) {
            this.client.sendResponse(new AlertPurchaseFailedComposer(0).compose());
            return;
        }
        this.client.getHabbo().getHabboStats().lastPurchaseTimestamp = Emulator.getIntUnixTimestamp();
        if (ShutdownEmulator.timestamp > 0) {
            this.client.sendResponse(new HotelWillCloseInMinutesComposer((ShutdownEmulator.timestamp - Emulator.getIntUnixTimestamp()) / 60));
            return;
        }
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        String string = this.packet.readString();
        int iIntValue3 = this.packet.readInt().intValue();
        try {
            if (this.client.getHabbo().getInventory().getItemsComponent().itemCount() > HabboInventory.MAXIMUM_ITEMS) {
                this.client.sendResponse(new AlertPurchaseFailedComposer(0).compose());
                this.client.getHabbo().alert(Emulator.getTexts().getValue("inventory.full"));
                return;
            }
        } catch (Exception e) {
            this.client.sendResponse(new AlertPurchaseFailedComposer(0).compose());
        }
        CatalogPage catalogPage = null;
        if (iIntValue == -12345678 || iIntValue == -1) {
            CatalogItem catalogItem = Emulator.getGameEnvironment().getCatalogManager().getCatalogItem(iIntValue2);
            if (catalogItem.getOfferId() > 0) {
                catalogPage = Emulator.getGameEnvironment().getCatalogManager().getCatalogPage(catalogItem.getPageId());
                if (catalogPage != null) {
                    if (catalogPage.getCatalogItem(iIntValue2).getOfferId() <= 0 || catalogPage.getRank() > this.client.getHabbo().getHabboInfo().getRank().getId()) {
                        catalogPage = null;
                    } else if (catalogPage.getLayout() != null && catalogPage.getLayout().equalsIgnoreCase(CatalogPageLayouts.club_gift.name())) {
                        catalogPage = null;
                    }
                }
            }
        } else {
            catalogPage = (CatalogPage) Emulator.getGameEnvironment().getCatalogManager().catalogPages.get(iIntValue);
            if (catalogPage != null && catalogPage.getLayout() != null && catalogPage.getLayout().equalsIgnoreCase(CatalogPageLayouts.club_gift.name())) {
                catalogPage = null;
            }
            if (catalogPage instanceof RoomBundleLayout) {
                final CatalogItem[] catalogItemArr = new CatalogItem[1];
                catalogPage.getCatalogItems().forEachValue(new TObjectProcedure<CatalogItem>() { // from class: com.eu.habbo.messages.incoming.catalog.CatalogBuyItemEvent.1
                    public boolean execute(CatalogItem catalogItem2) {
                        catalogItemArr[0] = catalogItem2;
                        return false;
                    }
                });
                CatalogItem catalogItem2 = catalogItemArr[0];
                if (catalogItem2 == null || catalogItem2.getCredits() > this.client.getHabbo().getHabboInfo().getCredits() || catalogItem2.getPoints() > this.client.getHabbo().getHabboInfo().getCurrencyAmount(catalogItem2.getPointsType())) {
                    this.client.sendResponse(new AlertPurchaseFailedComposer(0));
                    return;
                }
                int size = Emulator.getGameEnvironment().getRoomManager().getRoomsForHabbo(this.client.getHabbo()).size();
                int i = this.client.getHabbo().getHabboStats().hasActiveClub() ? RoomManager.MAXIMUM_ROOMS_HC : RoomManager.MAXIMUM_ROOMS_USER;
                if (size >= i) {
                    this.client.sendResponse(new CanCreateRoomComposer(size, i));
                    this.client.sendResponse(new PurchaseOKComposer(null));
                    return;
                }
                ((RoomBundleLayout) catalogPage).buyRoom(this.client.getHabbo());
                if (!this.client.getHabbo().hasPermission(Permission.ACC_INFINITE_CREDITS)) {
                    this.client.getHabbo().giveCredits(-catalogItem2.getCredits());
                }
                if (!this.client.getHabbo().hasPermission(Permission.ACC_INFINITE_POINTS)) {
                    this.client.getHabbo().givePoints(catalogItem2.getPointsType(), -catalogItem2.getPoints());
                }
                this.client.sendResponse(new PurchaseOKComposer());
                boolean[] zArr = {false};
                catalogItemArr[0].getBaseItems().stream().filter(item -> {
                    return item.getType() == FurnitureType.BADGE;
                }).forEach(item2 -> {
                    if (this.client.getHabbo().getInventory().getBadgesComponent().hasBadge(item2.getName())) {
                        zArr[0] = true;
                        return;
                    }
                    HabboBadge habboBadge = new HabboBadge(0, item2.getName(), 0, this.client.getHabbo());
                    Emulator.getThreading().run(habboBadge);
                    this.client.getHabbo().getInventory().getBadgesComponent().addBadge(habboBadge);
                    this.client.sendResponse(new AddUserBadgeComposer(habboBadge));
                    THashMap tHashMap = new THashMap();
                    tHashMap.put("display", "BUBBLE");
                    tHashMap.put("image", "${image.library.url}album1584/" + habboBadge.getCode() + ".gif");
                    tHashMap.put("message", Emulator.getTexts().getValue("commands.generic.cmd_badge.received"));
                    this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.RECEIVED_BADGE.key, (THashMap<String, String>) tHashMap));
                });
                if (zArr[0]) {
                    this.client.getHabbo().getClient().sendResponse(new AlertPurchaseFailedComposer(1));
                    return;
                }
                return;
            }
        }
        if (catalogPage == null) {
            this.client.sendResponse(new AlertPurchaseFailedComposer(0).compose());
            return;
        }
        if (catalogPage.getRank() > this.client.getHabbo().getHabboInfo().getRank().getId()) {
            this.client.sendResponse(new AlertPurchaseUnavailableComposer(0));
            return;
        }
        if (!(catalogPage instanceof ClubBuyLayout) && !(catalogPage instanceof VipBuyLayout)) {
            CatalogItem catalogItem3 = catalogPage instanceof RecentPurchasesLayout ? (CatalogItem) this.client.getHabbo().getHabboStats().getRecentPurchases().get(Integer.valueOf(iIntValue2)) : catalogPage.getCatalogItem(iIntValue2);
            if ((catalogPage instanceof BotsLayout) && !this.client.getHabbo().hasPermission(Permission.ACC_UNLIMITED_BOTS) && this.client.getHabbo().getInventory().getBotsComponent().getBots().size() >= BotManager.MAXIMUM_BOT_INVENTORY_SIZE) {
                this.client.getHabbo().alert(Emulator.getTexts().getValue("error.bots.max.inventory").replace("%amount%", BotManager.MAXIMUM_BOT_INVENTORY_SIZE + Emulator.PREVIEW));
                return;
            }
            if (catalogPage instanceof PetsLayout) {
                if (!this.client.getHabbo().hasPermission(Permission.ACC_UNLIMITED_PETS) && this.client.getHabbo().getInventory().getPetsComponent().getPets().size() >= PetManager.MAXIMUM_PET_INVENTORY_SIZE) {
                    this.client.getHabbo().alert(Emulator.getTexts().getValue("error.pets.max.inventory").replace("%amount%", PetManager.MAXIMUM_PET_INVENTORY_SIZE + Emulator.PREVIEW));
                    return;
                }
                String[] strArrSplit = string.split("\n");
                if (strArrSplit.length != 3 || strArrSplit[0].length() < CheckPetNameEvent.PET_NAME_LENGTH_MINIMUM || strArrSplit[0].length() > CheckPetNameEvent.PET_NAME_LENGTH_MAXIMUM || !StringUtils.isAlphanumeric(strArrSplit[0])) {
                    return;
                }
            }
            Emulator.getGameEnvironment().getCatalogManager().purchaseItem(catalogPage, catalogItem3, this.client.getHabbo(), iIntValue3, string, false);
            return;
        }
        ClubOffer clubOffer = (ClubOffer) Emulator.getGameEnvironment().getCatalogManager().clubOffers.get(Integer.valueOf(iIntValue2));
        if (clubOffer == null) {
            this.client.sendResponse(new AlertPurchaseFailedComposer(0).compose());
            return;
        }
        int days = 0;
        int credits = 0;
        int points = 0;
        for (int i2 = 0; i2 < iIntValue3; i2++) {
            days += clubOffer.getDays();
            credits += clubOffer.getCredits();
            points += clubOffer.getPoints();
        }
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
