package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;

public class UpdatePluginsCommand extends Command {
    public UpdatePluginsCommand() {
        super("cmd_update_plugins", Emulator.getTexts().getValue("commands.keys.cmd_update_plugins").split(";"));
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) throws Exception {
        Emulator.getPluginManager().reload();

        gameClient.getHabbo().whisper("Questo è un comando non sicuro e potrebbe causare memory leak.\rSi consiglia di riavviare l'emulatore per ricaricare i plugin.");
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_update_plugins").replace("%count%", Emulator.getPluginManager().getPlugins().size() + ""), RoomChatMessageBubbles.ALERT);
        return true;
    }
}
