package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/MannequinSaveLookEvent.class */
public class MannequinSaveLookEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        HabboItem habboItem;
        Habbo habbo = this.client.getHabbo();
        Room currentRoom = habbo.getHabboInfo().getCurrentRoom();
        if (currentRoom == null || !currentRoom.isOwner(habbo) || (habboItem = currentRoom.getHabboItem(this.packet.readInt().intValue())) == null) {
            return;
        }
        String[] strArrSplit = habboItem.getExtradata().split(":");
        StringBuilder sb = new StringBuilder();
        for (String str : habbo.getHabboInfo().getLook().split("\\.")) {
            if (!str.contains("hr") && !str.contains("hd") && !str.contains("he") && !str.contains("ea") && !str.contains("ha") && !str.contains("fa")) {
                sb.append(str).append(".");
            }
        }
        if (sb.length() > 0) {
            sb = new StringBuilder(sb.substring(0, sb.length() - 1));
        }
        if (strArrSplit.length == 3) {
            habboItem.setExtradata(habbo.getHabboInfo().getGender().name().toLowerCase() + ":" + ((Object) sb) + ":" + strArrSplit[2]);
        } else {
            habboItem.setExtradata(habbo.getHabboInfo().getGender().name().toLowerCase() + ":" + ((Object) sb) + ":" + habbo.getHabboInfo().getUsername() + "'s look.");
        }
        habboItem.needsUpdate(true);
        Emulator.getThreading().run(habboItem);
        currentRoom.updateItem(habboItem);
    }
}
