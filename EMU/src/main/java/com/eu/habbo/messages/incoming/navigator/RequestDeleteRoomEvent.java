package com.eu.habbo.messages.incoming.navigator;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.RideablePet;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.inventory.AddPetComposer;
import com.eu.habbo.plugin.events.navigator.NavigatorRoomDeletedEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestDeleteRoomEvent extends MessageHandler {
   private static final Logger LOGGER = LoggerFactory.getLogger(RequestDeleteRoomEvent.class);

   @Override
   public void handle() throws Exception {
      int roomId = this.packet.readInt();
      Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(roomId);
      if (room != null) {
         if (room.isOwner(this.client.getHabbo())) {
            if (room.getId() == this.client.getHabbo().getHabboInfo().getHomeRoom()) {
               return;
            }

            if (Emulator.getPluginManager().fireEvent(new NavigatorRoomDeletedEvent(this.client.getHabbo(), room)).isCancelled()) {
               return;
            }

            room.ejectAll();
            room.ejectUserFurni(room.getOwnerId());

            for (Bot bot : new ArrayList<Bot>(room.getCurrentBots().valueCollection())) {
               Emulator.getGameEnvironment().getBotManager().pickUpBot(bot, null);
            }

            for (Pet pet : new ArrayList<Pet>(room.getCurrentPets().valueCollection())) {
               if (pet instanceof RideablePet) {
                  RideablePet rideablePet = (RideablePet)pet;
                  if (rideablePet.getRider() != null) {
                     rideablePet.getRider().getHabboInfo().dismountPet(true);
                  }
               }

               pet.removeFromRoom();
               Emulator.getThreading().run(pet);
               Habbo owner = Emulator.getGameEnvironment().getHabboManager().getHabbo(pet.getUserId());
               if (owner != null) {
                  owner.getClient().sendResponse(new AddPetComposer(pet));
                  owner.getInventory().getPetsComponent().addPet(pet);
               }
            }

            if (room.getGuildId() > 0) {
               Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(room.getGuildId());
               if (guild != null) {
                  Emulator.getGameEnvironment().getGuildManager().deleteGuild(guild);
               }
            }

            room.preventUnloading = false;
            room.dispose();
            Emulator.getGameEnvironment().getRoomManager().uncacheRoom(room);

            try {
               Connection connection = Emulator.getDatabase().getDataSource().getConnection();

               try {
                  PreparedStatement statement = connection.prepareStatement("DELETE FROM rooms WHERE id = ? LIMIT 1");

                  try {
                     statement.setInt(1, roomId);
                     statement.execute();
                  } catch (Throwable var19) {
                     if (statement != null) {
                        try {
                           statement.close();
                        } catch (Throwable var14) {
                           var19.addSuppressed(var14);
                        }
                     }

                     throw var19;
                  }

                  if (statement != null) {
                     statement.close();
                  }

                  if (room.hasCustomLayout()) {
                     statement = connection.prepareStatement("DELETE FROM room_models_custom WHERE id = ? LIMIT 1");

                     try {
                        statement.setInt(1, roomId);
                        statement.execute();
                     } catch (Throwable var18) {
                        if (statement != null) {
                           try {
                              statement.close();
                           } catch (Throwable var13) {
                              var18.addSuppressed(var13);
                           }
                        }

                        throw var18;
                     }

                     if (statement != null) {
                        statement.close();
                     }
                  }

                  Emulator.getGameEnvironment().getRoomManager().unloadRoom(room);
                  statement = connection.prepareStatement("DELETE FROM room_rights WHERE room_id = ?");

                  try {
                     statement.setInt(1, roomId);
                     statement.execute();
                  } catch (Throwable var17) {
                     if (statement != null) {
                        try {
                           statement.close();
                        } catch (Throwable var12) {
                           var17.addSuppressed(var12);
                        }
                     }

                     throw var17;
                  }

                  if (statement != null) {
                     statement.close();
                  }

                  statement = connection.prepareStatement("DELETE FROM room_votes WHERE room_id = ?");

                  try {
                     statement.setInt(1, roomId);
                     statement.execute();
                  } catch (Throwable var16) {
                     if (statement != null) {
                        try {
                           statement.close();
                        } catch (Throwable var11) {
                           var16.addSuppressed(var11);
                        }
                     }

                     throw var16;
                  }

                  if (statement != null) {
                     statement.close();
                  }

                  statement = connection.prepareStatement("DELETE FROM room_wordfilter WHERE room_id = ?");

                  try {
                     statement.setInt(1, roomId);
                     statement.execute();
                  } catch (Throwable var15) {
                     if (statement != null) {
                        try {
                           statement.close();
                        } catch (Throwable var10) {
                           var15.addSuppressed(var10);
                        }
                     }

                     throw var15;
                  }

                  if (statement != null) {
                     statement.close();
                  }
               } catch (Throwable var20) {
                  if (connection != null) {
                     try {
                        connection.close();
                     } catch (Throwable var9) {
                        var20.addSuppressed(var9);
                     }
                  }

                  throw var20;
               }

               if (connection != null) {
                  connection.close();
               }
            } catch (SQLException e) {
               LOGGER.error("Caught SQL exception", e);
            }
         } else {
            String message = Emulator.getTexts()
               .getValue("scripter.warning.room.delete")
               .replace("%username%", this.client.getHabbo().getHabboInfo().getUsername())
               .replace("%roomname%", room.getName())
               .replace("%roomowner%", room.getOwnerName());
            ScripterManager.scripterDetected(this.client, message);
            LOGGER.info(message);
         }
      }
   }
}
