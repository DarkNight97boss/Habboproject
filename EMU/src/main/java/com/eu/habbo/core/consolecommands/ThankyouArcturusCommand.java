package com.eu.habbo.core.consolecommands;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThankyouArcturusCommand extends ConsoleCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThankyouArcturusCommand.class);

    public ThankyouArcturusCommand() {
        super("thankyou", "Mostra i ringraziamenti del progetto upstream.");
    }

    @Override
    public void handle(String[] args) throws Exception {
        LOGGER.info("Asteria Core e' un fork ribrandizzato di Arcturus Morningstar (GPL-3.0).");
        LOGGER.info("Progetto originale di TheGeneral. Ringraziamenti ai contributori upstream:");
        LOGGER.info("TheGeneral - Creazione di Arcturus");
        LOGGER.info("Capheus - Decompilazione");
        LOGGER.info("Beny - Lead Developer");
        LOGGER.info("Alejandro - Lead Developer");
        LOGGER.info("Harmonic, ArpyAge, Mike, Skeletor, zGrav, Swirny, Quadral, Dome, Necmi - Sviluppo");
        LOGGER.info("Oliver, Rasmus, Layne, Bill, Harmony - Supporto");
        LOGGER.info("Ridge, Tenshie, Wulles, Gizmo - Catalogo");
        LOGGER.info("TheJava - Motivazione");
        LOGGER.info("L'intera community Krews.org.");
    }
}