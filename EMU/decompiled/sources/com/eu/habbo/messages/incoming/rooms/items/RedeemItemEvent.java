package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.UpdateStackHeightComposer;
import com.eu.habbo.messages.outgoing.rooms.items.RemoveFloorItemComposer;
import com.eu.habbo.plugin.events.furniture.FurnitureRedeemedEvent;
import com.eu.habbo.threading.runnables.QueryDeleteHabboItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/RedeemItemEvent.class */
public class RedeemItemEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedeemItemEvent.class);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        HabboItem habboItem;
        int iIntValue = this.packet.readInt().intValue();
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null || (habboItem = currentRoom.getHabboItem(iIntValue)) == null || this.client.getHabbo().getHabboInfo().getId() != habboItem.getUserId()) {
            return;
        }
        boolean zIsRegistered = Emulator.getPluginManager().isRegistered(FurnitureRedeemedEvent.class, true);
        FurnitureRedeemedEvent furnitureRedeemedEvent = new FurnitureRedeemedEvent(habboItem, this.client.getHabbo(), 0, -1);
        if (habboItem.getBaseItem().getName().startsWith("CF_") || habboItem.getBaseItem().getName().startsWith("CFC_") || habboItem.getBaseItem().getName().startsWith("DF_") || habboItem.getBaseItem().getName().startsWith("PF_")) {
            if ((habboItem.getBaseItem().getName().startsWith("CF_") || habboItem.getBaseItem().getName().startsWith("CFC_")) && !habboItem.getBaseItem().getName().contains("_diamond_")) {
                try {
                    furnitureRedeemedEvent = new FurnitureRedeemedEvent(habboItem, this.client.getHabbo(), Integer.valueOf(habboItem.getBaseItem().getName().split("_")[1]).intValue(), -1);
                } catch (Exception e) {
                    LOGGER.error("Failed to parse redeemable furniture: " + habboItem.getBaseItem().getName() + ". Must be in format of CF_<amount>");
                    return;
                }
            } else if (habboItem.getBaseItem().getName().startsWith("PF_")) {
                try {
                    furnitureRedeemedEvent = new FurnitureRedeemedEvent(habboItem, this.client.getHabbo(), Integer.valueOf(habboItem.getBaseItem().getName().split("_")[1]).intValue(), 0);
                } catch (Exception e2) {
                    LOGGER.error("Failed to parse redeemable pixel furniture: " + habboItem.getBaseItem().getName() + ". Must be in format of PF_<amount>");
                    return;
                }
            } else if (habboItem.getBaseItem().getName().startsWith("DF_")) {
                try {
                    try {
                        furnitureRedeemedEvent = new FurnitureRedeemedEvent(habboItem, this.client.getHabbo(), Integer.valueOf(habboItem.getBaseItem().getName().split("_")[2]).intValue(), Integer.valueOf(habboItem.getBaseItem().getName().split("_")[1]).intValue());
                    } catch (Exception e3) {
                        LOGGER.error("Failed to parse redeemable points furniture: " + habboItem.getBaseItem().getName() + ". Must be in format of DF_<pointstype>_<amount> where <pointstype> equals integer representation of seasonal currency.");
                        return;
                    }
                } catch (Exception e4) {
                    LOGGER.error("Failed to parse redeemable points furniture: " + habboItem.getBaseItem().getName() + ". Must be in format of DF_<pointstype>_<amount> where <pointstype> equals integer representation of seasonal currency.");
                    return;
                }
            } else if (habboItem.getBaseItem().getName().startsWith("CF_diamond_")) {
                try {
                    furnitureRedeemedEvent = new FurnitureRedeemedEvent(habboItem, this.client.getHabbo(), Integer.valueOf(habboItem.getBaseItem().getName().split("_")[2]).intValue(), 5);
                } catch (Exception e5) {
                    LOGGER.error("Failed to parse redeemable diamonds furniture: " + habboItem.getBaseItem().getName() + ". Must be in format of CF_diamond_<amount>");
                    return;
                }
            }
            if (zIsRegistered) {
                Emulator.getPluginManager().fireEvent(furnitureRedeemedEvent);
                if (furnitureRedeemedEvent.isCancelled()) {
                }
            }
            if (furnitureRedeemedEvent.amount >= 1 && currentRoom.getHabboItem(habboItem.getId()) != null) {
                currentRoom.removeHabboItem(habboItem);
                currentRoom.sendComposer(new RemoveFloorItemComposer(habboItem).compose());
                RoomTile tile = currentRoom.getLayout().getTile(habboItem.getX(), habboItem.getY());
                tile.setStackHeight(currentRoom.getStackHeight(habboItem.getX(), habboItem.getY(), false));
                currentRoom.updateTile(tile);
                currentRoom.sendComposer(new UpdateStackHeightComposer(habboItem.getX(), habboItem.getY(), tile.z, tile.relativeHeight()).compose());
                Emulator.getThreading().run(new QueryDeleteHabboItem(habboItem.getId()));
                switch (furnitureRedeemedEvent.currencyID) {
                    case -1:
                        this.client.getHabbo().giveCredits(furnitureRedeemedEvent.amount);
                        break;
                    case 0:
                        this.client.getHabbo().givePixels(furnitureRedeemedEvent.amount);
                        break;
                    case 5:
                        this.client.getHabbo().givePoints(furnitureRedeemedEvent.amount);
                        break;
                    default:
                        this.client.getHabbo().givePoints(furnitureRedeemedEvent.currencyID, furnitureRedeemedEvent.amount);
                        break;
                }
            }
        }
    }
}
