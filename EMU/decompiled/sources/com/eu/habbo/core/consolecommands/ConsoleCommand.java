package com.eu.habbo.core.consolecommands;

import gnu.trove.map.hash.THashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/core/consolecommands/ConsoleCommand.class */
public abstract class ConsoleCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleCommand.class);
    private static final THashMap<String, ConsoleCommand> commands = new THashMap<>();
    public final String key;
    public final String usage;

    public ConsoleCommand(String str, String str2) {
        this.key = str;
        this.usage = str2;
    }

    public static void load() {
        addCommand(new ConsoleShutdownCommand());
        addCommand(new ConsoleInfoCommand());
        addCommand(new ConsoleTestCommand());
        addCommand(new ConsoleReconnectCameraCommand());
        addCommand(new ShowInteractionsCommand());
        addCommand(new ShowRCONCommands());
        addCommand(new ThankyouArcturusCommand());
    }

    public static void addCommand(ConsoleCommand consoleCommand) {
        commands.put(consoleCommand.key, consoleCommand);
    }

    public static ConsoleCommand findCommand(String str) {
        return (ConsoleCommand) commands.get(str);
    }

    public static boolean handle(String str) {
        String[] strArrSplit = str.split(" ");
        if (strArrSplit.length <= 0) {
            return false;
        }
        ConsoleCommand consoleCommandFindCommand = findCommand(strArrSplit[0]);
        if (consoleCommandFindCommand != null) {
            try {
                consoleCommandFindCommand.handle(strArrSplit);
                return true;
            } catch (Exception e) {
                LOGGER.error("Caught exception", e);
                return false;
            }
        }
        LOGGER.info("Unknown Console Command " + strArrSplit[0]);
        LOGGER.info("Commands Available (" + commands.size() + "): ");
        for (ConsoleCommand consoleCommand : commands.values()) {
            LOGGER.info(consoleCommand.key + " - " + consoleCommand.usage);
        }
        return false;
    }

    public abstract void handle(String[] strArr) throws Exception;
}
