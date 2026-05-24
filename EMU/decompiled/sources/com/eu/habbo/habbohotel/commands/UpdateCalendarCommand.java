package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/UpdateCalendarCommand.class */
public class UpdateCalendarCommand extends Command {
    public UpdateCalendarCommand() {
        super("cmd_update_calendar", Emulator.getTexts().getValue("commands.keys.cmd_update_calendar").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) {
        Emulator.getGameEnvironment().getCalendarManager().reload();
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.success.cmd_update_calendar"), RoomChatMessageBubbles.ALERT);
        return true;
    }
}
