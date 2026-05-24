package com.eu.habbo.messages.outgoing.events.resolution;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/events/resolution/NewYearResolutionProgressComposer.class */
public class NewYearResolutionProgressComposer extends MessageComposer {
    private final int stuffId;
    private final int achievementId;
    private final String achievementName;
    private final int currentProgress;
    private final int progressNeeded;
    private final int timeLeft;

    public NewYearResolutionProgressComposer(int i, int i2, String str, int i3, int i4, int i5) {
        this.stuffId = i;
        this.achievementId = i2;
        this.achievementName = str;
        this.currentProgress = i3;
        this.progressNeeded = i4;
        this.timeLeft = i5;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.NewYearResolutionProgressComposer);
        this.response.appendInt(Integer.valueOf(this.stuffId));
        this.response.appendInt(Integer.valueOf(this.achievementId));
        this.response.appendString(this.achievementName);
        this.response.appendInt(Integer.valueOf(this.currentProgress));
        this.response.appendInt(Integer.valueOf(this.progressNeeded));
        this.response.appendInt(Integer.valueOf(this.timeLeft));
        return this.response;
    }
}
