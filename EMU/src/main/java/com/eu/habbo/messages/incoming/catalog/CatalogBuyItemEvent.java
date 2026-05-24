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

public class CatalogBuyItemEvent extends MessageHandler {
   @Override
   public void handle() throws Exception {
      if (Emulator.getIntUnixTimestamp() - this.client.getHabbo().getHabboStats().lastPurchaseTimestamp >= CatalogManager.PURCHASE_COOLDOWN) {
         this.client.getHabbo().getHabboStats().lastPurchaseTimestamp = Emulator.getIntUnixTimestamp();
         if (ShutdownEmulator.timestamp > 0) {
            this.client.sendResponse(new HotelWillCloseInMinutesComposer((ShutdownEmulator.timestamp - Emulator.getIntUnixTimestamp()) / 60));
            return;
         }

         int pageId = this.packet.readInt();
         int itemId = this.packet.readInt();
         String extraData = this.packet.readString();
         int count = this.packet.readInt();

         try {
            if (this.client.getHabbo().getInventory().getItemsComponent().itemCount() > HabboInventory.MAXIMUM_ITEMS) {
               this.client.sendResponse(new AlertPurchaseFailedComposer(0).compose());
               this.client.getHabbo().alert(Emulator.getTexts().getValue("inventory.full"));
               return;
            }
         } catch (Exception e) {
            this.client.sendResponse(new AlertPurchaseFailedComposer(0).compose());
         }

         CatalogPage page = null;
         if (pageId != -12345678 && pageId != -1) {
            page = (CatalogPage)Emulator.getGameEnvironment().getCatalogManager().catalogPages.get(pageId);
            if (page != null && page.getLayout() != null && page.getLayout().equalsIgnoreCase(CatalogPageLayouts.club_gift.name())) {
               page = null;
            }

            if (page instanceof RoomBundleLayout) {
               final CatalogItem[] item = new CatalogItem[1];
               page.getCatalogItems().forEachValue(new TObjectProcedure<CatalogItem>() {
                  public boolean execute(CatalogItem object) {
                     item[0] = object;
                     return false;
                  }
               });
               CatalogItem roomBundleItem = item[0];
               if (roomBundleItem != null
                  && roomBundleItem.getCredits() <= this.client.getHabbo().getHabboInfo().getCredits()
                  && roomBundleItem.getPoints() <= this.client.getHabbo().getHabboInfo().getCurrencyAmount(roomBundleItem.getPointsType())) {
                  int roomCount = Emulator.getGameEnvironment().getRoomManager().getRoomsForHabbo(this.client.getHabbo()).size();
                  int maxRooms = this.client.getHabbo().getHabboStats().hasActiveClub() ? RoomManager.MAXIMUM_ROOMS_HC : RoomManager.MAXIMUM_ROOMS_USER;
                  if (roomCount >= maxRooms) {
                     this.client.sendResponse(new CanCreateRoomComposer(roomCount, maxRooms));
                     this.client.sendResponse(new PurchaseOKComposer(null));
                     return;
                  }

                  ((RoomBundleLayout)page).buyRoom(this.client.getHabbo());
                  if (!this.client.getHabbo().hasPermission(Permission.ACC_INFINITE_CREDITS)) {
                     this.client.getHabbo().giveCredits(-roomBundleItem.getCredits());
                  }

                  if (!this.client.getHabbo().hasPermission(Permission.ACC_INFINITE_POINTS)) {
                     this.client.getHabbo().givePoints(roomBundleItem.getPointsType(), -roomBundleItem.getPoints());
                  }

                  this.client.sendResponse(new PurchaseOKComposer());
                  boolean[] badgeFound = new boolean[]{false};
                  item[0].getBaseItems().stream().filter(ix -> ix.getType() == FurnitureType.BADGE).forEach(ix -> {
                     if (!this.client.getHabbo().getInventory().getBadgesComponent().hasBadge(ix.getName())) {
                        HabboBadge badge = new HabboBadge(0, ix.getName(), 0, this.client.getHabbo());
                        Emulator.getThreading().run(badge);
                        this.client.getHabbo().getInventory().getBadgesComponent().addBadge(badge);
                        this.client.sendResponse(new AddUserBadgeComposer(badge));
                        THashMap<String, String> keys = new THashMap();
                        keys.put("display", "BUBBLE");
                        keys.put("image", "${image.library.url}album1584/" + badge.getCode() + ".gif");
                        keys.put("message", Emulator.getTexts().getValue("commands.generic.cmd_badge.received"));
                        this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.RECEIVED_BADGE.key, keys));
                     } else {
                        badgeFound[0] = true;
                     }
                  });
                  if (badgeFound[0]) {
                     this.client.getHabbo().getClient().sendResponse(new AlertPurchaseFailedComposer(1));
                  }

                  return;
               }

               this.client.sendResponse(new AlertPurchaseFailedComposer(0));
               return;
            }
         } else {
            CatalogItem searchedItem = Emulator.getGameEnvironment().getCatalogManager().getCatalogItem(itemId);
            if (searchedItem.getOfferId() > 0) {
               page = Emulator.getGameEnvironment().getCatalogManager().getCatalogPage(searchedItem.getPageId());
               if (page != null) {
                  if (page.getCatalogItem(itemId).getOfferId() <= 0) {
                     page = null;
                  } else if (page.getRank() > this.client.getHabbo().getHabboInfo().getRank().getId()) {
                     page = null;
                  } else if (page.getLayout() != null && page.getLayout().equalsIgnoreCase(CatalogPageLayouts.club_gift.name())) {
                     page = null;
                  }
               }
            }
         }

         if (page == null) {
            this.client.sendResponse(new AlertPurchaseFailedComposer(0).compose());
            return;
         }

         if (page.getRank() > this.client.getHabbo().getHabboInfo().getRank().getId()) {
            this.client.sendResponse(new AlertPurchaseUnavailableComposer(0));
            return;
         }

         if (page instanceof ClubBuyLayout || page instanceof VipBuyLayout) {
            ClubOffer item = (ClubOffer)Emulator.getGameEnvironment().getCatalogManager().clubOffers.get(itemId);
            if (item == null) {
               this.client.sendResponse(new AlertPurchaseFailedComposer(0).compose());
               return;
            }

            int totalDays = 0;
            int totalCredits = 0;
            int totalDuckets = 0;

            for (int i = 0; i < count; i++) {
               totalDays += item.getDays();
               totalCredits += item.getCredits();
               totalDuckets += item.getPoints();
            }

            if (totalDays > 0) {
               if (this.client.getHabbo().getHabboInfo().getCurrencyAmount(item.getPointsType()) < totalDuckets) {
                  return;
               }

               if (this.client.getHabbo().getHabboInfo().getCredits() < totalCredits) {
                  return;
               }

               if (!this.client.getHabbo().hasPermission(Permission.ACC_INFINITE_CREDITS)) {
                  this.client.getHabbo().giveCredits(-totalCredits);
               }

               if (!this.client.getHabbo().hasPermission(Permission.ACC_INFINITE_POINTS)) {
                  this.client.getHabbo().givePoints(item.getPointsType(), -totalDuckets);
               }

               if (this.client.getHabbo().getHabboStats().createSubscription("HABBO_CLUB", totalDays * 86400) == null) {
                  this.client.sendResponse(new AlertPurchaseFailedComposer(0).compose());
                  throw new Exception("Unable to create or extend subscription");
               }

               this.client.sendResponse(new PurchaseOKComposer(null));
               this.client.sendResponse(new InventoryRefreshComposer());
               this.client.getHabbo().getHabboStats().run();
            }

            return;
         }

         CatalogItem item;
         if (page instanceof RecentPurchasesLayout) {
            item = (CatalogItem)this.client.getHabbo().getHabboStats().getRecentPurchases().get(itemId);
         } else {
            item = page.getCatalogItem(itemId);
         }

         if (page instanceof BotsLayout
            && !this.client.getHabbo().hasPermission(Permission.ACC_UNLIMITED_BOTS)
            && this.client.getHabbo().getInventory().getBotsComponent().getBots().size() >= BotManager.MAXIMUM_BOT_INVENTORY_SIZE) {
            this.client
               .getHabbo()
               .alert(Emulator.getTexts().getValue("error.bots.max.inventory").replace("%amount%", BotManager.MAXIMUM_BOT_INVENTORY_SIZE + ""));
            return;
         }

         if (page instanceof PetsLayout) {
            if (!this.client.getHabbo().hasPermission(Permission.ACC_UNLIMITED_PETS)
               && this.client.getHabbo().getInventory().getPetsComponent().getPets().size() >= PetManager.MAXIMUM_PET_INVENTORY_SIZE) {
               this.client
                  .getHabbo()
                  .alert(Emulator.getTexts().getValue("error.pets.max.inventory").replace("%amount%", PetManager.MAXIMUM_PET_INVENTORY_SIZE + ""));
               return;
            }

            String[] check = extraData.split("\n");
            if (check.length != 3
               || check[0].length() < CheckPetNameEvent.PET_NAME_LENGTH_MINIMUM
               || check[0].length() > CheckPetNameEvent.PET_NAME_LENGTH_MAXIMUM
               || !StringUtils.isAlphanumeric(check[0])) {
               return;
            }
         }

         Emulator.getGameEnvironment().getCatalogManager().purchaseItem(page, item, this.client.getHabbo(), count, extraData, false);
      } else {
         this.client.sendResponse(new AlertPurchaseFailedComposer(0).compose());
      }
   }
}
