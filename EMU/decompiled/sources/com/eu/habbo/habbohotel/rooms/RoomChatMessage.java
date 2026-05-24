package com.eu.habbo.habbohotel.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.core.DatabaseLoggable;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ISerialize;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.MessageHandler;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/rooms/RoomChatMessage.class */
public class RoomChatMessage implements Runnable, ISerialize, DatabaseLoggable {
    private static final String QUERY = "INSERT INTO chatlogs_room (user_from_id, user_to_id, message, timestamp, room_id) VALUES (?, ?, ?, ?, ?)";
    private final Habbo habbo;
    public int roomId;
    public boolean isCommand;
    public boolean filtered;
    private int roomUnitId;
    private String message;
    private String unfilteredMessage;
    private int timestamp;
    private RoomChatMessageBubbles bubble;
    private Habbo targetHabbo;
    private byte emotion;
    private static final Logger LOGGER = LoggerFactory.getLogger(RoomChatMessage.class);
    private static final List<String> chatColors = Arrays.asList("@red@", "@cyan@", "@blue@", "@green@", "@purple@");
    public static int MAXIMUM_LENGTH = 100;
    public static boolean SAVE_ROOM_CHATS = false;
    public static int[] BANNED_BUBBLES = new int[0];

    public RoomChatMessage(MessageHandler messageHandler) {
        this.isCommand = false;
        this.filtered = false;
        this.timestamp = 0;
        if (messageHandler.packet.getMessageId() == 1543) {
            String string = messageHandler.packet.readString();
            this.targetHabbo = messageHandler.client.getHabbo().getHabboInfo().getCurrentRoom().getHabbo(string.split(" ")[0]);
            this.message = string.substring(string.split(" ")[0].length() + 1);
        } else {
            this.message = messageHandler.packet.readString();
        }
        try {
            this.bubble = RoomChatMessageBubbles.getBubble(messageHandler.packet.readInt().intValue());
        } catch (Exception e) {
            this.bubble = RoomChatMessageBubbles.NORMAL;
        }
        if (!messageHandler.client.getHabbo().hasPermission(Permission.ACC_ANYCHATCOLOR)) {
            int[] iArr = BANNED_BUBBLES;
            int length = iArr.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                if (Integer.valueOf(iArr[i]).intValue() == this.bubble.getType()) {
                    this.bubble = RoomChatMessageBubbles.NORMAL;
                    break;
                }
                i++;
            }
        }
        this.habbo = messageHandler.client.getHabbo();
        this.roomUnitId = this.habbo.getRoomUnit().getId();
        this.unfilteredMessage = this.message;
        this.timestamp = Emulator.getIntUnixTimestamp();
        checkEmotion();
        filter();
    }

    public RoomChatMessage(RoomChatMessage roomChatMessage) {
        this.isCommand = false;
        this.filtered = false;
        this.timestamp = 0;
        this.message = roomChatMessage.getMessage();
        this.unfilteredMessage = roomChatMessage.getUnfilteredMessage();
        this.habbo = roomChatMessage.getHabbo();
        this.targetHabbo = roomChatMessage.getTargetHabbo();
        this.bubble = roomChatMessage.getBubble();
        this.roomUnitId = roomChatMessage.roomUnitId;
        this.emotion = (byte) roomChatMessage.getEmotion();
    }

    public RoomChatMessage(String str, RoomUnit roomUnit, RoomChatMessageBubbles roomChatMessageBubbles) {
        this.isCommand = false;
        this.filtered = false;
        this.timestamp = 0;
        this.message = str;
        this.unfilteredMessage = str;
        this.habbo = null;
        this.bubble = roomChatMessageBubbles;
        this.roomUnitId = roomUnit.getId();
    }

    public RoomChatMessage(String str, Habbo habbo, RoomChatMessageBubbles roomChatMessageBubbles) {
        this.isCommand = false;
        this.filtered = false;
        this.timestamp = 0;
        this.message = str;
        this.unfilteredMessage = str;
        this.habbo = habbo;
        this.bubble = roomChatMessageBubbles;
        checkEmotion();
        this.roomUnitId = habbo.getRoomUnit().getId();
        this.message = this.message.replace("\r", Emulator.PREVIEW).replace("\n", Emulator.PREVIEW);
        if (!this.bubble.isOverridable() || getHabbo().getHabboStats().chatColor == RoomChatMessageBubbles.NORMAL) {
            return;
        }
        this.bubble = getHabbo().getHabboStats().chatColor;
    }

    public RoomChatMessage(String str, Habbo habbo, Habbo habbo2, RoomChatMessageBubbles roomChatMessageBubbles) {
        this.isCommand = false;
        this.filtered = false;
        this.timestamp = 0;
        this.message = str;
        this.unfilteredMessage = str;
        this.habbo = habbo;
        this.targetHabbo = habbo2;
        this.bubble = roomChatMessageBubbles;
        checkEmotion();
        this.roomUnitId = this.habbo.getRoomUnit().getId();
        this.message = this.message.replace("\r", Emulator.PREVIEW).replace("\n", Emulator.PREVIEW);
        if (!this.bubble.isOverridable() || getHabbo().getHabboStats().chatColor == RoomChatMessageBubbles.NORMAL) {
            return;
        }
        this.bubble = getHabbo().getHabboStats().chatColor;
    }

    private void checkEmotion() {
        if (this.message.contains(":)") || this.message.contains(":-)") || this.message.contains(":]")) {
            this.emotion = (byte) 1;
            return;
        }
        if (this.message.contains(":@") || this.message.contains(">:(")) {
            this.emotion = (byte) 2;
            return;
        }
        if (this.message.contains(":o") || this.message.contains(":O") || this.message.contains(":0") || this.message.contains("O.o") || this.message.contains("o.O") || this.message.contains("O.O")) {
            this.emotion = (byte) 3;
        } else if (this.message.contains(":(") || this.message.contains(":-(") || this.message.contains(":[")) {
            this.emotion = (byte) 4;
        }
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.habbo == null) {
            return;
        }
        if (this.message.length() > MAXIMUM_LENGTH) {
            try {
                this.message = this.message.substring(0, MAXIMUM_LENGTH - 1);
            } catch (Exception e) {
                LOGGER.error("Caught exception", e);
            }
        }
        Emulator.getDatabaseLogger().store(this);
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String str) {
        this.message = str;
    }

    public String getUnfilteredMessage() {
        return this.unfilteredMessage;
    }

    public RoomChatMessageBubbles getBubble() {
        return this.bubble;
    }

    public Habbo getHabbo() {
        return this.habbo;
    }

    public Habbo getTargetHabbo() {
        return this.targetHabbo;
    }

    public int getEmotion() {
        return this.emotion;
    }

    @Override // com.eu.habbo.messages.ISerialize
    public void serialize(ServerMessage serverMessage) {
        if (this.habbo != null && this.bubble.isOverridable() && !this.habbo.hasPermission(Permission.ACC_ANYCHATCOLOR)) {
            int[] iArr = BANNED_BUBBLES;
            int length = iArr.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                if (Integer.valueOf(iArr[i]).intValue() == this.bubble.getType()) {
                    this.bubble = RoomChatMessageBubbles.NORMAL;
                    break;
                }
                i++;
            }
        }
        if (!getBubble().getPermission().isEmpty() && this.habbo != null && !this.habbo.hasPermission(getBubble().getPermission())) {
            this.bubble = RoomChatMessageBubbles.NORMAL;
        }
        try {
            serverMessage.appendInt(Integer.valueOf(this.roomUnitId));
            serverMessage.appendString(getMessage());
            serverMessage.appendInt(Integer.valueOf(getEmotion()));
            serverMessage.appendInt(Integer.valueOf(getBubble().getType()));
            serverMessage.appendInt((Integer) 0);
            serverMessage.appendInt(Integer.valueOf(getMessage().length()));
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
        }
    }

    public void filter() {
        if (!this.habbo.getHabboStats().hasActiveClub()) {
            Iterator<String> it = chatColors.iterator();
            while (it.hasNext()) {
                this.message = this.message.replace(it.next(), Emulator.PREVIEW);
            }
        }
        if (Emulator.getConfig().getBoolean("hotel.wordfilter.enabled") && Emulator.getConfig().getBoolean("hotel.wordfilter.rooms") && !this.habbo.hasPermission(Permission.ACC_CHAT_NO_FILTER)) {
            if (Emulator.getGameEnvironment().getWordFilter().autoReportCheck(this)) {
                int i = Emulator.getConfig().getInt("hotel.wordfilter.automute");
                if (i > 0) {
                    this.habbo.mute(i, false);
                } else {
                    LOGGER.error("Invalid hotel.wordfilter.automute defined in emulator_settings ({}).", Integer.valueOf(i));
                }
            } else if (!Emulator.getGameEnvironment().getWordFilter().hideMessageCheck(this.message)) {
                Emulator.getGameEnvironment().getWordFilter().filter(this, this.habbo);
                return;
            }
            this.message = Emulator.PREVIEW;
        }
    }

    @Override // com.eu.habbo.core.DatabaseLoggable
    public String getQuery() {
        return QUERY;
    }

    @Override // com.eu.habbo.core.DatabaseLoggable
    public void log(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setInt(1, this.habbo.getHabboInfo().getId());
        if (this.targetHabbo != null) {
            preparedStatement.setInt(2, this.targetHabbo.getHabboInfo().getId());
        } else {
            preparedStatement.setInt(2, 0);
        }
        preparedStatement.setString(3, this.unfilteredMessage);
        preparedStatement.setInt(4, this.timestamp);
        if (this.habbo.getHabboInfo().getCurrentRoom() != null) {
            preparedStatement.setInt(5, this.habbo.getHabboInfo().getCurrentRoom().getId());
        } else {
            preparedStatement.setInt(5, 0);
        }
        preparedStatement.addBatch();
    }
}
