package com.eu.habbo.habbohotel.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/modtool/ScripterManager.class */
public class ScripterManager {
    public static void scripterDetected(GameClient gameClient, String str) {
        ScripterEvent scripterEvent = new ScripterEvent(gameClient.getHabbo(), str);
        Emulator.getPluginManager().fireEvent(scripterEvent);
        if (!scripterEvent.isCancelled() && Emulator.getConfig().getBoolean("scripter.modtool.tickets", true)) {
            Emulator.getGameEnvironment().getModToolManager().quickTicket(gameClient.getHabbo(), "Scripter", str);
        }
    }
}
