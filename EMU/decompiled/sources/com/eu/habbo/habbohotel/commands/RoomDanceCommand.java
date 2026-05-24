package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.DanceType;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserDanceComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/RoomDanceCommand.class */
public class RoomDanceCommand extends Command {
    public RoomDanceCommand() {
        super("cmd_danceall", Emulator.getTexts().getValue("commands.keys.cmd_danceall").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length != 2) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_danceall.no_dance"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        try {
            int iIntValue = Integer.valueOf(strArr[1]).intValue();
            if (iIntValue < 0 || iIntValue > 4) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_danceall.outside_bounds"), RoomChatMessageBubbles.ALERT);
                return true;
            }
            for (Habbo habbo : gameClient.getHabbo().getHabboInfo().getCurrentRoom().getHabbos()) {
                habbo.getRoomUnit().setDanceType(DanceType.values()[iIntValue]);
                habbo.getHabboInfo().getCurrentRoom().sendComposer(new RoomUserDanceComposer(habbo.getRoomUnit()).compose());
            }
            return true;
        } catch (Exception e) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_danceall.invalid_dance"), RoomChatMessageBubbles.ALERT);
            return true;
        }
    }
}
