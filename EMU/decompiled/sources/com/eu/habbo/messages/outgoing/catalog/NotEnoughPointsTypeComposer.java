package com.eu.habbo.messages.outgoing.catalog;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/catalog/NotEnoughPointsTypeComposer.class */
public class NotEnoughPointsTypeComposer extends MessageComposer {
    private final boolean isCredits;
    private final boolean isPixels;
    private final int pointsType;

    public NotEnoughPointsTypeComposer(boolean z, boolean z2, int i) {
        this.isCredits = z;
        this.isPixels = z2;
        this.pointsType = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.NotEnoughPointsTypeComposer);
        this.response.appendBoolean(Boolean.valueOf(this.isCredits));
        this.response.appendBoolean(Boolean.valueOf(this.isPixels));
        this.response.appendInt(Integer.valueOf(this.pointsType));
        return this.response;
    }
}
