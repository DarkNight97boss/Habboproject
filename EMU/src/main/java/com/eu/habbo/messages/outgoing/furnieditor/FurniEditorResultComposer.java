package com.eu.habbo.messages.outgoing.furnieditor;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/**
 * 10044 FURNI_EDITOR_RESULT — generic ack for UPDATE / DELETE.
 *
 * Wire shape:
 *   bool success
 *   str  message
 *   int  id   (subject of the mutation; -1 on validation failure)
 */
public class FurniEditorResultComposer extends MessageComposer {

    private final boolean success;
    private final String message;
    private final int id;

    public FurniEditorResultComposer(boolean success, String message, int id) {
        this.success = success;
        this.message = message == null ? "" : message;
        this.id = id;
    }

    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.FurniEditorResultComposer);
        this.response.appendBoolean(this.success);
        this.response.appendString(this.message);
        this.response.appendInt(this.id);
        return this.response;
    }
}
