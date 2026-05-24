package com.eu.habbo.habbohotel.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ISerialize;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.threading.runnables.UpdateModToolIssue;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/modtool/ModToolIssue.class */
public class ModToolIssue implements ISerialize {
    public int id;
    public volatile ModToolTicketState state;
    public volatile ModToolTicketType type;
    public int category;
    public int timestamp;
    public volatile int priority;
    public int reportedId;
    public String reportedUsername;
    public int roomId;
    public int senderId;
    public String senderUsername;
    public volatile int modId;
    public volatile String modName;
    public String message;
    public ArrayList<ModToolChatLog> chatLogs;
    public int groupId;
    public int threadId;
    public int commentId;
    public HabboItem photoItem;

    public ModToolIssue(ResultSet resultSet) throws SQLException {
        this.modId = -1;
        this.modName = Emulator.PREVIEW;
        this.chatLogs = null;
        this.groupId = -1;
        this.threadId = -1;
        this.commentId = -1;
        this.photoItem = null;
        this.id = resultSet.getInt("id");
        this.state = ModToolTicketState.getState(resultSet.getInt("state"));
        this.timestamp = resultSet.getInt("timestamp");
        this.priority = resultSet.getInt("score");
        this.senderId = resultSet.getInt("sender_id");
        this.senderUsername = resultSet.getString("sender_username");
        this.reportedId = resultSet.getInt("reported_id");
        this.reportedUsername = resultSet.getString("reported_username");
        this.message = resultSet.getString("issue");
        this.modId = resultSet.getInt("mod_id");
        this.modName = resultSet.getString("mod_username");
        this.type = ModToolTicketType.values()[resultSet.getInt("type") - 1];
        this.category = resultSet.getInt("category");
        this.groupId = resultSet.getInt("group_id");
        this.threadId = resultSet.getInt("thread_id");
        this.commentId = resultSet.getInt("comment_id");
        int i = resultSet.getInt("photo_item_id");
        if (i != -1) {
            this.photoItem = Emulator.getGameEnvironment().getItemManager().loadHabboItem(i);
        }
        if (this.modId <= 0) {
            this.modName = Emulator.PREVIEW;
            this.state = ModToolTicketState.OPEN;
        }
    }

    public ModToolIssue(int i, String str, int i2, String str2, int i3, String str3, ModToolTicketType modToolTicketType) {
        this.modId = -1;
        this.modName = Emulator.PREVIEW;
        this.chatLogs = null;
        this.groupId = -1;
        this.threadId = -1;
        this.commentId = -1;
        this.photoItem = null;
        this.state = ModToolTicketState.OPEN;
        this.timestamp = Emulator.getIntUnixTimestamp();
        this.priority = 0;
        this.senderId = i;
        this.senderUsername = str;
        this.reportedUsername = str2;
        this.reportedId = i2;
        this.roomId = i3;
        this.message = str3;
        this.type = modToolTicketType;
        this.category = 1;
    }

    @Override // com.eu.habbo.messages.ISerialize
    public void serialize(ServerMessage serverMessage) {
        serverMessage.appendInt(Integer.valueOf(this.id));
        serverMessage.appendInt(Integer.valueOf(this.state.getState()));
        serverMessage.appendInt(Integer.valueOf(this.type.getType()));
        serverMessage.appendInt(Integer.valueOf(this.category));
        serverMessage.appendInt(Integer.valueOf(Emulator.getIntUnixTimestamp() - this.timestamp));
        serverMessage.appendInt(Integer.valueOf(this.priority));
        serverMessage.appendInt((Integer) 1);
        serverMessage.appendInt(Integer.valueOf(this.senderId));
        serverMessage.appendString(this.senderUsername);
        serverMessage.appendInt(Integer.valueOf(this.reportedId));
        serverMessage.appendString(this.reportedUsername);
        serverMessage.appendInt(Integer.valueOf(this.modId));
        serverMessage.appendString(this.modName);
        serverMessage.appendString(this.message);
        serverMessage.appendInt((Integer) 0);
        if (this.chatLogs == null) {
            serverMessage.appendInt((Integer) 0);
            return;
        }
        serverMessage.appendInt(Integer.valueOf(this.chatLogs.size()));
        for (ModToolChatLog modToolChatLog : this.chatLogs) {
            serverMessage.appendString(modToolChatLog.message);
            serverMessage.appendInt((Integer) 0);
            serverMessage.appendInt(Integer.valueOf(modToolChatLog.message.length()));
        }
    }

    public void updateInDatabase() {
        Emulator.getThreading().run(new UpdateModToolIssue(this));
    }
}
