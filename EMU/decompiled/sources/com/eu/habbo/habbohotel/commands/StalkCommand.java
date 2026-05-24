package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.rooms.RoomDataComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/StalkCommand.class */
public class StalkCommand extends Command {
    public StalkCommand() {
        super("cmd_stalk", Emulator.getTexts().getValue("commands.keys.cmd_stalk").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (gameClient.getHabbo().getHabboInfo().getCurrentRoom() == null) {
            return true;
        }
        if (strArr.length < 2) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_stalk.forgot_username"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(strArr[1]);
        if (habbo == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_stalk.not_found").replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (habbo.getHabboInfo().getCurrentRoom() == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_stalk.not_room").replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (gameClient.getHabbo().getHabboInfo().getUsername().equals(habbo.getHabboInfo().getUsername())) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.generic.cmd_stalk.self").replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (gameClient.getHabbo().getHabboInfo().getCurrentRoom() == habbo.getHabboInfo().getCurrentRoom()) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.generic.cmd_stalk.same_room").replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
            return true;
        }
        gameClient.sendResponse(new RoomDataComposer(habbo.getHabboInfo().getCurrentRoom(), gameClient.getHabbo(), true, false));
        return true;
    }
}
