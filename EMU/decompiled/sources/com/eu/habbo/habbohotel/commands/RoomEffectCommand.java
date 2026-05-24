package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import java.util.Iterator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/RoomEffectCommand.class */
public class RoomEffectCommand extends Command {
    public RoomEffectCommand() {
        super("cmd_roomeffect", Emulator.getTexts().getValue("commands.keys.cmd_roomeffect").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length < 2) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_roomeffect.no_effect"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        try {
            int iIntValue = Integer.valueOf(strArr[1]).intValue();
            if (iIntValue < 0) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_roomeffect.positive"), RoomChatMessageBubbles.ALERT);
                return true;
            }
            Room currentRoom = gameClient.getHabbo().getHabboInfo().getCurrentRoom();
            Iterator<Habbo> it = currentRoom.getHabbos().iterator();
            while (it.hasNext()) {
                currentRoom.giveEffect(it.next(), iIntValue, -1);
            }
            return true;
        } catch (Exception e) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_roomeffect.numbers_only"), RoomChatMessageBubbles.ALERT);
            return true;
        }
    }
}
