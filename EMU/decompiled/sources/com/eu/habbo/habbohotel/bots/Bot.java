package com.eu.habbo.habbohotel.bots;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessage;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUserAction;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboGender;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserActionComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserShoutComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserTalkComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserWhisperComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUsersComposer;
import com.eu.habbo.plugin.events.bots.BotChatEvent;
import com.eu.habbo.plugin.events.bots.BotShoutEvent;
import com.eu.habbo.plugin.events.bots.BotTalkEvent;
import com.eu.habbo.plugin.events.bots.BotWhisperEvent;
import com.eu.habbo.threading.runnables.BotFollowHabbo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/bots/Bot.class */
public class Bot implements Runnable {
    public static final String NO_CHAT_SET = "${bot.skill.chatter.configuration.text.placeholder}";
    private final ArrayList<String> chatLines;
    private transient int id;
    private String name;
    private String motto;
    private String figure;
    private HabboGender gender;
    private int ownerId;
    private String ownerName;
    private Room room;
    private RoomUnit roomUnit;
    private boolean chatAuto;
    private boolean chatRandom;
    private short chatDelay;
    private int chatTimeOut;
    private int chatTimestamp;
    private short lastChatIndex;
    private int bubble;
    private String type;
    private int effect;
    private transient boolean canWalk;
    private boolean needsUpdate;
    private transient int followingHabboId;
    private static final Logger LOGGER = LoggerFactory.getLogger(Bot.class);
    public static String[] PLACEMENT_MESSAGES = "Yo!;Hello I'm a real party animal!;Hello!".split(";");

    public Bot(int i, String str, String str2, String str3, HabboGender habboGender, int i2, String str4) {
        this.canWalk = true;
        this.id = i;
        this.name = str;
        this.motto = str2;
        this.figure = str3;
        this.gender = habboGender;
        this.ownerId = i2;
        this.ownerName = str4;
        this.chatAuto = false;
        this.chatRandom = false;
        this.chatDelay = (short) 1000;
        this.chatLines = new ArrayList<>();
        this.type = "generic_bot";
        this.room = null;
        this.bubble = RoomChatMessageBubbles.BOT_RENTABLE.getType();
    }

    public Bot(ResultSet resultSet) throws SQLException {
        this.canWalk = true;
        this.id = resultSet.getInt("id");
        this.name = resultSet.getString("name");
        this.motto = resultSet.getString("motto");
        this.figure = resultSet.getString("figure");
        this.gender = HabboGender.valueOf(resultSet.getString("gender"));
        this.ownerId = resultSet.getInt("user_id");
        this.ownerName = resultSet.getString("owner_name");
        this.chatAuto = resultSet.getString("chat_auto").equals("1");
        this.chatRandom = resultSet.getString("chat_random").equals("1");
        this.chatDelay = resultSet.getShort("chat_delay");
        this.chatLines = new ArrayList<>(Arrays.asList(resultSet.getString("chat_lines").split("\r")));
        this.type = resultSet.getString("type");
        this.effect = resultSet.getInt("effect");
        this.canWalk = resultSet.getString("freeroam").equals("1");
        this.room = null;
        this.roomUnit = null;
        this.chatTimeOut = Emulator.getIntUnixTimestamp() + this.chatDelay;
        this.needsUpdate = false;
        this.bubble = resultSet.getInt("bubble_id");
    }

    public Bot(Bot bot) {
        this.canWalk = true;
        this.name = bot.getName();
        this.motto = bot.getMotto();
        this.figure = bot.getFigure();
        this.gender = bot.getGender();
        this.ownerId = bot.getOwnerId();
        this.ownerName = bot.getOwnerName();
        this.chatAuto = true;
        this.chatRandom = false;
        this.chatDelay = (short) 10;
        this.chatTimeOut = Emulator.getIntUnixTimestamp() + this.chatDelay;
        this.chatLines = new ArrayList<>(Arrays.asList("Default Message :D"));
        this.type = bot.getType();
        this.effect = bot.getEffect();
        this.bubble = bot.getBubbleId();
        this.needsUpdate = false;
    }

    public static void initialise() {
    }

    public static void dispose() {
    }

    public void needsUpdate(boolean z) {
        this.needsUpdate = z;
    }

    public boolean needsUpdate() {
        return this.needsUpdate;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.needsUpdate) {
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE bots SET name = ?, motto = ?, figure = ?, gender = ?, user_id = ?, room_id = ?, x = ?, y = ?, z = ?, rot = ?, dance = ?, freeroam = ?, chat_lines = ?, chat_auto = ?, chat_random = ?, chat_delay = ?, effect = ?, bubble_id = ? WHERE id = ?");
                    try {
                        preparedStatementPrepareStatement.setString(1, this.name);
                        preparedStatementPrepareStatement.setString(2, this.motto);
                        preparedStatementPrepareStatement.setString(3, this.figure);
                        preparedStatementPrepareStatement.setString(4, this.gender.toString());
                        preparedStatementPrepareStatement.setInt(5, this.ownerId);
                        preparedStatementPrepareStatement.setInt(6, this.room == null ? 0 : this.room.getId());
                        preparedStatementPrepareStatement.setInt(7, this.roomUnit == null ? (short) 0 : this.roomUnit.getX());
                        preparedStatementPrepareStatement.setInt(8, this.roomUnit == null ? (short) 0 : this.roomUnit.getY());
                        preparedStatementPrepareStatement.setDouble(9, this.roomUnit == null ? 0.0d : this.roomUnit.getZ());
                        preparedStatementPrepareStatement.setInt(10, this.roomUnit == null ? 0 : this.roomUnit.getBodyRotation().getValue());
                        preparedStatementPrepareStatement.setInt(11, this.roomUnit == null ? 0 : this.roomUnit.getDanceType().getType());
                        preparedStatementPrepareStatement.setString(12, this.canWalk ? "1" : "0");
                        StringBuilder sb = new StringBuilder();
                        Iterator<String> it = this.chatLines.iterator();
                        while (it.hasNext()) {
                            sb.append(it.next()).append("\r");
                        }
                        preparedStatementPrepareStatement.setString(13, sb.toString());
                        preparedStatementPrepareStatement.setString(14, this.chatAuto ? "1" : "0");
                        preparedStatementPrepareStatement.setString(15, this.chatRandom ? "1" : "0");
                        preparedStatementPrepareStatement.setInt(16, this.chatDelay);
                        preparedStatementPrepareStatement.setInt(17, this.effect);
                        preparedStatementPrepareStatement.setInt(18, this.bubble);
                        preparedStatementPrepareStatement.setInt(19, this.id);
                        preparedStatementPrepareStatement.execute();
                        this.needsUpdate = false;
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
                        }
                        if (connection != null) {
                            connection.close();
                        }
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
                } finally {
                }
            } catch (SQLException e) {
                LOGGER.error("Caught SQL exception", e);
            }
        }
    }

    public void cycle(boolean z) {
        short sNextInt;
        if (this.roomUnit != null) {
            if (z && this.canWalk && !this.roomUnit.isWalking() && this.roomUnit.getWalkTimeOut() < Emulator.getIntUnixTimestamp() && this.followingHabboId == 0) {
                this.roomUnit.setGoalLocation(this.room.getRandomWalkableTile());
                int iNextInt = Emulator.getRandom().nextInt(20) * 2;
                this.roomUnit.setWalkTimeOut((iNextInt < 10 ? 5 : iNextInt) + Emulator.getIntUnixTimestamp());
            }
            if (this.chatLines.isEmpty() || this.chatTimeOut > Emulator.getIntUnixTimestamp() || !this.chatAuto || this.room == null) {
                return;
            }
            if (this.chatRandom) {
                sNextInt = (short) Emulator.getRandom().nextInt(this.chatLines.size());
            } else if (this.lastChatIndex == this.chatLines.size() - 1) {
                sNextInt = 0;
            } else {
                short s = this.lastChatIndex;
                sNextInt = s;
                this.lastChatIndex = (short) (s + 1);
            }
            this.lastChatIndex = sNextInt;
            if (this.lastChatIndex >= this.chatLines.size()) {
                this.lastChatIndex = (short) 0;
            }
            String strReplace = this.chatLines.get(this.lastChatIndex).replace(Emulator.getTexts().getValue("wired.variable.owner", "%owner%"), this.room.getOwnerName()).replace(Emulator.getTexts().getValue("wired.variable.item_count", "%item_count%"), this.room.itemCount() + Emulator.PREVIEW).replace(Emulator.getTexts().getValue("wired.variable.name", "%name%"), this.name).replace(Emulator.getTexts().getValue("wired.variable.roomname", "%roomname%"), this.room.getName()).replace(Emulator.getTexts().getValue("wired.variable.user_count", "%user_count%"), this.room.getUserCount() + Emulator.PREVIEW);
            if (!WiredHandler.handle(WiredTriggerType.SAY_SOMETHING, getRoomUnit(), this.room, new Object[]{strReplace})) {
                talk(strReplace);
            }
            this.chatTimeOut = Emulator.getIntUnixTimestamp() + this.chatDelay;
        }
    }

    public void talk(String str) {
        if (this.room != null) {
            BotTalkEvent botTalkEvent = new BotTalkEvent(this, str);
            if (((BotChatEvent) Emulator.getPluginManager().fireEvent(botTalkEvent)).isCancelled()) {
                return;
            }
            this.chatTimestamp = Emulator.getIntUnixTimestamp();
            this.room.botChat(new RoomUserTalkComposer(new RoomChatMessage(botTalkEvent.message, this.roomUnit, RoomChatMessageBubbles.getBubble(getBubbleId()))).compose());
            if (str.equals("o/") || str.equals("_o/")) {
                this.room.sendComposer(new RoomUserActionComposer(this.roomUnit, RoomUserAction.WAVE).compose());
            }
        }
    }

    public void shout(String str) {
        if (this.room != null) {
            BotShoutEvent botShoutEvent = new BotShoutEvent(this, str);
            if (((BotChatEvent) Emulator.getPluginManager().fireEvent(botShoutEvent)).isCancelled()) {
                return;
            }
            this.chatTimestamp = Emulator.getIntUnixTimestamp();
            this.room.botChat(new RoomUserShoutComposer(new RoomChatMessage(botShoutEvent.message, this.roomUnit, RoomChatMessageBubbles.getBubble(getBubbleId()))).compose());
            if (str.equals("o/") || str.equals("_o/")) {
                this.room.sendComposer(new RoomUserActionComposer(this.roomUnit, RoomUserAction.WAVE).compose());
            }
        }
    }

    public void whisper(String str, Habbo habbo) {
        if (this.room == null || habbo == null) {
            return;
        }
        BotWhisperEvent botWhisperEvent = new BotWhisperEvent(this, str, habbo);
        if (((BotWhisperEvent) Emulator.getPluginManager().fireEvent(botWhisperEvent)).isCancelled()) {
            return;
        }
        this.chatTimestamp = Emulator.getIntUnixTimestamp();
        botWhisperEvent.target.getClient().sendResponse(new RoomUserWhisperComposer(new RoomChatMessage(botWhisperEvent.message, this.roomUnit, RoomChatMessageBubbles.getBubble(getBubbleId()))));
    }

    public void onPlace(Habbo habbo, Room room) {
        if (this.roomUnit != null) {
            room.giveEffect(this.roomUnit, this.effect, -1);
        }
        if (PLACEMENT_MESSAGES.length > 0) {
            String str = PLACEMENT_MESSAGES[Emulator.getRandom().nextInt(PLACEMENT_MESSAGES.length)];
            if (WiredHandler.handle(WiredTriggerType.SAY_SOMETHING, getRoomUnit(), room, new Object[]{str})) {
                return;
            }
            talk(str);
        }
    }

    public void onPickUp(Habbo habbo, Room room) {
    }

    public void onUserSay(RoomChatMessage roomChatMessage) {
    }

    public int getId() {
        return this.id;
    }

    public void setId(int i) {
        this.id = i;
    }

    public String getName() {
        return this.name;
    }

    public int getBubbleId() {
        return this.bubble;
    }

    public void setName(String str) {
        this.name = str;
        this.needsUpdate = true;
    }

    public String getMotto() {
        return this.motto;
    }

    public void setMotto(String str) {
        this.motto = str;
        this.needsUpdate = true;
    }

    public String getFigure() {
        return this.figure;
    }

    public void setFigure(String str) {
        this.figure = str;
        this.needsUpdate = true;
        if (this.room != null) {
            this.room.sendComposer(new RoomUsersComposer(this).compose());
        }
    }

    public HabboGender getGender() {
        return this.gender;
    }

    public void setGender(HabboGender habboGender) {
        this.gender = habboGender;
        this.needsUpdate = true;
        if (this.room != null) {
            this.room.sendComposer(new RoomUsersComposer(this).compose());
        }
    }

    public int getOwnerId() {
        return this.ownerId;
    }

    public void setOwnerId(int i) {
        this.ownerId = i;
        this.needsUpdate = true;
        if (this.room != null) {
            this.room.sendComposer(new RoomUsersComposer(this).compose());
        }
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public void setOwnerName(String str) {
        this.ownerName = str;
        this.needsUpdate = true;
        if (this.room != null) {
            this.room.sendComposer(new RoomUsersComposer(this).compose());
        }
    }

    public Room getRoom() {
        return this.room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public RoomUnit getRoomUnit() {
        return this.roomUnit;
    }

    public void setRoomUnit(RoomUnit roomUnit) {
        this.roomUnit = roomUnit;
    }

    public boolean isChatAuto() {
        return this.chatAuto;
    }

    public void setChatAuto(boolean z) {
        this.chatAuto = z;
        this.needsUpdate = true;
    }

    public boolean isChatRandom() {
        return this.chatRandom;
    }

    public void setChatRandom(boolean z) {
        this.chatRandom = z;
        this.needsUpdate = true;
    }

    public boolean hasChat() {
        return !this.chatLines.isEmpty();
    }

    public int getChatDelay() {
        return this.chatDelay;
    }

    public void setChatDelay(short s) {
        this.chatDelay = (short) Math.min(Math.max((int) s, BotManager.MINIMUM_CHAT_SPEED), BotManager.MAXIMUM_CHAT_SPEED);
        this.needsUpdate = true;
        this.chatTimeOut = Emulator.getIntUnixTimestamp() + this.chatDelay;
    }

    public int getChatTimestamp() {
        return this.chatTimestamp;
    }

    public void clearChat() {
        synchronized (this.chatLines) {
            this.chatLines.clear();
            this.needsUpdate = true;
        }
    }

    public String getType() {
        return this.type;
    }

    public int getEffect() {
        return this.effect;
    }

    public void setEffect(int i, int i2) {
        this.effect = i;
        this.needsUpdate = true;
        if (this.roomUnit == null || this.room == null) {
            return;
        }
        this.room.giveEffect(this.roomUnit, this.effect, i2);
    }

    public void addChatLines(ArrayList<String> arrayList) {
        synchronized (this.chatLines) {
            this.chatLines.addAll(arrayList);
            this.needsUpdate = true;
        }
    }

    public void addChatLine(String str) {
        synchronized (this.chatLines) {
            this.chatLines.add(str);
            this.needsUpdate = true;
        }
    }

    public ArrayList<String> getChatLines() {
        return this.chatLines;
    }

    public int getFollowingHabboId() {
        return this.followingHabboId;
    }

    public void startFollowingHabbo(Habbo habbo) {
        this.followingHabboId = habbo.getHabboInfo().getId();
        Emulator.getThreading().run(new BotFollowHabbo(this, habbo, habbo.getHabboInfo().getCurrentRoom()));
    }

    public void stopFollowingHabbo() {
        this.followingHabboId = 0;
    }

    public boolean canWalk() {
        return this.canWalk;
    }

    public void setCanWalk(boolean z) {
        this.canWalk = z;
    }

    public void lookAt(Habbo habbo) {
        lookAt(habbo.getRoomUnit().getCurrentLocation());
    }

    public void lookAt(RoomUnit roomUnit) {
        lookAt(roomUnit.getCurrentLocation());
    }

    public void lookAt(RoomTile roomTile) {
        this.roomUnit.lookAtPoint(roomTile);
        this.roomUnit.statusUpdate(true);
    }
}
