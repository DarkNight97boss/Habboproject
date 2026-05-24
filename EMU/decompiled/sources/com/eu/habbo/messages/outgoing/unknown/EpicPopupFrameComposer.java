package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/EpicPopupFrameComposer.class */
public class EpicPopupFrameComposer extends MessageComposer {
    public static final String LIBRARY_URL = "${image.library.url}";
    private final String assetURI;

    public EpicPopupFrameComposer(String str) {
        this.assetURI = str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.EpicPopupFrameComposer);
        this.response.appendString(this.assetURI);
        return this.response;
    }
}
