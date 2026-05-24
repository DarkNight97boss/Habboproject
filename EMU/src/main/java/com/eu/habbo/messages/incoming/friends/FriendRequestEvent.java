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

public class FriendRequestEvent extends MessageHandler {
   private static final Logger LOGGER = LoggerFactory.getLogger(FriendRequestEvent.class);

   @Override
   public void handle() throws Exception {
      String username = this.packet.readString();
      if (this.client != null && username != null && !username.isEmpty()) {
         Habbo targetHabbo = Emulator.getGameServer().getGameClientManager().getHabbo(username);
         if (targetHabbo == null) {
            try {
               Connection connection = Emulator.getDatabase().getDataSource().getConnection();

               try {
                  PreparedStatement statement = connection.prepareStatement(
                     "SELECT users.*, users_settings.block_friendrequests FROM users INNER JOIN users_settings ON users.id = users_settings.user_id WHERE username = ? LIMIT 1"
                  );

                  try {
                     statement.setString(1, username);
                     ResultSet set = statement.executeQuery();

                     try {
                        while (set.next()) {
                           targetHabbo = new Habbo(set);
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
               return;
            }
         }

         if (targetHabbo == null) {
            this.client.sendResponse(new FriendRequestErrorComposer(4));
         } else {
            int targetId = targetHabbo.getHabboInfo().getId();
            boolean targetBlocksFriendRequests = targetHabbo.getHabboStats().blockFriendRequests;
            if (targetId != this.client.getHabbo().getHabboInfo().getId()) {
               if (targetBlocksFriendRequests) {
                  this.client.sendResponse(new FriendRequestErrorComposer(3));
               } else if (this.client.getHabbo().getMessenger().getFriends().values().size() >= this.client.getHabbo().getHabboStats().maxFriends
                  && !this.client.getHabbo().hasPermission("acc_infinite_friends")) {
                  this.client.sendResponse(new FriendRequestErrorComposer(1));
               } else if (targetHabbo.getMessenger().getFriends().values().size() >= targetHabbo.getHabboStats().maxFriends
                  && !targetHabbo.hasPermission("acc_infinite_friends")) {
                  this.client.sendResponse(new FriendRequestErrorComposer(2));
               } else if (Emulator.getPluginManager().fireEvent(new UserRequestFriendshipEvent(this.client.getHabbo(), username, targetHabbo)).isCancelled()) {
                  this.client.sendResponse(new FriendRequestErrorComposer(2));
               } else {
                  if (targetHabbo.isOnline()) {
                     targetHabbo.getClient().sendResponse(new FriendRequestComposer(this.client.getHabbo()));
                  }

                  Messenger.makeFriendRequest(this.client.getHabbo().getHabboInfo().getId(), targetId);
               }
            }
         }
      }
   }
}
