package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserHandItemComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/RoomItemCommand.class */
public class RoomItemCommand extends Command {
    public RoomItemCommand() {
        super("cmd_roomitem", Emulator.getTexts().getValue("commands.keys.cmd_roomitem").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        int iIntValue = 0;
        if (strArr.length >= 2) {
            try {
                iIntValue = Integer.valueOf(strArr[1]).intValue();
                if (iIntValue < 0) {
                    gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_roomitem.positive"), RoomChatMessageBubbles.ALERT);
                    return true;
                }
            } catch (Exception e) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_roomitem.no_item"), RoomChatMessageBubbles.ALERT);
                return true;
            }
        }
        for (Habbo habbo : gameClient.getHabbo().getHabboInfo().getCurrentRoom().getHabbos()) {
            habbo.getRoomUnit().setHandItem(iIntValue);
            habbo.getHabboInfo().getCurrentRoom().sendComposer(new RoomUserHandItemComposer(habbo.getRoomUnit()).compose());
        }
        if (iIntValue > 0) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_roomitem.given").replace("%item%", iIntValue + Emulator.PREVIEW), RoomChatMessageBubbles.ALERT);
            return true;
        }
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_roomitem.removed"), RoomChatMessageBubbles.ALERT);
        return true;
    }
}
