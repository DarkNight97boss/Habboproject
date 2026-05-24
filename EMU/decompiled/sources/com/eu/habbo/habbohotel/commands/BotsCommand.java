package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.gameclients.GameClient;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/BotsCommand.class */
public class BotsCommand extends Command {
    public BotsCommand() {
        super("cmd_bots", Emulator.getTexts().getValue("commands.keys.cmd_bots").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (gameClient.getHabbo().getHabboInfo().getCurrentRoom() == null || !gameClient.getHabbo().getHabboInfo().getCurrentRoom().hasRights(gameClient.getHabbo())) {
            return false;
        }
        StringBuilder sb = new StringBuilder(Emulator.getTexts().getValue("total") + ": " + gameClient.getHabbo().getHabboInfo().getCurrentRoom().getCurrentBots().values().length);
        for (Object obj : gameClient.getHabbo().getHabboInfo().getCurrentRoom().getCurrentBots().values()) {
            if (obj instanceof Bot) {
                sb.append("\r");
                sb.append("<b>").append(Emulator.getTexts().getValue("generic.bot.name")).append("</b>: ").append(((Bot) obj).getName()).append(" <b>").append(Emulator.getTexts().getValue("generic.bot.id")).append("</b>: ").append(((Bot) obj).getId());
            }
        }
        gameClient.getHabbo().alert(sb.toString());
        return true;
    }
}
