package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/UpdateAchievements.class */
public class UpdateAchievements extends Command {
    public UpdateAchievements() {
        super("cmd_update_achievements", Emulator.getTexts().getValue("commands.keys.cmd_update_achievements").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        Emulator.getGameEnvironment().getAchievementManager().reload();
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_update_achievements.updated"), RoomChatMessageBubbles.ALERT);
        return true;
    }
}
