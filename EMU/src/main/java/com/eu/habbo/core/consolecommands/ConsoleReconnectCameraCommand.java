package com.eu.habbo.core.consolecommands;

import com.eu.habbo.networking.camera.CameraClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleReconnectCameraCommand extends ConsoleCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleReconnectCameraCommand.class);

    public ConsoleReconnectCameraCommand() {
        super("camera", "Tenta di riconnettersi al server camera.");
    }

    @Override
    public void handle(String[] args) throws Exception {
        LOGGER.info("Connessione alla camera in corso...");
        CameraClient.attemptReconnect = true;
    }
}