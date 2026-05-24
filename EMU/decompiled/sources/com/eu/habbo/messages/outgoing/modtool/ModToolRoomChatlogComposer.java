package com.eu.habbo.messages.outgoing.modtool;

import com.eu.habbo.habbohotel.modtool.ModToolChatLog;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/modtool/ModToolRoomChatlogComposer.class */
public class ModToolRoomChatlogComposer extends MessageComposer {
    private final Room room;
    private final ArrayList<ModToolChatLog> chatlog;

    public ModToolRoomChatlogComposer(Room room, ArrayList<ModToolChatLog> arrayList) {
        this.room = room;
        this.chatlog = arrayList;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.ModToolRoomChatlogComposer);
        this.response.appendByte(1);
        this.response.appendShort(2);
        this.response.appendString("roomName");
        this.response.appendByte(2);
        this.response.appendString(this.room.getName());
        this.response.appendString("roomId");
        this.response.appendByte(1);
        this.response.appendInt(Integer.valueOf(this.room.getId()));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        this.response.appendShort(this.chatlog.size());
        for (ModToolChatLog modToolChatLog : this.chatlog) {
            this.response.appendString(simpleDateFormat.format(new Date(((long) modToolChatLog.timestamp) * 1000)));
            this.response.appendInt(Integer.valueOf(modToolChatLog.habboId));
            this.response.appendString(modToolChatLog.username);
            this.response.appendString(modToolChatLog.message);
            this.response.appendBoolean(false);
        }
        return this.response;
    }
}
