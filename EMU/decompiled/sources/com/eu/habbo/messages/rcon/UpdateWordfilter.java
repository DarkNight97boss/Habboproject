package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.google.gson.Gson;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/UpdateWordfilter.class */
public class UpdateWordfilter extends RCONMessage<WordFilterJSON> {

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/UpdateWordfilter$WordFilterJSON.class */
    static class WordFilterJSON {
        WordFilterJSON() {
        }
    }

    public UpdateWordfilter() {
        super(WordFilterJSON.class);
    }

    @Override // com.eu.habbo.messages.rcon.RCONMessage
    public void handle(Gson gson, WordFilterJSON wordFilterJSON) {
        Emulator.getGameEnvironment().getWordFilter().reload();
    }
}
