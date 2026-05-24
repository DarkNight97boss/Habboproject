package com.eu.habbo.messages.outgoing.users;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/users/MutedWhisperComposer.class */
public class MutedWhisperComposer extends MessageComposer {
    private final int seconds;

    public MutedWhisperComposer(int i) {
        this.seconds = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(826);
        this.response.appendInt(Integer.valueOf(this.seconds));
        return this.response;
    }
}
