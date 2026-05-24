package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/SnowWarsUserEnteredArenaComposer.class */
public class SnowWarsUserEnteredArenaComposer extends MessageComposer {
    private final int type;

    public SnowWarsUserEnteredArenaComposer(int i) {
        this.type = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(3425);
        if (this.type == 1) {
            this.response.appendInt((Integer) 1);
            this.response.appendString("Admin");
            this.response.appendString("ca-1807-64.lg-275-78.hd-3093-1.hr-802-42.ch-3110-65-62.fa-1211-62");
            this.response.appendString("m");
            this.response.appendInt((Integer) 1);
        } else {
            this.response.appendInt((Integer) 0);
            this.response.appendString("Droppy");
            this.response.appendString("ca-1807-64.lg-275-78.hd-3093-1.hr-802-42.ch-3110-65-62.fa-1211-62");
            this.response.appendString("m");
            this.response.appendInt((Integer) 2);
        }
        return this.response;
    }
}
