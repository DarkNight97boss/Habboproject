package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.messenger.Message;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.messages.outgoing.friends.FriendChatMessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/StaffAlertCommand.class */
public class StaffAlertCommand extends Command {
    public StaffAlertCommand() {
        super("cmd_staffalert", Emulator.getTexts().getValue("commands.keys.cmd_staffalert").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length <= 1) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_staffalert.forgot_message"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < strArr.length; i++) {
            sb.append(strArr[i]).append(" ");
        }
        Emulator.getGameEnvironment().getHabboManager().staffAlert(((Object) sb) + "\r\n-" + gameClient.getHabbo().getHabboInfo().getUsername());
        Emulator.getGameServer().getGameClientManager().sendBroadcastResponse(new FriendChatMessageComposer(new Message(gameClient.getHabbo().getHabboInfo().getId(), -1, sb.toString())).compose(), "acc_staff_chat", gameClient);
        return true;
    }
}
