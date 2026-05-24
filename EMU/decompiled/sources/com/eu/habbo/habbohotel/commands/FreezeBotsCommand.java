package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/FreezeBotsCommand.class */
public class FreezeBotsCommand extends Command {
    public FreezeBotsCommand() {
        super("cmd_freeze_bots", Emulator.getTexts().getValue("commands.keys.cmd_freeze_bots").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (gameClient.getHabbo().getHabboInfo().getCurrentRoom() == null) {
            return false;
        }
        if (gameClient.getHabbo().getHabboInfo().getId() != gameClient.getHabbo().getHabboInfo().getCurrentRoom().getOwnerId() && !gameClient.getHabbo().hasPermission(Permission.ACC_ANYROOMOWNER)) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("generic.cannot_do_that"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        gameClient.getHabbo().getHabboInfo().getCurrentRoom().setAllowBotsWalk(!gameClient.getHabbo().getHabboInfo().getCurrentRoom().isAllowBotsWalk());
        gameClient.getHabbo().whisper(gameClient.getHabbo().getHabboInfo().getCurrentRoom().isAllowBotsWalk() ? Emulator.getTexts().getValue("commands.succes.cmd_freeze_bots.unfrozen") : Emulator.getTexts().getValue("commands.succes.cmd_freeze_bots.frozen"), RoomChatMessageBubbles.ALERT);
        return true;
    }
}
