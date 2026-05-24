package com.eu.habbo.messages.outgoing.camera;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/camera/CameraCompetitionStatusComposer.class */
public class CameraCompetitionStatusComposer extends MessageComposer {
    private final boolean unknownBoolean;
    private final String unknownString;

    public CameraCompetitionStatusComposer(boolean z, String str) {
        this.unknownBoolean = z;
        this.unknownString = str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.CameraCompetitionStatusComposer);
        this.response.appendBoolean(Boolean.valueOf(this.unknownBoolean));
        this.response.appendString(this.unknownString);
        return this.response;
    }
}
