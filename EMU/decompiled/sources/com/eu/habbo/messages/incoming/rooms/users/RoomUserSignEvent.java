package com.eu.habbo.messages.incoming.rooms.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionVoteCounter;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.plugin.events.users.UserSignEvent;
import gnu.trove.iterator.hash.TObjectHashIterator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/users/RoomUserSignEvent.class */
public class RoomUserSignEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null) {
            return;
        }
        UserSignEvent userSignEvent = new UserSignEvent(this.client.getHabbo(), iIntValue);
        if (((UserSignEvent) Emulator.getPluginManager().fireEvent(userSignEvent)).isCancelled()) {
            return;
        }
        this.client.getHabbo().getRoomUnit().setStatus(RoomUnitStatus.SIGN, userSignEvent.sign + Emulator.PREVIEW);
        this.client.getHabbo().getHabboInfo().getCurrentRoom().unIdle(this.client.getHabbo());
        if (iIntValue <= 10) {
            int id = this.client.getHabbo().getHabboInfo().getId();
            TObjectHashIterator it = currentRoom.getFloorItems().iterator();
            while (it.hasNext()) {
                HabboItem habboItem = (HabboItem) it.next();
                if (habboItem instanceof InteractionVoteCounter) {
                    ((InteractionVoteCounter) habboItem).vote(currentRoom, id, iIntValue);
                }
            }
        }
    }
}
