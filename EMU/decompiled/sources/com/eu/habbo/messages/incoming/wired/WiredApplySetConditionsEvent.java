package com.eu.habbo.messages.incoming.wired;

import com.eu.habbo.habbohotel.items.interactions.wired.interfaces.InteractionWiredMatchFurniSettings;
import com.eu.habbo.habbohotel.rooms.FurnitureMovementError;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomTileState;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;
import com.eu.habbo.messages.outgoing.rooms.items.FloorItemOnRollerComposer;
import java.util.ArrayList;
import java.util.Optional;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/wired/WiredApplySetConditionsEvent.class */
public class WiredApplySetConditionsEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public int getRatelimit() {
        return 500;
    }

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        if (!this.client.getHabbo().getRoomUnit().isInRoom()) {
            this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, FurnitureMovementError.NO_RIGHTS.errorCode));
            return;
        }
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom != null) {
            if (currentRoom.hasRights(this.client.getHabbo()) || currentRoom.isOwner(this.client.getHabbo())) {
                ArrayList arrayList = new ArrayList();
                arrayList.addAll(currentRoom.getRoomSpecialTypes().getConditions());
                arrayList.addAll(currentRoom.getRoomSpecialTypes().getEffects());
                Optional optionalFindFirst = arrayList.stream().filter(habboItem -> {
                    return habboItem.getId() == iIntValue;
                }).findFirst();
                if (optionalFindFirst.isPresent()) {
                    Object obj = (HabboItem) optionalFindFirst.get();
                    if (obj instanceof InteractionWiredMatchFurniSettings) {
                        InteractionWiredMatchFurniSettings interactionWiredMatchFurniSettings = (InteractionWiredMatchFurniSettings) obj;
                        interactionWiredMatchFurniSettings.getMatchFurniSettings().forEach(wiredMatchFurniSetting -> {
                            HabboItem habboItem2 = currentRoom.getHabboItem(wiredMatchFurniSetting.item_id);
                            if (interactionWiredMatchFurniSettings.shouldMatchState() && habboItem2.allowWiredResetState() && !wiredMatchFurniSetting.state.equals(" ") && !habboItem2.getExtradata().equals(wiredMatchFurniSetting.state)) {
                                habboItem2.setExtradata(wiredMatchFurniSetting.state);
                                currentRoom.updateItemState(habboItem2);
                            }
                            RoomTile tile = currentRoom.getLayout().getTile(habboItem2.getX(), habboItem2.getY());
                            double z = habboItem2.getZ();
                            if (interactionWiredMatchFurniSettings.shouldMatchRotation() && !interactionWiredMatchFurniSettings.shouldMatchPosition()) {
                                if (habboItem2.getRotation() == wiredMatchFurniSetting.rotation || currentRoom.furnitureFitsAt(tile, habboItem2, wiredMatchFurniSetting.rotation, false) != FurnitureMovementError.NONE) {
                                    return;
                                }
                                currentRoom.moveFurniTo(habboItem2, tile, wiredMatchFurniSetting.rotation, null, true);
                                return;
                            }
                            if (interactionWiredMatchFurniSettings.shouldMatchPosition()) {
                                boolean z2 = !interactionWiredMatchFurniSettings.shouldMatchRotation() || habboItem2.getRotation() == wiredMatchFurniSetting.rotation;
                                RoomTile tile2 = currentRoom.getLayout().getTile((short) wiredMatchFurniSetting.x, (short) wiredMatchFurniSetting.y);
                                int rotation = interactionWiredMatchFurniSettings.shouldMatchRotation() ? wiredMatchFurniSetting.rotation : habboItem2.getRotation();
                                if (tile2 == null || tile2.state == RoomTileState.INVALID) {
                                    return;
                                }
                                if (!(tile2 == tile && rotation == habboItem2.getRotation()) && currentRoom.furnitureFitsAt(tile2, habboItem2, rotation, true) == FurnitureMovementError.NONE) {
                                    if (currentRoom.moveFurniTo(habboItem2, tile2, rotation, null, !z2) == FurnitureMovementError.NONE && z2) {
                                        currentRoom.sendComposer(new FloorItemOnRollerComposer(habboItem2, null, tile, z, tile2, habboItem2.getZ(), 0.0d, currentRoom).compose());
                                    }
                                }
                            }
                        });
                    }
                }
            }
        }
    }
}
