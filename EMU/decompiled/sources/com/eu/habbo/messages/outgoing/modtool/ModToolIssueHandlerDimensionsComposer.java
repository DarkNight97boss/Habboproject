package com.eu.habbo.messages.outgoing.modtool;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/modtool/ModToolIssueHandlerDimensionsComposer.class */
public class ModToolIssueHandlerDimensionsComposer extends MessageComposer {
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public ModToolIssueHandlerDimensionsComposer(int i, int i2, int i3, int i4) {
        this.x = i;
        this.y = i2;
        this.width = i3;
        this.height = i4;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.ModToolIssueHandlerDimensionsComposer);
        this.response.appendInt(Integer.valueOf(this.x));
        this.response.appendInt(Integer.valueOf(this.y));
        this.response.appendInt(Integer.valueOf(this.width));
        this.response.appendInt(Integer.valueOf(this.height));
        return this.response;
    }
}
