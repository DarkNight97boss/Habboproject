package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/MannequinSaveNameEvent.class */
public class MannequinSaveNameEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        HabboItem habboItem;
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null || !currentRoom.isOwner(this.client.getHabbo()) || (habboItem = currentRoom.getHabboItem(this.packet.readInt().intValue())) == null) {
            return;
        }
        String[] strArrSplit = habboItem.getExtradata().split(":");
        String string = this.packet.readString();
        if (string.length() < 3 || string.length() > 15) {
            string = Emulator.getTexts().getValue("hotel.mannequin.name.default", "My look");
        }
        if (strArrSplit.length == 3) {
            habboItem.setExtradata(this.client.getHabbo().getHabboInfo().getGender().name().toUpperCase() + ":" + strArrSplit[1] + ":" + string);
        } else {
            habboItem.setExtradata(this.client.getHabbo().getHabboInfo().getGender().name().toUpperCase() + ":" + this.client.getHabbo().getHabboInfo().getLook() + ":" + string);
        }
        habboItem.needsUpdate(true);
        Emulator.getThreading().run(habboItem);
        currentRoom.updateItem(habboItem);
    }
}
