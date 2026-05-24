package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/UpdatePluginsCommand.class */
public class UpdatePluginsCommand extends Command {
    public UpdatePluginsCommand() {
        super("cmd_update_plugins", Emulator.getTexts().getValue("commands.keys.cmd_update_plugins").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        Emulator.getPluginManager().reload();
        gameClient.getHabbo().whisper("This is an unsafe command and could possibly lead to memory leaks.\rIt is recommended to restart the emulator in order to reload plugins.");
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_update_plugins").replace("%count%", Emulator.getPluginManager().getPlugins().size() + Emulator.PREVIEW), RoomChatMessageBubbles.ALERT);
        return true;
    }
}
