package com.eu.habbo.messages.outgoing.friends;

import com.eu.habbo.habbohotel.messenger.MessengerBuddy;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.hash.THashSet;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/friends/RoomInviteErrorComposer.class */
public class RoomInviteErrorComposer extends MessageComposer {
    private final int errorCode;
    private final THashSet<MessengerBuddy> buddies;

    public RoomInviteErrorComposer(int i, THashSet<MessengerBuddy> tHashSet) {
        this.errorCode = i;
        this.buddies = tHashSet;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomInviteErrorComposer);
        this.response.appendInt(Integer.valueOf(this.errorCode));
        this.response.appendInt(Integer.valueOf(this.buddies.size()));
        this.buddies.forEach(new TObjectProcedure<MessengerBuddy>() { // from class: com.eu.habbo.messages.outgoing.friends.RoomInviteErrorComposer.1
            public boolean execute(MessengerBuddy messengerBuddy) {
                RoomInviteErrorComposer.this.response.appendInt(Integer.valueOf(messengerBuddy.getId()));
                return true;
            }
        });
        return this.response;
    }
}
