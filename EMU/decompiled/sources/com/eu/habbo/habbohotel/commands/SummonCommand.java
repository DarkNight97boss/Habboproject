package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.rooms.ForwardToRoomComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/SummonCommand.class */
public class SummonCommand extends Command {
    public SummonCommand() {
        super("cmd_summon", Emulator.getTexts().getValue("commands.keys.cmd_summon").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (gameClient.getHabbo().getHabboInfo().getCurrentRoom() == null) {
            return true;
        }
        if (strArr.length < 2) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_summon.forgot_username"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(strArr[1]);
        if (habbo == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_summon.not_found").replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (gameClient.getHabbo().getHabboInfo().getUsername().equals(habbo.getHabboInfo().getUsername())) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.generic.cmd_summon.self").replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (gameClient.getHabbo().getHabboInfo().getCurrentRoom() == habbo.getHabboInfo().getCurrentRoom()) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.generic.cmd_summon.same_room").replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
            return true;
        }
        Room currentRoom = habbo.getHabboInfo().getCurrentRoom();
        if (currentRoom != null) {
            Emulator.getGameEnvironment().getRoomManager().logExit(habbo);
            currentRoom.removeHabbo(habbo, true);
            habbo.getHabboInfo().setCurrentRoom(null);
        }
        Emulator.getGameEnvironment().getRoomManager().enterRoom(habbo, gameClient.getHabbo().getHabboInfo().getCurrentRoom().getId(), Emulator.PREVIEW, true);
        habbo.getClient().sendResponse(new ForwardToRoomComposer(gameClient.getHabbo().getHabboInfo().getCurrentRoom().getId()));
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_summon.summoned").replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
        habbo.alert(Emulator.getTexts().getValue("commands.generic.cmd_summon.been_summoned").replace("%user%", gameClient.getHabbo().getHabboInfo().getUsername()));
        return true;
    }
}
