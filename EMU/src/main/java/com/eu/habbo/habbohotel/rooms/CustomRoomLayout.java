package com.eu.habbo.habbohotel.rooms;

import com.eu.habbo.Emulator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomRoomLayout extends RoomLayout implements Runnable {
   private static final Logger LOGGER = LoggerFactory.getLogger(CustomRoomLayout.class);
   private final int roomId;
   private boolean needsUpdate;

   public CustomRoomLayout(ResultSet set, Room room) throws SQLException {
      super(set, room);
      this.roomId = room.getId();
   }

   @Override
   public void run() {
      if (this.needsUpdate) {
         this.needsUpdate = false;

         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               PreparedStatement statement = connection.prepareStatement(
                  "UPDATE room_models_custom SET door_x = ?, door_y = ?, door_dir = ?, heightmap = ? WHERE id = ? LIMIT 1"
               );

               try {
                  statement.setInt(1, this.getDoorX());
                  statement.setInt(2, this.getDoorY());
                  statement.setInt(3, this.getDoorDirection());
                  statement.setString(4, this.getHeightmap());
                  statement.setInt(5, this.roomId);
                  statement.execute();
               } catch (Throwable var7) {
                  if (statement != null) {
                     try {
                        statement.close();
                     } catch (Throwable var6) {
                        var7.addSuppressed(var6);
                     }
                  }

                  throw var7;
               }

               if (statement != null) {
                  statement.close();
               }
            } catch (Throwable var8) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var5) {
                     var8.addSuppressed(var5);
                  }
               }

               throw var8;
            }

            if (connection != null) {
               connection.close();
            }
         } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
         }
      }
   }

   public boolean needsUpdate() {
      return this.needsUpdate;
   }

   public void needsUpdate(boolean needsUpdate) {
      this.needsUpdate = needsUpdate;
   }
}
