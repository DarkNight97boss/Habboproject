package com.eu.habbo.messages.incoming.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.forums.ForumThread;
import com.eu.habbo.habbohotel.modtool.ModToolChatLog;
import com.eu.habbo.habbohotel.modtool.ModToolIssue;
import com.eu.habbo.habbohotel.modtool.ModToolIssueChatlogType;
import com.eu.habbo.habbohotel.modtool.ModToolTicketType;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.modtool.ModToolIssueChatlogComposer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/modtool/ModToolRequestIssueChatlogEvent.class */
public class ModToolRequestIssueChatlogEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (!this.client.getHabbo().hasPermission(Permission.ACC_SUPPORTTOOL)) {
            ScripterManager.scripterDetected(this.client, Emulator.getTexts().getValue("scripter.warning.modtools.chatlog").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()));
            return;
        }
        ModToolIssue ticket = Emulator.getGameEnvironment().getModToolManager().getTicket(this.packet.readInt().intValue());
        if (ticket != null) {
            List arrayList = new ArrayList();
            ModToolIssueChatlogType modToolIssueChatlogType = ModToolIssueChatlogType.CHAT;
            if (ticket.type == ModToolTicketType.IM) {
                arrayList = Emulator.getGameEnvironment().getModToolManager().getMessengerChatlog(ticket.reportedId, ticket.senderId);
                modToolIssueChatlogType = ModToolIssueChatlogType.IM;
            } else if (ticket.type == ModToolTicketType.DISCUSSION) {
                if (ticket.commentId == -1) {
                    modToolIssueChatlogType = ModToolIssueChatlogType.FORUM_THREAD;
                    ForumThread byId = ForumThread.getById(ticket.threadId);
                    if (byId != null) {
                        arrayList = (List) byId.getComments().stream().map(forumThreadComment -> {
                            return new ModToolChatLog(forumThreadComment.getCreatedAt(), forumThreadComment.getHabbo().getHabboInfo().getId(), forumThreadComment.getHabbo().getHabboInfo().getUsername(), forumThreadComment.getMessage());
                        }).collect(Collectors.toList());
                    }
                } else {
                    modToolIssueChatlogType = ModToolIssueChatlogType.FORUM_COMMENT;
                    ForumThread byId2 = ForumThread.getById(ticket.threadId);
                    if (byId2 != null) {
                        arrayList = (List) byId2.getComments().stream().map(forumThreadComment2 -> {
                            return new ModToolChatLog(forumThreadComment2.getCreatedAt(), forumThreadComment2.getHabbo().getHabboInfo().getId(), forumThreadComment2.getHabbo().getHabboInfo().getUsername(), forumThreadComment2.getMessage(), forumThreadComment2.getCommentId() == ticket.commentId);
                        }).collect(Collectors.toList());
                    }
                }
            } else if (ticket.type != ModToolTicketType.PHOTO) {
                modToolIssueChatlogType = ModToolIssueChatlogType.CHAT;
                if (ticket.roomId > 0) {
                    arrayList = Emulator.getGameEnvironment().getModToolManager().getRoomChatlog(ticket.roomId);
                } else {
                    arrayList = new ArrayList();
                    arrayList.addAll(Emulator.getGameEnvironment().getModToolManager().getUserChatlog(ticket.reportedId));
                    arrayList.addAll(Emulator.getGameEnvironment().getModToolManager().getUserChatlog(ticket.senderId));
                }
            } else if (ticket.photoItem != null) {
                modToolIssueChatlogType = ModToolIssueChatlogType.PHOTO;
                arrayList = Emulator.getGameEnvironment().getModToolManager().getRoomChatlog(ticket.roomId);
            }
            Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(ticket.roomId);
            String name = Emulator.PREVIEW;
            if (room != null) {
                name = room.getName();
            }
            this.client.sendResponse(new ModToolIssueChatlogComposer(ticket, arrayList, name, modToolIssueChatlogType));
        }
    }
}
