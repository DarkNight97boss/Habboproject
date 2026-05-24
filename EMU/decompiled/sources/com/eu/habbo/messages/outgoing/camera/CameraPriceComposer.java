package com.eu.habbo.messages.outgoing.camera;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/camera/CameraPriceComposer.class */
public class CameraPriceComposer extends MessageComposer {
    public final int credits;
    public final int points;
    public final int pointsType;

    public CameraPriceComposer(int i, int i2, int i3) {
        this.credits = i;
        this.points = i2;
        this.pointsType = i3;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(3878);
        this.response.appendInt(Integer.valueOf(this.credits));
        this.response.appendInt(Integer.valueOf(this.points));
        this.response.appendInt(Integer.valueOf(this.pointsType));
        return this.response;
    }
}
