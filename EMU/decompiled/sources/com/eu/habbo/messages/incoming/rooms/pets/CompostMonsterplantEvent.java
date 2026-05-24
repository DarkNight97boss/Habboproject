package com.eu.habbo.messages.incoming.rooms.pets;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.pets.MonsterplantPet;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.items.AddFloorItemComposer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/pets/CompostMonsterplantEvent.class */
public class CompostMonsterplantEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompostMonsterplantEvent.class);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        Pet pet = currentRoom.getPet(iIntValue);
        if (pet != null && (pet instanceof MonsterplantPet) && pet.getUserId() == this.client.getHabbo().getHabboInfo().getId() && ((MonsterplantPet) pet).isDead()) {
            Item item = Emulator.getGameEnvironment().getItemManager().getItem("mnstr_compost");
            if (item != null) {
                HabboItem habboItemCreateItem = Emulator.getGameEnvironment().getItemManager().createItem(pet.getUserId(), item, 0, 0, Emulator.PREVIEW);
                habboItemCreateItem.setX(pet.getRoomUnit().getX());
                habboItemCreateItem.setY(pet.getRoomUnit().getY());
                habboItemCreateItem.setZ(pet.getRoomUnit().getZ());
                habboItemCreateItem.setRotation(pet.getRoomUnit().getBodyRotation().getValue());
                currentRoom.addHabboItem(habboItemCreateItem);
                currentRoom.sendComposer(new AddFloorItemComposer(habboItemCreateItem, this.client.getHabbo().getHabboInfo().getUsername()).compose());
            }
            pet.removeFromRoom();
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("DELETE FROM users_pets WHERE id = ? LIMIT 1");
                    try {
                        preparedStatementPrepareStatement.setInt(1, pet.getId());
                        preparedStatementPrepareStatement.executeUpdate();
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
