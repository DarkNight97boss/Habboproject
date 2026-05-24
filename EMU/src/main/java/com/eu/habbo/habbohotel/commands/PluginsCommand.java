package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.messages.outgoing.generic.alerts.MessagesForYouComposer;
import com.eu.habbo.plugin.HabboPlugin;
import gnu.trove.iterator.hash.TObjectHashIterator;
import java.util.Collections;

public class PluginsCommand extends Command {
   public PluginsCommand() {
      super(null, Emulator.getTexts().getValue("commands.keys.cmd_plugins").split(";"));
   }

   @Override
   public boolean handle(GameClient gameClient, String[] params) throws Exception {
      StringBuilder message = new StringBuilder("Plugins (" + Emulator.getPluginManager().getPlugins().size() + ")\r");
      TObjectHashIterator var4 = Emulator.getPluginManager().getPlugins().iterator();

      while (var4.hasNext()) {
         HabboPlugin plugin = (HabboPlugin)var4.next();
         message.append("\r").append(plugin.configuration.name).append(" By ").append(plugin.configuration.author);
      }

      if (Emulator.getConfig().getBoolean("commands.plugins.oldstyle")) {
         gameClient.sendResponse(new MessagesForYouComposer(Collections.singletonList(message.toString())));
      } else {
         gameClient.getHabbo().alert(message.toString());
      }

      return true;
   }
}
