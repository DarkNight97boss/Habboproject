package com.eu.habbo.core.consolecommands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.CatalogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class ConsoleInfoCommand extends ConsoleCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleInfoCommand.class);

    public ConsoleInfoCommand() {
        super("info", "Mostra le statistiche correnti.");
    }

    @Override
    public void handle(String[] args) throws Exception {
        int seconds = Emulator.getIntUnixTimestamp() - Emulator.getTimeStarted();
        int day = (int) TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) - (day * 24L);
        long minute = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds) * 60);
        long second = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) * 60);

        LOGGER.info("Versione emulatore: " + Emulator.version);
        LOGGER.info("Build emulatore: {}", Emulator.build);

        LOGGER.info("");

        LOGGER.info("Statistiche Hotel");
        LOGGER.info("- Utenti: {}", Emulator.getGameEnvironment().getHabboManager().getOnlineCount());
        LOGGER.info("- Stanze: {}", Emulator.getGameEnvironment().getRoomManager().getActiveRooms().size());
        LOGGER.info("- Catalogo:  {} pagine e {} oggetti.", Emulator.getGameEnvironment().getCatalogManager().catalogPages.size(), CatalogManager.catalogItemAmount);
        LOGGER.info("- Arredi: {} oggetti.", Emulator.getGameEnvironment().getItemManager().getItems().size());
        LOGGER.info("");
        LOGGER.info("Statistiche Server");
        LOGGER.info("- Tempo di attività: {}{}{}{}{}{}{}{}", day, day > 1 ? " giorni, " : " giorno, ", hours, hours > 1 ? " ore, " : " ora, ", minute, minute > 1 ? " minuti, " : " minuto, ", second, second > 1 ? " secondi!" : " secondo!");
        LOGGER.info("- Utilizzo RAM: {}/{}MB", (Emulator.getRuntime().totalMemory() - Emulator.getRuntime().freeMemory()) / (1024 * 1024), (Emulator.getRuntime().freeMemory()) / (1024 * 1024));
        LOGGER.info("- Core CPU: {}", Emulator.getRuntime().availableProcessors());
        LOGGER.info("- Memoria totale: {}MB", Emulator.getRuntime().maxMemory() / (1024 * 1024));
    }
}