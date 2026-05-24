package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.google.gson.Gson;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/StaffAlert.class */
public class StaffAlert extends RCONMessage<JSON> {

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/StaffAlert$JSON.class */
    static class JSON {
        public String message;

        JSON() {
        }
    }

    public StaffAlert() {
        super(JSON.class);
    }

    @Override // com.eu.habbo.messages.rcon.RCONMessage
    public void handle(Gson gson, JSON json) {
        Emulator.getGameEnvironment().getHabboManager().staffAlert(json.message);
    }
}
