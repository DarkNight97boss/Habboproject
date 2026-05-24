package com.eu.habbo.messages.incoming.rooms.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.plugin.events.users.UserRightsGivenEvent;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/users/RoomUserGiveRightsEvent.class */
public class RoomUserGiveRightsEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null) {
            return;
        }
        if (currentRoom.getOwnerId() == this.client.getHabbo().getHabboInfo().getId() || this.client.getHabbo().hasPermission(Permission.ACC_ANYROOMOWNER)) {
            Habbo habbo = currentRoom.getHabbo(iIntValue);
            if (habbo != null) {
                if (((UserRightsGivenEvent) Emulator.getPluginManager().fireEvent(new UserRightsGivenEvent(this.client.getHabbo(), habbo))).isCancelled()) {
                    return;
                }
                currentRoom.giveRights(habbo);
            } else if (this.client.getHabbo().getMessenger().getFriend(iIntValue) != null) {
                currentRoom.giveRights(iIntValue);
            }
        }
    }
}
