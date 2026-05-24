package com.eu.habbo.core.consolecommands;

import com.eu.habbo.Emulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/core/consolecommands/ConsoleTestCommand.class */
public class ConsoleTestCommand extends ConsoleCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleTestCommand.class);

    public ConsoleTestCommand() {
        super("test", "This is just a test.");
    }

    @Override // com.eu.habbo.core.consolecommands.ConsoleCommand
    public void handle(String[] strArr) throws Exception {
        if (Emulator.debugging) {
            LOGGER.info("This is a test command for live debugging.");
            Emulator.getGameEnvironment().getHabboManager().getHabbo(1).getHabboInfo().getMachineID();
        }
    }
}
