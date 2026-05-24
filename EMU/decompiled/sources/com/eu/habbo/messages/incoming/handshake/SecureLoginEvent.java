package com.eu.habbo.messages.incoming.handshake;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.messenger.Messenger;
import com.eu.habbo.habbohotel.modtool.ModToolSanctionItem;
import com.eu.habbo.habbohotel.modtool.ModToolSanctions;
import com.eu.habbo.habbohotel.navigation.NavigatorFavoriteFilter;
import com.eu.habbo.habbohotel.navigation.NavigatorSavedSearch;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.RoomManager;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboManager;
import com.eu.habbo.habbohotel.users.clothingvalidation.ClothingValidationManager;
import com.eu.habbo.habbohotel.users.subscriptions.Subscription;
import com.eu.habbo.habbohotel.users.subscriptions.SubscriptionHabboClub;
import com.eu.habbo.messages.NoAuthMessage;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.gamecenter.GameCenterAccountInfoComposer;
import com.eu.habbo.messages.outgoing.gamecenter.GameCenterGameListComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.GenericAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.MessagesForYouComposer;
import com.eu.habbo.messages.outgoing.habboway.nux.NewUserIdentityComposer;
import com.eu.habbo.messages.outgoing.handshake.AvailabilityStatusMessageComposer;
import com.eu.habbo.messages.outgoing.handshake.EnableNotificationsComposer;
import com.eu.habbo.messages.outgoing.handshake.PingComposer;
import com.eu.habbo.messages.outgoing.handshake.SecureLoginOKComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryAchievementsComposer;
import com.eu.habbo.messages.outgoing.inventory.UserEffectsListComposer;
import com.eu.habbo.messages.outgoing.modtool.CfhTopicsMessageComposer;
import com.eu.habbo.messages.outgoing.modtool.ModToolComposer;
import com.eu.habbo.messages.outgoing.modtool.ModToolSanctionInfoComposer;
import com.eu.habbo.messages.outgoing.mysterybox.MysteryBoxKeysComposer;
import com.eu.habbo.messages.outgoing.navigator.NewNavigatorSavedSearchesComposer;
import com.eu.habbo.messages.outgoing.unknown.BuildersClubExpiredComposer;
import com.eu.habbo.messages.outgoing.users.FavoriteRoomsCountComposer;
import com.eu.habbo.messages.outgoing.users.UserAchievementScoreComposer;
import com.eu.habbo.messages.outgoing.users.UserClothesComposer;
import com.eu.habbo.messages.outgoing.users.UserClubComposer;
import com.eu.habbo.messages.outgoing.users.UserHomeRoomComposer;
import com.eu.habbo.messages.outgoing.users.UserPermissionsComposer;
import com.eu.habbo.plugin.events.emulator.SSOAuthenticationEvent;
import com.eu.habbo.plugin.events.users.UserLoginEvent;
import java.util.ArrayList;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/handshake/SecureLoginEvent.class */
@NoAuthMessage
public class SecureLoginEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecureLoginEvent.class);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        ArrayList arrayList;
        if (!this.client.getChannel().isOpen()) {
            Emulator.getGameServer().getGameClientManager().disposeClient(this.client);
            return;
        }
        if (Emulator.isReady) {
            if (Emulator.getConfig().getBoolean("encryption.forced", false) && Emulator.getCrypto().isEnabled() && !this.client.isHandshakeFinished()) {
                Emulator.getGameServer().getGameClientManager().disposeClient(this.client);
                LOGGER.warn("Encryption is forced and TLS Handshake isn't finished! Closed connection...");
                return;
            }
            String strReplace = this.packet.readString().replace(" ", Emulator.PREVIEW);
            if (((SSOAuthenticationEvent) Emulator.getPluginManager().fireEvent(new SSOAuthenticationEvent(strReplace))).isCancelled()) {
                Emulator.getGameServer().getGameClientManager().disposeClient(this.client);
                LOGGER.info("SSO Authentication is cancelled by a plugin. Closed connection...");
                return;
            }
            if (strReplace.isEmpty()) {
                Emulator.getGameServer().getGameClientManager().disposeClient(this.client);
                LOGGER.debug("Client is trying to connect without SSO ticket! Closed connection...");
                return;
            }
            if (this.client.getHabbo() != null) {
                Emulator.getGameServer().getGameClientManager().disposeClient(this.client);
                return;
            }
            Habbo habboLoadHabbo = Emulator.getGameEnvironment().getHabboManager().loadHabbo(strReplace);
            if (habboLoadHabbo == null) {
                Emulator.getGameServer().getGameClientManager().disposeClient(this.client);
                LOGGER.warn("Someone tried to login with a non-existing SSO token! Closed connection...");
                return;
            }
            try {
                habboLoadHabbo.setClient(this.client);
                this.client.setHabbo(habboLoadHabbo);
                if (!this.client.getHabbo().connect()) {
                    Emulator.getGameServer().getGameClientManager().disposeClient(this.client);
                    return;
                }
                if (this.client.getHabbo().getHabboInfo() == null) {
                    Emulator.getGameServer().getGameClientManager().disposeClient(this.client);
                    return;
                }
                if (this.client.getHabbo().getHabboInfo().getRank() == null) {
                    throw new NullPointerException(habboLoadHabbo.getHabboInfo().getUsername() + " has a NON EXISTING RANK!");
                }
                Emulator.getThreading().run(habboLoadHabbo);
                Emulator.getGameEnvironment().getHabboManager().addHabbo(habboLoadHabbo);
                if (ClothingValidationManager.VALIDATE_ON_LOGIN) {
                    String strValidateLook = ClothingValidationManager.validateLook(this.client.getHabbo());
                    if (!strValidateLook.equals(this.client.getHabbo().getHabboInfo().getLook())) {
                        this.client.getHabbo().getHabboInfo().setLook(strValidateLook);
                    }
                }
                ArrayList<ServerMessage> arrayList2 = new ArrayList<>();
                arrayList2.add(new SecureLoginOKComposer().compose());
                int homeRoom = 0;
                if (!this.client.getHabbo().getHabboStats().nux || (Emulator.getConfig().getBoolean("retro.style.homeroom") && this.client.getHabbo().getHabboInfo().getHomeRoom() != 0)) {
                    homeRoom = this.client.getHabbo().getHabboInfo().getHomeRoom();
                } else if (!this.client.getHabbo().getHabboStats().nux || (Emulator.getConfig().getBoolean("retro.style.homeroom") && RoomManager.HOME_ROOM_ID > 0)) {
                    homeRoom = RoomManager.HOME_ROOM_ID;
                }
                arrayList2.add(new UserHomeRoomComposer(this.client.getHabbo().getHabboInfo().getHomeRoom(), homeRoom).compose());
                arrayList2.add(new UserEffectsListComposer(habboLoadHabbo, this.client.getHabbo().getInventory().getEffectsComponent().effects.values()).compose());
                arrayList2.add(new UserClothesComposer(this.client.getHabbo()).compose());
                arrayList2.add(new NewUserIdentityComposer(habboLoadHabbo).compose());
                arrayList2.add(new UserPermissionsComposer(this.client.getHabbo()).compose());
                arrayList2.add(new AvailabilityStatusMessageComposer(true, false, true).compose());
                arrayList2.add(new PingComposer().compose());
                arrayList2.add(new EnableNotificationsComposer(Emulator.getConfig().getBoolean("bubblealerts.enabled", true)).compose());
                arrayList2.add(new UserAchievementScoreComposer(this.client.getHabbo()).compose());
                arrayList2.add(new IsFirstLoginOfDayComposer(true).compose());
                arrayList2.add(new MysteryBoxKeysComposer().compose());
                arrayList2.add(new BuildersClubExpiredComposer().compose());
                arrayList2.add(new CfhTopicsMessageComposer().compose());
                arrayList2.add(new FavoriteRoomsCountComposer(this.client.getHabbo()).compose());
                arrayList2.add(new GameCenterGameListComposer().compose());
                arrayList2.add(new GameCenterAccountInfoComposer(3, 100).compose());
                arrayList2.add(new GameCenterAccountInfoComposer(0, 100).compose());
                arrayList2.add(new UserClubComposer(this.client.getHabbo(), Subscription.HABBO_CLUB, UserClubComposer.RESPONSE_TYPE_LOGIN).compose());
                if (this.client.getHabbo().hasPermission(Permission.ACC_SUPPORTTOOL)) {
                    arrayList2.add(new ModToolComposer(this.client.getHabbo()).compose());
                }
                this.client.sendResponses(arrayList2);
                this.client.sendResponse(new InventoryAchievementsComposer());
                ModToolSanctions modToolSanctions = Emulator.getGameEnvironment().getModToolSanctions();
                if (Emulator.getConfig().getBoolean("hotel.sanctions.enabled") && (arrayList = (ArrayList) Emulator.getGameEnvironment().getModToolSanctions().getSanctions(habboLoadHabbo.getHabboInfo().getId()).get(Integer.valueOf(habboLoadHabbo.getHabboInfo().getId()))) != null && arrayList.size() > 0) {
                    ModToolSanctionItem modToolSanctionItem = (ModToolSanctionItem) arrayList.get(arrayList.size() - 1);
                    if (modToolSanctionItem.sanctionLevel > 0 && modToolSanctionItem.probationTimestamp != 0 && modToolSanctionItem.probationTimestamp > Emulator.getIntUnixTimestamp()) {
                        this.client.sendResponse(new ModToolSanctionInfoComposer(this.client.getHabbo()));
                    } else if (modToolSanctionItem.sanctionLevel > 0 && modToolSanctionItem.probationTimestamp != 0 && modToolSanctionItem.probationTimestamp <= Emulator.getIntUnixTimestamp()) {
                        modToolSanctions.updateSanction(modToolSanctionItem.id, 0);
                    }
                    if (modToolSanctionItem.tradeLockedUntil > 0 && modToolSanctionItem.tradeLockedUntil <= Emulator.getIntUnixTimestamp()) {
                        modToolSanctions.updateTradeLockedUntil(modToolSanctionItem.id, 0);
                        habboLoadHabbo.getHabboStats().setAllowTrade(true);
                    } else if (modToolSanctionItem.tradeLockedUntil > 0 && modToolSanctionItem.tradeLockedUntil > Emulator.getIntUnixTimestamp()) {
                        habboLoadHabbo.getHabboStats().setAllowTrade(false);
                    }
                    if (modToolSanctionItem.isMuted && modToolSanctionItem.muteDuration <= Emulator.getIntUnixTimestamp()) {
                        modToolSanctions.updateMuteDuration(modToolSanctionItem.id, 0);
                        habboLoadHabbo.unMute();
                    } else if (modToolSanctionItem.isMuted && modToolSanctionItem.muteDuration > Emulator.getIntUnixTimestamp()) {
                        habboLoadHabbo.mute(Math.toIntExact(new Date(((long) modToolSanctionItem.muteDuration) * 1000).getTime() - Emulator.getDate().getTime()), false);
                    }
                }
                UserLoginEvent userLoginEvent = new UserLoginEvent(habboLoadHabbo, this.client.getHabbo().getHabboInfo().getIpLogin());
                Emulator.getPluginManager().fireEvent(userLoginEvent);
                if (userLoginEvent.isCancelled()) {
                    Emulator.getGameServer().getGameClientManager().disposeClient(this.client);
                    return;
                }
                if (Emulator.getConfig().getBoolean("hotel.welcome.alert.enabled")) {
                    Emulator.getThreading().run(() -> {
                        if (Emulator.getConfig().getBoolean("hotel.welcome.alert.oldstyle")) {
                            this.client.sendResponse(new MessagesForYouComposer(HabboManager.WELCOME_MESSAGE.replace("%username%", habboLoadHabbo.getHabboInfo().getUsername()).replace("%user%", habboLoadHabbo.getHabboInfo().getUsername()).split("<br/>")));
                        } else {
                            this.client.sendResponse(new GenericAlertComposer(HabboManager.WELCOME_MESSAGE.replace("%username%", habboLoadHabbo.getHabboInfo().getUsername()).replace("%user%", habboLoadHabbo.getHabboInfo().getUsername())));
                        }
                    }, Emulator.getConfig().getInt("hotel.welcome.alert.delay", 5000));
                }
                if (SubscriptionHabboClub.HC_PAYDAY_ENABLED) {
                    SubscriptionHabboClub.processUnclaimed(habboLoadHabbo);
                }
                SubscriptionHabboClub.processClubBadge(habboLoadHabbo);
                Messenger.checkFriendSizeProgress(habboLoadHabbo);
                if (habboLoadHabbo.getHabboStats().hasGottenDefaultSavedSearches) {
                    return;
                }
                habboLoadHabbo.getHabboStats().hasGottenDefaultSavedSearches = true;
                Emulator.getThreading().run(habboLoadHabbo.getHabboStats());
                habboLoadHabbo.getHabboInfo().addSavedSearch(new NavigatorSavedSearch("official-root", Emulator.PREVIEW));
                habboLoadHabbo.getHabboInfo().addSavedSearch(new NavigatorSavedSearch("my", Emulator.PREVIEW));
                habboLoadHabbo.getHabboInfo().addSavedSearch(new NavigatorSavedSearch(NavigatorFavoriteFilter.name, Emulator.PREVIEW));
                this.client.sendResponse(new NewNavigatorSavedSearchesComposer(this.client.getHabbo().getHabboInfo().getSavedSearches()));
            } catch (Exception e) {
                LOGGER.error("Caught exception", e);
                Emulator.getGameServer().getGameClientManager().disposeClient(this.client);
            }
        }
    }
}
