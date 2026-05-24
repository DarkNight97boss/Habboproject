package com.eu.habbo.messages.outgoing.guides;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guides.GuideTour;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guides/GuideSessionAttachedComposer.class */
public class GuideSessionAttachedComposer extends MessageComposer {
    private final GuideTour tour;
    private final boolean isHelper;

    public GuideSessionAttachedComposer(GuideTour guideTour, boolean z) {
        this.tour = guideTour;
        this.isHelper = z;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GuideSessionAttachedComposer);
        this.response.appendBoolean(Boolean.valueOf(this.isHelper));
        this.response.appendInt((Integer) 1);
        this.response.appendString(this.tour.getHelpRequest());
        this.response.appendInt(Integer.valueOf(this.isHelper ? 60 : Emulator.getGameEnvironment().getGuideManager().getAverageWaitingTime()));
        return this.response;
    }
}
