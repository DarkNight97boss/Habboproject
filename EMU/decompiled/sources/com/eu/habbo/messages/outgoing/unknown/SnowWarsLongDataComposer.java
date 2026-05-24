package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/SnowWarsLongDataComposer.class */
public class SnowWarsLongDataComposer extends MessageComposer {
    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(2823);
        this.response.appendInt((Integer) 1);
        this.response.appendString("SnowStorm level 10");
        this.response.appendInt((Integer) 10);
        this.response.appendInt((Integer) 10);
        this.response.appendInt((Integer) 4);
        this.response.appendInt((Integer) 8);
        this.response.appendString("Admin");
        this.response.appendInt((Integer) 14);
        this.response.appendInt((Integer) 2);
        this.response.appendInt((Integer) 1);
        this.response.appendString("Admin");
        this.response.appendString("ca-1807-64.lg-275-78.hd-3093-1.hr-802-42.ch-3110-65-62.fa-1211-62");
        this.response.appendString("m");
        this.response.appendInt((Integer) 1);
        this.response.appendInt((Integer) 0);
        this.response.appendInt((Integer) 0);
        this.response.appendInt((Integer) 10);
        this.response.appendInt((Integer) 2);
        this.response.appendString("Emetophobic");
        this.response.appendString("ca-1807-64.lg-275-78.hd-3093-1.hr-802-42.ch-3110-65-62.fa-1211-63");
        this.response.appendString("m");
        this.response.appendInt((Integer) 2);
        this.response.appendInt((Integer) 0);
        this.response.appendInt((Integer) 0);
        this.response.appendInt((Integer) 10);
        return this.response;
    }
}
