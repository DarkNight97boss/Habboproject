package com.eu.habbo.core.consolecommands;

import com.eu.habbo.habbohotel.commands.ShutdownCommand;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/core/consolecommands/ConsoleShutdownCommand.class */
public class ConsoleShutdownCommand extends ConsoleCommand {
    public ConsoleShutdownCommand() {
        super("stop", "Stop the emulator.");
    }

    @Override // com.eu.habbo.core.consolecommands.ConsoleCommand
    public void handle(String[] strArr) throws Exception {
        new ShutdownCommand().handle(null, strArr);
    }
}
