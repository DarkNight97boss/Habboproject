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
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/navigator/RequestDeleteRoomEvent.class */
public class RequestDeleteRoomEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestDeleteRoomEvent.class);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        PreparedStatement preparedStatementPrepareStatement;
        Guild guild;
        int iIntValue = this.packet.readInt().intValue();
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(iIntValue);
        if (room != null) {
            if (!room.isOwner(this.client.getHabbo())) {
                String strReplace = Emulator.getTexts().getValue("scripter.warning.room.delete").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()).replace("%roomname%", room.getName()).replace("%roomowner%", room.getOwnerName());
                ScripterManager.scripterDetected(this.client, strReplace);
                LOGGER.info(strReplace);
                return;
            }
            if (room.getId() == this.client.getHabbo().getHabboInfo().getHomeRoom() || ((NavigatorRoomDeletedEvent) Emulator.getPluginManager().fireEvent(new NavigatorRoomDeletedEvent(this.client.getHabbo(), room))).isCancelled()) {
                return;
            }
            room.ejectAll();
            room.ejectUserFurni(room.getOwnerId());
            Iterator it = new ArrayList(room.getCurrentBots().valueCollection()).iterator();
            while (it.hasNext()) {
                Emulator.getGameEnvironment().getBotManager().pickUpBot((Bot) it.next(), (Habbo) null);
            }
            for (Pet pet : new ArrayList(room.getCurrentPets().valueCollection())) {
                if (pet instanceof RideablePet) {
                    RideablePet rideablePet = (RideablePet) pet;
                    if (rideablePet.getRider() != null) {
                        rideablePet.getRider().getHabboInfo().dismountPet(true);
                    }
                }
                pet.removeFromRoom();
                Emulator.getThreading().run(pet);
                Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(pet.getUserId());
                if (habbo != null) {
                    habbo.getClient().sendResponse(new AddPetComposer(pet));
                    habbo.getInventory().getPetsComponent().addPet(pet);
                }
            }
            if (room.getGuildId() > 0 && (guild = Emulator.getGameEnvironment().getGuildManager().getGuild(room.getGuildId())) != null) {
                Emulator.getGameEnvironment().getGuildManager().deleteGuild(guild);
            }
            room.preventUnloading = false;
            room.dispose();
            Emulator.getGameEnvironment().getRoomManager().uncacheRoom(room);
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement2 = connection.prepareStatement("DELETE FROM rooms WHERE id = ? LIMIT 1");
                    try {
                        preparedStatementPrepareStatement2.setInt(1, iIntValue);
                        preparedStatementPrepareStatement2.execute();
                        if (preparedStatementPrepareStatement2 != null) {
                            preparedStatementPrepareStatement2.close();
                        }
                        if (room.hasCustomLayout()) {
                            preparedStatementPrepareStatement = connection.prepareStatement("DELETE FROM room_models_custom WHERE id = ? LIMIT 1");
                            try {
                                preparedStatementPrepareStatement.setInt(1, iIntValue);
                                preparedStatementPrepareStatement.execute();
                                if (preparedStatementPrepareStatement != null) {
                                    preparedStatementPrepareStatement.close();
                                }
                            } finally {
                            }
                        }
                        Emulator.getGameEnvironment().getRoomManager().unloadRoom(room);
                        PreparedStatement preparedStatementPrepareStatement3 = connection.prepareStatement("DELETE FROM room_rights WHERE room_id = ?");
                        try {
                            preparedStatementPrepareStatement3.setInt(1, iIntValue);
                            preparedStatementPrepareStatement3.execute();
                            if (preparedStatementPrepareStatement3 != null) {
                                preparedStatementPrepareStatement3.close();
                            }
                            preparedStatementPrepareStatement = connection.prepareStatement("DELETE FROM room_votes WHERE room_id = ?");
                            try {
                                preparedStatementPrepareStatement.setInt(1, iIntValue);
                                preparedStatementPrepareStatement.execute();
                                if (preparedStatementPrepareStatement != null) {
                                    preparedStatementPrepareStatement.close();
                                }
                                PreparedStatement preparedStatementPrepareStatement4 = connection.prepareStatement("DELETE FROM room_wordfilter WHERE room_id = ?");
                                try {
                                    preparedStatementPrepareStatement4.setInt(1, iIntValue);
                                    preparedStatementPrepareStatement4.execute();
                                    if (preparedStatementPrepareStatement4 != null) {
                                        preparedStatementPrepareStatement4.close();
                                    }
                                    if (connection != null) {
                                        connection.close();
                                    }
                                } finally {
                                }
                            } finally {
                                if (preparedStatementPrepareStatement != null) {
                                    try {
                                        preparedStatementPrepareStatement.close();
                                    } catch (Throwable th) {
                                        th.addSuppressed(th);
                                    }
                                }
                            }
                        } finally {
                            if (preparedStatementPrepareStatement3 != null) {
                                try {
                                    preparedStatementPrepareStatement3.close();
                                } catch (Throwable th2) {
                                    th.addSuppressed(th2);
                                }
                            }
                        }
                    } finally {
                        if (preparedStatementPrepareStatement2 != null) {
                            try {
                                preparedStatementPrepareStatement2.close();
                            } catch (Throwable th3) {
                                th.addSuppressed(th3);
                            }
                        }
                    }
                } finally {
                }
            } catch (SQLException e) {
                LOGGER.error("Caught SQL exception", e);
            }
        }
    }
}
