package com.eu.habbo.messages.outgoing.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ModToolChatLog;
import com.eu.habbo.habbohotel.modtool.ModToolChatRecordDataContext;
import com.eu.habbo.habbohotel.modtool.ModToolIssue;
import com.eu.habbo.habbohotel.modtool.ModToolIssueChatlogType;
import com.eu.habbo.habbohotel.modtool.ModToolTicketType;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/modtool/ModToolIssueChatlogComposer.class */
public class ModToolIssueChatlogComposer extends MessageComposer {
    public static SimpleDateFormat format = new SimpleDateFormat("HH:mm");
    private final ModToolIssue issue;
    private final List<ModToolChatLog> chatlog;
    private final String roomName;
    private ModToolIssueChatlogType type;

    public ModToolIssueChatlogComposer(ModToolIssue modToolIssue, List<ModToolChatLog> list, String str) {
        this.type = ModToolIssueChatlogType.CHAT;
        this.issue = modToolIssue;
        this.chatlog = list;
        this.roomName = str;
    }

    public ModToolIssueChatlogComposer(ModToolIssue modToolIssue, List<ModToolChatLog> list, String str, ModToolIssueChatlogType modToolIssueChatlogType) {
        this.type = ModToolIssueChatlogType.CHAT;
        this.issue = modToolIssue;
        this.chatlog = list;
        this.roomName = str;
        this.type = modToolIssueChatlogType;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.ModToolIssueChatlogComposer);
        this.response.appendInt(Integer.valueOf(this.issue.id));
        this.response.appendInt(Integer.valueOf(this.issue.senderId));
        this.response.appendInt(Integer.valueOf(this.issue.reportedId));
        this.response.appendInt(Integer.valueOf(this.issue.roomId));
        Collections.sort(this.chatlog);
        if (this.chatlog.isEmpty()) {
            return null;
        }
        this.response.appendByte(Integer.valueOf(this.type.getType()));
        if (this.issue.type == ModToolTicketType.IM) {
            this.response.appendShort(1);
            ModToolChatRecordDataContext.MESSAGE_ID.append(this.response);
            this.response.appendInt(Integer.valueOf(this.issue.senderId));
        } else if (this.issue.type == ModToolTicketType.DISCUSSION) {
            this.response.appendShort(this.type == ModToolIssueChatlogType.FORUM_COMMENT ? 3 : 2);
            ModToolChatRecordDataContext.GROUP_ID.append(this.response);
            this.response.appendInt(Integer.valueOf(this.issue.groupId));
            ModToolChatRecordDataContext.THREAD_ID.append(this.response);
            this.response.appendInt(Integer.valueOf(this.issue.threadId));
            if (this.type == ModToolIssueChatlogType.FORUM_COMMENT) {
                ModToolChatRecordDataContext.MESSAGE_ID.append(this.response);
                this.response.appendInt(Integer.valueOf(this.issue.commentId));
            }
        } else if (this.issue.type == ModToolTicketType.PHOTO) {
            this.response.appendShort(2);
            ModToolChatRecordDataContext.ROOM_NAME.append(this.response);
            this.response.appendString(this.roomName);
            ModToolChatRecordDataContext.PHOTO_ID.append(this.response);
            this.response.appendString(this.issue.photoItem.getId() + Emulator.PREVIEW);
        } else {
            this.response.appendShort(3);
            ModToolChatRecordDataContext.ROOM_NAME.append(this.response);
            this.response.appendString(this.roomName);
            ModToolChatRecordDataContext.ROOM_ID.append(this.response);
            this.response.appendInt(Integer.valueOf(this.issue.roomId));
            ModToolChatRecordDataContext.GROUP_ID.append(this.response);
            Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(this.issue.roomId);
            this.response.appendInt(Integer.valueOf(room == null ? 0 : room.getGuildId()));
        }
        this.response.appendShort(this.chatlog.size());
        for (ModToolChatLog modToolChatLog : this.chatlog) {
            this.response.appendString(format.format(Long.valueOf(((long) modToolChatLog.timestamp) * 1000)));
            this.response.appendInt(Integer.valueOf(modToolChatLog.habboId));
            this.response.appendString(modToolChatLog.username);
            this.response.appendString(modToolChatLog.message);
            this.response.appendBoolean(Boolean.valueOf(modToolChatLog.highlighted));
        }
        return this.response;
    }
}
