package com.eu.habbo.core.consolecommands;

import com.eu.habbo.networking.camera.CameraClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/core/consolecommands/ConsoleReconnectCameraCommand.class */
public class ConsoleReconnectCameraCommand extends ConsoleCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleReconnectCameraCommand.class);

    public ConsoleReconnectCameraCommand() {
        super("camera", "Attempt to reconnect to the camera server.");
    }

    @Override // com.eu.habbo.core.consolecommands.ConsoleCommand
    public void handle(String[] strArr) throws Exception {
        LOGGER.info("Connecting to the camera...");
        CameraClient.attemptReconnect = true;
    }
}
