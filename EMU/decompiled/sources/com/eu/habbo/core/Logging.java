package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/core/Logging.class */
public class Logging {
    private static final Logger LOGGER = LoggerFactory.getLogger("LegacyLogger");

    @Deprecated
    public void logStart(Object obj) {
        LOGGER.info("[LOADING] {}", obj);
    }

    @Deprecated
    public void logShutdownLine(Object obj) {
        LOGGER.info("[SHUTDOWN] {}", obj);
    }

    @Deprecated
    public void logUserLine(Object obj) {
        LOGGER.info("[USER] {}", obj);
    }

    @Deprecated
    public void logDebugLine(Object obj) {
        LOGGER.debug("[DEBUG] {}", obj);
    }

    @Deprecated
    public void logPacketLine(Object obj) {
        if (Emulator.getConfig().getBoolean("debug.show.packets")) {
            LOGGER.debug("[PACKET] {}", obj);
        }
    }

    @Deprecated
    public void logUndefinedPacketLine(Object obj) {
        if (Emulator.getConfig().getBoolean("debug.show.packets.undefined")) {
            LOGGER.debug("[PACKET] [UNDEFINED] {}", obj);
        }
    }

    @Deprecated
    public void logErrorLine(Object obj) {
        LOGGER.error("[ERROR] {}", obj);
    }

    @Deprecated
    public void logSQLException(SQLException sQLException) {
        LOGGER.error("[ERROR] SQLException", sQLException);
    }

    @Deprecated
    public void logPacketError(Object obj) {
        LOGGER.error("[ERROR] PacketError {}", obj);
    }

    @Deprecated
    public void handleException(Exception exc) {
        LOGGER.error("[ERROR] Exception", exc);
    }
}
