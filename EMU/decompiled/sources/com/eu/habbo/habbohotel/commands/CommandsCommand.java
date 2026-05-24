package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/CommandsCommand.class */
public class CommandsCommand extends Command {
    public CommandsCommand() {
        super("cmd_commands", Emulator.getTexts().getValue("commands.keys.cmd_commands").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        StringBuilder sb = new StringBuilder(Emulator.getTexts().getValue("commands.generic.cmd_commands.text"));
        List<Command> commandsForRank = Emulator.getGameEnvironment().getCommandHandler().getCommandsForRank(gameClient.getHabbo().getHabboInfo().getRank().getId());
        sb.append("(").append(commandsForRank.size()).append("):\r\n");
        for (Command command : commandsForRank) {
            sb.append(Emulator.getTexts().getValue("commands.description." + command.permission, "commands.description." + command.permission)).append("\r");
        }
        gameClient.getHabbo().alert(new String[]{sb.toString()});
        return true;
    }
}
