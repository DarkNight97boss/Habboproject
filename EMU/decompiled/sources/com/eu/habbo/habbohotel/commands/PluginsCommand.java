package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.messages.outgoing.generic.alerts.MessagesForYouComposer;
import com.eu.habbo.plugin.HabboPlugin;
import gnu.trove.iterator.hash.TObjectHashIterator;
import java.util.Collections;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/PluginsCommand.class */
public class PluginsCommand extends Command {
    public PluginsCommand() {
        super(null, Emulator.getTexts().getValue("commands.keys.cmd_plugins").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        StringBuilder sb = new StringBuilder("Plugins (" + Emulator.getPluginManager().getPlugins().size() + ")\r");
        TObjectHashIterator it = Emulator.getPluginManager().getPlugins().iterator();
        while (it.hasNext()) {
            HabboPlugin habboPlugin = (HabboPlugin) it.next();
            sb.append("\r").append(habboPlugin.configuration.name).append(" By ").append(habboPlugin.configuration.author);
        }
        if (Emulator.getConfig().getBoolean("commands.plugins.oldstyle")) {
            gameClient.sendResponse(new MessagesForYouComposer((List<String>) Collections.singletonList(sb.toString())));
            return true;
        }
        gameClient.getHabbo().alert(sb.toString());
        return true;
    }
}
