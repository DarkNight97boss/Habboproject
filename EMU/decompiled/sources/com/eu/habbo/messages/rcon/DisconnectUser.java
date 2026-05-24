package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.google.gson.Gson;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/DisconnectUser.class */
public class DisconnectUser extends RCONMessage<DisconnectUserJSON> {

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/DisconnectUser$DisconnectUserJSON.class */
    static class DisconnectUserJSON {
        public int user_id = -1;
        public String username;

        DisconnectUserJSON() {
        }
    }

    public DisconnectUser() {
        super(DisconnectUserJSON.class);
    }

    @Override // com.eu.habbo.messages.rcon.RCONMessage
    public void handle(Gson gson, DisconnectUserJSON disconnectUserJSON) {
        Habbo habbo;
        if (disconnectUserJSON.user_id >= 0) {
            habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(disconnectUserJSON.user_id);
        } else {
            if (disconnectUserJSON.username.isEmpty()) {
                this.status = 2;
                return;
            }
            habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(disconnectUserJSON.username);
        }
        if (habbo == null) {
            this.status = 1;
            this.message = Emulator.getTexts().getValue("commands.error.cmd_disconnect.user_offline");
        } else {
            Emulator.getGameServer().getGameClientManager().disposeClient(habbo.getClient());
            this.message = Emulator.getTexts().getValue("commands.succes.cmd_disconnect.disconnected").replace("%user%", habbo.getHabboInfo().getUsername());
        }
    }
}
