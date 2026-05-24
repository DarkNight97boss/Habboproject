package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.habbohotel.items.FurnitureType;
import com.eu.habbo.habbohotel.items.interactions.InteractionBackgroundToner;
import com.eu.habbo.habbohotel.items.interactions.InteractionBuildArea;
import com.eu.habbo.habbohotel.items.interactions.InteractionCannon;
import com.eu.habbo.habbohotel.items.interactions.InteractionJukeBox;
import com.eu.habbo.habbohotel.items.interactions.InteractionMoodLight;
import com.eu.habbo.habbohotel.items.interactions.InteractionPostIt;
import com.eu.habbo.habbohotel.items.interactions.InteractionPuzzleBox;
import com.eu.habbo.habbohotel.items.interactions.InteractionRoller;
import com.eu.habbo.habbohotel.items.interactions.InteractionRoomAds;
import com.eu.habbo.habbohotel.items.interactions.InteractionStackHelper;
import com.eu.habbo.habbohotel.items.interactions.InteractionWired;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.rooms.FurnitureMovementError;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;
import com.eu.habbo.messages.outgoing.inventory.RemoveHabboItemComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/RoomPlaceItemEvent.class */
public class RoomPlaceItemEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        String[] strArrSplit = this.packet.readString().split(" ");
        int i = -1;
        if (strArrSplit.length != 0) {
            i = Integer.parseInt(strArrSplit[0]);
        }
        if (!this.client.getHabbo().getRoomUnit().isInRoom()) {
            this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, FurnitureMovementError.NO_RIGHTS.errorCode));
            return;
        }
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null) {
            return;
        }
        HabboItem habboItem = null;
        if (this.client.getHabbo().getHabboStats().isRentingSpace()) {
            habboItem = currentRoom.getHabboItem(this.client.getHabbo().getHabboStats().rentedItemId);
        }
        HabboItem habboItem2 = this.client.getHabbo().getInventory().getItemsComponent().getHabboItem(i);
        if (habboItem2 == null || habboItem2.getBaseItem().getInteractionType().getType() == InteractionPostIt.class) {
            return;
        }
        if (currentRoom.getId() == habboItem2.getRoomId() || habboItem2.getRoomId() == 0) {
            if ((habboItem2 instanceof InteractionMoodLight) && !currentRoom.getRoomSpecialTypes().getItemsOfType(InteractionMoodLight.class).isEmpty()) {
                this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, FurnitureMovementError.MAX_DIMMERS.errorCode));
                return;
            }
            if ((habboItem2 instanceof InteractionJukeBox) && !currentRoom.getRoomSpecialTypes().getItemsOfType(InteractionJukeBox.class).isEmpty()) {
                this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, FurnitureMovementError.MAX_SOUNDFURNI.errorCode));
                return;
            }
            if (habboItem2.getBaseItem().getType() == FurnitureType.FLOOR) {
                short s = Short.parseShort(strArrSplit[1]);
                short s2 = Short.parseShort(strArrSplit[2]);
                int i2 = Integer.parseInt(strArrSplit[3]);
                RoomTile tile = currentRoom.getLayout().getTile(s, s2);
                if (tile == null) {
                    ScripterManager.scripterDetected(this.client, "User [" + this.client.getHabbo().getHabboInfo().getUsername() + "] tried to place a furni with itemId [" + i + "] at a tile which is not existing in room [" + currentRoom.getId() + "], tile: [" + ((int) s) + "," + ((int) s2) + "]");
                    return;
                }
                HabboItem habboItem3 = null;
                TObjectHashIterator it = currentRoom.getRoomSpecialTypes().getItemsOfType(InteractionBuildArea.class).iterator();
                while (it.hasNext()) {
                    HabboItem habboItem4 = (HabboItem) it.next();
                    if (((InteractionBuildArea) habboItem4).inSquare(tile)) {
                        habboItem3 = habboItem4;
                    }
                }
                if ((habboItem != null || habboItem3 != null) && !currentRoom.hasRights(this.client.getHabbo())) {
                    if ((habboItem2 instanceof InteractionRoller) || (habboItem2 instanceof InteractionStackHelper) || (habboItem2 instanceof InteractionWired) || (habboItem2 instanceof InteractionBackgroundToner) || (habboItem2 instanceof InteractionRoomAds) || (habboItem2 instanceof InteractionCannon) || (habboItem2 instanceof InteractionPuzzleBox) || habboItem2.getBaseItem().getType() == FurnitureType.WALL) {
                        this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, FurnitureMovementError.NO_RIGHTS.errorCode));
                        return;
                    } else if (habboItem != null && !RoomLayout.squareInSquare(RoomLayout.getRectangle(habboItem.getX(), habboItem.getY(), habboItem.getBaseItem().getWidth(), habboItem.getBaseItem().getLength(), habboItem.getRotation()), RoomLayout.getRectangle(s, s2, habboItem2.getBaseItem().getWidth(), habboItem2.getBaseItem().getLength(), i2))) {
                        this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, FurnitureMovementError.NO_RIGHTS.errorCode));
                        return;
                    }
                }
                FurnitureMovementError furnitureMovementErrorCanPlaceFurnitureAt = currentRoom.canPlaceFurnitureAt(habboItem2, this.client.getHabbo(), tile, i2);
                if (!furnitureMovementErrorCanPlaceFurnitureAt.equals(FurnitureMovementError.NONE)) {
                    this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, furnitureMovementErrorCanPlaceFurnitureAt.errorCode));
                    return;
                }
                FurnitureMovementError furnitureMovementErrorPlaceFloorFurniAt = currentRoom.placeFloorFurniAt(habboItem2, tile, i2, this.client.getHabbo());
                if (!furnitureMovementErrorPlaceFloorFurniAt.equals(FurnitureMovementError.NONE)) {
                    this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, furnitureMovementErrorPlaceFloorFurniAt.errorCode));
                    return;
                }
            } else {
                FurnitureMovementError furnitureMovementErrorPlaceWallFurniAt = currentRoom.placeWallFurniAt(habboItem2, strArrSplit[1] + " " + strArrSplit[2] + " " + strArrSplit[3], this.client.getHabbo());
                if (!furnitureMovementErrorPlaceWallFurniAt.equals(FurnitureMovementError.NONE)) {
                    this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, furnitureMovementErrorPlaceWallFurniAt.errorCode));
                    return;
                }
            }
            this.client.sendResponse(new RemoveHabboItemComposer(habboItem2.getGiftAdjustedId()));
            this.client.getHabbo().getInventory().getItemsComponent().removeHabboItem(habboItem2.getId());
            habboItem2.setFromGift(false);
        }
    }
}
