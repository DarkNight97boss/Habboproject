package com.eu.habbo.messages.incoming.friends;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.messenger.Messenger;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.friends.FriendRequestComposer;
import com.eu.habbo.messages.outgoing.friends.FriendRequestErrorComposer;
import com.eu.habbo.plugin.events.users.friends.UserRequestFriendshipEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/friends/FriendRequestEvent.class */
public class FriendRequestEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(FriendRequestEvent.class);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        String string = this.packet.readString();
        if (this.client == null || string == null || string.isEmpty()) {
            return;
        }
        Habbo habbo = Emulator.getGameServer().getGameClientManager().getHabbo(string);
        if (habbo == null) {
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT users.*, users_settings.block_friendrequests FROM users INNER JOIN users_settings ON users.id = users_settings.user_id WHERE username = ? LIMIT 1");
                    try {
                        preparedStatementPrepareStatement.setString(1, string);
                        ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                        while (resultSetExecuteQuery.next()) {
                            try {
                                habbo = new Habbo(resultSetExecuteQuery);
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
                return;
            }
        }
        if (habbo == null) {
            this.client.sendResponse(new FriendRequestErrorComposer(4));
            return;
        }
        int id = habbo.getHabboInfo().getId();
        boolean z = habbo.getHabboStats().blockFriendRequests;
        if (id == this.client.getHabbo().getHabboInfo().getId()) {
            return;
        }
        if (z) {
            this.client.sendResponse(new FriendRequestErrorComposer(3));
            return;
        }
        if (this.client.getHabbo().getMessenger().getFriends().values().size() >= this.client.getHabbo().getHabboStats().maxFriends && !this.client.getHabbo().hasPermission("acc_infinite_friends")) {
            this.client.sendResponse(new FriendRequestErrorComposer(1));
            return;
        }
        if (habbo.getMessenger().getFriends().values().size() >= habbo.getHabboStats().maxFriends && !habbo.hasPermission("acc_infinite_friends")) {
            this.client.sendResponse(new FriendRequestErrorComposer(2));
        } else {
            if (((UserRequestFriendshipEvent) Emulator.getPluginManager().fireEvent(new UserRequestFriendshipEvent(this.client.getHabbo(), string, habbo))).isCancelled()) {
                this.client.sendResponse(new FriendRequestErrorComposer(2));
                return;
            }
            if (habbo.isOnline()) {
                habbo.getClient().sendResponse(new FriendRequestComposer(this.client.getHabbo()));
            }
            Messenger.makeFriendRequest(this.client.getHabbo().getHabboInfo().getId(), id);
        }
    }
}
