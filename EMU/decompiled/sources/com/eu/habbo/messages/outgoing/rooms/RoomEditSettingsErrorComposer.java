package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/RoomEditSettingsErrorComposer.class */
public class RoomEditSettingsErrorComposer extends MessageComposer {
    public static final int PASSWORD_REQUIRED = 5;
    public static final int ROOM_NAME_MISSING = 7;
    public static final int ROOM_NAME_BADWORDS = 8;
    public static final int ROOM_DESCRIPTION_BADWORDS = 10;
    public static final int ROOM_TAGS_BADWWORDS = 11;
    public static final int RESTRICTED_TAGS = 12;
    public static final int TAGS_TOO_LONG = 13;
    private final int roomId;
    private final int errorCode;
    private final String info;

    public RoomEditSettingsErrorComposer(int i, int i2, String str) {
        this.roomId = i;
        this.errorCode = i2;
        this.info = str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomEditSettingsErrorComposer);
        this.response.appendInt(Integer.valueOf(this.roomId));
        this.response.appendInt(Integer.valueOf(this.errorCode));
        this.response.appendString(this.info);
        return this.response;
    }
}
