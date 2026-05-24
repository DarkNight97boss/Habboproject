package com.eu.habbo.messages.incoming.rooms.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUserAction;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserActionComposer;
import com.eu.habbo.plugin.events.users.UserIdleEvent;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/users/RoomUserActionEvent.class */
public class RoomUserActionEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom != null) {
            Habbo habbo = this.client.getHabbo();
            if (this.client.getHabbo().getRoomUnit().getCacheable().get("control") != null) {
                habbo = (Habbo) this.client.getHabbo().getRoomUnit().getCacheable().get("control");
                if (habbo.getHabboInfo().getCurrentRoom() != currentRoom) {
                    habbo.getRoomUnit().getCacheable().remove("controller");
                    this.client.getHabbo().getRoomUnit().getCacheable().remove("control");
                    habbo = this.client.getHabbo();
                }
            }
            int iIntValue = this.packet.readInt().intValue();
            if (iIntValue == 5) {
                UserIdleEvent userIdleEvent = new UserIdleEvent(this.client.getHabbo(), UserIdleEvent.IdleReason.ACTION, true);
                Emulator.getPluginManager().fireEvent(userIdleEvent);
                if (!userIdleEvent.isCancelled()) {
                    if (userIdleEvent.idle) {
                        currentRoom.idle(habbo);
                    } else {
                        currentRoom.unIdle(habbo);
                    }
                }
            } else {
                UserIdleEvent userIdleEvent2 = new UserIdleEvent(this.client.getHabbo(), UserIdleEvent.IdleReason.ACTION, false);
                Emulator.getPluginManager().fireEvent(userIdleEvent2);
                if (!userIdleEvent2.isCancelled() && !userIdleEvent2.idle) {
                    currentRoom.unIdle(habbo);
                }
            }
            currentRoom.sendComposer(new RoomUserActionComposer(habbo.getRoomUnit(), RoomUserAction.fromValue(iIntValue)).compose());
        }
    }
}
