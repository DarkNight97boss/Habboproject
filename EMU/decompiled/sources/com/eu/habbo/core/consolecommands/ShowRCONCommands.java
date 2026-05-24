package com.eu.habbo.core.consolecommands;

import com.eu.habbo.Emulator;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/core/consolecommands/ShowRCONCommands.class */
public class ShowRCONCommands extends ConsoleCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShowRCONCommands.class);

    public ShowRCONCommands() {
        super("rconcommands", "Show a list of all RCON commands");
    }

    @Override // com.eu.habbo.core.consolecommands.ConsoleCommand
    public void handle(String[] strArr) throws Exception {
        Iterator<String> it = Emulator.getRconServer().getCommands().iterator();
        while (it.hasNext()) {
            LOGGER.info(it.next());
        }
    }
}
