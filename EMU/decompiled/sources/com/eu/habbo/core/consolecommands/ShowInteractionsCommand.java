package com.eu.habbo.core.consolecommands;

import com.eu.habbo.Emulator;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/core/consolecommands/ShowInteractionsCommand.class */
public class ShowInteractionsCommand extends ConsoleCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShowInteractionsCommand.class);

    public ShowInteractionsCommand() {
        super("interactions", "Show a list of available furniture interactions.");
    }

    @Override // com.eu.habbo.core.consolecommands.ConsoleCommand
    public void handle(String[] strArr) throws Exception {
        Iterator<String> it = Emulator.getGameEnvironment().getItemManager().getInteractionList().iterator();
        while (it.hasNext()) {
            LOGGER.info(it.next());
        }
    }
}
