package com.eu.habbo.messages.incoming.friends;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.friends.FriendsComposer;
import com.eu.habbo.messages.outgoing.friends.MessengerInitComposer;
import java.util.ArrayList;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/friends/RequestInitFriendsEvent.class */
public class RequestInitFriendsEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        ArrayList<ServerMessage> arrayList = new ArrayList<>();
        arrayList.add(new MessengerInitComposer(this.client.getHabbo()).compose());
        arrayList.addAll(FriendsComposer.getMessagesForBuddyList(this.client.getHabbo().getMessenger().getFriends().values()));
        this.client.sendResponses(arrayList);
    }
}
