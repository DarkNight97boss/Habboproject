package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ModToolIssue;
import com.eu.habbo.habbohotel.modtool.ModToolTicketType;
import com.google.gson.Gson;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/CreateModToolTicket.class */
public class CreateModToolTicket extends RCONMessage<JSON> {

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/CreateModToolTicket$JSON.class */
    static class JSON {
        public int sender_id;
        public String sender_username;
        public int reported_id;
        public String reported_username;
        public int reported_room_id = 0;
        public String message;

        JSON() {
        }
    }

    public CreateModToolTicket() {
        super(JSON.class);
    }

    @Override // com.eu.habbo.messages.rcon.RCONMessage
    public void handle(Gson gson, JSON json) {
        ModToolIssue modToolIssue = new ModToolIssue(json.sender_id, json.sender_username, json.reported_id, json.reported_username, json.reported_room_id, json.message, ModToolTicketType.NORMAL);
        Emulator.getGameEnvironment().getModToolManager().addTicket(modToolIssue);
        Emulator.getGameEnvironment().getModToolManager().updateTicketToMods(modToolIssue);
    }
}
