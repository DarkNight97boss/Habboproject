package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.google.gson.Gson;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/AlertUser.class */
public class AlertUser extends RCONMessage<JSONAlertUser> {

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/AlertUser$JSONAlertUser.class */
    static class JSONAlertUser {
        int user_id;
        String message;

        JSONAlertUser() {
        }
    }

    public AlertUser() {
        super(JSONAlertUser.class);
    }

    @Override // com.eu.habbo.messages.rcon.RCONMessage
    public void handle(Gson gson, JSONAlertUser jSONAlertUser) {
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(jSONAlertUser.user_id);
        if (habbo != null) {
            habbo.alert(jSONAlertUser.message);
        }
        this.status = 2;
    }
}
