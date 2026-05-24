package com.eu.habbo.threading.runnables.hopper;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class HopperActionTwo implements Runnable {
   private static final Logger LOGGER = LoggerFactory.getLogger(HopperActionTwo.class);
   private final HabboItem teleportOne;
   private final Room room;
   private final GameClient client;

   public HopperActionTwo(HabboItem teleportOne, Room room, GameClient client) {
      this.teleportOne = teleportOne;
      this.room = room;
      this.client = client;
   }

   @Override
   public void run() {
      this.teleportOne.setExtradata("2");
      int targetRoomId = 0;
      int targetItemId = 0;

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "SELECT items.id, items.room_id FROM items_hoppers INNER JOIN items ON items_hoppers.item_id = items.id WHERE base_item = ? AND items.id != ? AND room_id > 0 ORDER BY RAND() LIMIT 1"
            );

            try {
               statement.setInt(1, this.teleportOne.getBaseItem().getId());
               statement.setInt(2, this.teleportOne.getId());
               ResultSet set = statement.executeQuery();

               try {
                  if (set.next()) {
                     targetItemId = set.getInt("id");
                     targetRoomId = set.getInt("room_id");
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
      }

      if (targetRoomId != 0 && targetItemId != 0) {
         Emulator.getThreading().run(new HopperActionThree(this.teleportOne, this.room, this.client, targetRoomId, targetItemId), 500L);
      } else {
         this.teleportOne.setExtradata("0");
         this.client.getHabbo().getRoomUnit().setCanWalk(true);
         this.client.getHabbo().getRoomUnit().isTeleporting = false;
         Emulator.getThreading().run(new HopperActionFour(this.teleportOne, this.room, this.client), 500L);
      }

      this.room.updateItem(this.teleportOne);
   }
}
