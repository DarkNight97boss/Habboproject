package com.eu.habbo.messages.outgoing.camera;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/camera/CameraURLComposer.class */
public class CameraURLComposer extends MessageComposer {
    private final String URL;

    public CameraURLComposer(String str) {
        this.URL = str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.CameraURLComposer);
        this.response.appendString(this.URL);
        return this.response;
    }
}
