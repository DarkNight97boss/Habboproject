package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.ServerMessage;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/ShutdownEmulator.class */
public class ShutdownEmulator implements Runnable {
    public static boolean instantiated = false;
    public static int timestamp = 0;

    public ShutdownEmulator(ServerMessage serverMessage) {
        if (instantiated) {
            return;
        }
        instantiated = true;
        if (serverMessage != null) {
            Emulator.getGameServer().getGameClientManager().sendBroadcastResponse(serverMessage);
        }
    }

    @Override // java.lang.Runnable
    public void run() {
        Emulator.getRuntime().exit(0);
    }
}
