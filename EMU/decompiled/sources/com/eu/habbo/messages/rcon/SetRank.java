package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.google.gson.Gson;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/SetRank.class */
public class SetRank extends RCONMessage<JSONSetRank> {

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/SetRank$JSONSetRank.class */
    static class JSONSetRank {
        public int user_id;
        public int rank;

        JSONSetRank() {
        }
    }

    public SetRank() {
        super(JSONSetRank.class);
    }

    @Override // com.eu.habbo.messages.rcon.RCONMessage
    public void handle(Gson gson, JSONSetRank jSONSetRank) {
        try {
            Emulator.getGameEnvironment().getHabboManager().setRank(jSONSetRank.user_id, jSONSetRank.rank);
            this.message = "updated offline user";
            if (Emulator.getGameEnvironment().getHabboManager().getHabbo(jSONSetRank.user_id) != null) {
                this.message = "updated online user";
            }
        } catch (Exception e) {
            this.status = 4;
            this.message = "invalid rank";
        }
    }
}
