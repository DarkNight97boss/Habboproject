package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.PostItColor;
import com.eu.habbo.habbohotel.items.interactions.InteractionPostIt;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import java.util.Arrays;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/PostItSaveDataEvent.class */
public class PostItSaveDataEvent extends MessageHandler {
    private static List<String> COLORS = Arrays.asList("9CCEFF", "FF9CFF", "9CFF9C", "FFFF33");

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Room currentRoom;
        int iIntValue = this.packet.readInt().intValue();
        String string = this.packet.readString();
        String strReplace = this.packet.readString().replace("\t", Emulator.PREVIEW);
        if (strReplace.length() > Emulator.getConfig().getInt("postit.charlimit")) {
            ScripterManager.scripterDetected(this.client, Emulator.getTexts().getValue("scripter.warning.sticky.size").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()).replace("%amount%", strReplace.length() + Emulator.PREVIEW).replace("%limit%", Emulator.getConfig().getInt("postit.charlimit") + Emulator.PREVIEW));
            return;
        }
        if (COLORS.contains(string) && (currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom()) != null) {
            HabboItem habboItem = currentRoom.getHabboItem(iIntValue);
            if (habboItem instanceof InteractionPostIt) {
                if (string.equalsIgnoreCase(PostItColor.YELLOW.hexColor) || currentRoom.hasRights(this.client.getHabbo())) {
                    if (!currentRoom.hasRights(this.client.getHabbo())) {
                        return;
                    }
                } else if (!strReplace.startsWith(habboItem.getExtradata().replace(habboItem.getExtradata().split(" ")[0], Emulator.PREVIEW))) {
                    return;
                }
                if (string.isEmpty()) {
                    string = PostItColor.YELLOW.hexColor;
                }
                habboItem.setUserId(currentRoom.getOwnerId());
                habboItem.setExtradata(string + " " + strReplace);
                habboItem.needsUpdate(true);
                currentRoom.updateItem(habboItem);
                Emulator.getThreading().run(habboItem);
            }
        }
    }
}
