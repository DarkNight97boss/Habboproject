package com.eu.habbo.messages.incoming.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.plugin.events.furniture.FurnitureRoomTonerEvent;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/RoomBackgroundEvent.class */
public class RoomBackgroundEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        HabboItem habboItem;
        int iIntValue = this.packet.readInt().intValue();
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null) {
            return;
        }
        if ((currentRoom.hasRights(this.client.getHabbo()) || this.client.getHabbo().hasPermission(Permission.ACC_PLACEFURNI)) && (habboItem = currentRoom.getHabboItem(iIntValue)) != null) {
            FurnitureRoomTonerEvent furnitureRoomTonerEvent = (FurnitureRoomTonerEvent) Emulator.getPluginManager().fireEvent(new FurnitureRoomTonerEvent(habboItem, this.client.getHabbo(), this.packet.readInt().intValue(), this.packet.readInt().intValue(), this.packet.readInt().intValue()));
            if (furnitureRoomTonerEvent.isCancelled()) {
                return;
            }
            habboItem.setExtradata(habboItem.getExtradata().split(":")[0] + ":" + (furnitureRoomTonerEvent.hue % 256) + ":" + (furnitureRoomTonerEvent.saturation % 256) + ":" + (furnitureRoomTonerEvent.brightness % 256));
            habboItem.needsUpdate(true);
            Emulator.getThreading().run(habboItem);
            currentRoom.updateItem(habboItem);
        }
    }
}
