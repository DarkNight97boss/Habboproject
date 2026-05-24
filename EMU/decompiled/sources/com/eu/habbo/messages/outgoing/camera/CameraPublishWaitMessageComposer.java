package com.eu.habbo.messages.outgoing.camera;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/camera/CameraPublishWaitMessageComposer.class */
public class CameraPublishWaitMessageComposer extends MessageComposer {
    public final boolean isOk;
    public final int cooldownSeconds;
    public final String extraDataId;

    public CameraPublishWaitMessageComposer(boolean z, int i, String str) {
        this.isOk = z;
        this.cooldownSeconds = i;
        this.extraDataId = str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.CameraPublishWaitMessageComposer);
        this.response.appendBoolean(Boolean.valueOf(this.isOk));
        this.response.appendInt(Integer.valueOf(this.cooldownSeconds));
        if (!this.extraDataId.isEmpty()) {
            this.response.appendString(this.extraDataId);
        }
        return this.response;
    }
}
