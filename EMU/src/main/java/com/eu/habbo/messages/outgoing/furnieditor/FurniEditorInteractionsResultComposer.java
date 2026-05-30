package com.eu.habbo.messages.outgoing.furnieditor;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

import java.util.List;

/**
 * 10043 FURNI_EDITOR_INTERACTIONS_RESULT — distinct {@code interaction_type}
 * values from {@code items_base}, used to populate the editor's dropdown.
 *
 * Wire shape:
 *   int count
 *   repeat count: str interactionType
 */
public class FurniEditorInteractionsResultComposer extends MessageComposer {

    private final List<String> interactions;

    public FurniEditorInteractionsResultComposer(List<String> interactions) {
        this.interactions = interactions;
    }

    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.FurniEditorInteractionsResultComposer);
        this.response.appendInt(this.interactions.size());
        for (String s : this.interactions) {
            this.response.appendString(s == null ? "" : s);
        }
        return this.response;
    }
}
