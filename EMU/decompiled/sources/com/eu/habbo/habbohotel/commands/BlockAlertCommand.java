package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/BlockAlertCommand.class */
public class BlockAlertCommand extends Command {
    public BlockAlertCommand() {
        super("cmd_blockalert", Emulator.getTexts().getValue("commands.keys.cmd_blockalert").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (gameClient.getHabbo().getHabboInfo().getCurrentRoom() == null) {
            return false;
        }
        gameClient.getHabbo().getHabboStats().blockStaffAlerts = !gameClient.getHabbo().getHabboStats().blockStaffAlerts;
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_blockalert").replace("%state%", gameClient.getHabbo().getHabboStats().blockStaffAlerts ? Emulator.getTexts().getValue("generic.on") : Emulator.getTexts().getValue("generic.off")), RoomChatMessageBubbles.ALERT);
        return true;
    }
}
