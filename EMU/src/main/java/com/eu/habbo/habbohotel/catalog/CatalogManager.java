package com.eu.habbo.habbohotel.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.catalog.layouts.BadgeDisplayLayout;
import com.eu.habbo.habbohotel.catalog.layouts.BotsLayout;
import com.eu.habbo.habbohotel.catalog.layouts.BuildersClubAddonsLayout;
import com.eu.habbo.habbohotel.catalog.layouts.BuildersClubFrontPageLayout;
import com.eu.habbo.habbohotel.catalog.layouts.BuildersClubLoyaltyLayout;
import com.eu.habbo.habbohotel.catalog.layouts.CatalogRootLayout;
import com.eu.habbo.habbohotel.catalog.layouts.ClubBuyLayout;
import com.eu.habbo.habbohotel.catalog.layouts.ClubGiftsLayout;
import com.eu.habbo.habbohotel.catalog.layouts.ColorGroupingLayout;
import com.eu.habbo.habbohotel.catalog.layouts.Default_3x3Layout;
import com.eu.habbo.habbohotel.catalog.layouts.FrontPageFeaturedLayout;
import com.eu.habbo.habbohotel.catalog.layouts.FrontpageLayout;
import com.eu.habbo.habbohotel.catalog.layouts.GuildForumLayout;
import com.eu.habbo.habbohotel.catalog.layouts.GuildFrontpageLayout;
import com.eu.habbo.habbohotel.catalog.layouts.GuildFurnitureLayout;
import com.eu.habbo.habbohotel.catalog.layouts.InfoDucketsLayout;
import com.eu.habbo.habbohotel.catalog.layouts.InfoLoyaltyLayout;
import com.eu.habbo.habbohotel.catalog.layouts.InfoMonkeyLayout;
import com.eu.habbo.habbohotel.catalog.layouts.InfoNikoLayout;
import com.eu.habbo.habbohotel.catalog.layouts.InfoPetsLayout;
import com.eu.habbo.habbohotel.catalog.layouts.InfoRentablesLayout;
import com.eu.habbo.habbohotel.catalog.layouts.LoyaltyVipBuyLayout;
import com.eu.habbo.habbohotel.catalog.layouts.MadMoneyLayout;
import com.eu.habbo.habbohotel.catalog.layouts.MarketplaceLayout;
import com.eu.habbo.habbohotel.catalog.layouts.MarketplaceOwnItems;
import com.eu.habbo.habbohotel.catalog.layouts.PetCustomizationLayout;
import com.eu.habbo.habbohotel.catalog.layouts.Pets2Layout;
import com.eu.habbo.habbohotel.catalog.layouts.Pets3Layout;
import com.eu.habbo.habbohotel.catalog.layouts.PetsLayout;
import com.eu.habbo.habbohotel.catalog.layouts.RecentPurchasesLayout;
import com.eu.habbo.habbohotel.catalog.layouts.RecyclerInfoLayout;
import com.eu.habbo.habbohotel.catalog.layouts.RecyclerLayout;
import com.eu.habbo.habbohotel.catalog.layouts.RecyclerPrizesLayout;
import com.eu.habbo.habbohotel.catalog.layouts.RoomAdsLayout;
import com.eu.habbo.habbohotel.catalog.layouts.RoomBundleLayout;
import com.eu.habbo.habbohotel.catalog.layouts.SingleBundle;
import com.eu.habbo.habbohotel.catalog.layouts.SoldLTDItemsLayout;
import com.eu.habbo.habbohotel.catalog.layouts.SpacesLayout;
import com.eu.habbo.habbohotel.catalog.layouts.TraxLayout;
import com.eu.habbo.habbohotel.catalog.layouts.TrophiesLayout;
import com.eu.habbo.habbohotel.catalog.layouts.VipBuyLayout;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.items.FurnitureType;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.SoundTrack;
import com.eu.habbo.habbohotel.items.interactions.InteractionBadgeDisplay;
import com.eu.habbo.habbohotel.items.interactions.InteractionGuildFurni;
import com.eu.habbo.habbohotel.items.interactions.InteractionGuildGate;
import com.eu.habbo.habbohotel.items.interactions.InteractionHopper;
import com.eu.habbo.habbohotel.items.interactions.InteractionMusicDisc;
import com.eu.habbo.habbohotel.items.interactions.InteractionTeleport;
import com.eu.habbo.habbohotel.items.interactions.InteractionTrophy;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboBadge;
import com.eu.habbo.habbohotel.users.HabboGender;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.catalog.AlertLimitedSoldOutComposer;
import com.eu.habbo.messages.outgoing.catalog.AlertPurchaseFailedComposer;
import com.eu.habbo.messages.outgoing.catalog.AlertPurchaseUnavailableComposer;
import com.eu.habbo.messages.outgoing.catalog.DiscountComposer;
import com.eu.habbo.messages.outgoing.catalog.PetBoughtNotificationComposer;
import com.eu.habbo.messages.outgoing.catalog.PurchaseOKComposer;
import com.eu.habbo.messages.outgoing.catalog.RedeemVoucherErrorComposer;
import com.eu.habbo.messages.outgoing.catalog.RedeemVoucherOKComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;
import com.eu.habbo.messages.outgoing.inventory.AddBotComposer;
import com.eu.habbo.messages.outgoing.inventory.AddHabboItemComposer;
import com.eu.habbo.messages.outgoing.inventory.AddPetComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import com.eu.habbo.messages.outgoing.modtool.ModToolIssueHandledComposer;
import com.eu.habbo.messages.outgoing.users.AddUserBadgeComposer;
import com.eu.habbo.plugin.events.emulator.EmulatorLoadCatalogManagerEvent;
import com.eu.habbo.plugin.events.users.catalog.UserCatalogFurnitureBoughtEvent;
import com.eu.habbo.plugin.events.users.catalog.UserCatalogItemPurchasedEvent;
import gnu.trove.TCollections;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.hash.THashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CatalogManager {
   private static final Logger LOGGER = LoggerFactory.getLogger(CatalogManager.class);
   public static final THashMap<String, Class<? extends CatalogPage>> pageDefinitions = new THashMap<String, Class<? extends CatalogPage>>(
      CatalogPageLayouts.values().length
   ) {
      {
         for (CatalogPageLayouts layout : CatalogPageLayouts.values()) {
            switch (layout) {
               case frontpage:
                  this.put(layout.name().toLowerCase(), FrontpageLayout.class);
                  break;
               case badge_display:
                  this.put(layout.name().toLowerCase(), BadgeDisplayLayout.class);
                  break;
               case spaces_new:
                  this.put(layout.name().toLowerCase(), SpacesLayout.class);
                  break;
               case trophies:
                  this.put(layout.name().toLowerCase(), TrophiesLayout.class);
                  break;
               case bots:
                  this.put(layout.name().toLowerCase(), BotsLayout.class);
                  break;
               case club_buy:
                  this.put(layout.name().toLowerCase(), ClubBuyLayout.class);
                  break;
               case club_gift:
                  this.put(layout.name().toLowerCase(), ClubGiftsLayout.class);
                  break;
               case sold_ltd_items:
                  this.put(layout.name().toLowerCase(), SoldLTDItemsLayout.class);
                  break;
               case single_bundle:
                  this.put(layout.name().toLowerCase(), SingleBundle.class);
                  break;
               case roomads:
                  this.put(layout.name().toLowerCase(), RoomAdsLayout.class);
                  break;
               case recycler:
                  if (Emulator.getConfig().getBoolean("hotel.ecotron.enabled")) {
                     this.put(layout.name().toLowerCase(), RecyclerLayout.class);
                  }
                  break;
               case recycler_info:
                  if (Emulator.getConfig().getBoolean("hotel.ecotron.enabled")) {
                     this.put(layout.name().toLowerCase(), RecyclerInfoLayout.class);
                  }
               case recycler_prizes:
                  if (Emulator.getConfig().getBoolean("hotel.ecotron.enabled")) {
                     this.put(layout.name().toLowerCase(), RecyclerPrizesLayout.class);
                  }
                  break;
               case marketplace:
                  if (Emulator.getConfig().getBoolean("hotel.marketplace.enabled")) {
                     this.put(layout.name().toLowerCase(), MarketplaceLayout.class);
                  }
                  break;
               case marketplace_own_items:
                  if (Emulator.getConfig().getBoolean("hotel.marketplace.enabled")) {
                     this.put(layout.name().toLowerCase(), MarketplaceOwnItems.class);
                  }
                  break;
               case info_duckets:
                  this.put(layout.name().toLowerCase(), InfoDucketsLayout.class);
                  break;
               case info_pets:
                  this.put(layout.name().toLowerCase(), InfoPetsLayout.class);
                  break;
               case info_rentables:
                  this.put(layout.name().toLowerCase(), InfoRentablesLayout.class);
                  break;
               case info_loyalty:
                  this.put(layout.name().toLowerCase(), InfoLoyaltyLayout.class);
                  break;
               case loyalty_vip_buy:
                  this.put(layout.name().toLowerCase(), LoyaltyVipBuyLayout.class);
                  break;
               case guilds:
                  this.put(layout.name().toLowerCase(), GuildFrontpageLayout.class);
                  break;
               case guild_furni:
                  this.put(layout.name().toLowerCase(), GuildFurnitureLayout.class);
                  break;
               case guild_forum:
                  this.put(layout.name().toLowerCase(), GuildForumLayout.class);
                  break;
               case pets:
                  this.put(layout.name().toLowerCase(), PetsLayout.class);
                  break;
               case pets2:
                  this.put(layout.name().toLowerCase(), Pets2Layout.class);
                  break;
               case pets3:
                  this.put(layout.name().toLowerCase(), Pets3Layout.class);
                  break;
               case soundmachine:
                  this.put(layout.name().toLowerCase(), TraxLayout.class);
                  break;
               case default_3x3_color_grouping:
                  this.put(layout.name().toLowerCase(), ColorGroupingLayout.class);
                  break;
               case recent_purchases:
                  this.put(layout.name().toLowerCase(), RecentPurchasesLayout.class);
                  break;
               case room_bundle:
                  this.put(layout.name().toLowerCase(), RoomBundleLayout.class);
                  break;
               case petcustomization:
                  this.put(layout.name().toLowerCase(), PetCustomizationLayout.class);
                  break;
               case vip_buy:
                  this.put(layout.name().toLowerCase(), VipBuyLayout.class);
                  break;
               case frontpage_featured:
                  this.put(layout.name().toLowerCase(), FrontPageFeaturedLayout.class);
                  break;
               case builders_club_addons:
                  this.put(layout.name().toLowerCase(), BuildersClubAddonsLayout.class);
                  break;
               case builders_club_frontpage:
                  this.put(layout.name().toLowerCase(), BuildersClubFrontPageLayout.class);
                  break;
               case builders_club_loyalty:
                  this.put(layout.name().toLowerCase(), BuildersClubLoyaltyLayout.class);
                  break;
               case monkey:
                  this.put(layout.name().toLowerCase(), InfoMonkeyLayout.class);
                  break;
               case niko:
                  this.put(layout.name().toLowerCase(), InfoNikoLayout.class);
                  break;
               case mad_money:
                  this.put(layout.name().toLowerCase(), MadMoneyLayout.class);
                  break;
               case default_3x3:
               default:
                  this.put("default_3x3", Default_3x3Layout.class);
            }
         }
      }
   };
   public static int catalogItemAmount;
   public static int PURCHASE_COOLDOWN = 1;
   public static boolean SORT_USING_ORDERNUM = false;
   public final TIntObjectMap<CatalogPage> catalogPages;
   public final TIntObjectMap<CatalogFeaturedPage> catalogFeaturedPages;
   public final THashMap<Integer, THashSet<Item>> prizes;
   public final THashMap<Integer, Integer> giftWrappers;
   public final THashMap<Integer, Integer> giftFurnis;
   public final THashSet<CatalogItem> clubItems;
   public final THashMap<Integer, ClubOffer> clubOffers;
   public final THashMap<Integer, TargetOffer> targetOffers;
   public final THashMap<Integer, ClothItem> clothing;
   public final TIntIntHashMap offerDefs;
   public final Item ecotronItem;
   public final THashMap<Integer, CatalogLimitedConfiguration> limitedNumbers;
   private final List<Voucher> vouchers;

   public CatalogManager() {
      long millis = System.currentTimeMillis();
      this.catalogPages = TCollections.synchronizedMap(new TIntObjectHashMap());
      this.catalogFeaturedPages = new TIntObjectHashMap();
      this.prizes = new THashMap();
      this.giftWrappers = new THashMap();
      this.giftFurnis = new THashMap();
      this.clubItems = new THashSet();
      this.clubOffers = new THashMap();
      this.targetOffers = new THashMap();
      this.clothing = new THashMap();
      this.offerDefs = new TIntIntHashMap();
      this.vouchers = new ArrayList<>();
      this.limitedNumbers = new THashMap();
      this.initialize();
      this.ecotronItem = Emulator.getGameEnvironment().getItemManager().getItem("ecotron_box");
      LOGGER.info("Catalog Manager -> Loaded! (" + (System.currentTimeMillis() - millis) + " MS)");
   }

   public synchronized void initialize() {
      Emulator.getPluginManager().fireEvent(new EmulatorLoadCatalogManagerEvent());
      this.loadLimitedNumbers();
      this.loadCatalogPages();
      this.loadCatalogFeaturedPages();
      this.loadCatalogItems();
      this.loadClubOffers();
      this.loadTargetOffers();
      this.loadVouchers();
      this.loadClothing();
      this.loadRecycler();
      this.loadGiftWrappers();
   }

   private synchronized void loadLimitedNumbers() {
      this.limitedNumbers.clear();
      THashMap<Integer, LinkedList<Integer>> limiteds = new THashMap();
      TIntIntHashMap totals = new TIntIntHashMap();

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM catalog_items_limited");

            try {
               ResultSet set = statement.executeQuery();

               try {
                  while (set.next()) {
                     if (!limiteds.containsKey(set.getInt("catalog_item_id"))) {
                        limiteds.put(set.getInt("catalog_item_id"), new LinkedList());
                     }

                     totals.adjustOrPutValue(set.getInt("catalog_item_id"), 1, 1);
                     if (set.getInt("user_id") == 0) {
                        ((LinkedList)limiteds.get(set.getInt("catalog_item_id"))).push(set.getInt("number"));
                     }
                  }
               } catch (Throwable var11) {
                  if (set != null) {
                     try {
                        set.close();
                     } catch (Throwable var10) {
                        var11.addSuppressed(var10);
                     }
                  }

                  throw var11;
               }

               if (set != null) {
                  set.close();
               }
            } catch (Throwable var12) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var9) {
                     var12.addSuppressed(var9);
                  }
               }

               throw var12;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var13) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var8) {
                  var13.addSuppressed(var8);
               }
            }

            throw var13;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }

      for (Entry<Integer, LinkedList<Integer>> set : limiteds.entrySet()) {
         this.limitedNumbers.put(set.getKey(), new CatalogLimitedConfiguration(set.getKey(), set.getValue(), totals.get(set.getKey())));
      }
   }

   private synchronized void loadCatalogPages() {
      this.catalogPages.clear();
      THashMap<Integer, CatalogPage> pages = new THashMap();
      pages.put(-1, new CatalogRootLayout());

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM catalog_pages ORDER BY parent_id, id");

            try {
               ResultSet set = statement.executeQuery();

               try {
                  while (set.next()) {
                     Class<? extends CatalogPage> pageClazz = (Class<? extends CatalogPage>)pageDefinitions.get(set.getString("page_layout"));
                     if (pageClazz == null) {
                        LOGGER.info("Unknown Page Layout: " + set.getString("page_layout"));
                     } else {
                        try {
                           CatalogPage page = pageClazz.getConstructor(ResultSet.class).newInstance(set);
                           pages.put(page.getId(), page);
                        } catch (Exception e) {
                           LOGGER.error("Failed to load layout: {}", set.getString("page_layout"));
                        }
                     }
                  }
               } catch (Throwable var11) {
                  if (set != null) {
                     try {
                        set.close();
                     } catch (Throwable var9) {
                        var11.addSuppressed(var9);
                     }
                  }

                  throw var11;
               }

               if (set != null) {
                  set.close();
               }
            } catch (Throwable var12) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var8) {
                     var12.addSuppressed(var8);
                  }
               }

               throw var12;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var13) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var7) {
                  var13.addSuppressed(var7);
               }
            }

            throw var13;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }

      pages.forEachValue(object -> {
         CatalogPage page = (CatalogPage)pages.get(object.parentId);
         if (page != null) {
            if (page.id != object.id) {
               page.addChildPage(object);
            }
         } else if (object.parentId != -2) {
            LOGGER.info("Parent Page not found for " + object.getPageName() + " (ID: " + object.id + ", parent_id: " + object.parentId + ")");
         }

         return true;
      });
      this.catalogPages.putAll(pages);
      LOGGER.info("Loaded " + this.catalogPages.size() + " Catalog Pages!");
   }

   private synchronized void loadCatalogFeaturedPages() {
      this.catalogFeaturedPages.clear();

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            Statement statement = connection.createStatement();

            try {
               ResultSet set = statement.executeQuery("SELECT * FROM catalog_featured_pages ORDER BY slot_id ASC");

               try {
                  while (set.next()) {
                     this.catalogFeaturedPages
                        .put(
                           set.getInt("slot_id"),
                           new CatalogFeaturedPage(
                              set.getInt("slot_id"),
                              set.getString("caption"),
                              set.getString("image"),
                              CatalogFeaturedPage.Type.valueOf(set.getString("type").toUpperCase()),
                              set.getInt("expire_timestamp"),
                              set.getString("page_name"),
                              set.getInt("page_id"),
                              set.getString("product_name")
                           )
                        );
                  }
               } catch (Throwable var9) {
                  if (set != null) {
                     try {
                        set.close();
                     } catch (Throwable var8) {
                        var9.addSuppressed(var8);
                     }
                  }

                  throw var9;
               }

               if (set != null) {
                  set.close();
               }
            } catch (Throwable var10) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var7) {
                     var10.addSuppressed(var7);
                  }
               }

               throw var10;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var11) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var6) {
                  var11.addSuppressed(var6);
               }
            }

            throw var11;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }
   }

   private synchronized void loadCatalogItems() {
      this.clubItems.clear();
      catalogItemAmount = 0;

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            Statement statement = connection.createStatement();

            try {
               ResultSet set = statement.executeQuery("SELECT * FROM catalog_items");

               try {
                  while (set.next()) {
                     if (!set.getString("item_ids").equals("0")) {
                        if (set.getString("catalog_name").contains("HABBO_CLUB_")) {
                           this.clubItems.add(new CatalogItem(set));
                        } else {
                           CatalogPage page = (CatalogPage)this.catalogPages.get(set.getInt("page_id"));
                           if (page != null) {
                              CatalogItem item = page.getCatalogItem(set.getInt("id"));
                              if (item == null) {
                                 catalogItemAmount++;
                                 item = new CatalogItem(set);
                                 page.addItem(item);
                                 if (item.getOfferId() != -1) {
                                    page.addOfferId(item.getOfferId());
                                    this.offerDefs.put(item.getOfferId(), item.getId());
                                 }
                              } else {
                                 item.update(set);
                              }

                              if (item.isLimited()) {
                                 this.createOrUpdateLimitedConfig(item);
                              }
                           }
                        }
                     }
                  }
               } catch (Throwable var9) {
                  if (set != null) {
                     try {
                        set.close();
                     } catch (Throwable var8) {
                        var9.addSuppressed(var8);
                     }
                  }

                  throw var9;
               }

               if (set != null) {
                  set.close();
               }
            } catch (Throwable var10) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var7) {
                     var10.addSuppressed(var7);
                  }
               }

               throw var10;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var11) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var6) {
                  var11.addSuppressed(var6);
               }
            }

            throw var11;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }

      for (CatalogPage page : this.catalogPages.valueCollection()) {
         for (Integer id : page.getIncluded()) {
            CatalogPage p = (CatalogPage)this.catalogPages.get(id);
            if (p != null) {
               page.getCatalogItems().putAll(p.getCatalogItems());
            }
         }
      }
   }

   private void loadClubOffers() {
      this.clubOffers.clear();

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM catalog_club_offers WHERE enabled = ?");

            try {
               statement.setString(1, "1");
               ResultSet set = statement.executeQuery();

               try {
                  while (set.next()) {
                     this.clubOffers.put(set.getInt("id"), new ClubOffer(set));
                  }
               } catch (Throwable var9) {
                  if (set != null) {
                     try {
                        set.close();
                     } catch (Throwable var8) {
                        var9.addSuppressed(var8);
                     }
                  }

                  throw var9;
               }

               if (set != null) {
                  set.close();
               }
            } catch (Throwable var10) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var7) {
                     var10.addSuppressed(var7);
                  }
               }

               throw var10;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var11) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var6) {
                  var11.addSuppressed(var6);
               }
            }

            throw var11;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }
   }

   private void loadTargetOffers() {
      synchronized (this.targetOffers) {
         this.targetOffers.clear();

         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               PreparedStatement statement = connection.prepareStatement("SELECT * FROM catalog_target_offers WHERE end_timestamp > ?");

               try {
                  statement.setInt(1, Emulator.getIntUnixTimestamp());
                  ResultSet set = statement.executeQuery();

                  try {
                     while (set.next()) {
                        this.targetOffers.put(set.getInt("id"), new TargetOffer(set));
                     }
                  } catch (Throwable var11) {
                     if (set != null) {
                        try {
                           set.close();
                        } catch (Throwable var10) {
                           var11.addSuppressed(var10);
                        }
                     }

                     throw var11;
                  }

                  if (set != null) {
                     set.close();
                  }
               } catch (Throwable var12) {
                  if (statement != null) {
                     try {
                        statement.close();
                     } catch (Throwable var9) {
                        var12.addSuppressed(var9);
                     }
                  }

                  throw var12;
               }

               if (statement != null) {
                  statement.close();
               }
            } catch (Throwable var13) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var8) {
                     var13.addSuppressed(var8);
                  }
               }

               throw var13;
            }

            if (connection != null) {
               connection.close();
            }
         } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
         }
      }
   }

   private void loadVouchers() {
      synchronized (this.vouchers) {
         this.vouchers.clear();

         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               Statement statement = connection.createStatement();

               try {
                  ResultSet set = statement.executeQuery("SELECT * FROM vouchers");

                  try {
                     while (set.next()) {
                        this.vouchers.add(new Voucher(set));
                     }
                  } catch (Throwable var11) {
                     if (set != null) {
                        try {
                           set.close();
                        } catch (Throwable var10) {
                           var11.addSuppressed(var10);
                        }
                     }

                     throw var11;
                  }

                  if (set != null) {
                     set.close();
                  }
               } catch (Throwable var12) {
                  if (statement != null) {
                     try {
                        statement.close();
                     } catch (Throwable var9) {
                        var12.addSuppressed(var9);
                     }
                  }

                  throw var12;
               }

               if (statement != null) {
                  statement.close();
               }
            } catch (Throwable var13) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var8) {
                     var13.addSuppressed(var8);
                  }
               }

               throw var13;
            }

            if (connection != null) {
               connection.close();
            }
         } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
         }
      }
   }

   public void loadRecycler() {
      synchronized (this.prizes) {
         this.prizes.clear();

         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               Statement statement = connection.createStatement();

               try {
                  ResultSet set = statement.executeQuery("SELECT * FROM recycler_prizes");

                  try {
                     while (set.next()) {
                        Item item = Emulator.getGameEnvironment().getItemManager().getItem(set.getInt("item_id"));
                        if (item != null) {
                           if (this.prizes.get(set.getInt("rarity")) == null) {
                              this.prizes.put(set.getInt("rarity"), new THashSet());
                           }

                           ((THashSet)this.prizes.get(set.getInt("rarity"))).add(item);
                        } else {
                           LOGGER.error("Cannot load item with ID: {} as recycler reward!", set.getInt("item_id"));
                        }
                     }
                  } catch (Throwable var11) {
                     if (set != null) {
                        try {
                           set.close();
                        } catch (Throwable var10) {
                           var11.addSuppressed(var10);
                        }
                     }

                     throw var11;
                  }

                  if (set != null) {
                     set.close();
                  }
               } catch (Throwable var12) {
                  if (statement != null) {
                     try {
                        statement.close();
                     } catch (Throwable var9) {
                        var12.addSuppressed(var9);
                     }
                  }

                  throw var12;
               }

               if (statement != null) {
                  statement.close();
               }
            } catch (Throwable var13) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var8) {
                     var13.addSuppressed(var8);
                  }
               }

               throw var13;
            }

            if (connection != null) {
               connection.close();
            }
         } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
         }
      }
   }

   public void loadGiftWrappers() {
      synchronized (this.giftWrappers) {
         synchronized (this.giftFurnis) {
            this.giftWrappers.clear();
            this.giftFurnis.clear();

            try {
               Connection connection = Emulator.getDatabase().getDataSource().getConnection();

               try {
                  Statement statement = connection.createStatement();

                  try {
                     ResultSet set = statement.executeQuery("SELECT * FROM gift_wrappers ORDER BY sprite_id DESC");

                     try {
                        while (set.next()) {
                           switch (set.getString("type")) {
                              case "wrapper":
                                 this.giftWrappers.put(set.getInt("sprite_id"), set.getInt("item_id"));
                                 break;
                              case "gift":
                                 this.giftFurnis.put(set.getInt("sprite_id"), set.getInt("item_id"));
                           }
                        }
                     } catch (Throwable var13) {
                        if (set != null) {
                           try {
                              set.close();
                           } catch (Throwable var12) {
                              var13.addSuppressed(var12);
                           }
                        }

                        throw var13;
                     }

                     if (set != null) {
                        set.close();
                     }
                  } catch (Throwable var14) {
                     if (statement != null) {
                        try {
                           statement.close();
                        } catch (Throwable var11) {
                           var14.addSuppressed(var11);
                        }
                     }

                     throw var14;
                  }

                  if (statement != null) {
                     statement.close();
                  }
               } catch (Throwable var15) {
                  if (connection != null) {
                     try {
                        connection.close();
                     } catch (Throwable var10) {
                        var15.addSuppressed(var10);
                     }
                  }

                  throw var15;
               }

               if (connection != null) {
                  connection.close();
               }
            } catch (SQLException e) {
               LOGGER.error("Caught SQL exception", e);
            }
         }
      }
   }

   private void loadClothing() {
      synchronized (this.clothing) {
         this.clothing.clear();

         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               Statement statement = connection.createStatement();

               try {
                  ResultSet set = statement.executeQuery("SELECT * FROM catalog_clothing");

                  try {
                     while (set.next()) {
                        this.clothing.put(set.getInt("id"), new ClothItem(set));
                     }
                  } catch (Throwable var11) {
                     if (set != null) {
                        try {
                           set.close();
                        } catch (Throwable var10) {
                           var11.addSuppressed(var10);
                        }
                     }

                     throw var11;
                  }

                  if (set != null) {
                     set.close();
                  }
               } catch (Throwable var12) {
                  if (statement != null) {
                     try {
                        statement.close();
                     } catch (Throwable var9) {
                        var12.addSuppressed(var9);
                     }
                  }

                  throw var12;
               }

               if (statement != null) {
                  statement.close();
               }
            } catch (Throwable var13) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var8) {
                     var13.addSuppressed(var8);
                  }
               }

               throw var13;
            }

            if (connection != null) {
               connection.close();
            }
         } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
         }
      }
   }

   public ClothItem getClothing(String name) {
      for (ClothItem item : this.clothing.values()) {
         if (item.name.equalsIgnoreCase(name)) {
            return item;
         }
      }

      return null;
   }

   public Voucher getVoucher(String code) {
      synchronized (this.vouchers) {
         for (Voucher voucher : this.vouchers) {
            if (voucher.code.equals(code)) {
               return voucher;
            }
         }

         return null;
      }
   }

   public void redeemVoucher(GameClient client, String voucherCode) {
      Habbo habbo = client.getHabbo();
      if (habbo != null) {
         Voucher voucher = Emulator.getGameEnvironment().getCatalogManager().getVoucher(voucherCode);
         if (voucher == null) {
            client.sendResponse(new RedeemVoucherErrorComposer(0));
         } else if (voucher.isExhausted()) {
            client.sendResponse(new RedeemVoucherErrorComposer(Emulator.getGameEnvironment().getCatalogManager().deleteVoucher(voucher) ? 0 : 1));
         } else if (voucher.hasUserExhausted(habbo.getHabboInfo().getId())) {
            client.sendResponse(new ModToolIssueHandledComposer("You have exceeded the limit for redeeming this voucher."));
         } else {
            voucher.addHistoryEntry(habbo.getHabboInfo().getId());
            if (voucher.points > 0) {
               client.getHabbo().givePoints(voucher.pointsType, voucher.points);
            }

            if (voucher.credits > 0) {
               client.getHabbo().giveCredits(voucher.credits);
            }

            if (voucher.catalogItemId > 0) {
               CatalogItem item = this.getCatalogItem(voucher.catalogItemId);
               if (item != null) {
                  this.purchaseItem(null, item, client.getHabbo(), 1, "", true);
               }
            }

            client.sendResponse(new RedeemVoucherOKComposer());
         }
      }
   }

   public boolean deleteVoucher(Voucher voucher) {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         boolean var12;
         try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM vouchers WHERE code = ?");

            try {
               statement.setString(1, voucher.code);
               synchronized (this.vouchers) {
                  this.vouchers.remove(voucher);
               }

               var12 = statement.executeUpdate() >= 1;
            } catch (Throwable var9) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var7) {
                     var9.addSuppressed(var7);
                  }
               }

               throw var9;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var10) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var6) {
                  var10.addSuppressed(var6);
               }
            }

            throw var10;
         }

         if (connection != null) {
            connection.close();
         }

         return var12;
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
         return false;
      }
   }

   public CatalogPage getCatalogPage(int pageId) {
      return (CatalogPage)this.catalogPages.get(pageId);
   }

   public CatalogPage getCatalogPage(String captionSafe) {
      return this.catalogPages
         .valueCollection()
         .stream()
         .filter(p -> p != null && p.getPageName() != null && p.getPageName().equalsIgnoreCase(captionSafe))
         .findAny()
         .orElse(null);
   }

   public CatalogPage getCatalogPageByLayout(String layoutName) {
      return this.catalogPages
         .valueCollection()
         .stream()
         .filter(p -> p != null && p.isVisible() && p.isEnabled() && p.getRank() < 2 && p.getLayout() != null && p.getLayout().equalsIgnoreCase(layoutName))
         .findAny()
         .orElse(null);
   }

   public CatalogItem getCatalogItem(final int id) {
      final CatalogItem[] item = new CatalogItem[]{null};
      synchronized (this.catalogPages) {
         this.catalogPages.forEachValue(new TObjectProcedure<CatalogPage>() {
            public boolean execute(CatalogPage object) {
               item[0] = object.getCatalogItem(id);
               return item[0] == null;
            }
         });
      }

      return item[0];
   }

   public List<CatalogPage> getCatalogPages(int parentId, final Habbo habbo) {
      final List<CatalogPage> pages = new ArrayList<>();
      ((CatalogPage)this.catalogPages.get(parentId)).childPages.forEachValue(new TObjectProcedure<CatalogPage>() {
         public boolean execute(CatalogPage object) {
            boolean isVisiblePage = object.visible;
            boolean hasRightRank = object.getRank() <= habbo.getHabboInfo().getRank().getId();
            boolean clubRightsOkay = true;
            if (object.isClubOnly() && !habbo.getHabboInfo().getHabboStats().hasActiveClub()) {
               clubRightsOkay = false;
            }

            if (isVisiblePage && hasRightRank && clubRightsOkay) {
               pages.add(object);
            }

            return true;
         }
      });
      Collections.sort(pages);
      return pages;
   }

   public TIntObjectMap<CatalogFeaturedPage> getCatalogFeaturedPages() {
      return this.catalogFeaturedPages;
   }

   public CatalogItem getClubItem(int itemId) {
      synchronized (this.clubItems) {
         TObjectHashIterator var3 = this.clubItems.iterator();

         while (var3.hasNext()) {
            CatalogItem item = (CatalogItem)var3.next();
            if (item.getId() == itemId) {
               return item;
            }
         }

         return null;
      }
   }

   public boolean moveCatalogItem(CatalogItem item, int pageId) {
      CatalogPage page = this.getCatalogPage(item.getPageId());
      if (page == null) {
         return false;
      }

      page.getCatalogItems().remove(item.getId());
      page = this.getCatalogPage(pageId);
      if (page == null) {
         return false;
      }

      page.getCatalogItems().put(item.getId(), item);
      item.setPageId(pageId);
      item.setNeedsUpdate(true);
      item.run();
      return true;
   }

   public Item getRandomRecyclerPrize() {
      int level = 1;
      if (Emulator.getRandom().nextInt(Emulator.getConfig().getInt("hotel.ecotron.rarity.chance.5")) + 1
         == Emulator.getConfig().getInt("hotel.ecotron.rarity.chance.5")) {
         level = 5;
      } else if (Emulator.getRandom().nextInt(Emulator.getConfig().getInt("hotel.ecotron.rarity.chance.4")) + 1
         == Emulator.getConfig().getInt("hotel.ecotron.rarity.chance.4")) {
         level = 4;
      } else if (Emulator.getRandom().nextInt(Emulator.getConfig().getInt("hotel.ecotron.rarity.chance.3")) + 1
         == Emulator.getConfig().getInt("hotel.ecotron.rarity.chance.3")) {
         level = 3;
      } else if (Emulator.getRandom().nextInt(Emulator.getConfig().getInt("hotel.ecotron.rarity.chance.2")) + 1
         == Emulator.getConfig().getInt("hotel.ecotron.rarity.chance.2")) {
         level = 2;
      }

      if (this.prizes.containsKey(level) && !((THashSet)this.prizes.get(level)).isEmpty()) {
         return (Item)((THashSet)this.prizes.get(level)).toArray()[Emulator.getRandom().nextInt(((THashSet)this.prizes.get(level)).size())];
      }

      LOGGER.error("No rewards specified for rarity level {}", level);
      return null;
   }

   public CatalogPage createCatalogPage(String caption, String captionSave, int roomId, int icon, CatalogPageLayouts layout, int minRank, int parentId) {
      CatalogPage catalogPage = null;

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "INSERT INTO catalog_pages (parent_id, caption, caption_save, icon_image, visible, enabled, min_rank, page_layout, room_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
               1
            );

            try {
               statement.setInt(1, parentId);
               statement.setString(2, caption);
               statement.setString(3, captionSave);
               statement.setInt(4, icon);
               statement.setString(5, "1");
               statement.setString(6, "1");
               statement.setInt(7, minRank);
               statement.setString(8, layout.name());
               statement.setInt(9, roomId);
               statement.execute();
               ResultSet set = statement.getGeneratedKeys();

               try {
                  if (set.next()) {
                     PreparedStatement stmt = connection.prepareStatement("SELECT * FROM catalog_pages WHERE id = ?");

                     try {
                        stmt.setInt(1, set.getInt(1));
                        ResultSet page = stmt.executeQuery();

                        try {
                           if (page.next()) {
                              Class<? extends CatalogPage> pageClazz = (Class<? extends CatalogPage>)pageDefinitions.get(page.getString("page_layout"));
                              if (pageClazz != null) {
                                 try {
                                    catalogPage = pageClazz.getConstructor(ResultSet.class).newInstance(page);
                                 } catch (Exception e) {
                                    LOGGER.error("Caught exception", e);
                                 }
                              } else {
                                 LOGGER.error("Unknown page layout: {}", page.getString("page_layout"));
                              }
                           }
                        } catch (Throwable var22) {
                           if (page != null) {
                              try {
                                 page.close();
                              } catch (Throwable var20) {
                                 var22.addSuppressed(var20);
                              }
                           }

                           throw var22;
                        }

                        if (page != null) {
                           page.close();
                        }
                     } catch (Throwable var23) {
                        if (stmt != null) {
                           try {
                              stmt.close();
                           } catch (Throwable var19) {
                              var23.addSuppressed(var19);
                           }
                        }

                        throw var23;
                     }

                     if (stmt != null) {
                        stmt.close();
                     }
                  }
               } catch (Throwable var24) {
                  if (set != null) {
                     try {
                        set.close();
                     } catch (Throwable var18) {
                        var24.addSuppressed(var18);
                     }
                  }

                  throw var24;
               }

               if (set != null) {
                  set.close();
               }
            } catch (Throwable var25) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var17) {
                     var25.addSuppressed(var17);
                  }
               }

               throw var25;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var26) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var16) {
                  var26.addSuppressed(var16);
               }
            }

            throw var26;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }

      if (catalogPage != null) {
         this.catalogPages.put(catalogPage.getId(), catalogPage);
      }

      return catalogPage;
   }

   public CatalogLimitedConfiguration getLimitedConfig(CatalogItem item) {
      synchronized (this.limitedNumbers) {
         return (CatalogLimitedConfiguration)this.limitedNumbers.get(item.getId());
      }
   }

   public CatalogLimitedConfiguration createOrUpdateLimitedConfig(CatalogItem item) {
      if (item.isLimited()) {
         CatalogLimitedConfiguration limitedConfiguration = (CatalogLimitedConfiguration)this.limitedNumbers.get(item.getId());
         if (limitedConfiguration == null) {
            limitedConfiguration = new CatalogLimitedConfiguration(item.getId(), new LinkedList<>(), 0);
            limitedConfiguration.generateNumbers(1, item.limitedStack);
            this.limitedNumbers.put(item.getId(), limitedConfiguration);
         } else if (limitedConfiguration.getTotalSet() != item.limitedStack) {
            if (limitedConfiguration.getTotalSet() == 0) {
               limitedConfiguration.setTotalSet(item.limitedStack);
            } else if (item.limitedStack > limitedConfiguration.getTotalSet()) {
               limitedConfiguration.generateNumbers(item.limitedStack + 1, item.limitedStack - limitedConfiguration.getTotalSet());
            } else {
               item.limitedStack = limitedConfiguration.getTotalSet();
            }
         }

         return limitedConfiguration;
      } else {
         return null;
      }
   }

   public void dispose() {
      TIntObjectIterator<CatalogPage> pageIterator = this.catalogPages.iterator();

      while (pageIterator.hasNext()) {
         pageIterator.advance();

         for (CatalogItem item : ((CatalogPage)pageIterator.value()).getCatalogItems().valueCollection()) {
            item.run();
            if (item.isLimited()) {
               ((CatalogLimitedConfiguration)this.limitedNumbers.get(item.getId())).run();
            }
         }
      }

      LOGGER.info("Catalog Manager -> Disposed!");
   }

   public void purchaseItem(CatalogPage page, CatalogItem item, Habbo habbo, int amount, String extradata, boolean free) {
      Item cBaseItem = null;
      if (item != null && !habbo.getHabboStats().isPurchasingFurniture) {
         habbo.getHabboStats().isPurchasingFurniture = true;

         try {
            if (item.isClubOnly() && !habbo.getClient().getHabbo().getHabboStats().hasActiveClub()) {
               habbo.getClient().sendResponse(new AlertPurchaseUnavailableComposer(1));
            } else if (amount <= 0) {
               habbo.getClient().sendResponse(new AlertPurchaseUnavailableComposer(0));
            } else {
               try {
                  CatalogLimitedConfiguration limitedConfiguration = null;
                  int limitedStack = 0;
                  int limitedNumber = 0;
                  if (item.isLimited()) {
                     amount = 1;
                     if (this.getLimitedConfig(item).available() == 0) {
                        habbo.getClient().sendResponse(new AlertLimitedSoldOutComposer());
                        return;
                     }

                     if (Emulator.getConfig().getBoolean("hotel.catalog.ltd.limit.enabled")) {
                        int ltdLimit = Emulator.getConfig().getInt("hotel.purchase.ltd.limit.daily.total");
                        if (habbo.getHabboStats().totalLtds() >= ltdLimit) {
                           habbo.alert(
                              Emulator.getTexts()
                                 .getValue("error.catalog.buy.limited.daily.total")
                                 .replace("%itemname%", ((Item)item.getBaseItems().iterator().next()).getFullName())
                                 .replace("%limit%", ltdLimit + "")
                           );
                           return;
                        }

                        ltdLimit = Emulator.getConfig().getInt("hotel.purchase.ltd.limit.daily.item");
                        if (habbo.getHabboStats().totalLtds(item.id) >= ltdLimit) {
                           habbo.alert(
                              Emulator.getTexts()
                                 .getValue("error.catalog.buy.limited.daily.item")
                                 .replace("%itemname%", ((Item)item.getBaseItems().iterator().next()).getFullName())
                                 .replace("%limit%", ltdLimit + "")
                           );
                           return;
                        }
                     }
                  }

                  if (amount > 1) {
                     if (amount == item.getAmount()) {
                        amount = 1;
                     } else if (amount * item.getAmount() > 100) {
                        habbo.alert("Whoops! You tried to buy this " + amount * item.getAmount() + " times. This must've been a mistake.");
                        habbo.getClient().sendResponse(new AlertPurchaseUnavailableComposer(0));
                        return;
                     }
                  }

                  THashSet<HabboItem> itemsList = new THashSet();
                  if (amount > 1 && !CatalogItem.haveOffer(item)) {
                     String message = Emulator.getTexts()
                        .getValue("scripter.warning.catalog.amount")
                        .replace("%username%", habbo.getHabboInfo().getUsername())
                        .replace("%itemname%", item.getName())
                        .replace("%pagename%", page.getCaption());
                     ScripterManager.scripterDetected(habbo.getClient(), message);
                     LOGGER.info(message);
                     habbo.getClient().sendResponse(new AlertPurchaseUnavailableComposer(0));
                  } else {
                     if (item.isLimited()) {
                        limitedConfiguration = this.getLimitedConfig(item);
                        if (limitedConfiguration == null) {
                           limitedConfiguration = this.createOrUpdateLimitedConfig(item);
                        }

                        limitedNumber = limitedConfiguration.getNumber();
                        limitedStack = limitedConfiguration.getTotalSet();
                     }

                     int totalCredits = free ? 0 : this.calculateDiscountedPrice(item.getCredits(), amount, item);
                     int totalPoints = free ? 0 : this.calculateDiscountedPrice(item.getPoints(), amount, item);
                     if (totalCredits <= 0 || habbo.getHabboInfo().getCredits() - totalCredits >= 0) {
                        if (totalPoints <= 0 || habbo.getHabboInfo().getCurrencyAmount(item.getPointsType()) - totalPoints >= 0) {
                           List<String> badges = new ArrayList<>();
                           Map<AddHabboItemComposer.AddHabboItemCategory, List<Integer>> unseenItems = new HashMap<>();
                           boolean badgeFound = false;

                           for (int i = 0; i < amount; i++) {
                              habbo.getHabboStats().addLtdLog(item.getId(), Emulator.getIntUnixTimestamp());
                              TObjectHashIterator itemIds = item.getBaseItems().iterator();

                              while (itemIds.hasNext()) {
                                 Item baseItem = (Item)itemIds.next();

                                 for (int k = 0; k < item.getItemAmount(baseItem.getId()); k++) {
                                    if (baseItem.getName().startsWith("rentable_bot_") || baseItem.getName().startsWith("bot_")) {
                                       String type = item.getName().replace("rentable_bot_", "");
                                       type = type.replace("bot_", "");
                                       type = type.replace("visitor_logger", "visitor_log");
                                       THashMap<String, String> data = new THashMap();

                                       for (String s : item.getExtradata().split(";")) {
                                          if (s.contains(":")) {
                                             data.put(s.split(":")[0], s.split(":")[1]);
                                          }
                                       }

                                       Bot bot = Emulator.getGameEnvironment().getBotManager().createBot(data, type);
                                       if (bot == null) {
                                          throw new Exception("Failed to create bot of type: " + type);
                                       }

                                       bot.setOwnerId(habbo.getClient().getHabbo().getHabboInfo().getId());
                                       bot.setOwnerName(habbo.getClient().getHabbo().getHabboInfo().getUsername());
                                       bot.needsUpdate(true);
                                       Emulator.getThreading().run(bot);
                                       habbo.getClient().getHabbo().getInventory().getBotsComponent().addBot(bot);
                                       habbo.getClient().sendResponse(new AddBotComposer(bot));
                                       if (!unseenItems.containsKey(AddHabboItemComposer.AddHabboItemCategory.BOT)) {
                                          unseenItems.put(AddHabboItemComposer.AddHabboItemCategory.BOT, new ArrayList<>());
                                       }

                                       unseenItems.get(AddHabboItemComposer.AddHabboItemCategory.BOT).add(bot.getId());
                                    } else if (baseItem.getType() == FurnitureType.EFFECT) {
                                       int effectId = baseItem.getEffectM();
                                       if (habbo.getHabboInfo().getGender().equals(HabboGender.F)) {
                                          effectId = baseItem.getEffectF();
                                       }

                                       if (effectId > 0) {
                                          habbo.getInventory().getEffectsComponent().createEffect(effectId);
                                       }
                                    } else if (Item.isPet(baseItem)) {
                                       String[] data = extradata.split("\n");
                                       if (data.length < 3) {
                                          habbo.getClient().sendResponse(new AlertPurchaseFailedComposer(0));
                                          return;
                                       }

                                       Pet pet = null;

                                       try {
                                          pet = Emulator.getGameEnvironment().getPetManager().createPet(baseItem, data[0], data[1], data[2], habbo.getClient());
                                       } catch (Exception e) {
                                          LOGGER.error("Caught exception", e);
                                          habbo.getClient().sendResponse(new AlertPurchaseFailedComposer(0));
                                       }

                                       if (pet == null) {
                                          habbo.getClient().sendResponse(new AlertPurchaseFailedComposer(0));
                                          return;
                                       }

                                       habbo.getClient().getHabbo().getInventory().getPetsComponent().addPet(pet);
                                       habbo.getClient().sendResponse(new AddPetComposer(pet));
                                       habbo.getClient().sendResponse(new PetBoughtNotificationComposer(pet, false));
                                       AchievementManager.progressAchievement(
                                          habbo.getClient().getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("PetLover")
                                       );
                                       if (!unseenItems.containsKey(AddHabboItemComposer.AddHabboItemCategory.PET)) {
                                          unseenItems.put(AddHabboItemComposer.AddHabboItemCategory.PET, new ArrayList<>());
                                       }

                                       unseenItems.get(AddHabboItemComposer.AddHabboItemCategory.PET).add(pet.getId());
                                    } else if (baseItem.getType() == FurnitureType.BADGE) {
                                       if (!habbo.getInventory().getBadgesComponent().hasBadge(baseItem.getName())) {
                                          if (!badges.contains(baseItem.getName())) {
                                             badges.add(baseItem.getName());
                                          }
                                       } else {
                                          badgeFound = true;
                                       }
                                    } else {
                                       if (baseItem.getInteractionType().getType() == InteractionTrophy.class
                                          || baseItem.getInteractionType().getType() == InteractionBadgeDisplay.class) {
                                          if (baseItem.getInteractionType().getType() == InteractionBadgeDisplay.class
                                             && !habbo.getClient().getHabbo().getInventory().getBadgesComponent().hasBadge(extradata)) {
                                             ScripterManager.scripterDetected(
                                                habbo.getClient(),
                                                Emulator.getTexts()
                                                   .getValue("scripter.warning.catalog.badge_display")
                                                   .replace("%username%", habbo.getClient().getHabbo().getHabboInfo().getUsername())
                                                   .replace("%badge%", extradata)
                                             );
                                             extradata = "UMAD";
                                          }

                                          if (extradata.length() > Emulator.getConfig().getInt("hotel.trophies.length.max", 300)) {
                                             extradata = extradata.substring(0, Emulator.getConfig().getInt("hotel.trophies.length.max", 300));
                                          }

                                          extradata = habbo.getClient().getHabbo().getHabboInfo().getUsername()
                                             + '\t'
                                             + Calendar.getInstance().get(5)
                                             + "-"
                                             + (Calendar.getInstance().get(2) + 1)
                                             + "-"
                                             + Calendar.getInstance().get(1)
                                             + '\t'
                                             + Emulator.getGameEnvironment().getWordFilter().filter(extradata.replace("\t", ""), habbo);
                                       }

                                       if (InteractionTeleport.class.isAssignableFrom(baseItem.getInteractionType().getType())) {
                                          HabboItem teleportOne = Emulator.getGameEnvironment()
                                             .getItemManager()
                                             .createItem(habbo.getClient().getHabbo().getHabboInfo().getId(), baseItem, limitedStack, limitedNumber, extradata);
                                          HabboItem teleportTwo = Emulator.getGameEnvironment()
                                             .getItemManager()
                                             .createItem(habbo.getClient().getHabbo().getHabboInfo().getId(), baseItem, limitedStack, limitedNumber, extradata);
                                          Emulator.getGameEnvironment().getItemManager().insertTeleportPair(teleportOne.getId(), teleportTwo.getId());
                                          itemsList.add(teleportOne);
                                          itemsList.add(teleportTwo);
                                       } else if (baseItem.getInteractionType().getType() == InteractionHopper.class) {
                                          HabboItem hopper = Emulator.getGameEnvironment()
                                             .getItemManager()
                                             .createItem(habbo.getClient().getHabbo().getHabboInfo().getId(), baseItem, limitedStack, limitedNumber, extradata);
                                          Emulator.getGameEnvironment().getItemManager().insertHopper(hopper);
                                          itemsList.add(hopper);
                                       } else if (baseItem.getInteractionType().getType() == InteractionGuildFurni.class
                                          || baseItem.getInteractionType().getType() == InteractionGuildGate.class) {
                                          int guildId;
                                          try {
                                             guildId = Integer.parseInt(extradata);
                                          } catch (Exception e) {
                                             LOGGER.error("Caught exception", e);
                                             habbo.getClient().sendResponse(new AlertPurchaseFailedComposer(0));
                                             return;
                                          }

                                          Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(guildId);
                                          if (guild != null && Emulator.getGameEnvironment().getGuildManager().getGuildMember(guild, habbo) != null) {
                                             InteractionGuildFurni habboItem = (InteractionGuildFurni)Emulator.getGameEnvironment()
                                                .getItemManager()
                                                .createItem(
                                                   habbo.getClient().getHabbo().getHabboInfo().getId(), baseItem, limitedStack, limitedNumber, extradata
                                                );
                                             habboItem.setExtradata("");
                                             habboItem.needsUpdate(true);
                                             Emulator.getThreading().run(habboItem);
                                             Emulator.getGameEnvironment().getGuildManager().setGuild(habboItem, guildId);
                                             itemsList.add(habboItem);
                                             if (baseItem.getName().equals("guild_forum")) {
                                                guild.setForum(true);
                                                guild.needsUpdate = true;
                                                guild.run();
                                             }
                                          }
                                       } else if (baseItem.getInteractionType().getType() == InteractionMusicDisc.class) {
                                          SoundTrack track = Emulator.getGameEnvironment().getItemManager().getSoundTrack(item.getExtradata());
                                          if (track == null) {
                                             habbo.getClient().sendResponse(new AlertPurchaseFailedComposer(0));
                                             return;
                                          }

                                          InteractionMusicDisc habboItem = (InteractionMusicDisc)Emulator.getGameEnvironment()
                                             .getItemManager()
                                             .createItem(
                                                habbo.getClient().getHabbo().getHabboInfo().getId(),
                                                baseItem,
                                                limitedStack,
                                                limitedNumber,
                                                habbo.getClient().getHabbo().getHabboInfo().getUsername()
                                                   + "\n"
                                                   + Calendar.getInstance().get(5)
                                                   + "\n"
                                                   + (Calendar.getInstance().get(2) + 1)
                                                   + "\n"
                                                   + Calendar.getInstance().get(1)
                                                   + "\n"
                                                   + track.getLength()
                                                   + "\n"
                                                   + track.getName()
                                                   + "\n"
                                                   + track.getId()
                                             );
                                          habboItem.needsUpdate(true);
                                          Emulator.getThreading().run(habboItem);
                                          itemsList.add(habboItem);
                                          AchievementManager.progressAchievement(
                                             habbo, Emulator.getGameEnvironment().getAchievementManager().getAchievement("MusicCollector")
                                          );
                                       } else {
                                          HabboItem habboItem = Emulator.getGameEnvironment()
                                             .getItemManager()
                                             .createItem(habbo.getClient().getHabbo().getHabboInfo().getId(), baseItem, limitedStack, limitedNumber, extradata);
                                          itemsList.add(habboItem);
                                       }
                                    }
                                 }
                              }
                           }

                           if (!badgeFound || item.getBaseItems().size() != 1) {
                              UserCatalogItemPurchasedEvent purchasedEvent = new UserCatalogItemPurchasedEvent(
                                 habbo, item, itemsList, totalCredits, totalPoints, badges
                              );
                              Emulator.getPluginManager().fireEvent(purchasedEvent);
                              if (!free && !habbo.getClient().getHabbo().hasPermission(Permission.ACC_INFINITE_CREDITS) && purchasedEvent.totalCredits > 0) {
                                 habbo.getClient().getHabbo().giveCredits(-purchasedEvent.totalCredits);
                              }

                              if (!free && !habbo.getClient().getHabbo().hasPermission(Permission.ACC_INFINITE_POINTS) && purchasedEvent.totalPoints > 0) {
                                 habbo.getClient().getHabbo().givePoints(item.getPointsType(), -purchasedEvent.totalPoints);
                              }

                              if (purchasedEvent.itemsList != null && !purchasedEvent.itemsList.isEmpty()) {
                                 habbo.getClient().getHabbo().getInventory().getItemsComponent().addItems(purchasedEvent.itemsList);
                                 unseenItems.put(
                                    AddHabboItemComposer.AddHabboItemCategory.OWNED_FURNI,
                                    purchasedEvent.itemsList.stream().map(HabboItem::getId).collect(Collectors.toList())
                                 );
                                 Emulator.getPluginManager().fireEvent(new UserCatalogFurnitureBoughtEvent(habbo, item, purchasedEvent.itemsList));
                                 if (limitedConfiguration != null) {
                                    TObjectHashIterator var40 = purchasedEvent.itemsList.iterator();

                                    while (var40.hasNext()) {
                                       HabboItem itm = (HabboItem)var40.next();
                                       limitedConfiguration.limitedSold(item.getId(), habbo, itm);
                                    }
                                 }
                              }

                              if (!purchasedEvent.badges.isEmpty() && !unseenItems.containsKey(AddHabboItemComposer.AddHabboItemCategory.BADGE)) {
                                 unseenItems.put(AddHabboItemComposer.AddHabboItemCategory.BADGE, new ArrayList<>());
                              }

                              for (String b : purchasedEvent.badges) {
                                 HabboBadge badge = new HabboBadge(0, b, 0, habbo);
                                 Emulator.getThreading().run(badge);
                                 habbo.getInventory().getBadgesComponent().addBadge(badge);
                                 habbo.getClient().sendResponse(new AddUserBadgeComposer(badge));
                                 THashMap<String, String> keys = new THashMap();
                                 keys.put("display", "BUBBLE");
                                 keys.put("image", "${image.library.url}album1584/" + badge.getCode() + ".gif");
                                 keys.put("message", Emulator.getTexts().getValue("commands.generic.cmd_badge.received"));
                                 habbo.getClient().sendResponse(new BubbleAlertComposer(BubbleAlertKeys.RECEIVED_BADGE.key, keys));
                                 unseenItems.get(AddHabboItemComposer.AddHabboItemCategory.BADGE).add(badge.getId());
                              }

                              habbo.getClient().getHabbo().getHabboStats().addPurchase(purchasedEvent.catalogItem);
                              habbo.getClient().sendResponse(new AddHabboItemComposer(unseenItems));
                              habbo.getClient().sendResponse(new PurchaseOKComposer(purchasedEvent.catalogItem));
                              habbo.getClient().sendResponse(new InventoryRefreshComposer());
                              THashSet<String> itemIds = new THashSet();
                              TObjectHashIterator var45 = purchasedEvent.itemsList.iterator();

                              while (var45.hasNext()) {
                                 HabboItem ix = (HabboItem)var45.next();
                                 itemIds.add(ix.getId() + "");
                              }

                              if (!free) {
                                 Emulator.getThreading()
                                    .run(
                                       new CatalogPurchaseLogEntry(
                                          Emulator.getIntUnixTimestamp(),
                                          purchasedEvent.habbo.getHabboInfo().getId(),
                                          purchasedEvent.catalogItem != null ? purchasedEvent.catalogItem.getId() : 0,
                                          String.join(";", itemIds),
                                          purchasedEvent.catalogItem != null ? purchasedEvent.catalogItem.getName() : "",
                                          purchasedEvent.totalCredits,
                                          purchasedEvent.totalPoints,
                                          item != null ? item.getPointsType() : 0,
                                          amount
                                       )
                                    );
                              }
                           } else {
                              habbo.getClient().sendResponse(new AlertPurchaseFailedComposer(1));
                           }
                        }
                     }
                  }
               } catch (Exception e) {
                  LOGGER.error("Exception caught", e);
                  habbo.getClient().sendResponse(new AlertPurchaseFailedComposer(0));
               }
            }
         } finally {
            habbo.getHabboStats().isPurchasingFurniture = false;
         }
      } else {
         habbo.getClient().sendResponse(new AlertPurchaseFailedComposer(0).compose());
      }
   }

   public List<ClubOffer> getClubOffers() {
      List<ClubOffer> offers = new ArrayList<>();

      for (Entry<Integer, ClubOffer> entry : this.clubOffers.entrySet()) {
         if (!entry.getValue().isDeal()) {
            offers.add(entry.getValue());
         }
      }

      return offers;
   }

   public TargetOffer getTargetOffer(int offerId) {
      return (TargetOffer)this.targetOffers.get(offerId);
   }

   private int calculateDiscountedPrice(int originalPrice, int amount, CatalogItem item) {
      if (!CatalogItem.haveOffer(item)) {
         return originalPrice * amount;
      }

      int basicDiscount = amount / DiscountComposer.DISCOUNT_BATCH_SIZE;
      int bonusDiscount = 0;
      if (basicDiscount >= DiscountComposer.MINIMUM_DISCOUNTS_FOR_BONUS) {
         if (amount % DiscountComposer.DISCOUNT_BATCH_SIZE == DiscountComposer.DISCOUNT_BATCH_SIZE - 1) {
            bonusDiscount = 1;
         }

         bonusDiscount += basicDiscount - DiscountComposer.MINIMUM_DISCOUNTS_FOR_BONUS;
      }

      int additionalDiscounts = 0;

      for (int threshold : DiscountComposer.ADDITIONAL_DISCOUNT_THRESHOLDS) {
         if (amount >= threshold) {
            additionalDiscounts++;
         }
      }

      int totalDiscountedItems = basicDiscount * DiscountComposer.DISCOUNT_AMOUNT_PER_BATCH + bonusDiscount + additionalDiscounts;
      return Math.max(0, originalPrice * (amount - totalDiscountedItems));
   }
}
