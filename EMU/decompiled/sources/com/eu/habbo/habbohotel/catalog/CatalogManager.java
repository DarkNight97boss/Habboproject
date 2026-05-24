package com.eu.habbo.habbohotel.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.catalog.CatalogFeaturedPage;
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
import com.eu.habbo.habbohotel.games.battlebanzai.BattleBanzaiGame;
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
import com.eu.habbo.messages.incoming.Incoming;
import com.eu.habbo.messages.outgoing.Outgoing;
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
import com.eu.habbo.messages.outgoing.rooms.RoomEditSettingsErrorComposer;
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/catalog/CatalogManager.class */
public class CatalogManager {
    public static int catalogItemAmount;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogManager.class);
    public static final THashMap<String, Class<? extends CatalogPage>> pageDefinitions = new THashMap<String, Class<? extends CatalogPage>>(CatalogPageLayouts.values().length) { // from class: com.eu.habbo.habbohotel.catalog.CatalogManager.1
        {
            for (CatalogPageLayouts catalogPageLayouts : CatalogPageLayouts.values()) {
                switch (AnonymousClass4.$SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[catalogPageLayouts.ordinal()]) {
                    case 1:
                        put(catalogPageLayouts.name().toLowerCase(), FrontpageLayout.class);
                        continue;
                        break;
                    case 2:
                        put(catalogPageLayouts.name().toLowerCase(), BadgeDisplayLayout.class);
                        continue;
                        break;
                    case 3:
                        put(catalogPageLayouts.name().toLowerCase(), SpacesLayout.class);
                        continue;
                        break;
                    case 4:
                        put(catalogPageLayouts.name().toLowerCase(), TrophiesLayout.class);
                        continue;
                        break;
                    case 5:
                        put(catalogPageLayouts.name().toLowerCase(), BotsLayout.class);
                        continue;
                        break;
                    case 6:
                        put(catalogPageLayouts.name().toLowerCase(), ClubBuyLayout.class);
                        continue;
                        break;
                    case 7:
                        put(catalogPageLayouts.name().toLowerCase(), ClubGiftsLayout.class);
                        continue;
                        break;
                    case 8:
                        put(catalogPageLayouts.name().toLowerCase(), SoldLTDItemsLayout.class);
                        continue;
                        break;
                    case 9:
                        put(catalogPageLayouts.name().toLowerCase(), SingleBundle.class);
                        continue;
                        break;
                    case 10:
                        put(catalogPageLayouts.name().toLowerCase(), RoomAdsLayout.class);
                        continue;
                        break;
                    case 11:
                        if (Emulator.getConfig().getBoolean("hotel.ecotron.enabled")) {
                            put(catalogPageLayouts.name().toLowerCase(), RecyclerLayout.class);
                        } else {
                            continue;
                        }
                        break;
                    case RoomEditSettingsErrorComposer.RESTRICTED_TAGS /* 12 */:
                        if (Emulator.getConfig().getBoolean("hotel.ecotron.enabled")) {
                            put(catalogPageLayouts.name().toLowerCase(), RecyclerInfoLayout.class);
                        }
                        break;
                    case RoomEditSettingsErrorComposer.TAGS_TOO_LONG /* 13 */:
                        break;
                    case 14:
                        if (Emulator.getConfig().getBoolean("hotel.marketplace.enabled")) {
                            put(catalogPageLayouts.name().toLowerCase(), MarketplaceLayout.class);
                        } else {
                            continue;
                        }
                        break;
                    case Incoming.ModToolPickTicketEvent /* 15 */:
                        if (Emulator.getConfig().getBoolean("hotel.marketplace.enabled")) {
                            put(catalogPageLayouts.name().toLowerCase(), MarketplaceOwnItems.class);
                        } else {
                            continue;
                        }
                        break;
                    case 16:
                        put(catalogPageLayouts.name().toLowerCase(), InfoDucketsLayout.class);
                        continue;
                        break;
                    case Incoming.RequestRoomUserTagsEvent /* 17 */:
                        put(catalogPageLayouts.name().toLowerCase(), InfoPetsLayout.class);
                        continue;
                        break;
                    case 18:
                        put(catalogPageLayouts.name().toLowerCase(), InfoRentablesLayout.class);
                        continue;
                        break;
                    case 19:
                        put(catalogPageLayouts.name().toLowerCase(), InfoLoyaltyLayout.class);
                        continue;
                        break;
                    case 20:
                        put(catalogPageLayouts.name().toLowerCase(), LoyaltyVipBuyLayout.class);
                        continue;
                        break;
                    case Incoming.GetHabboGuildBadgesMessageEvent /* 21 */:
                        put(catalogPageLayouts.name().toLowerCase(), GuildFrontpageLayout.class);
                        continue;
                        break;
                    case 22:
                        put(catalogPageLayouts.name().toLowerCase(), GuildFurnitureLayout.class);
                        continue;
                        break;
                    case 23:
                        put(catalogPageLayouts.name().toLowerCase(), GuildForumLayout.class);
                        continue;
                        break;
                    case 24:
                        put(catalogPageLayouts.name().toLowerCase(), PetsLayout.class);
                        continue;
                        break;
                    case 25:
                        put(catalogPageLayouts.name().toLowerCase(), Pets2Layout.class);
                        continue;
                        break;
                    case 26:
                        put(catalogPageLayouts.name().toLowerCase(), Pets3Layout.class);
                        continue;
                        break;
                    case 27:
                        put(catalogPageLayouts.name().toLowerCase(), TraxLayout.class);
                        continue;
                        break;
                    case 28:
                        put(catalogPageLayouts.name().toLowerCase(), ColorGroupingLayout.class);
                        continue;
                        break;
                    case 29:
                        put(catalogPageLayouts.name().toLowerCase(), RecentPurchasesLayout.class);
                        continue;
                        break;
                    case Outgoing.GuardianVotingTimeEnded /* 30 */:
                        put(catalogPageLayouts.name().toLowerCase(), RoomBundleLayout.class);
                        continue;
                        break;
                    case 31:
                        put(catalogPageLayouts.name().toLowerCase(), PetCustomizationLayout.class);
                        continue;
                        break;
                    case BattleBanzaiGame.effectId /* 32 */:
                        put(catalogPageLayouts.name().toLowerCase(), VipBuyLayout.class);
                        continue;
                        break;
                    case 33:
                        put(catalogPageLayouts.name().toLowerCase(), FrontPageFeaturedLayout.class);
                        continue;
                        break;
                    case Outgoing.JukeBoxPlayListComposer /* 34 */:
                        put(catalogPageLayouts.name().toLowerCase(), BuildersClubAddonsLayout.class);
                        continue;
                        break;
                    case Outgoing.RentableItemBuyOutPriceComposer /* 35 */:
                        put(catalogPageLayouts.name().toLowerCase(), BuildersClubFrontPageLayout.class);
                        continue;
                        break;
                    case 36:
                        put(catalogPageLayouts.name().toLowerCase(), BuildersClubLoyaltyLayout.class);
                        continue;
                        break;
                    case 37:
                        put(catalogPageLayouts.name().toLowerCase(), InfoMonkeyLayout.class);
                        continue;
                        break;
                    case 38:
                        put(catalogPageLayouts.name().toLowerCase(), InfoNikoLayout.class);
                        continue;
                        break;
                    case 39:
                        put(catalogPageLayouts.name().toLowerCase(), MadMoneyLayout.class);
                        continue;
                        break;
                    case 40:
                    default:
                        put("default_3x3", Default_3x3Layout.class);
                        continue;
                        break;
                }
                if (Emulator.getConfig().getBoolean("hotel.ecotron.enabled")) {
                    put(catalogPageLayouts.name().toLowerCase(), RecyclerPrizesLayout.class);
                }
            }
        }
    };
    public static int PURCHASE_COOLDOWN = 1;
    public static boolean SORT_USING_ORDERNUM = false;

    /* JADX INFO: renamed from: com.eu.habbo.habbohotel.catalog.CatalogManager$4, reason: invalid class name */
    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/catalog/CatalogManager$4.class */
    static /* synthetic */ class AnonymousClass4 {
        static final /* synthetic */ int[] $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts = new int[CatalogPageLayouts.values().length];

        static {
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.frontpage.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.badge_display.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.spaces_new.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.trophies.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.bots.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.club_buy.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.club_gift.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.sold_ltd_items.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.single_bundle.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.roomads.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.recycler.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.recycler_info.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.recycler_prizes.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.marketplace.ordinal()] = 14;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.marketplace_own_items.ordinal()] = 15;
            } catch (NoSuchFieldError e15) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.info_duckets.ordinal()] = 16;
            } catch (NoSuchFieldError e16) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.info_pets.ordinal()] = 17;
            } catch (NoSuchFieldError e17) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.info_rentables.ordinal()] = 18;
            } catch (NoSuchFieldError e18) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.info_loyalty.ordinal()] = 19;
            } catch (NoSuchFieldError e19) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.loyalty_vip_buy.ordinal()] = 20;
            } catch (NoSuchFieldError e20) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.guilds.ordinal()] = 21;
            } catch (NoSuchFieldError e21) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.guild_furni.ordinal()] = 22;
            } catch (NoSuchFieldError e22) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.guild_forum.ordinal()] = 23;
            } catch (NoSuchFieldError e23) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.pets.ordinal()] = 24;
            } catch (NoSuchFieldError e24) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.pets2.ordinal()] = 25;
            } catch (NoSuchFieldError e25) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.pets3.ordinal()] = 26;
            } catch (NoSuchFieldError e26) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.soundmachine.ordinal()] = 27;
            } catch (NoSuchFieldError e27) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.default_3x3_color_grouping.ordinal()] = 28;
            } catch (NoSuchFieldError e28) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.recent_purchases.ordinal()] = 29;
            } catch (NoSuchFieldError e29) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.room_bundle.ordinal()] = 30;
            } catch (NoSuchFieldError e30) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.petcustomization.ordinal()] = 31;
            } catch (NoSuchFieldError e31) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.vip_buy.ordinal()] = 32;
            } catch (NoSuchFieldError e32) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.frontpage_featured.ordinal()] = 33;
            } catch (NoSuchFieldError e33) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.builders_club_addons.ordinal()] = 34;
            } catch (NoSuchFieldError e34) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.builders_club_frontpage.ordinal()] = 35;
            } catch (NoSuchFieldError e35) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.builders_club_loyalty.ordinal()] = 36;
            } catch (NoSuchFieldError e36) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.monkey.ordinal()] = 37;
            } catch (NoSuchFieldError e37) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.niko.ordinal()] = 38;
            } catch (NoSuchFieldError e38) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.mad_money.ordinal()] = 39;
            } catch (NoSuchFieldError e39) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$catalog$CatalogPageLayouts[CatalogPageLayouts.default_3x3.ordinal()] = 40;
            } catch (NoSuchFieldError e40) {
            }
        }
    }

    public CatalogManager() {
        long jCurrentTimeMillis = System.currentTimeMillis();
        this.catalogPages = TCollections.synchronizedMap(new TIntObjectHashMap());
        this.catalogFeaturedPages = new TIntObjectHashMap();
        this.prizes = new THashMap<>();
        this.giftWrappers = new THashMap<>();
        this.giftFurnis = new THashMap<>();
        this.clubItems = new THashSet<>();
        this.clubOffers = new THashMap<>();
        this.targetOffers = new THashMap<>();
        this.clothing = new THashMap<>();
        this.offerDefs = new TIntIntHashMap();
        this.vouchers = new ArrayList();
        this.limitedNumbers = new THashMap<>();
        initialize();
        this.ecotronItem = Emulator.getGameEnvironment().getItemManager().getItem("ecotron_box");
        LOGGER.info("Catalog Manager -> Loaded! (" + (System.currentTimeMillis() - jCurrentTimeMillis) + " MS)");
    }

    public synchronized void initialize() {
        Emulator.getPluginManager().fireEvent(new EmulatorLoadCatalogManagerEvent());
        loadLimitedNumbers();
        loadCatalogPages();
        loadCatalogFeaturedPages();
        loadCatalogItems();
        loadClubOffers();
        loadTargetOffers();
        loadVouchers();
        loadClothing();
        loadRecycler();
        loadGiftWrappers();
    }

    private synchronized void loadLimitedNumbers() {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        this.limitedNumbers.clear();
        THashMap tHashMap = new THashMap();
        TIntIntHashMap tIntIntHashMap = new TIntIntHashMap();
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM catalog_items_limited");
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
            while (resultSetExecuteQuery.next()) {
                try {
                    if (!tHashMap.containsKey(Integer.valueOf(resultSetExecuteQuery.getInt("catalog_item_id")))) {
                        tHashMap.put(Integer.valueOf(resultSetExecuteQuery.getInt("catalog_item_id")), new LinkedList());
                    }
                    tIntIntHashMap.adjustOrPutValue(resultSetExecuteQuery.getInt("catalog_item_id"), 1, 1);
                    if (resultSetExecuteQuery.getInt("user_id") == 0) {
                        ((LinkedList) tHashMap.get(Integer.valueOf(resultSetExecuteQuery.getInt("catalog_item_id")))).push(Integer.valueOf(resultSetExecuteQuery.getInt("number")));
                    }
                } catch (Throwable th) {
                    if (resultSetExecuteQuery != null) {
                        try {
                            resultSetExecuteQuery.close();
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                    }
                    throw th;
                }
            }
            if (resultSetExecuteQuery != null) {
                resultSetExecuteQuery.close();
            }
            if (preparedStatementPrepareStatement != null) {
                preparedStatementPrepareStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
            for (Map.Entry entry : tHashMap.entrySet()) {
                this.limitedNumbers.put((Integer) entry.getKey(), new CatalogLimitedConfiguration(((Integer) entry.getKey()).intValue(), (LinkedList) entry.getValue(), tIntIntHashMap.get(((Integer) entry.getKey()).intValue())));
            }
        } catch (Throwable th3) {
            if (preparedStatementPrepareStatement != null) {
                try {
                    preparedStatementPrepareStatement.close();
                } catch (Throwable th4) {
                    th3.addSuppressed(th4);
                }
            }
            throw th3;
        }
    }

    private synchronized void loadCatalogPages() {
        Connection connection;
        this.catalogPages.clear();
        THashMap tHashMap = new THashMap();
        tHashMap.put(-1, new CatalogRootLayout());
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM catalog_pages ORDER BY parent_id, id");
            try {
                ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                while (resultSetExecuteQuery.next()) {
                    try {
                        Class cls = (Class) pageDefinitions.get(resultSetExecuteQuery.getString("page_layout"));
                        if (cls == null) {
                            LOGGER.info("Unknown Page Layout: " + resultSetExecuteQuery.getString("page_layout"));
                        } else {
                            try {
                                CatalogPage catalogPage = (CatalogPage) cls.getConstructor(ResultSet.class).newInstance(resultSetExecuteQuery);
                                tHashMap.put(Integer.valueOf(catalogPage.getId()), catalogPage);
                            } catch (Exception e2) {
                                LOGGER.error("Failed to load layout: {}", resultSetExecuteQuery.getString("page_layout"));
                            }
                        }
                    } catch (Throwable th) {
                        if (resultSetExecuteQuery != null) {
                            try {
                                resultSetExecuteQuery.close();
                            } catch (Throwable th2) {
                                th.addSuppressed(th2);
                            }
                        }
                        throw th;
                    }
                }
                if (resultSetExecuteQuery != null) {
                    resultSetExecuteQuery.close();
                }
                if (preparedStatementPrepareStatement != null) {
                    preparedStatementPrepareStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
                tHashMap.forEachValue(catalogPage2 -> {
                    CatalogPage catalogPage2 = (CatalogPage) tHashMap.get(Integer.valueOf(catalogPage2.parentId));
                    if (catalogPage2 != null) {
                        if (catalogPage2.id == catalogPage2.id) {
                            return true;
                        }
                        catalogPage2.addChildPage(catalogPage2);
                        return true;
                    }
                    if (catalogPage2.parentId == -2) {
                        return true;
                    }
                    LOGGER.info("Parent Page not found for " + catalogPage2.getPageName() + " (ID: " + catalogPage2.id + ", parent_id: " + catalogPage2.parentId + ")");
                    return true;
                });
                this.catalogPages.putAll(tHashMap);
                LOGGER.info("Loaded " + this.catalogPages.size() + " Catalog Pages!");
            } catch (Throwable th3) {
                if (preparedStatementPrepareStatement != null) {
                    try {
                        preparedStatementPrepareStatement.close();
                    } catch (Throwable th4) {
                        th3.addSuppressed(th4);
                    }
                }
                throw th3;
            }
        } finally {
        }
    }

    private synchronized void loadCatalogFeaturedPages() {
        this.catalogFeaturedPages.clear();
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                Statement statementCreateStatement = connection.createStatement();
                try {
                    ResultSet resultSetExecuteQuery = statementCreateStatement.executeQuery("SELECT * FROM catalog_featured_pages ORDER BY slot_id ASC");
                    while (resultSetExecuteQuery.next()) {
                        try {
                            this.catalogFeaturedPages.put(resultSetExecuteQuery.getInt("slot_id"), new CatalogFeaturedPage(resultSetExecuteQuery.getInt("slot_id"), resultSetExecuteQuery.getString("caption"), resultSetExecuteQuery.getString("image"), CatalogFeaturedPage.Type.valueOf(resultSetExecuteQuery.getString("type").toUpperCase()), resultSetExecuteQuery.getInt("expire_timestamp"), resultSetExecuteQuery.getString("page_name"), resultSetExecuteQuery.getInt("page_id"), resultSetExecuteQuery.getString("product_name")));
                        } catch (Throwable th) {
                            if (resultSetExecuteQuery != null) {
                                try {
                                    resultSetExecuteQuery.close();
                                } catch (Throwable th2) {
                                    th.addSuppressed(th2);
                                }
                            }
                            throw th;
                        }
                    }
                    if (resultSetExecuteQuery != null) {
                        resultSetExecuteQuery.close();
                    }
                    if (statementCreateStatement != null) {
                        statementCreateStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Throwable th3) {
                    if (statementCreateStatement != null) {
                        try {
                            statementCreateStatement.close();
                        } catch (Throwable th4) {
                            th3.addSuppressed(th4);
                        }
                    }
                    throw th3;
                }
            } finally {
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
                Statement statementCreateStatement = connection.createStatement();
                try {
                    ResultSet resultSetExecuteQuery = statementCreateStatement.executeQuery("SELECT * FROM catalog_items");
                    while (resultSetExecuteQuery.next()) {
                        try {
                            if (!resultSetExecuteQuery.getString("item_ids").equals("0")) {
                                if (resultSetExecuteQuery.getString("catalog_name").contains("HABBO_CLUB_")) {
                                    this.clubItems.add(new CatalogItem(resultSetExecuteQuery));
                                } else {
                                    CatalogPage catalogPage = (CatalogPage) this.catalogPages.get(resultSetExecuteQuery.getInt("page_id"));
                                    if (catalogPage != null) {
                                        CatalogItem catalogItem = catalogPage.getCatalogItem(resultSetExecuteQuery.getInt("id"));
                                        if (catalogItem == null) {
                                            catalogItemAmount++;
                                            catalogItem = new CatalogItem(resultSetExecuteQuery);
                                            catalogPage.addItem(catalogItem);
                                            if (catalogItem.getOfferId() != -1) {
                                                catalogPage.addOfferId(catalogItem.getOfferId());
                                                this.offerDefs.put(catalogItem.getOfferId(), catalogItem.getId());
                                            }
                                        } else {
                                            catalogItem.update(resultSetExecuteQuery);
                                        }
                                        if (catalogItem.isLimited()) {
                                            createOrUpdateLimitedConfig(catalogItem);
                                        }
                                    }
                                }
                            }
                        } catch (Throwable th) {
                            if (resultSetExecuteQuery != null) {
                                try {
                                    resultSetExecuteQuery.close();
                                } catch (Throwable th2) {
                                    th.addSuppressed(th2);
                                }
                            }
                            throw th;
                        }
                    }
                    if (resultSetExecuteQuery != null) {
                        resultSetExecuteQuery.close();
                    }
                    if (statementCreateStatement != null) {
                        statementCreateStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Throwable th3) {
                    if (statementCreateStatement != null) {
                        try {
                            statementCreateStatement.close();
                        } catch (Throwable th4) {
                            th3.addSuppressed(th4);
                        }
                    }
                    throw th3;
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        for (CatalogPage catalogPage2 : this.catalogPages.valueCollection()) {
            Iterator<Integer> it = catalogPage2.getIncluded().iterator();
            while (it.hasNext()) {
                CatalogPage catalogPage3 = (CatalogPage) this.catalogPages.get(it.next().intValue());
                if (catalogPage3 != null) {
                    catalogPage2.getCatalogItems().putAll(catalogPage3.getCatalogItems());
                }
            }
        }
    }

    private void loadClubOffers() {
        this.clubOffers.clear();
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM catalog_club_offers WHERE enabled = ?");
                try {
                    preparedStatementPrepareStatement.setString(1, "1");
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            this.clubOffers.put(Integer.valueOf(resultSetExecuteQuery.getInt("id")), new ClubOffer(resultSetExecuteQuery));
                        } catch (Throwable th) {
                            if (resultSetExecuteQuery != null) {
                                try {
                                    resultSetExecuteQuery.close();
                                } catch (Throwable th2) {
                                    th.addSuppressed(th2);
                                }
                            }
                            throw th;
                        }
                    }
                    if (resultSetExecuteQuery != null) {
                        resultSetExecuteQuery.close();
                    }
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Throwable th3) {
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th4) {
                            th3.addSuppressed(th4);
                        }
                    }
                    throw th3;
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }

    private void loadTargetOffers() {
        Connection connection;
        synchronized (this.targetOffers) {
            this.targetOffers.clear();
            try {
                connection = Emulator.getDatabase().getDataSource().getConnection();
            } catch (SQLException e) {
                LOGGER.error("Caught SQL exception", e);
            }
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM catalog_target_offers WHERE end_timestamp > ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, Emulator.getIntUnixTimestamp());
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            this.targetOffers.put(Integer.valueOf(resultSetExecuteQuery.getInt("id")), new TargetOffer(resultSetExecuteQuery));
                        } catch (Throwable th) {
                            if (resultSetExecuteQuery != null) {
                                try {
                                    resultSetExecuteQuery.close();
                                } catch (Throwable th2) {
                                    th.addSuppressed(th2);
                                }
                            }
                            throw th;
                        }
                    }
                    if (resultSetExecuteQuery != null) {
                        resultSetExecuteQuery.close();
                    }
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Throwable th3) {
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th4) {
                            th3.addSuppressed(th4);
                        }
                    }
                    throw th3;
                }
            } catch (Throwable th5) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Throwable th6) {
                        th5.addSuppressed(th6);
                    }
                }
                throw th5;
            }
        }
    }

    private void loadVouchers() {
        synchronized (this.vouchers) {
            this.vouchers.clear();
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    Statement statementCreateStatement = connection.createStatement();
                    try {
                        ResultSet resultSetExecuteQuery = statementCreateStatement.executeQuery("SELECT * FROM vouchers");
                        while (resultSetExecuteQuery.next()) {
                            try {
                                this.vouchers.add(new Voucher(resultSetExecuteQuery));
                            } catch (Throwable th) {
                                if (resultSetExecuteQuery != null) {
                                    try {
                                        resultSetExecuteQuery.close();
                                    } catch (Throwable th2) {
                                        th.addSuppressed(th2);
                                    }
                                }
                                throw th;
                            }
                        }
                        if (resultSetExecuteQuery != null) {
                            resultSetExecuteQuery.close();
                        }
                        if (statementCreateStatement != null) {
                            statementCreateStatement.close();
                        }
                        if (connection != null) {
                            connection.close();
                        }
                    } catch (Throwable th3) {
                        if (statementCreateStatement != null) {
                            try {
                                statementCreateStatement.close();
                            } catch (Throwable th4) {
                                th3.addSuppressed(th4);
                            }
                        }
                        throw th3;
                    }
                } catch (Throwable th5) {
                    if (connection != null) {
                        try {
                            connection.close();
                        } catch (Throwable th6) {
                            th5.addSuppressed(th6);
                        }
                    }
                    throw th5;
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
                    Statement statementCreateStatement = connection.createStatement();
                    try {
                        ResultSet resultSetExecuteQuery = statementCreateStatement.executeQuery("SELECT * FROM recycler_prizes");
                        while (resultSetExecuteQuery.next()) {
                            try {
                                Item item = Emulator.getGameEnvironment().getItemManager().getItem(resultSetExecuteQuery.getInt("item_id"));
                                if (item != null) {
                                    if (this.prizes.get(Integer.valueOf(resultSetExecuteQuery.getInt("rarity"))) == null) {
                                        this.prizes.put(Integer.valueOf(resultSetExecuteQuery.getInt("rarity")), new THashSet());
                                    }
                                    ((THashSet) this.prizes.get(Integer.valueOf(resultSetExecuteQuery.getInt("rarity")))).add(item);
                                } else {
                                    LOGGER.error("Cannot load item with ID: {} as recycler reward!", Integer.valueOf(resultSetExecuteQuery.getInt("item_id")));
                                }
                            } catch (Throwable th) {
                                if (resultSetExecuteQuery != null) {
                                    try {
                                        resultSetExecuteQuery.close();
                                    } catch (Throwable th2) {
                                        th.addSuppressed(th2);
                                    }
                                }
                                throw th;
                            }
                        }
                        if (resultSetExecuteQuery != null) {
                            resultSetExecuteQuery.close();
                        }
                        if (statementCreateStatement != null) {
                            statementCreateStatement.close();
                        }
                        if (connection != null) {
                            connection.close();
                        }
                    } catch (Throwable th3) {
                        if (statementCreateStatement != null) {
                            try {
                                statementCreateStatement.close();
                            } catch (Throwable th4) {
                                th3.addSuppressed(th4);
                            }
                        }
                        throw th3;
                    }
                } catch (Throwable th5) {
                    if (connection != null) {
                        try {
                            connection.close();
                        } catch (Throwable th6) {
                            th5.addSuppressed(th6);
                        }
                    }
                    throw th5;
                }
            } catch (SQLException e) {
                LOGGER.error("Caught SQL exception", e);
            }
        }
    }

    public void loadGiftWrappers() {
        Connection connection;
        synchronized (this.giftWrappers) {
            synchronized (this.giftFurnis) {
                this.giftWrappers.clear();
                this.giftFurnis.clear();
                try {
                    connection = Emulator.getDatabase().getDataSource().getConnection();
                } catch (SQLException e) {
                    LOGGER.error("Caught SQL exception", e);
                }
                try {
                    Statement statementCreateStatement = connection.createStatement();
                    try {
                        ResultSet resultSetExecuteQuery = statementCreateStatement.executeQuery("SELECT * FROM gift_wrappers ORDER BY sprite_id DESC");
                        while (resultSetExecuteQuery.next()) {
                            try {
                                switch (resultSetExecuteQuery.getString("type")) {
                                    case "wrapper":
                                        this.giftWrappers.put(Integer.valueOf(resultSetExecuteQuery.getInt("sprite_id")), Integer.valueOf(resultSetExecuteQuery.getInt("item_id")));
                                        break;
                                    case "gift":
                                        this.giftFurnis.put(Integer.valueOf(resultSetExecuteQuery.getInt("sprite_id")), Integer.valueOf(resultSetExecuteQuery.getInt("item_id")));
                                        break;
                                }
                            } catch (Throwable th) {
                                if (resultSetExecuteQuery != null) {
                                    try {
                                        resultSetExecuteQuery.close();
                                    } catch (Throwable th2) {
                                        th.addSuppressed(th2);
                                    }
                                }
                                throw th;
                            }
                        }
                        if (resultSetExecuteQuery != null) {
                            resultSetExecuteQuery.close();
                        }
                        if (statementCreateStatement != null) {
                            statementCreateStatement.close();
                        }
                        if (connection != null) {
                            connection.close();
                        }
                    } catch (Throwable th3) {
                        if (statementCreateStatement != null) {
                            try {
                                statementCreateStatement.close();
                            } catch (Throwable th4) {
                                th3.addSuppressed(th4);
                            }
                        }
                        throw th3;
                    }
                } catch (Throwable th5) {
                    if (connection != null) {
                        try {
                            connection.close();
                        } catch (Throwable th6) {
                            th5.addSuppressed(th6);
                        }
                    }
                    throw th5;
                }
            }
        }
    }

    private void loadClothing() {
        Connection connection;
        synchronized (this.clothing) {
            this.clothing.clear();
            try {
                connection = Emulator.getDatabase().getDataSource().getConnection();
            } catch (SQLException e) {
                LOGGER.error("Caught SQL exception", e);
            }
            try {
                Statement statementCreateStatement = connection.createStatement();
                try {
                    ResultSet resultSetExecuteQuery = statementCreateStatement.executeQuery("SELECT * FROM catalog_clothing");
                    while (resultSetExecuteQuery.next()) {
                        try {
                            this.clothing.put(Integer.valueOf(resultSetExecuteQuery.getInt("id")), new ClothItem(resultSetExecuteQuery));
                        } catch (Throwable th) {
                            if (resultSetExecuteQuery != null) {
                                try {
                                    resultSetExecuteQuery.close();
                                } catch (Throwable th2) {
                                    th.addSuppressed(th2);
                                }
                            }
                            throw th;
                        }
                    }
                    if (resultSetExecuteQuery != null) {
                        resultSetExecuteQuery.close();
                    }
                    if (statementCreateStatement != null) {
                        statementCreateStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Throwable th3) {
                    if (statementCreateStatement != null) {
                        try {
                            statementCreateStatement.close();
                        } catch (Throwable th4) {
                            th3.addSuppressed(th4);
                        }
                    }
                    throw th3;
                }
            } catch (Throwable th5) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Throwable th6) {
                        th5.addSuppressed(th6);
                    }
                }
                throw th5;
            }
        }
    }

    public ClothItem getClothing(String str) {
        for (ClothItem clothItem : this.clothing.values()) {
            if (clothItem.name.equalsIgnoreCase(str)) {
                return clothItem;
            }
        }
        return null;
    }

    public Voucher getVoucher(String str) {
        synchronized (this.vouchers) {
            for (Voucher voucher : this.vouchers) {
                if (voucher.code.equals(str)) {
                    return voucher;
                }
            }
            return null;
        }
    }

    public void redeemVoucher(GameClient gameClient, String str) {
        CatalogItem catalogItem;
        Habbo habbo = gameClient.getHabbo();
        if (habbo == null) {
            return;
        }
        Voucher voucher = Emulator.getGameEnvironment().getCatalogManager().getVoucher(str);
        if (voucher == null) {
            gameClient.sendResponse(new RedeemVoucherErrorComposer(0));
            return;
        }
        if (voucher.isExhausted()) {
            gameClient.sendResponse(new RedeemVoucherErrorComposer(Emulator.getGameEnvironment().getCatalogManager().deleteVoucher(voucher) ? 0 : 1));
            return;
        }
        if (voucher.hasUserExhausted(habbo.getHabboInfo().getId())) {
            gameClient.sendResponse(new ModToolIssueHandledComposer("You have exceeded the limit for redeeming this voucher."));
            return;
        }
        voucher.addHistoryEntry(habbo.getHabboInfo().getId());
        if (voucher.points > 0) {
            gameClient.getHabbo().givePoints(voucher.pointsType, voucher.points);
        }
        if (voucher.credits > 0) {
            gameClient.getHabbo().giveCredits(voucher.credits);
        }
        if (voucher.catalogItemId > 0 && (catalogItem = getCatalogItem(voucher.catalogItemId)) != null) {
            purchaseItem(null, catalogItem, gameClient.getHabbo(), 1, Emulator.PREVIEW, true);
        }
        gameClient.sendResponse(new RedeemVoucherOKComposer());
    }

    public boolean deleteVoucher(Voucher voucher) {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("DELETE FROM vouchers WHERE code = ?");
                try {
                    preparedStatementPrepareStatement.setString(1, voucher.code);
                    synchronized (this.vouchers) {
                        this.vouchers.remove(voucher);
                    }
                    boolean z = preparedStatementPrepareStatement.executeUpdate() >= 1;
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                    return z;
                } catch (Throwable th) {
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Throwable th4) {
                        th3.addSuppressed(th4);
                    }
                }
                throw th3;
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
            return false;
        }
    }

    public CatalogPage getCatalogPage(int i) {
        return (CatalogPage) this.catalogPages.get(i);
    }

    public CatalogPage getCatalogPage(String str) {
        return (CatalogPage) this.catalogPages.valueCollection().stream().filter(catalogPage -> {
            return (catalogPage == null || catalogPage.getPageName() == null || !catalogPage.getPageName().equalsIgnoreCase(str)) ? false : true;
        }).findAny().orElse(null);
    }

    public CatalogPage getCatalogPageByLayout(String str) {
        return (CatalogPage) this.catalogPages.valueCollection().stream().filter(catalogPage -> {
            return catalogPage != null && catalogPage.isVisible() && catalogPage.isEnabled() && catalogPage.getRank() < 2 && catalogPage.getLayout() != null && catalogPage.getLayout().equalsIgnoreCase(str);
        }).findAny().orElse(null);
    }

    public CatalogItem getCatalogItem(final int i) {
        final CatalogItem[] catalogItemArr = {null};
        synchronized (this.catalogPages) {
            this.catalogPages.forEachValue(new TObjectProcedure<CatalogPage>() { // from class: com.eu.habbo.habbohotel.catalog.CatalogManager.2
                public boolean execute(CatalogPage catalogPage) {
                    catalogItemArr[0] = catalogPage.getCatalogItem(i);
                    return catalogItemArr[0] == null;
                }
            });
        }
        return catalogItemArr[0];
    }

    public List<CatalogPage> getCatalogPages(int i, final Habbo habbo) {
        final ArrayList arrayList = new ArrayList();
        ((CatalogPage) this.catalogPages.get(i)).childPages.forEachValue(new TObjectProcedure<CatalogPage>() { // from class: com.eu.habbo.habbohotel.catalog.CatalogManager.3
            public boolean execute(CatalogPage catalogPage) {
                boolean z = catalogPage.visible;
                boolean z2 = catalogPage.getRank() <= habbo.getHabboInfo().getRank().getId();
                boolean z3 = true;
                if (catalogPage.isClubOnly() && !habbo.getHabboInfo().getHabboStats().hasActiveClub()) {
                    z3 = false;
                }
                if (!z || !z2 || !z3) {
                    return true;
                }
                arrayList.add(catalogPage);
                return true;
            }
        });
        Collections.sort(arrayList);
        return arrayList;
    }

    public TIntObjectMap<CatalogFeaturedPage> getCatalogFeaturedPages() {
        return this.catalogFeaturedPages;
    }

    public CatalogItem getClubItem(int i) {
        synchronized (this.clubItems) {
            TObjectHashIterator it = this.clubItems.iterator();
            while (it.hasNext()) {
                CatalogItem catalogItem = (CatalogItem) it.next();
                if (catalogItem.getId() == i) {
                    return catalogItem;
                }
            }
            return null;
        }
    }

    public boolean moveCatalogItem(CatalogItem catalogItem, int i) {
        CatalogPage catalogPage = getCatalogPage(catalogItem.getPageId());
        if (catalogPage == null) {
            return false;
        }
        catalogPage.getCatalogItems().remove(catalogItem.getId());
        CatalogPage catalogPage2 = getCatalogPage(i);
        if (catalogPage2 == null) {
            return false;
        }
        catalogPage2.getCatalogItems().put(catalogItem.getId(), catalogItem);
        catalogItem.setPageId(i);
        catalogItem.setNeedsUpdate(true);
        catalogItem.run();
        return true;
    }

    public Item getRandomRecyclerPrize() {
        int i = 1;
        if (Emulator.getRandom().nextInt(Emulator.getConfig().getInt("hotel.ecotron.rarity.chance.5")) + 1 == Emulator.getConfig().getInt("hotel.ecotron.rarity.chance.5")) {
            i = 5;
        } else if (Emulator.getRandom().nextInt(Emulator.getConfig().getInt("hotel.ecotron.rarity.chance.4")) + 1 == Emulator.getConfig().getInt("hotel.ecotron.rarity.chance.4")) {
            i = 4;
        } else if (Emulator.getRandom().nextInt(Emulator.getConfig().getInt("hotel.ecotron.rarity.chance.3")) + 1 == Emulator.getConfig().getInt("hotel.ecotron.rarity.chance.3")) {
            i = 3;
        } else if (Emulator.getRandom().nextInt(Emulator.getConfig().getInt("hotel.ecotron.rarity.chance.2")) + 1 == Emulator.getConfig().getInt("hotel.ecotron.rarity.chance.2")) {
            i = 2;
        }
        if (this.prizes.containsKey(Integer.valueOf(i)) && !((THashSet) this.prizes.get(Integer.valueOf(i))).isEmpty()) {
            return (Item) ((THashSet) this.prizes.get(Integer.valueOf(i))).toArray()[Emulator.getRandom().nextInt(((THashSet) this.prizes.get(Integer.valueOf(i))).size())];
        }
        LOGGER.error("No rewards specified for rarity level {}", Integer.valueOf(i));
        return null;
    }

    public CatalogPage createCatalogPage(String str, String str2, int i, int i2, CatalogPageLayouts catalogPageLayouts, int i3, int i4) {
        CatalogPage catalogPage = null;
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO catalog_pages (parent_id, caption, caption_save, icon_image, visible, enabled, min_rank, page_layout, room_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", 1);
                try {
                    preparedStatementPrepareStatement.setInt(1, i4);
                    preparedStatementPrepareStatement.setString(2, str);
                    preparedStatementPrepareStatement.setString(3, str2);
                    preparedStatementPrepareStatement.setInt(4, i2);
                    preparedStatementPrepareStatement.setString(5, "1");
                    preparedStatementPrepareStatement.setString(6, "1");
                    preparedStatementPrepareStatement.setInt(7, i3);
                    preparedStatementPrepareStatement.setString(8, catalogPageLayouts.name());
                    preparedStatementPrepareStatement.setInt(9, i);
                    preparedStatementPrepareStatement.execute();
                    ResultSet generatedKeys = preparedStatementPrepareStatement.getGeneratedKeys();
                    try {
                        if (generatedKeys.next()) {
                            PreparedStatement preparedStatementPrepareStatement2 = connection.prepareStatement("SELECT * FROM catalog_pages WHERE id = ?");
                            try {
                                preparedStatementPrepareStatement2.setInt(1, generatedKeys.getInt(1));
                                ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement2.executeQuery();
                                try {
                                    if (resultSetExecuteQuery.next()) {
                                        Class cls = (Class) pageDefinitions.get(resultSetExecuteQuery.getString("page_layout"));
                                        if (cls != null) {
                                            try {
                                                catalogPage = (CatalogPage) cls.getConstructor(ResultSet.class).newInstance(resultSetExecuteQuery);
                                            } catch (Exception e) {
                                                LOGGER.error("Caught exception", e);
                                            }
                                        } else {
                                            LOGGER.error("Unknown page layout: {}", resultSetExecuteQuery.getString("page_layout"));
                                        }
                                    }
                                    if (resultSetExecuteQuery != null) {
                                        resultSetExecuteQuery.close();
                                    }
                                    if (preparedStatementPrepareStatement2 != null) {
                                        preparedStatementPrepareStatement2.close();
                                    }
                                } catch (Throwable th) {
                                    if (resultSetExecuteQuery != null) {
                                        try {
                                            resultSetExecuteQuery.close();
                                        } catch (Throwable th2) {
                                            th.addSuppressed(th2);
                                        }
                                    }
                                    throw th;
                                }
                            } catch (Throwable th3) {
                                if (preparedStatementPrepareStatement2 != null) {
                                    try {
                                        preparedStatementPrepareStatement2.close();
                                    } catch (Throwable th4) {
                                        th3.addSuppressed(th4);
                                    }
                                }
                                throw th3;
                            }
                        }
                        if (generatedKeys != null) {
                            generatedKeys.close();
                        }
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
                        }
                        if (connection != null) {
                            connection.close();
                        }
                    } catch (Throwable th5) {
                        if (generatedKeys != null) {
                            try {
                                generatedKeys.close();
                            } catch (Throwable th6) {
                                th5.addSuppressed(th6);
                            }
                        }
                        throw th5;
                    }
                } catch (Throwable th7) {
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th8) {
                            th7.addSuppressed(th8);
                        }
                    }
                    throw th7;
                }
            } finally {
            }
        } catch (SQLException e2) {
            LOGGER.error("Caught SQL exception", e2);
        }
        if (catalogPage != null) {
            this.catalogPages.put(catalogPage.getId(), catalogPage);
        }
        return catalogPage;
    }

    public CatalogLimitedConfiguration getLimitedConfig(CatalogItem catalogItem) {
        CatalogLimitedConfiguration catalogLimitedConfiguration;
        synchronized (this.limitedNumbers) {
            catalogLimitedConfiguration = (CatalogLimitedConfiguration) this.limitedNumbers.get(Integer.valueOf(catalogItem.getId()));
        }
        return catalogLimitedConfiguration;
    }

    public CatalogLimitedConfiguration createOrUpdateLimitedConfig(CatalogItem catalogItem) {
        if (!catalogItem.isLimited()) {
            return null;
        }
        CatalogLimitedConfiguration catalogLimitedConfiguration = (CatalogLimitedConfiguration) this.limitedNumbers.get(Integer.valueOf(catalogItem.getId()));
        if (catalogLimitedConfiguration == null) {
            catalogLimitedConfiguration = new CatalogLimitedConfiguration(catalogItem.getId(), new LinkedList(), 0);
            catalogLimitedConfiguration.generateNumbers(1, catalogItem.limitedStack);
            this.limitedNumbers.put(Integer.valueOf(catalogItem.getId()), catalogLimitedConfiguration);
        } else if (catalogLimitedConfiguration.getTotalSet() != catalogItem.limitedStack) {
            if (catalogLimitedConfiguration.getTotalSet() == 0) {
                catalogLimitedConfiguration.setTotalSet(catalogItem.limitedStack);
            } else if (catalogItem.limitedStack > catalogLimitedConfiguration.getTotalSet()) {
                catalogLimitedConfiguration.generateNumbers(catalogItem.limitedStack + 1, catalogItem.limitedStack - catalogLimitedConfiguration.getTotalSet());
            } else {
                catalogItem.limitedStack = catalogLimitedConfiguration.getTotalSet();
            }
        }
        return catalogLimitedConfiguration;
    }

    public void dispose() {
        TIntObjectIterator it = this.catalogPages.iterator();
        while (it.hasNext()) {
            it.advance();
            for (CatalogItem catalogItem : ((CatalogPage) it.value()).getCatalogItems().valueCollection()) {
                catalogItem.run();
                if (catalogItem.isLimited()) {
                    ((CatalogLimitedConfiguration) this.limitedNumbers.get(Integer.valueOf(catalogItem.getId()))).run();
                }
            }
        }
        LOGGER.info("Catalog Manager -> Disposed!");
    }

    public void purchaseItem(CatalogPage catalogPage, CatalogItem catalogItem, Habbo habbo, int i, String str, boolean z) {
        CatalogLimitedConfiguration limitedConfig;
        int totalSet;
        int number;
        THashSet tHashSet;
        if (catalogItem == null || habbo.getHabboStats().isPurchasingFurniture) {
            habbo.getClient().sendResponse(new AlertPurchaseFailedComposer(0).compose());
            return;
        }
        habbo.getHabboStats().isPurchasingFurniture = true;
        try {
            if (catalogItem.isClubOnly() && !habbo.getClient().getHabbo().getHabboStats().hasActiveClub()) {
                habbo.getClient().sendResponse(new AlertPurchaseUnavailableComposer(1));
                habbo.getHabboStats().isPurchasingFurniture = false;
                return;
            }
            if (i <= 0) {
                habbo.getClient().sendResponse(new AlertPurchaseUnavailableComposer(0));
                habbo.getHabboStats().isPurchasingFurniture = false;
                return;
            }
            try {
                limitedConfig = null;
                totalSet = 0;
                number = 0;
                if (catalogItem.isLimited()) {
                    i = 1;
                    if (getLimitedConfig(catalogItem).available() == 0) {
                        habbo.getClient().sendResponse(new AlertLimitedSoldOutComposer());
                        habbo.getHabboStats().isPurchasingFurniture = false;
                        return;
                    } else if (Emulator.getConfig().getBoolean("hotel.catalog.ltd.limit.enabled")) {
                        int i2 = Emulator.getConfig().getInt("hotel.purchase.ltd.limit.daily.total");
                        if (habbo.getHabboStats().totalLtds() >= i2) {
                            habbo.alert(Emulator.getTexts().getValue("error.catalog.buy.limited.daily.total").replace("%itemname%", ((Item) catalogItem.getBaseItems().iterator().next()).getFullName()).replace("%limit%", i2 + Emulator.PREVIEW));
                            habbo.getHabboStats().isPurchasingFurniture = false;
                            return;
                        } else {
                            int i3 = Emulator.getConfig().getInt("hotel.purchase.ltd.limit.daily.item");
                            if (habbo.getHabboStats().totalLtds(catalogItem.id) >= i3) {
                                habbo.alert(Emulator.getTexts().getValue("error.catalog.buy.limited.daily.item").replace("%itemname%", ((Item) catalogItem.getBaseItems().iterator().next()).getFullName()).replace("%limit%", i3 + Emulator.PREVIEW));
                                habbo.getHabboStats().isPurchasingFurniture = false;
                                return;
                            }
                        }
                    }
                }
                if (i > 1) {
                    if (i == catalogItem.getAmount()) {
                        i = 1;
                    } else if (i * catalogItem.getAmount() > 100) {
                        habbo.alert("Whoops! You tried to buy this " + (i * catalogItem.getAmount()) + " times. This must've been a mistake.");
                        habbo.getClient().sendResponse(new AlertPurchaseUnavailableComposer(0));
                        habbo.getHabboStats().isPurchasingFurniture = false;
                        return;
                    }
                }
                tHashSet = new THashSet();
            } catch (Exception e) {
                LOGGER.error("Exception caught", e);
                habbo.getClient().sendResponse(new AlertPurchaseFailedComposer(0));
            }
            if (i > 1 && !CatalogItem.haveOffer(catalogItem)) {
                String strReplace = Emulator.getTexts().getValue("scripter.warning.catalog.amount").replace("%username%", habbo.getHabboInfo().getUsername()).replace("%itemname%", catalogItem.getName()).replace("%pagename%", catalogPage.getCaption());
                ScripterManager.scripterDetected(habbo.getClient(), strReplace);
                LOGGER.info(strReplace);
                habbo.getClient().sendResponse(new AlertPurchaseUnavailableComposer(0));
                habbo.getHabboStats().isPurchasingFurniture = false;
                return;
            }
            if (catalogItem.isLimited()) {
                limitedConfig = getLimitedConfig(catalogItem);
                if (limitedConfig == null) {
                    limitedConfig = createOrUpdateLimitedConfig(catalogItem);
                }
                number = limitedConfig.getNumber();
                totalSet = limitedConfig.getTotalSet();
            }
            int iCalculateDiscountedPrice = z ? 0 : calculateDiscountedPrice(catalogItem.getCredits(), i, catalogItem);
            int iCalculateDiscountedPrice2 = z ? 0 : calculateDiscountedPrice(catalogItem.getPoints(), i, catalogItem);
            if (iCalculateDiscountedPrice <= 0 || habbo.getHabboInfo().getCredits() - iCalculateDiscountedPrice >= 0) {
                if (iCalculateDiscountedPrice2 > 0 && habbo.getHabboInfo().getCurrencyAmount(catalogItem.getPointsType()) - iCalculateDiscountedPrice2 < 0) {
                    habbo.getHabboStats().isPurchasingFurniture = false;
                    return;
                }
                ArrayList arrayList = new ArrayList();
                HashMap map = new HashMap();
                boolean z2 = false;
                for (int i4 = 0; i4 < i; i4++) {
                    habbo.getHabboStats().addLtdLog(catalogItem.getId(), Emulator.getIntUnixTimestamp());
                    TObjectHashIterator it = catalogItem.getBaseItems().iterator();
                    while (it.hasNext()) {
                        Item item = (Item) it.next();
                        for (int i5 = 0; i5 < catalogItem.getItemAmount(item.getId()); i5++) {
                            if (item.getName().startsWith("rentable_bot_") || item.getName().startsWith("bot_")) {
                                String strReplace2 = catalogItem.getName().replace("rentable_bot_", Emulator.PREVIEW).replace("bot_", Emulator.PREVIEW).replace("visitor_logger", "visitor_log");
                                THashMap<String, String> tHashMap = new THashMap<>();
                                for (String str2 : catalogItem.getExtradata().split(";")) {
                                    if (str2.contains(":")) {
                                        tHashMap.put(str2.split(":")[0], str2.split(":")[1]);
                                    }
                                }
                                Bot botCreateBot = Emulator.getGameEnvironment().getBotManager().createBot(tHashMap, strReplace2);
                                if (botCreateBot == null) {
                                    throw new Exception("Failed to create bot of type: " + strReplace2);
                                }
                                botCreateBot.setOwnerId(habbo.getClient().getHabbo().getHabboInfo().getId());
                                botCreateBot.setOwnerName(habbo.getClient().getHabbo().getHabboInfo().getUsername());
                                botCreateBot.needsUpdate(true);
                                Emulator.getThreading().run(botCreateBot);
                                habbo.getClient().getHabbo().getInventory().getBotsComponent().addBot(botCreateBot);
                                habbo.getClient().sendResponse(new AddBotComposer(botCreateBot));
                                if (!map.containsKey(AddHabboItemComposer.AddHabboItemCategory.BOT)) {
                                    map.put(AddHabboItemComposer.AddHabboItemCategory.BOT, new ArrayList());
                                }
                                ((List) map.get(AddHabboItemComposer.AddHabboItemCategory.BOT)).add(Integer.valueOf(botCreateBot.getId()));
                            } else if (item.getType() == FurnitureType.EFFECT) {
                                int effectM = item.getEffectM();
                                if (habbo.getHabboInfo().getGender().equals(HabboGender.F)) {
                                    effectM = item.getEffectF();
                                }
                                if (effectM > 0) {
                                    habbo.getInventory().getEffectsComponent().createEffect(effectM);
                                }
                            } else if (Item.isPet(item)) {
                                String[] strArrSplit = str.split("\n");
                                if (strArrSplit.length < 3) {
                                    habbo.getClient().sendResponse(new AlertPurchaseFailedComposer(0));
                                    habbo.getHabboStats().isPurchasingFurniture = false;
                                    return;
                                }
                                Pet petCreatePet = null;
                                try {
                                    petCreatePet = Emulator.getGameEnvironment().getPetManager().createPet(item, strArrSplit[0], strArrSplit[1], strArrSplit[2], habbo.getClient());
                                } catch (Exception e2) {
                                    LOGGER.error("Caught exception", e2);
                                    habbo.getClient().sendResponse(new AlertPurchaseFailedComposer(0));
                                }
                                if (petCreatePet == null) {
                                    habbo.getClient().sendResponse(new AlertPurchaseFailedComposer(0));
                                    habbo.getHabboStats().isPurchasingFurniture = false;
                                    return;
                                }
                                habbo.getClient().getHabbo().getInventory().getPetsComponent().addPet(petCreatePet);
                                habbo.getClient().sendResponse(new AddPetComposer(petCreatePet));
                                habbo.getClient().sendResponse(new PetBoughtNotificationComposer(petCreatePet, false));
                                AchievementManager.progressAchievement(habbo.getClient().getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("PetLover"));
                                if (!map.containsKey(AddHabboItemComposer.AddHabboItemCategory.PET)) {
                                    map.put(AddHabboItemComposer.AddHabboItemCategory.PET, new ArrayList());
                                }
                                ((List) map.get(AddHabboItemComposer.AddHabboItemCategory.PET)).add(Integer.valueOf(petCreatePet.getId()));
                            } else if (item.getType() != FurnitureType.BADGE) {
                                if (item.getInteractionType().getType() == InteractionTrophy.class || item.getInteractionType().getType() == InteractionBadgeDisplay.class) {
                                    if (item.getInteractionType().getType() == InteractionBadgeDisplay.class && !habbo.getClient().getHabbo().getInventory().getBadgesComponent().hasBadge(str)) {
                                        ScripterManager.scripterDetected(habbo.getClient(), Emulator.getTexts().getValue("scripter.warning.catalog.badge_display").replace("%username%", habbo.getClient().getHabbo().getHabboInfo().getUsername()).replace("%badge%", str));
                                        str = "UMAD";
                                    }
                                    if (str.length() > Emulator.getConfig().getInt("hotel.trophies.length.max", 300)) {
                                        str = str.substring(0, Emulator.getConfig().getInt("hotel.trophies.length.max", 300));
                                    }
                                    str = habbo.getClient().getHabbo().getHabboInfo().getUsername() + '\t' + Calendar.getInstance().get(5) + "-" + (Calendar.getInstance().get(2) + 1) + "-" + Calendar.getInstance().get(1) + '\t' + Emulator.getGameEnvironment().getWordFilter().filter(str.replace("\t", Emulator.PREVIEW), habbo);
                                }
                                if (InteractionTeleport.class.isAssignableFrom(item.getInteractionType().getType())) {
                                    HabboItem habboItemCreateItem = Emulator.getGameEnvironment().getItemManager().createItem(habbo.getClient().getHabbo().getHabboInfo().getId(), item, totalSet, number, str);
                                    HabboItem habboItemCreateItem2 = Emulator.getGameEnvironment().getItemManager().createItem(habbo.getClient().getHabbo().getHabboInfo().getId(), item, totalSet, number, str);
                                    Emulator.getGameEnvironment().getItemManager().insertTeleportPair(habboItemCreateItem.getId(), habboItemCreateItem2.getId());
                                    tHashSet.add(habboItemCreateItem);
                                    tHashSet.add(habboItemCreateItem2);
                                } else if (item.getInteractionType().getType() == InteractionHopper.class) {
                                    HabboItem habboItemCreateItem3 = Emulator.getGameEnvironment().getItemManager().createItem(habbo.getClient().getHabbo().getHabboInfo().getId(), item, totalSet, number, str);
                                    Emulator.getGameEnvironment().getItemManager().insertHopper(habboItemCreateItem3);
                                    tHashSet.add(habboItemCreateItem3);
                                } else if (item.getInteractionType().getType() == InteractionGuildFurni.class || item.getInteractionType().getType() == InteractionGuildGate.class) {
                                    try {
                                        int i6 = Integer.parseInt(str);
                                        Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(i6);
                                        if (guild != null && Emulator.getGameEnvironment().getGuildManager().getGuildMember(guild, habbo) != null) {
                                            InteractionGuildFurni interactionGuildFurni = (InteractionGuildFurni) Emulator.getGameEnvironment().getItemManager().createItem(habbo.getClient().getHabbo().getHabboInfo().getId(), item, totalSet, number, str);
                                            interactionGuildFurni.setExtradata(Emulator.PREVIEW);
                                            interactionGuildFurni.needsUpdate(true);
                                            Emulator.getThreading().run(interactionGuildFurni);
                                            Emulator.getGameEnvironment().getGuildManager().setGuild(interactionGuildFurni, i6);
                                            tHashSet.add(interactionGuildFurni);
                                            if (item.getName().equals("guild_forum")) {
                                                guild.setForum(true);
                                                guild.needsUpdate = true;
                                                guild.run();
                                            }
                                        }
                                    } catch (Exception e3) {
                                        LOGGER.error("Caught exception", e3);
                                        habbo.getClient().sendResponse(new AlertPurchaseFailedComposer(0));
                                        habbo.getHabboStats().isPurchasingFurniture = false;
                                        return;
                                    }
                                } else if (item.getInteractionType().getType() == InteractionMusicDisc.class) {
                                    SoundTrack soundTrack = Emulator.getGameEnvironment().getItemManager().getSoundTrack(catalogItem.getExtradata());
                                    if (soundTrack == null) {
                                        habbo.getClient().sendResponse(new AlertPurchaseFailedComposer(0));
                                        habbo.getHabboStats().isPurchasingFurniture = false;
                                        return;
                                    } else {
                                        InteractionMusicDisc interactionMusicDisc = (InteractionMusicDisc) Emulator.getGameEnvironment().getItemManager().createItem(habbo.getClient().getHabbo().getHabboInfo().getId(), item, totalSet, number, habbo.getClient().getHabbo().getHabboInfo().getUsername() + "\n" + Calendar.getInstance().get(5) + "\n" + (Calendar.getInstance().get(2) + 1) + "\n" + Calendar.getInstance().get(1) + "\n" + soundTrack.getLength() + "\n" + soundTrack.getName() + "\n" + soundTrack.getId());
                                        interactionMusicDisc.needsUpdate(true);
                                        Emulator.getThreading().run(interactionMusicDisc);
                                        tHashSet.add(interactionMusicDisc);
                                        AchievementManager.progressAchievement(habbo, Emulator.getGameEnvironment().getAchievementManager().getAchievement("MusicCollector"));
                                    }
                                } else {
                                    tHashSet.add(Emulator.getGameEnvironment().getItemManager().createItem(habbo.getClient().getHabbo().getHabboInfo().getId(), item, totalSet, number, str));
                                }
                            } else if (habbo.getInventory().getBadgesComponent().hasBadge(item.getName())) {
                                z2 = true;
                            } else if (!arrayList.contains(item.getName())) {
                                arrayList.add(item.getName());
                            }
                        }
                    }
                }
                if (z2 && catalogItem.getBaseItems().size() == 1) {
                    habbo.getClient().sendResponse(new AlertPurchaseFailedComposer(1));
                    habbo.getHabboStats().isPurchasingFurniture = false;
                    return;
                }
                UserCatalogItemPurchasedEvent userCatalogItemPurchasedEvent = new UserCatalogItemPurchasedEvent(habbo, catalogItem, tHashSet, iCalculateDiscountedPrice, iCalculateDiscountedPrice2, arrayList);
                Emulator.getPluginManager().fireEvent(userCatalogItemPurchasedEvent);
                if (!z && !habbo.getClient().getHabbo().hasPermission(Permission.ACC_INFINITE_CREDITS) && userCatalogItemPurchasedEvent.totalCredits > 0) {
                    habbo.getClient().getHabbo().giveCredits(-userCatalogItemPurchasedEvent.totalCredits);
                }
                if (!z && !habbo.getClient().getHabbo().hasPermission(Permission.ACC_INFINITE_POINTS) && userCatalogItemPurchasedEvent.totalPoints > 0) {
                    habbo.getClient().getHabbo().givePoints(catalogItem.getPointsType(), -userCatalogItemPurchasedEvent.totalPoints);
                }
                if (userCatalogItemPurchasedEvent.itemsList != null && !userCatalogItemPurchasedEvent.itemsList.isEmpty()) {
                    habbo.getClient().getHabbo().getInventory().getItemsComponent().addItems(userCatalogItemPurchasedEvent.itemsList);
                    map.put(AddHabboItemComposer.AddHabboItemCategory.OWNED_FURNI, (List) userCatalogItemPurchasedEvent.itemsList.stream().map((v0) -> {
                        return v0.getId();
                    }).collect(Collectors.toList()));
                    Emulator.getPluginManager().fireEvent(new UserCatalogFurnitureBoughtEvent(habbo, catalogItem, userCatalogItemPurchasedEvent.itemsList));
                    if (limitedConfig != null) {
                        TObjectHashIterator it2 = userCatalogItemPurchasedEvent.itemsList.iterator();
                        while (it2.hasNext()) {
                            limitedConfig.limitedSold(catalogItem.getId(), habbo, (HabboItem) it2.next());
                        }
                    }
                }
                if (!userCatalogItemPurchasedEvent.badges.isEmpty() && !map.containsKey(AddHabboItemComposer.AddHabboItemCategory.BADGE)) {
                    map.put(AddHabboItemComposer.AddHabboItemCategory.BADGE, new ArrayList());
                }
                Iterator<String> it3 = userCatalogItemPurchasedEvent.badges.iterator();
                while (it3.hasNext()) {
                    HabboBadge habboBadge = new HabboBadge(0, it3.next(), 0, habbo);
                    Emulator.getThreading().run(habboBadge);
                    habbo.getInventory().getBadgesComponent().addBadge(habboBadge);
                    habbo.getClient().sendResponse(new AddUserBadgeComposer(habboBadge));
                    THashMap tHashMap2 = new THashMap();
                    tHashMap2.put("display", "BUBBLE");
                    tHashMap2.put("image", "${image.library.url}album1584/" + habboBadge.getCode() + ".gif");
                    tHashMap2.put("message", Emulator.getTexts().getValue("commands.generic.cmd_badge.received"));
                    habbo.getClient().sendResponse(new BubbleAlertComposer(BubbleAlertKeys.RECEIVED_BADGE.key, (THashMap<String, String>) tHashMap2));
                    ((List) map.get(AddHabboItemComposer.AddHabboItemCategory.BADGE)).add(Integer.valueOf(habboBadge.getId()));
                }
                habbo.getClient().getHabbo().getHabboStats().addPurchase(userCatalogItemPurchasedEvent.catalogItem);
                habbo.getClient().sendResponse(new AddHabboItemComposer(map));
                habbo.getClient().sendResponse(new PurchaseOKComposer(userCatalogItemPurchasedEvent.catalogItem));
                habbo.getClient().sendResponse(new InventoryRefreshComposer());
                THashSet tHashSet2 = new THashSet();
                TObjectHashIterator it4 = userCatalogItemPurchasedEvent.itemsList.iterator();
                while (it4.hasNext()) {
                    tHashSet2.add(((HabboItem) it4.next()).getId() + Emulator.PREVIEW);
                }
                if (!z) {
                    Emulator.getThreading().run(new CatalogPurchaseLogEntry(Emulator.getIntUnixTimestamp(), userCatalogItemPurchasedEvent.habbo.getHabboInfo().getId(), userCatalogItemPurchasedEvent.catalogItem != null ? userCatalogItemPurchasedEvent.catalogItem.getId() : 0, String.join(";", (Iterable<? extends CharSequence>) tHashSet2), userCatalogItemPurchasedEvent.catalogItem != null ? userCatalogItemPurchasedEvent.catalogItem.getName() : Emulator.PREVIEW, userCatalogItemPurchasedEvent.totalCredits, userCatalogItemPurchasedEvent.totalPoints, catalogItem != null ? catalogItem.getPointsType() : 0, i));
                }
                habbo.getHabboStats().isPurchasingFurniture = false;
            }
        } finally {
            habbo.getHabboStats().isPurchasingFurniture = false;
        }
    }

    public List<ClubOffer> getClubOffers() {
        ArrayList arrayList = new ArrayList();
        for (Map.Entry entry : this.clubOffers.entrySet()) {
            if (!((ClubOffer) entry.getValue()).isDeal()) {
                arrayList.add((ClubOffer) entry.getValue());
            }
        }
        return arrayList;
    }

    public TargetOffer getTargetOffer(int i) {
        return (TargetOffer) this.targetOffers.get(Integer.valueOf(i));
    }

    private int calculateDiscountedPrice(int i, int i2, CatalogItem catalogItem) {
        if (!CatalogItem.haveOffer(catalogItem)) {
            return i * i2;
        }
        int i3 = i2 / DiscountComposer.DISCOUNT_BATCH_SIZE;
        if (i3 >= DiscountComposer.MINIMUM_DISCOUNTS_FOR_BONUS) {
            i = (i2 % DiscountComposer.DISCOUNT_BATCH_SIZE == DiscountComposer.DISCOUNT_BATCH_SIZE - 1 ? 1 : 0) + (i3 - DiscountComposer.MINIMUM_DISCOUNTS_FOR_BONUS);
        }
        int i4 = 0;
        for (int i5 : DiscountComposer.ADDITIONAL_DISCOUNT_THRESHOLDS) {
            if (i2 >= i5) {
                i4++;
            }
        }
        return Math.max(0, i * (i2 - (((i3 * DiscountComposer.DISCOUNT_AMOUNT_PER_BATCH) + i) + i4)));
    }
}
