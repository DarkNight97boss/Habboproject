package com.eu.habbo.messages.incoming.rooms.users;

import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/users/RoomUserLookAtPoint.class */
public class RoomUserLookAtPoint extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        RoomTile tile;
        if (this.client.getHabbo().getHabboInfo().getCurrentRoom() == null) {
            return;
        }
        Habbo habbo = this.client.getHabbo();
        if (habbo.getRoomUnit().getCacheable().get("control") != null) {
            habbo = (Habbo) this.client.getHabbo().getRoomUnit().getCacheable().get("control");
            if (habbo.getHabboInfo().getCurrentRoom() != this.client.getHabbo().getHabboInfo().getCurrentRoom()) {
                habbo.getRoomUnit().getCacheable().remove("controller");
                this.client.getHabbo().getRoomUnit().getCacheable().remove("control");
                habbo = this.client.getHabbo();
            }
        }
        RoomUnit roomUnit = habbo.getRoomUnit();
        if (!roomUnit.canWalk() || roomUnit.isWalking() || roomUnit.hasStatus(RoomUnitStatus.MOVE) || roomUnit.cmdLay || roomUnit.hasStatus(RoomUnitStatus.LAY) || roomUnit.isIdle()) {
            return;
        }
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        if ((iIntValue == roomUnit.getX() && iIntValue2 == roomUnit.getY()) || (tile = habbo.getHabboInfo().getCurrentRoom().getLayout().getTile((short) iIntValue, (short) iIntValue2)) == null) {
            return;
        }
        roomUnit.lookAtPoint(tile);
        roomUnit.statusUpdate(true);
    }
}
