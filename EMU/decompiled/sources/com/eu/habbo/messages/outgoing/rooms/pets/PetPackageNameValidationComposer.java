package com.eu.habbo.messages.outgoing.rooms.pets;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/pets/PetPackageNameValidationComposer.class */
public class PetPackageNameValidationComposer extends MessageComposer {
    public static final int CLOSE_WIDGET = 0;
    public static final int NAME_TOO_SHORT = 1;
    public static final int NAME_TOO_LONG = 2;
    public static final int CONTAINS_INVALID_CHARS = 3;
    public static final int FORBIDDEN_WORDS = 4;
    private final int itemId;
    private final int errorCode;
    private final String errorString;

    public PetPackageNameValidationComposer(int i, int i2, String str) {
        this.itemId = i;
        this.errorCode = i2;
        this.errorString = str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.PetPackageNameValidationComposer);
        this.response.appendInt(Integer.valueOf(this.itemId));
        this.response.appendInt(Integer.valueOf(this.errorCode));
        this.response.appendString(this.errorString);
        return this.response;
    }
}
