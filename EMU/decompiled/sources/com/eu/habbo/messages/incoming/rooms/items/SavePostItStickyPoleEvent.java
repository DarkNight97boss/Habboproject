package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.commands.CommandHandler;
import com.eu.habbo.habbohotel.items.PostItColor;
import com.eu.habbo.habbohotel.items.interactions.InteractionPostIt;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/SavePostItStickyPoleEvent.class */
public class SavePostItStickyPoleEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SavePostItStickyPoleEvent.class);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        this.packet.readString();
        String string = this.packet.readString();
        if (iIntValue == -1234) {
            if (!this.client.getHabbo().hasPermission("cmd_multi")) {
                LOGGER.info("Scripter Alert! " + this.client.getHabbo().getHabboInfo().getUsername() + " | " + this.packet.readString());
                return;
            }
            for (String str : this.packet.readString().split("\r")) {
                CommandHandler.handleCommand(this.client, str.replace("<br>", "\r"));
            }
            return;
        }
        String string2 = this.packet.readString();
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        HabboItem habboItem = currentRoom.getHabboItem(iIntValue);
        if (habboItem == null || habboItem.getUserId() != this.client.getHabbo().getHabboInfo().getId()) {
            return;
        }
        habboItem.setUserId(currentRoom.getOwnerId());
        if (string.equalsIgnoreCase(PostItColor.YELLOW.hexColor)) {
            string = PostItColor.randomColorNotYellow().hexColor;
        }
        if (!InteractionPostIt.STICKYPOLE_PREFIX_TEXT.isEmpty()) {
            string2 = InteractionPostIt.STICKYPOLE_PREFIX_TEXT.replace("\\r", "\r").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()).replace("%timestamp%", LocalDate.now().toString()) + string2;
        }
        habboItem.setUserId(currentRoom.getOwnerId());
        habboItem.setExtradata(string + " " + string2);
        habboItem.needsUpdate(true);
        currentRoom.updateItem(habboItem);
        Emulator.getThreading().run(habboItem);
    }
}
