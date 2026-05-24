package com.eu.habbo.messages.incoming.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/users/ChangeChatBubbleEvent.class */
public class ChangeChatBubbleEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        if (!this.client.getHabbo().hasPermission(Permission.ACC_ANYCHATCOLOR)) {
            for (String str : Emulator.getConfig().getValue("commands.cmd_chatcolor.banned_numbers").split(";")) {
                if (Integer.valueOf(str).intValue() == iIntValue) {
                    return;
                }
            }
        }
        this.client.getHabbo().getHabboStats().chatColor = RoomChatMessageBubbles.getBubble(iIntValue);
    }
}
