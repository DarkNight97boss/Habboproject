package com.eu.habbo.habbohotel.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.messenger.Messenger;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessage;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUnitType;
import com.eu.habbo.habbohotel.rooms.RoomUserAction;
import com.eu.habbo.habbohotel.users.inventory.BadgesComponent;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;
import com.eu.habbo.messages.outgoing.generic.alerts.GenericAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.MessagesForYouComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.StaffAlertWithLinkComposer;
import com.eu.habbo.messages.outgoing.inventory.AddBotComposer;
import com.eu.habbo.messages.outgoing.inventory.AddHabboItemComposer;
import com.eu.habbo.messages.outgoing.inventory.AddPetComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryBadgesComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import com.eu.habbo.messages.outgoing.inventory.RemoveBotComposer;
import com.eu.habbo.messages.outgoing.inventory.RemoveHabboItemComposer;
import com.eu.habbo.messages.outgoing.inventory.RemovePetComposer;
import com.eu.habbo.messages.outgoing.rooms.FloodCounterComposer;
import com.eu.habbo.messages.outgoing.rooms.ForwardToRoomComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserActionComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserIgnoredComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserRespectComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserShoutComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserTalkComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserWhisperComposer;
import com.eu.habbo.messages.outgoing.users.AddUserBadgeComposer;
import com.eu.habbo.messages.outgoing.users.MutedWhisperComposer;
import com.eu.habbo.messages.outgoing.users.UserCreditsComposer;
import com.eu.habbo.messages.outgoing.users.UserCurrencyComposer;
import com.eu.habbo.messages.outgoing.users.UserPointsComposer;
import com.eu.habbo.plugin.events.users.UserCreditsEvent;
import com.eu.habbo.plugin.events.users.UserDisconnectEvent;
import com.eu.habbo.plugin.events.users.UserGetIPAddressEvent;
import com.eu.habbo.plugin.events.users.UserPointsEvent;
import gnu.trove.TIntCollection;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import java.net.InetSocketAddress;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/users/Habbo.class */
public class Habbo implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Habbo.class);
    private final HabboInfo habboInfo;
    private final HabboStats habboStats;
    private RoomUnit roomUnit;
    private volatile boolean update;
    private volatile boolean disconnected = false;
    private volatile boolean disconnecting = false;
    private GameClient client = null;
    private final HabboInventory habboInventory = new HabboInventory(this);
    private final Messenger messenger = new Messenger();

    public Habbo(ResultSet resultSet) {
        this.habboInfo = new HabboInfo(resultSet);
        this.habboStats = HabboStats.load(this.habboInfo);
        this.messenger.loadFriends(this);
        this.messenger.loadFriendRequests(this);
        this.roomUnit = new RoomUnit();
        this.roomUnit.setRoomUnitType(RoomUnitType.USER);
        this.update = false;
    }

    public boolean isOnline() {
        return this.habboInfo.isOnline();
    }

    void isOnline(boolean z) {
        this.habboInfo.setOnline(z);
        update();
    }

    void update() {
        this.update = true;
        run();
    }

    void needsUpdate(boolean z) {
        this.update = z;
    }

    boolean needsUpdate() {
        return this.update;
    }

    public Messenger getMessenger() {
        return this.messenger;
    }

    public HabboInfo getHabboInfo() {
        return this.habboInfo;
    }

    public HabboStats getHabboStats() {
        return this.habboStats;
    }

    public HabboInventory getInventory() {
        return this.habboInventory;
    }

    public RoomUnit getRoomUnit() {
        return this.roomUnit;
    }

    public void setRoomUnit(RoomUnit roomUnit) {
        this.roomUnit = roomUnit;
    }

    public GameClient getClient() {
        return this.client;
    }

    public void setClient(GameClient gameClient) {
        this.client = gameClient;
    }

    public boolean connect() {
        String hostAddress;
        String updatedIp = Emulator.PREVIEW;
        if (Emulator.getConfig().getBoolean("networking.tcp.proxy") || this.client.getChannel().remoteAddress() == null) {
            hostAddress = ((InetSocketAddress) this.client.getChannel().remoteAddress()).getAddress().getHostAddress();
        } else {
            updatedIp = ((InetSocketAddress) this.client.getChannel().remoteAddress()).getAddress().getHostAddress();
            hostAddress = "- no proxy server used";
        }
        if (Emulator.getPluginManager().isRegistered(UserGetIPAddressEvent.class, true)) {
            UserGetIPAddressEvent userGetIPAddressEvent = (UserGetIPAddressEvent) Emulator.getPluginManager().fireEvent(new UserGetIPAddressEvent(this, updatedIp));
            if (userGetIPAddressEvent.hasChangedIP()) {
                updatedIp = userGetIPAddressEvent.getUpdatedIp();
            }
        }
        if (!updatedIp.isEmpty()) {
            this.habboInfo.setIpLogin(updatedIp);
        }
        if (Emulator.getGameEnvironment().getModToolManager().hasMACBan(this.client) || Emulator.getGameEnvironment().getModToolManager().hasIPBan(this.habboInfo.getIpLogin())) {
            return false;
        }
        this.habboInfo.setMachineID(this.client.getMachineId());
        isOnline(true);
        this.messenger.connectionChanged(this, true, false);
        Emulator.getGameEnvironment().getRoomManager().loadRoomsForHabbo(this);
        LOGGER.info("{} logged in from IP {} using proxyserver {}", new Object[]{this.habboInfo.getUsername(), this.habboInfo.getIpLogin(), hostAddress});
        return true;
    }

    public synchronized void disconnect() {
        Room room;
        if ((!Emulator.isShuttingDown && ((UserDisconnectEvent) Emulator.getPluginManager().fireEvent(new UserDisconnectEvent(this))).isCancelled()) || this.disconnected || this.disconnecting) {
            return;
        }
        this.disconnecting = true;
        try {
            if (getHabboInfo().getCurrentRoom() != null) {
                Emulator.getGameEnvironment().getRoomManager().leaveRoom(this, getHabboInfo().getCurrentRoom());
            }
            if (getHabboInfo().getRoomQueueId() > 0 && (room = Emulator.getGameEnvironment().getRoomManager().getRoom(getHabboInfo().getRoomQueueId())) != null) {
                room.removeFromQueue(this);
            }
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
        }
        try {
            try {
                Emulator.getGameEnvironment().getGuideManager().userLogsOut(this);
                isOnline(false);
                needsUpdate(true);
                run();
                getInventory().dispose();
                this.messenger.connectionChanged(this, false, false);
                this.messenger.dispose();
                this.disconnected = true;
                AchievementManager.saveAchievements(this);
                this.habboStats.dispose();
                Emulator.getGameEnvironment().getRoomManager().unloadRoomsForHabbo(this);
                Emulator.getGameEnvironment().getHabboManager().removeHabbo(this);
                LOGGER.info("{} disconnected.", this.habboInfo.getUsername());
                this.client = null;
            } catch (Exception e2) {
                LOGGER.error("Caught exception", e2);
                Emulator.getGameEnvironment().getRoomManager().unloadRoomsForHabbo(this);
                Emulator.getGameEnvironment().getHabboManager().removeHabbo(this);
            }
        } catch (Throwable th) {
            Emulator.getGameEnvironment().getRoomManager().unloadRoomsForHabbo(this);
            Emulator.getGameEnvironment().getHabboManager().removeHabbo(this);
            throw th;
        }
    }

    @Override // java.lang.Runnable
    public void run() {
        if (needsUpdate()) {
            this.habboInfo.run();
            needsUpdate(false);
        }
    }

    public boolean hasPermission(String str) {
        return hasPermission(str, false);
    }

    public boolean hasPermission(String str, boolean z) {
        return Emulator.getGameEnvironment().getPermissionsManager().hasPermission(this, str, z);
    }

    public void giveCredits(int i) {
        if (i == 0) {
            return;
        }
        UserCreditsEvent userCreditsEvent = new UserCreditsEvent(this, i);
        if (((UserCreditsEvent) Emulator.getPluginManager().fireEvent(userCreditsEvent)).isCancelled()) {
            return;
        }
        getHabboInfo().addCredits(userCreditsEvent.credits);
        if (this.client != null) {
            this.client.sendResponse(new UserCreditsComposer(this.client.getHabbo()));
        }
    }

    public void givePixels(int i) {
        if (i == 0) {
            return;
        }
        UserPointsEvent userPointsEvent = new UserPointsEvent(this, i, 0);
        if (((UserPointsEvent) Emulator.getPluginManager().fireEvent(userPointsEvent)).isCancelled()) {
            return;
        }
        getHabboInfo().addPixels(userPointsEvent.points);
        if (this.client != null) {
            this.client.sendResponse(new UserCurrencyComposer(this.client.getHabbo()));
        }
    }

    public void givePoints(int i) {
        givePoints(Emulator.getConfig().getInt("seasonal.primary.type"), i);
    }

    public void givePoints(int i, int i2) {
        if (i2 == 0) {
            return;
        }
        UserPointsEvent userPointsEvent = new UserPointsEvent(this, i2, i);
        if (((UserPointsEvent) Emulator.getPluginManager().fireEvent(userPointsEvent)).isCancelled()) {
            return;
        }
        getHabboInfo().addCurrencyAmount(userPointsEvent.type, userPointsEvent.points);
        if (this.client != null) {
            this.client.sendResponse(new UserPointsComposer(this.client.getHabbo().getHabboInfo().getCurrencyAmount(i), userPointsEvent.points, userPointsEvent.type));
        }
    }

    public void whisper(String str) {
        whisper(str, this.habboStats.chatColor);
    }

    public void whisper(String str, RoomChatMessageBubbles roomChatMessageBubbles) {
        if (getRoomUnit().isInRoom()) {
            this.client.sendResponse(new RoomUserWhisperComposer(new RoomChatMessage(str, this.client.getHabbo().getRoomUnit(), roomChatMessageBubbles)));
        }
    }

    public void talk(String str) {
        talk(str, this.habboStats.chatColor);
    }

    public void talk(String str, RoomChatMessageBubbles roomChatMessageBubbles) {
        if (getRoomUnit().isInRoom()) {
            getHabboInfo().getCurrentRoom().sendComposer(new RoomUserTalkComposer(new RoomChatMessage(str, this.client.getHabbo().getRoomUnit(), roomChatMessageBubbles)).compose());
        }
    }

    public void shout(String str) {
        shout(str, this.habboStats.chatColor);
    }

    public void shout(String str, RoomChatMessageBubbles roomChatMessageBubbles) {
        if (getRoomUnit().isInRoom()) {
            getHabboInfo().getCurrentRoom().sendComposer(new RoomUserShoutComposer(new RoomChatMessage(str, this.client.getHabbo().getRoomUnit(), roomChatMessageBubbles)).compose());
        }
    }

    public void alert(String str) {
        if (Emulator.getConfig().getBoolean("hotel.alert.oldstyle")) {
            this.client.sendResponse(new MessagesForYouComposer(new String[]{str}));
        } else {
            this.client.sendResponse(new GenericAlertComposer(str));
        }
    }

    public void alert(String[] strArr) {
        this.client.sendResponse(new MessagesForYouComposer(strArr));
    }

    public void alertWithUrl(String str, String str2) {
        this.client.sendResponse(new StaffAlertWithLinkComposer(str, str2));
    }

    public void goToRoom(int i) {
        this.client.sendResponse(new ForwardToRoomComposer(i));
    }

    public void addFurniture(HabboItem habboItem) {
        this.habboInventory.getItemsComponent().addItem(habboItem);
        this.client.sendResponse(new AddHabboItemComposer(habboItem));
        this.client.sendResponse(new InventoryRefreshComposer());
    }

    public void addFurniture(THashSet<HabboItem> tHashSet) {
        this.habboInventory.getItemsComponent().addItems(tHashSet);
        this.client.sendResponse(new AddHabboItemComposer(tHashSet));
        this.client.sendResponse(new InventoryRefreshComposer());
    }

    public void removeFurniture(HabboItem habboItem) {
        this.habboInventory.getItemsComponent().removeHabboItem(habboItem);
        this.client.sendResponse(new RemoveHabboItemComposer(habboItem.getId()));
    }

    public void addBot(Bot bot) {
        this.habboInventory.getBotsComponent().addBot(bot);
        this.client.sendResponse(new AddBotComposer(bot));
    }

    public void removeBot(Bot bot) {
        this.habboInventory.getBotsComponent().removeBot(bot);
        this.client.sendResponse(new RemoveBotComposer(bot));
    }

    public void deleteBot(Bot bot) {
        removeBot(bot);
        bot.getRoom().removeBot(bot);
        Emulator.getGameEnvironment().getBotManager().deleteBot(bot);
    }

    public void addPet(Pet pet) {
        this.habboInventory.getPetsComponent().addPet(pet);
        this.client.sendResponse(new AddPetComposer(pet));
    }

    public void removePet(Pet pet) {
        this.habboInventory.getPetsComponent().removePet(pet);
        this.client.sendResponse(new RemovePetComposer(pet));
    }

    public boolean addBadge(String str) {
        if (this.habboInventory.getBadgesComponent().hasBadge(str)) {
            return false;
        }
        HabboBadge habboBadgeCreateBadge = BadgesComponent.createBadge(str, this);
        this.habboInventory.getBadgesComponent().addBadge(habboBadgeCreateBadge);
        this.client.sendResponse(new AddUserBadgeComposer(habboBadgeCreateBadge));
        this.client.sendResponse(new AddHabboItemComposer(habboBadgeCreateBadge.getId(), AddHabboItemComposer.AddHabboItemCategory.BADGE));
        THashMap tHashMap = new THashMap();
        tHashMap.put("display", "BUBBLE");
        tHashMap.put("image", "${image.library.url}album1584/" + habboBadgeCreateBadge.getCode() + ".gif");
        tHashMap.put("message", Emulator.getTexts().getValue("commands.generic.cmd_badge.received"));
        this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.RECEIVED_BADGE.key, (THashMap<String, String>) tHashMap));
        return true;
    }

    public void deleteBadge(HabboBadge habboBadge) {
        if (habboBadge != null) {
            this.habboInventory.getBadgesComponent().removeBadge(habboBadge);
            BadgesComponent.deleteBadge(getHabboInfo().getId(), habboBadge.getCode());
            this.client.sendResponse(new InventoryBadgesComposer(this));
        }
    }

    public void mute(int i, boolean z) {
        if (i <= 0) {
            LOGGER.warn("Tried to mute user for {} seconds, which is invalid.", Integer.valueOf(i));
            return;
        }
        if (hasPermission("acc_no_mute")) {
            return;
        }
        int iAddMuteTime = this.habboStats.addMuteTime(i);
        this.client.sendResponse(new FloodCounterComposer(iAddMuteTime));
        this.client.sendResponse(new MutedWhisperComposer(iAddMuteTime));
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null || z) {
            return;
        }
        currentRoom.sendComposer(new RoomUserIgnoredComposer(this, 2).compose());
    }

    public void unMute() {
        this.habboStats.unMute();
        this.client.sendResponse(new FloodCounterComposer(3));
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom != null) {
            currentRoom.sendComposer(new RoomUserIgnoredComposer(this, 3).compose());
        }
    }

    public int noobStatus() {
        return 1;
    }

    public void clearCaches() {
        int intUnixTimestamp = Emulator.getIntUnixTimestamp();
        THashMap<Integer, List<Integer>> tHashMap = new THashMap<>();
        for (Map.Entry entry : this.habboStats.ltdPurchaseLog.entrySet()) {
            for (Integer num : (List) entry.getValue()) {
                if (num.intValue() > intUnixTimestamp) {
                    if (!tHashMap.containsKey(entry.getKey())) {
                        tHashMap.put((Integer) entry.getKey(), new ArrayList());
                    }
                    ((List) tHashMap.get(entry.getKey())).add(num);
                }
            }
        }
        this.habboStats.ltdPurchaseLog = tHashMap;
    }

    public void respect(Habbo habbo) {
        if (habbo == null || habbo == this.client.getHabbo()) {
            return;
        }
        habbo.getHabboStats().respectPointsReceived++;
        this.client.getHabbo().getHabboStats().respectPointsGiven++;
        this.client.getHabbo().getHabboStats().respectPointsToGive--;
        this.client.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RoomUserRespectComposer(habbo).compose());
        this.client.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RoomUserActionComposer(this.client.getHabbo().getRoomUnit(), RoomUserAction.THUMB_UP).compose());
        AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("RespectGiven"));
        AchievementManager.progressAchievement(habbo, Emulator.getGameEnvironment().getAchievementManager().getAchievement("RespectEarned"));
        this.client.getHabbo().getHabboInfo().getCurrentRoom().unIdle(this.client.getHabbo());
        this.client.getHabbo().getHabboInfo().getCurrentRoom().dance(this.client.getHabbo().getRoomUnit(), DanceType.NONE);
    }

    public Set<Integer> getForbiddenClothing() {
        TIntCollection clothing = getInventory().getWardrobeComponent().getClothing();
        return (Set) Emulator.getGameEnvironment().getCatalogManager().clothing.values().stream().filter(clothItem -> {
            return !clothing.contains(clothItem.id);
        }).map(clothItem2 -> {
            return clothItem2.setId;
        }).flatMap(iArr -> {
            return Arrays.stream(iArr).boxed();
        }).collect(Collectors.toSet());
    }
}
