package com.eu.habbo.habbohotel.modtool;

import com.eu.habbo.messages.ServerMessage;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/modtool/ModToolChatRecordDataContext.class */
public enum ModToolChatRecordDataContext {
    ROOM_NAME("roomName", 2),
    ROOM_ID("roomId", 1),
    GROUP_ID("groupId", 1),
    THREAD_ID("threadId", 1),
    MESSAGE_ID("messageId", 1),
    PHOTO_ID("extraDataId", 2);

    public final String key;
    public final int type;

    ModToolChatRecordDataContext(String str, int i) {
        this.key = str;
        this.type = i;
    }

    public void append(ServerMessage serverMessage) {
        serverMessage.appendString(this.key);
        serverMessage.appendByte(Integer.valueOf(this.type));
    }
}
