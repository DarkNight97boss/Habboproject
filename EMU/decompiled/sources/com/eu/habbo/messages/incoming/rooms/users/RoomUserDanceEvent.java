package com.eu.habbo.messages.incoming.rooms.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.DanceType;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserDanceComposer;
import com.eu.habbo.plugin.events.users.UserIdleEvent;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/users/RoomUserDanceEvent.class */
public class RoomUserDanceEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue;
        if (this.client.getHabbo().getHabboInfo().getCurrentRoom() != null && (iIntValue = this.packet.readInt().intValue()) >= 0 && iIntValue <= 5 && this.client.getHabbo().getRoomUnit().isInRoom()) {
            Habbo habbo = this.client.getHabbo();
            if (this.client.getHabbo().getRoomUnit().getCacheable().get("control") != null) {
                habbo = (Habbo) this.client.getHabbo().getRoomUnit().getCacheable().get("control");
                if (habbo.getHabboInfo().getCurrentRoom() != this.client.getHabbo().getHabboInfo().getCurrentRoom()) {
                    habbo.getRoomUnit().getCacheable().remove("controller");
                    this.client.getHabbo().getRoomUnit().getCacheable().remove("control");
                    habbo = this.client.getHabbo();
                }
            }
            habbo.getRoomUnit().setDanceType(DanceType.values()[iIntValue]);
            UserIdleEvent userIdleEvent = new UserIdleEvent(this.client.getHabbo(), UserIdleEvent.IdleReason.DANCE, false);
            Emulator.getPluginManager().fireEvent(userIdleEvent);
            if (!userIdleEvent.isCancelled() && !userIdleEvent.idle) {
                this.client.getHabbo().getHabboInfo().getCurrentRoom().unIdle(habbo);
            }
            this.client.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RoomUserDanceComposer(habbo.getRoomUnit()).compose());
        }
    }
}
