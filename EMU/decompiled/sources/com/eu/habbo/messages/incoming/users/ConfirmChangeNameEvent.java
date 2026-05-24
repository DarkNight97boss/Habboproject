package com.eu.habbo.messages.incoming.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboManager;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.users.ChangeNameUpdatedComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserNameChangedComposer;
import com.eu.habbo.messages.outgoing.users.ChangeNameCheckResultComposer;
import com.eu.habbo.messages.outgoing.users.UserDataComposer;
import com.eu.habbo.plugin.events.users.UserNameChangedEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/users/ConfirmChangeNameEvent.class */
public class ConfirmChangeNameEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfirmChangeNameEvent.class);
    public static final List<String> changingUsernames = new ArrayList(2);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (this.client.getHabbo().getHabboStats().allowNameChange) {
            String string = this.packet.readString();
            if (string.equalsIgnoreCase(this.client.getHabbo().getHabboInfo().getUsername())) {
                this.client.getHabbo().getHabboStats().allowNameChange = false;
                this.client.sendResponse(new ChangeNameUpdatedComposer(this.client.getHabbo()));
                this.client.sendResponse(new RoomUserNameChangedComposer(this.client.getHabbo()).compose());
                this.client.sendResponse(new UserDataComposer(this.client.getHabbo()));
                return;
            }
            if (string.equals(this.client.getHabbo().getHabboStats().changeNameChecked)) {
                if (HabboManager.getOfflineHabboInfo(string) != null) {
                    this.client.sendResponse(new ChangeNameCheckResultComposer(5, string, new ArrayList()));
                    return;
                }
                synchronized (changingUsernames) {
                    if (changingUsernames.contains(string)) {
                        return;
                    }
                    changingUsernames.add(string);
                    String username = this.client.getHabbo().getHabboInfo().getUsername();
                    this.client.getHabbo().getHabboStats().allowNameChange = false;
                    this.client.getHabbo().getHabboInfo().setUsername(string);
                    this.client.getHabbo().getHabboInfo().run();
                    Emulator.getPluginManager().fireEvent(new UserNameChangedEvent(this.client.getHabbo(), username));
                    for (Room room : Emulator.getGameEnvironment().getRoomManager().getRoomsForHabbo(this.client.getHabbo())) {
                        room.setOwnerName(string);
                        room.setNeedsUpdate(true);
                        room.save();
                    }
                    synchronized (changingUsernames) {
                        changingUsernames.remove(string);
                    }
                    this.client.sendResponse(new ChangeNameUpdatedComposer(this.client.getHabbo()));
                    if (this.client.getHabbo().getHabboInfo().getCurrentRoom() != null) {
                        this.client.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RoomUserNameChangedComposer(this.client.getHabbo()).compose());
                    } else {
                        this.client.sendResponse(new RoomUserNameChangedComposer(this.client.getHabbo()).compose());
                    }
                    this.client.getHabbo().getMessenger().connectionChanged(this.client.getHabbo(), true, this.client.getHabbo().getHabboInfo().getCurrentRoom() != null);
                    this.client.getHabbo().getClient().sendResponse(new UserDataComposer(this.client.getHabbo()));
                    try {
                        Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                        try {
                            PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO namechange_log (user_id, old_name, new_name, timestamp) VALUES (?, ?, ?, ?) ");
                            try {
                                preparedStatementPrepareStatement.setInt(1, this.client.getHabbo().getHabboInfo().getId());
                                preparedStatementPrepareStatement.setString(2, username);
                                preparedStatementPrepareStatement.setString(3, string);
                                preparedStatementPrepareStatement.setInt(4, Emulator.getIntUnixTimestamp());
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
            }
        }
    }
}
