package com.eu.habbo.messages.incoming.rooms;

import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomRightLevels;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.RoomRightsComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserRemoveRightsComposer;
import gnu.trove.procedure.TIntProcedure;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/RoomRemoveAllRightsEvent.class */
public class RoomRemoveAllRightsEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        final Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null || currentRoom.getId() != this.packet.readInt().intValue()) {
            return;
        }
        if (currentRoom.getOwnerId() == this.client.getHabbo().getHabboInfo().getId() || this.client.getHabbo().hasPermission(Permission.ACC_ANYROOMOWNER)) {
            currentRoom.getRights().forEach(new TIntProcedure() { // from class: com.eu.habbo.messages.incoming.rooms.RoomRemoveAllRightsEvent.1
                public boolean execute(int i) {
                    Habbo habbo = currentRoom.getHabbo(i);
                    if (habbo == null) {
                        return true;
                    }
                    currentRoom.sendComposer(new RoomUserRemoveRightsComposer(currentRoom, i).compose());
                    habbo.getRoomUnit().removeStatus(RoomUnitStatus.FLAT_CONTROL);
                    habbo.getClient().sendResponse(new RoomRightsComposer(RoomRightLevels.NONE));
                    return true;
                }
            });
            currentRoom.removeAllRights();
        }
    }
}
