package com.eu.habbo.core.consolecommands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.CatalogManager;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/core/consolecommands/ConsoleInfoCommand.class */
public class ConsoleInfoCommand extends ConsoleCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleInfoCommand.class);

    public ConsoleInfoCommand() {
        super("info", "Show current statistics.");
    }

    @Override // com.eu.habbo.core.consolecommands.ConsoleCommand
    public void handle(String[] strArr) throws Exception {
        int intUnixTimestamp = Emulator.getIntUnixTimestamp() - Emulator.getTimeStarted();
        int days = (int) TimeUnit.SECONDS.toDays(intUnixTimestamp);
        long hours = TimeUnit.SECONDS.toHours(intUnixTimestamp) - ((long) (days * 24));
        long minutes = TimeUnit.SECONDS.toMinutes(intUnixTimestamp) - (TimeUnit.SECONDS.toHours(intUnixTimestamp) * 60);
        long seconds = TimeUnit.SECONDS.toSeconds(intUnixTimestamp) - (TimeUnit.SECONDS.toMinutes(intUnixTimestamp) * 60);
        LOGGER.info("Emulator version: Arcturus Morningstar 3.5.3 ");
        LOGGER.info("Emulator build: " + Emulator.build);
        LOGGER.info(Emulator.PREVIEW);
        LOGGER.info("Hotel Statistics");
        LOGGER.info("- Users: " + Emulator.getGameEnvironment().getHabboManager().getOnlineCount());
        LOGGER.info("- Rooms: " + Emulator.getGameEnvironment().getRoomManager().getActiveRooms().size());
        LOGGER.info("- Shop:  " + Emulator.getGameEnvironment().getCatalogManager().catalogPages.size() + " pages and " + CatalogManager.catalogItemAmount + " items.");
        LOGGER.info("- Furni: " + Emulator.getGameEnvironment().getItemManager().getItems().size() + " items.");
        LOGGER.info(Emulator.PREVIEW);
        LOGGER.info("Server Statistics");
        LOGGER.info("- Uptime: " + days + (days > 1 ? " days, " : " day, ") + hours + (hours > 1 ? " hours, " : " hour, ") + minutes + (minutes > 1 ? " minutes, " : " minute, ") + seconds + (seconds > 1 ? " seconds!" : " second!"));
        LOGGER.info("- RAM Usage: " + ((Emulator.getRuntime().totalMemory() - Emulator.getRuntime().freeMemory()) / 1048576) + "/" + (Emulator.getRuntime().freeMemory() / 1048576) + "MB");
        LOGGER.info("- CPU Cores: " + Emulator.getRuntime().availableProcessors());
        LOGGER.info("- Total Memory: " + (Emulator.getRuntime().maxMemory() / 1048576) + "MB");
    }
}
