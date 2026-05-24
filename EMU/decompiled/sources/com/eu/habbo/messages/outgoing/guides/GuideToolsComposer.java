package com.eu.habbo.messages.outgoing.guides;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guides/GuideToolsComposer.class */
public class GuideToolsComposer extends MessageComposer {
    private final boolean onDuty;

    public GuideToolsComposer(boolean z) {
        this.onDuty = z;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GuideToolsComposer);
        this.response.appendBoolean(Boolean.valueOf(this.onDuty));
        this.response.appendInt((Integer) 0);
        this.response.appendInt(Integer.valueOf(Emulator.getGameEnvironment().getGuideManager().getGuidesCount()));
        this.response.appendInt(Integer.valueOf(Emulator.getGameEnvironment().getGuideManager().getGuardiansCount()));
        return this.response;
    }
}
