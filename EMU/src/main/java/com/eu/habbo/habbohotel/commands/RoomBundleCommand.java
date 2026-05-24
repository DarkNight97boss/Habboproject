package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.CatalogItem;
import com.eu.habbo.habbohotel.catalog.CatalogPage;
import com.eu.habbo.habbohotel.catalog.CatalogPageLayouts;
import com.eu.habbo.habbohotel.catalog.layouts.RoomBundleLayout;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoomBundleCommand extends Command {
   private static final Logger LOGGER = LoggerFactory.getLogger(RoomBundleCommand.class);

   public RoomBundleCommand() {
      super("cmd_bundle", Emulator.getTexts().getValue("commands.keys.cmd_bundle").split(";"));
   }

   @Override
   public boolean handle(GameClient gameClient, String[] params) throws Exception {
      if (params.length < 5) {
         gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_bundle.missing_params"), RoomChatMessageBubbles.ALERT);
         return true;
      }

      if (Emulator.getGameEnvironment().getCatalogManager().getCatalogPage("room_bundle_" + gameClient.getHabbo().getHabboInfo().getCurrentRoom().getId())
         != null) {
         gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_bundle.duplicate"), RoomChatMessageBubbles.ALERT);
         return true;
      }

      int parentId = Integer.valueOf(params[1]);
      int credits = Integer.valueOf(params[2]);
      int points = Integer.valueOf(params[3]);
      int pointsType = Integer.valueOf(params[4]);
      CatalogPage page = Emulator.getGameEnvironment()
         .getCatalogManager()
         .createCatalogPage(
            "Room Bundle: " + gameClient.getHabbo().getHabboInfo().getCurrentRoom().getName(),
            "room_bundle_" + gameClient.getHabbo().getHabboInfo().getCurrentRoom().getId(),
            gameClient.getHabbo().getHabboInfo().getCurrentRoom().getId(),
            0,
            CatalogPageLayouts.room_bundle,
            gameClient.getHabbo().getHabboInfo().getRank().getId(),
            parentId
         );
      if (page instanceof RoomBundleLayout) {
         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               PreparedStatement statement = connection.prepareStatement(
                  "INSERT INTO catalog_items (page_id, item_ids, catalog_name, cost_credits, cost_points, points_type ) VALUES (?, ?, ?, ?, ?, ?)", 1
               );

               try {
                  statement.setInt(1, page.getId());
                  statement.setString(2, "");
                  statement.setString(3, "room_bundle");
                  statement.setInt(4, credits);
                  statement.setInt(5, points);
                  statement.setInt(6, pointsType);
                  statement.execute();
                  ResultSet set = statement.getGeneratedKeys();

                  try {
                     if (set.next()) {
                        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM catalog_items WHERE id = ?");

                        try {
                           stmt.setInt(1, set.getInt(1));
                           ResultSet st = stmt.executeQuery();

                           try {
                              if (st.next()) {
                                 page.addItem(new CatalogItem(st));
                              }
                           } catch (Throwable var20) {
                              if (st != null) {
                                 try {
                                    st.close();
                                 } catch (Throwable var19) {
                                    var20.addSuppressed(var19);
                                 }
                              }

                              throw var20;
                           }

                           if (st != null) {
                              st.close();
                           }
                        } catch (Throwable var21) {
                           if (stmt != null) {
                              try {
                                 stmt.close();
                              } catch (Throwable var18) {
                                 var21.addSuppressed(var18);
                              }
                           }

                           throw var21;
                        }

                        if (stmt != null) {
                           stmt.close();
                        }
                     }
                  } catch (Throwable var22) {
                     if (set != null) {
                        try {
                           set.close();
                        } catch (Throwable var17) {
                           var22.addSuppressed(var17);
                        }
                     }

                     throw var22;
                  }

                  if (set != null) {
                     set.close();
                  }
               } catch (Throwable var23) {
                  if (statement != null) {
                     try {
                        statement.close();
                     } catch (Throwable var16) {
                        var23.addSuppressed(var16);
                     }
                  }

                  throw var23;
               }

               if (statement != null) {
                  statement.close();
               }
            } catch (Throwable var24) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var15) {
                     var24.addSuppressed(var15);
                  }
               }

               throw var24;
            }

            if (connection != null) {
               connection.close();
            }
         } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
         }

         ((RoomBundleLayout)page).loadItems(gameClient.getHabbo().getHabboInfo().getCurrentRoom());
         gameClient.getHabbo()
            .whisper(Emulator.getTexts().getValue("commands.succes.cmd_bundle").replace("%id%", page.getId() + ""), RoomChatMessageBubbles.ALERT);
      }

      return true;
   }
}
