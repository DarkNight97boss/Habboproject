package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.messages.outgoing.modtool.ModToolIssueHandledComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/RoomAlertCommand.class */
public class RoomAlertCommand extends Command {
    public RoomAlertCommand() {
        super("cmd_roomalert", Emulator.getTexts().getValue("commands.keys.cmd_roomalert").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        StringBuilder sb = new StringBuilder();
        if (strArr.length < 2) {
            return false;
        }
        for (int i = 1; i < strArr.length; i++) {
            sb.append(strArr[i]).append(" ");
        }
        if (sb.length() == 0) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_roomalert.empty"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        Room currentRoom = gameClient.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null) {
            return false;
        }
        currentRoom.sendComposer(new ModToolIssueHandledComposer(sb.toString()).compose());
        return true;
    }
}
