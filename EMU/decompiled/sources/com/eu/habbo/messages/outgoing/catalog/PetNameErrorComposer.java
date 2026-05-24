package com.eu.habbo.messages.outgoing.catalog;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/catalog/PetNameErrorComposer.class */
public class PetNameErrorComposer extends MessageComposer {
    public static final int NAME_OK = 0;
    public static final int NAME_TO_LONG = 1;
    public static final int NAME_TO_SHORT = 2;
    public static final int FORBIDDEN_CHAR = 3;
    public static final int FORBIDDEN_WORDS = 4;
    private final int type;
    private final String value;

    public PetNameErrorComposer(int i, String str) {
        this.type = i;
        this.value = str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.PetNameErrorComposer);
        this.response.appendInt(Integer.valueOf(this.type));
        this.response.appendString(this.value);
        return this.response;
    }
}
