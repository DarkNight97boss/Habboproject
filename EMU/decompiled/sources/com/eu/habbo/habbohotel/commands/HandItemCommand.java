package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserHandItemComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/HandItemCommand.class */
public class HandItemCommand extends Command {
    public HandItemCommand() {
        super("cmd_hand_item", Emulator.getTexts().getValue("commands.keys.cmd_hand_item").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length != 2) {
            return true;
        }
        try {
            if (gameClient.getHabbo().getHabboInfo().getCurrentRoom() != null) {
                gameClient.getHabbo().getRoomUnit().setHandItem(Integer.parseInt(strArr[1]));
                gameClient.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RoomUserHandItemComposer(gameClient.getHabbo().getRoomUnit()).compose());
            }
            return true;
        } catch (Exception e) {
            return true;
        }
    }
}
