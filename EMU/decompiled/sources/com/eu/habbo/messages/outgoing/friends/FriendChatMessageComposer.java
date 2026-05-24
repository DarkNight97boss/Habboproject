package com.eu.habbo.messages.outgoing.friends;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.messenger.Message;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/friends/FriendChatMessageComposer.class */
public class FriendChatMessageComposer extends MessageComposer {
    private final Message message;
    private final int toId;
    private final int fromId;

    public FriendChatMessageComposer(Message message) {
        this.message = message;
        this.toId = message.getFromId();
        this.fromId = message.getFromId();
    }

    public FriendChatMessageComposer(Message message, int i, int i2) {
        this.message = message;
        this.toId = i;
        this.fromId = i2;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.FriendChatMessageComposer);
        this.response.appendInt(Integer.valueOf(this.toId));
        this.response.appendString(this.message.getMessage());
        this.response.appendInt(Integer.valueOf(Emulator.getIntUnixTimestamp() - this.message.getTimestamp()));
        if (this.toId < 0) {
            String username = "AUTO_MODERATOR";
            String look = "lg-5635282-1193.hd-3091-1.sh-3089-73.cc-156282-64.hr-831-34.ha-1012-1186.ch-3050-62-62";
            if (this.fromId > 0) {
                Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(this.fromId);
                if (habbo != null) {
                    username = habbo.getHabboInfo().getUsername();
                    look = habbo.getHabboInfo().getLook();
                } else {
                    username = "UNKNOWN";
                }
            }
            this.response.appendString(username + "/" + look + "/" + this.fromId);
        }
        return this.response;
    }
}
