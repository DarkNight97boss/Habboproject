package com.eu.habbo.messages.incoming.rooms.pets;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.pets.HorsePet;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.inventory.AddHabboItemComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import com.eu.habbo.messages.outgoing.rooms.pets.RoomPetHorseFigureComposer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HorseRemoveSaddleEvent extends MessageHandler {
   private static final Logger LOGGER = LoggerFactory.getLogger(HorseRemoveSaddleEvent.class);

   @Override
   public void handle() throws Exception {
      Room room = this.client.getHabbo().getHabboInfo().getCurrentRoom();
      Pet pet = room.getPet(this.packet.readInt());
      if (pet != null && pet instanceof HorsePet && pet.getUserId() == this.client.getHabbo().getHabboInfo().getId()) {
         HorsePet horse = (HorsePet)pet;
         if (horse.hasSaddle()) {
            int saddleItemId = horse.getSaddleItemId();
            if (saddleItemId == 0) {
               try {
                  label123: {
                     Connection connection = Emulator.getDatabase().getDataSource().getConnection();

                     label111: {
                        try {
                           PreparedStatement statement;
                           label125: {
                              statement = connection.prepareStatement("SELECT id FROM items_base WHERE item_name LIKE 'horse_saddle%' LIMIT 1");

                              try {
                                 ResultSet set = statement.executeQuery();

                                 label100: {
                                    try {
                                       if (!set.next()) {
                                          LOGGER.error(
                                             "There is no viable fallback saddle item for old horses with no saddle item ID. Horse pet ID: " + horse.getId()
                                          );
                                          break label100;
                                       }

                                       saddleItemId = set.getInt("id");
                                    } catch (Throwable var13) {
                                       if (set != null) {
                                          try {
                                             set.close();
                                          } catch (Throwable var12) {
                                             var13.addSuppressed(var12);
                                          }
                                       }

                                       throw var13;
                                    }

                                    if (set != null) {
                                       set.close();
                                    }
                                    break label125;
                                 }

                                 if (set != null) {
                                    set.close();
                                 }
                              } catch (Throwable var14) {
                                 if (statement != null) {
                                    try {
                                       statement.close();
                                    } catch (Throwable var11) {
                                       var14.addSuppressed(var11);
                                    }
                                 }

                                 throw var14;
                              }

                              if (statement != null) {
                                 statement.close();
                              }
                              break label111;
                           }

                           if (statement != null) {
                              statement.close();
                           }
                        } catch (Throwable var15) {
                           if (connection != null) {
                              try {
                                 connection.close();
                              } catch (Throwable var10) {
                                 var15.addSuppressed(var10);
                              }
                           }

                           throw var15;
                        }

                        if (connection != null) {
                           connection.close();
                        }
                        break label123;
                     }

                     if (connection != null) {
                        connection.close();
                     }

                     return;
                  }
               } catch (SQLException e) {
                  LOGGER.error("Caught SQL exception", e);
               }
            }

            Item saddleItem = Emulator.getGameEnvironment().getItemManager().getItem(saddleItemId);
            if (saddleItem != null) {
               horse.hasSaddle(false);
               horse.needsUpdate = true;
               Emulator.getThreading().run(pet);
               this.client.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RoomPetHorseFigureComposer(horse).compose());
               HabboItem saddle = Emulator.getGameEnvironment()
                  .getItemManager()
                  .createItem(this.client.getHabbo().getHabboInfo().getId(), saddleItem, 0, 0, "");
               this.client.getHabbo().getInventory().getItemsComponent().addItem(saddle);
               this.client.sendResponse(new AddHabboItemComposer(saddle));
               this.client.sendResponse(new InventoryRefreshComposer());
            }
         }
      }
   }
}
