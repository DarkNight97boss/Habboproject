package com.eu.habbo.habbohotel.messenger;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.WordFilter;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboGender;
import com.eu.habbo.messages.ISerialize;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.friends.FriendChatMessageComposer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/messenger/MessengerBuddy.class */
public class MessengerBuddy implements Runnable, ISerialize {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessengerBuddy.class);
    private int id;
    private String username;
    private HabboGender gender;
    private int online;
    private String look;
    private String motto;
    private short relation;
    private int categoryId;
    private boolean inRoom;
    private int userOne;

    public MessengerBuddy(ResultSet resultSet) {
        Habbo habbo;
        this.gender = HabboGender.M;
        this.online = 0;
        this.look = Emulator.PREVIEW;
        this.motto = Emulator.PREVIEW;
        this.categoryId = 0;
        this.userOne = 0;
        try {
            this.id = resultSet.getInt("id");
            this.username = resultSet.getString("username");
            this.gender = HabboGender.valueOf(resultSet.getString("gender"));
            this.online = resultSet.getInt("online");
            this.motto = resultSet.getString("motto");
            this.look = resultSet.getString("look");
            this.relation = (short) resultSet.getInt("relation");
            this.categoryId = resultSet.getInt("category");
            this.userOne = resultSet.getInt("user_one_id");
            this.inRoom = false;
            if (this.online == 1 && (habbo = Emulator.getGameServer().getGameClientManager().getHabbo(this.username)) != null) {
                this.inRoom = habbo.getHabboInfo().getCurrentRoom() != null;
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }

    public MessengerBuddy(ResultSet resultSet, boolean z) {
        this.gender = HabboGender.M;
        this.online = 0;
        this.look = Emulator.PREVIEW;
        this.motto = Emulator.PREVIEW;
        this.categoryId = 0;
        this.userOne = 0;
        try {
            this.id = resultSet.getInt("id");
            this.username = resultSet.getString("username");
            this.look = resultSet.getString("look");
            this.relation = (short) 0;
            this.userOne = 0;
            this.online = resultSet.getInt("online");
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }

    public MessengerBuddy(int i, String str, String str2, Short sh, int i2) {
        this.gender = HabboGender.M;
        this.online = 0;
        this.look = Emulator.PREVIEW;
        this.motto = Emulator.PREVIEW;
        this.categoryId = 0;
        this.userOne = 0;
        this.id = i;
        this.username = str;
        this.gender = HabboGender.M;
        this.online = 0;
        this.motto = Emulator.PREVIEW;
        this.look = str2;
        this.relation = sh.shortValue();
        this.userOne = i2;
    }

    public MessengerBuddy(Habbo habbo, int i) {
        this.gender = HabboGender.M;
        this.online = 0;
        this.look = Emulator.PREVIEW;
        this.motto = Emulator.PREVIEW;
        this.categoryId = 0;
        this.userOne = 0;
        this.id = habbo.getHabboInfo().getId();
        this.username = habbo.getHabboInfo().getUsername();
        this.gender = habbo.getHabboInfo().getGender();
        this.online = habbo.getHabboInfo().isOnline() ? 1 : 0;
        this.motto = habbo.getHabboInfo().getMotto();
        this.look = habbo.getHabboInfo().getLook();
        this.relation = (short) 0;
        this.userOne = i;
        this.inRoom = habbo.getHabboInfo().getCurrentRoom() != null;
    }

    public int getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String str) {
        this.username = str;
    }

    public HabboGender getGender() {
        return this.gender;
    }

    public void setGender(HabboGender habboGender) {
        this.gender = habboGender;
    }

    public int getOnline() {
        return this.online;
    }

    public void setOnline(boolean z) {
        this.online = z ? 1 : 0;
    }

    public String getLook() {
        return this.look;
    }

    public void setLook(String str) {
        this.look = str;
    }

    public String getMotto() {
        return this.motto;
    }

    public short getRelation() {
        return this.relation;
    }

    public void setRelation(int i) {
        this.relation = (short) i;
        Emulator.getThreading().run(this);
    }

    public int getCategoryId() {
        return this.categoryId;
    }

    public boolean inRoom() {
        return this.inRoom;
    }

    public void inRoom(boolean z) {
        this.inRoom = z;
    }

    @Override // java.lang.Runnable
    public void run() {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE messenger_friendships SET relation = ? WHERE user_one_id = ? AND user_two_id = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, this.relation);
                    preparedStatementPrepareStatement.setInt(2, this.userOne);
                    preparedStatementPrepareStatement.setInt(3, this.id);
                    preparedStatementPrepareStatement.execute();
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

    public void onMessageReceived(Habbo habbo, String str) {
        Habbo habbo2 = Emulator.getGameServer().getGameClientManager().getHabbo(this.id);
        if (habbo2 == null) {
            return;
        }
        Message message = new Message(habbo.getHabboInfo().getId(), this.id, str);
        Emulator.getThreading().run(message);
        if (WordFilter.ENABLED_FRIENDCHAT) {
            message.setMessage(Emulator.getGameEnvironment().getWordFilter().filter(message.getMessage(), habbo));
        }
        habbo2.getClient().sendResponse(new FriendChatMessageComposer(message));
    }

    @Override // com.eu.habbo.messages.ISerialize
    public void serialize(ServerMessage serverMessage) {
        serverMessage.appendInt(Integer.valueOf(this.id));
        serverMessage.appendString(this.username);
        serverMessage.appendInt(Integer.valueOf(this.gender.equals(HabboGender.M) ? 0 : 1));
        serverMessage.appendBoolean(Boolean.valueOf(this.online == 1));
        serverMessage.appendBoolean(Boolean.valueOf(this.inRoom));
        serverMessage.appendString(this.look);
        serverMessage.appendInt(Integer.valueOf(this.categoryId));
        serverMessage.appendString(this.motto);
        serverMessage.appendString(Emulator.PREVIEW);
        serverMessage.appendString(Emulator.PREVIEW);
        serverMessage.appendBoolean(false);
        serverMessage.appendBoolean(false);
        serverMessage.appendBoolean(false);
        serverMessage.appendShort(this.relation);
    }
}
