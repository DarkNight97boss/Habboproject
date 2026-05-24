package com.eu.habbo.messages.incoming.guilds.forums;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.guilds.forums.GuildForumListComposer;
import com.eu.habbo.messages.outgoing.handshake.ConnectionErrorComposer;
import gnu.trove.set.hash.THashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuildForumListEvent extends MessageHandler {
   private static final Logger LOGGER = LoggerFactory.getLogger(GuildForumListEvent.class);

   @Override
   public void handle() throws Exception {
      int mode = this.packet.readInt();
      int offset = this.packet.readInt();
      int amount = this.packet.readInt();
      Set<Guild> guilds = null;
      switch (mode) {
         case 0:
            guilds = this.getActiveForums();
            break;
         case 1:
            guilds = Emulator.getGameEnvironment().getGuildManager().getMostViewed();
            break;
         case 2:
            guilds = this.getMyForums(this.client.getHabbo().getHabboInfo().getId());
      }

      if (guilds != null) {
         this.client.sendResponse(new GuildForumListComposer(guilds, this.client.getHabbo(), mode, offset));
      }
   }

   private THashSet<Guild> getActiveForums() {
      THashSet<Guild> guilds = new THashSet();

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "SELECT `guilds`.`id`, SUM(`guilds_forums_threads`.`posts_count`) AS `post_count` FROM `guilds_forums_threads` LEFT JOIN `guilds` ON `guilds`.`id` = `guilds_forums_threads`.`guild_id` WHERE `guilds`.`read_forum` = 'EVERYONE' AND `guilds_forums_threads`.`created_at` > ? GROUP BY `guilds`.`id` ORDER BY `post_count` DESC LIMIT 100"
            );

            try {
               statement.setInt(1, Emulator.getIntUnixTimestamp() - 604800);
               ResultSet set = statement.executeQuery();

               while (set.next()) {
                  Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(set.getInt("id"));
                  if (guild != null) {
                     guilds.add(guild);
                  }
               }
            } catch (Throwable var8) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var7) {
                     var8.addSuppressed(var7);
                  }
               }

               throw var8;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var9) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var6) {
                  var9.addSuppressed(var6);
               }
            }

            throw var9;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
         this.client.sendResponse(new ConnectionErrorComposer(500));
      }

      return guilds;
   }

   private THashSet<Guild> getMyForums(int userId) {
      THashSet<Guild> guilds = new THashSet();

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "SELECT `guilds`.`id` FROM `guilds_members` LEFT JOIN `guilds` ON `guilds`.`id` = `guilds_members`.`guild_id` WHERE `guilds_members`.`user_id` = ? AND `guilds`.`forum` = '1'"
            );

            try {
               statement.setInt(1, userId);
               ResultSet set = statement.executeQuery();

               while (set.next()) {
                  Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(set.getInt("id"));
                  if (guild != null) {
                     guilds.add(guild);
                  }
               }
            } catch (Throwable var9) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var8) {
                     var9.addSuppressed(var8);
                  }
               }

               throw var9;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var10) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var7) {
                  var10.addSuppressed(var7);
               }
            }

            throw var10;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
         this.client.sendResponse(new ConnectionErrorComposer(500));
      }

      return guilds;
   }
}
