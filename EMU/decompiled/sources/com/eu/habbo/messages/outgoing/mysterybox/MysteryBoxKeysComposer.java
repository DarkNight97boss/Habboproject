package com.eu.habbo.messages.outgoing.mysterybox;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/mysterybox/MysteryBoxKeysComposer.class */
public class MysteryBoxKeysComposer extends MessageComposer {
    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.MysteryBoxKeysComposer);
        this.response.appendString(Emulator.PREVIEW);
        this.response.appendString(Emulator.PREVIEW);
        return this.response;
    }
}
