package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.rooms.ForwardToRoomComposer;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/SummonRankCommand.class */
public class SummonRankCommand extends Command {
    public SummonRankCommand() {
        super("cmd_summonrank", Emulator.getTexts().getValue("commands.keys.cmd_summonrank").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length < 2) {
            return true;
        }
        try {
            int i = Integer.parseInt(strArr[1]);
            for (Map.Entry<Integer, Habbo> entry : Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().entrySet()) {
                if (entry.getValue().getHabboInfo().getRank().getId() >= i && entry.getValue() != gameClient.getHabbo() && entry.getValue().getHabboInfo().getCurrentRoom() != gameClient.getHabbo().getHabboInfo().getCurrentRoom()) {
                    Room currentRoom = entry.getValue().getHabboInfo().getCurrentRoom();
                    if (currentRoom != null) {
                        Emulator.getGameEnvironment().getRoomManager().logExit(entry.getValue());
                        currentRoom.removeHabbo(entry.getValue(), true);
                        entry.getValue().getHabboInfo().setCurrentRoom(null);
                    }
                    Emulator.getGameEnvironment().getRoomManager().enterRoom(entry.getValue(), gameClient.getHabbo().getHabboInfo().getCurrentRoom().getId(), Emulator.PREVIEW, true);
                    entry.getValue().getClient().sendResponse(new ForwardToRoomComposer(gameClient.getHabbo().getHabboInfo().getCurrentRoom().getId()));
                }
            }
            return true;
        } catch (Exception e) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.generic.cmd_summonrank.error"), RoomChatMessageBubbles.ALERT);
            return true;
        }
    }
}
