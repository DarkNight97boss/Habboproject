package com.eu.habbo.messages.outgoing.modtool;

import com.eu.habbo.habbohotel.modtool.ModToolChatLog;
import com.eu.habbo.habbohotel.modtool.ModToolRoomVisit;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.iterator.hash.TObjectHashIterator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/modtool/ModToolUserChatlogComposer.class */
public class ModToolUserChatlogComposer extends MessageComposer {
    public static SimpleDateFormat format = new SimpleDateFormat("HH:mm");
    private final ArrayList<ModToolRoomVisit> set;
    private final int userId;
    private final String username;

    public ModToolUserChatlogComposer(ArrayList<ModToolRoomVisit> arrayList, int i, String str) {
        this.set = arrayList;
        this.userId = i;
        this.username = str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.ModToolUserChatlogComposer);
        this.response.appendInt(Integer.valueOf(this.userId));
        this.response.appendString(this.username);
        this.response.appendInt(Integer.valueOf(this.set.size()));
        for (ModToolRoomVisit modToolRoomVisit : this.set) {
            this.response.appendByte(1);
            this.response.appendShort(2);
            this.response.appendString("roomName");
            this.response.appendByte(2);
            this.response.appendString(modToolRoomVisit.roomName);
            this.response.appendString("roomId");
            this.response.appendByte(1);
            this.response.appendInt(Integer.valueOf(modToolRoomVisit.roomId));
            this.response.appendShort(modToolRoomVisit.chat.size());
            TObjectHashIterator it = modToolRoomVisit.chat.iterator();
            while (it.hasNext()) {
                ModToolChatLog modToolChatLog = (ModToolChatLog) it.next();
                this.response.appendString(format.format(Long.valueOf(((long) modToolChatLog.timestamp) * 1000)));
                this.response.appendInt(Integer.valueOf(modToolChatLog.habboId));
                this.response.appendString(modToolChatLog.username);
                this.response.appendString(modToolChatLog.message);
                this.response.appendBoolean(false);
            }
        }
        return this.response;
    }
}
