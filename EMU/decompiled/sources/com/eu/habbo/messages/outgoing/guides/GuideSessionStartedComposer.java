package com.eu.habbo.messages.outgoing.guides;

import com.eu.habbo.habbohotel.guides.GuideTour;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guides/GuideSessionStartedComposer.class */
public class GuideSessionStartedComposer extends MessageComposer {
    private final GuideTour tour;

    public GuideSessionStartedComposer(GuideTour guideTour) {
        this.tour = guideTour;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GuideSessionStartedComposer);
        this.response.appendInt(Integer.valueOf(this.tour.getNoob().getHabboInfo().getId()));
        this.response.appendString(this.tour.getNoob().getHabboInfo().getUsername());
        this.response.appendString(this.tour.getNoob().getHabboInfo().getLook());
        this.response.appendInt(Integer.valueOf(this.tour.getHelper().getHabboInfo().getId()));
        this.response.appendString(this.tour.getHelper().getHabboInfo().getUsername());
        this.response.appendString(this.tour.getHelper().getHabboInfo().getLook());
        return this.response;
    }
}
