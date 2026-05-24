package com.eu.habbo.messages.incoming.rooms.pets;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.pets.HorsePet;
import com.eu.habbo.habbohotel.pets.Pet;
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

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/pets/HorseRemoveSaddleEvent.class */
public class HorseRemoveSaddleEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(HorseRemoveSaddleEvent.class);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Pet pet = this.client.getHabbo().getHabboInfo().getCurrentRoom().getPet(this.packet.readInt().intValue());
        if (pet != null && (pet instanceof HorsePet) && pet.getUserId() == this.client.getHabbo().getHabboInfo().getId()) {
            HorsePet horsePet = (HorsePet) pet;
            if (horsePet.hasSaddle()) {
                int saddleItemId = horsePet.getSaddleItemId();
                if (saddleItemId == 0) {
                    try {
                        Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                        try {
                            PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT id FROM items_base WHERE item_name LIKE 'horse_saddle%' LIMIT 1");
                            try {
                                ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                                try {
                                    if (!resultSetExecuteQuery.next()) {
                                        LOGGER.error("There is no viable fallback saddle item for old horses with no saddle item ID. Horse pet ID: " + horsePet.getId());
                                        if (resultSetExecuteQuery != null) {
                                            resultSetExecuteQuery.close();
                                        }
                                        if (preparedStatementPrepareStatement != null) {
                                            preparedStatementPrepareStatement.close();
                                        }
                                        if (connection != null) {
                                            connection.close();
                                            return;
                                        }
                                        return;
                                    }
                                    saddleItemId = resultSetExecuteQuery.getInt("id");
                                    if (resultSetExecuteQuery != null) {
                                        resultSetExecuteQuery.close();
                                    }
                                    if (preparedStatementPrepareStatement != null) {
                                        preparedStatementPrepareStatement.close();
                                    }
                                    if (connection != null) {
                                        connection.close();
                                    }
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
                    }
                }
                Item item = Emulator.getGameEnvironment().getItemManager().getItem(saddleItemId);
                if (item == null) {
                    return;
                }
                horsePet.hasSaddle(false);
                horsePet.needsUpdate = true;
                Emulator.getThreading().run(pet);
                this.client.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RoomPetHorseFigureComposer(horsePet).compose());
                HabboItem habboItemCreateItem = Emulator.getGameEnvironment().getItemManager().createItem(this.client.getHabbo().getHabboInfo().getId(), item, 0, 0, Emulator.PREVIEW);
                this.client.getHabbo().getInventory().getItemsComponent().addItem(habboItemCreateItem);
                this.client.sendResponse(new AddHabboItemComposer(habboItemCreateItem));
                this.client.sendResponse(new InventoryRefreshComposer());
            }
        }
    }
}
