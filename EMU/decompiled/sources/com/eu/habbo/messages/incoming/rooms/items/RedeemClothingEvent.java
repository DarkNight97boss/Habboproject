package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.ClothItem;
import com.eu.habbo.habbohotel.items.interactions.InteractionClothing;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;
import com.eu.habbo.messages.outgoing.rooms.UpdateStackHeightComposer;
import com.eu.habbo.messages.outgoing.rooms.items.RemoveFloorItemComposer;
import com.eu.habbo.messages.outgoing.users.UserClothesComposer;
import com.eu.habbo.threading.runnables.QueryDeleteHabboItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/RedeemClothingEvent.class */
public class RedeemClothingEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedeemClothingEvent.class);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        HabboItem habboItem;
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        int iIntValue = this.packet.readInt().intValue();
        if (this.client.getHabbo().getHabboInfo().getCurrentRoom() != null && this.client.getHabbo().getHabboInfo().getCurrentRoom().hasRights(this.client.getHabbo()) && (habboItem = this.client.getHabbo().getHabboInfo().getCurrentRoom().getHabboItem(iIntValue)) != null && habboItem.getUserId() == this.client.getHabbo().getHabboInfo().getId() && (habboItem instanceof InteractionClothing)) {
            ClothItem clothing = Emulator.getGameEnvironment().getCatalogManager().getClothing(habboItem.getBaseItem().getName());
            if (clothing == null) {
                LOGGER.error("[Catalog] No definition in catalog_clothing found for clothing name " + habboItem.getBaseItem().getName() + ". Could not redeem clothing!");
                return;
            }
            if (this.client.getHabbo().getInventory().getWardrobeComponent().getClothing().contains(clothing.id)) {
                this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FIGURESET_OWNED_ALREADY.key));
                return;
            }
            habboItem.setRoomId(0);
            RoomTile tile = this.client.getHabbo().getHabboInfo().getCurrentRoom().getLayout().getTile(habboItem.getX(), habboItem.getY());
            this.client.getHabbo().getHabboInfo().getCurrentRoom().removeHabboItem(habboItem);
            this.client.getHabbo().getHabboInfo().getCurrentRoom().updateTile(tile);
            this.client.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new UpdateStackHeightComposer(tile.x, tile.y, tile.z, tile.relativeHeight()).compose());
            this.client.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RemoveFloorItemComposer(habboItem, true).compose());
            Emulator.getThreading().run(new QueryDeleteHabboItem(habboItem.getId()));
            try {
                connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO users_clothing (user_id, clothing_id) VALUES (?, ?)");
                } finally {
                }
            } catch (SQLException e) {
                LOGGER.error("Caught SQL exception", e);
            }
            try {
                preparedStatementPrepareStatement.setInt(1, this.client.getHabbo().getHabboInfo().getId());
                preparedStatementPrepareStatement.setInt(2, clothing.id);
                preparedStatementPrepareStatement.execute();
                if (preparedStatementPrepareStatement != null) {
                    preparedStatementPrepareStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
                this.client.getHabbo().getInventory().getWardrobeComponent().getClothing().add(clothing.id);
                this.client.getHabbo().getInventory().getWardrobeComponent().getClothingSets().addAll(clothing.setId);
                this.client.sendResponse(new UserClothesComposer(this.client.getHabbo()));
                this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FIGURESET_REDEEMED.key));
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
        }
    }
}
