package com.eu.habbo.messages.incoming.friends;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.messenger.Messenger;
import com.eu.habbo.habbohotel.messenger.MessengerBuddy;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.friends.UserSearchResultComposer;
import gnu.trove.set.hash.THashSet;
import java.util.concurrent.ConcurrentHashMap;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/friends/SearchUserEvent.class */
public class SearchUserEvent extends MessageHandler {
    public static ConcurrentHashMap<String, THashSet<MessengerBuddy>> cachedResults = new ConcurrentHashMap<>();

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (System.currentTimeMillis() - this.client.getHabbo().getHabboStats().lastUsersSearched < 3000) {
            return;
        }
        String lowerCase = this.packet.readString().replace(" ", Emulator.PREVIEW).toLowerCase();
        if (lowerCase.isEmpty()) {
            return;
        }
        if (lowerCase.length() > 15) {
            lowerCase = lowerCase.substring(0, 15);
        }
        if (this.client.getHabbo().getMessenger() != null) {
            THashSet<MessengerBuddy> tHashSetSearchUsers = cachedResults.get(lowerCase);
            if (tHashSetSearchUsers == null) {
                tHashSetSearchUsers = Messenger.searchUsers(lowerCase);
                cachedResults.put(lowerCase, tHashSetSearchUsers);
            }
            this.client.sendResponse(new UserSearchResultComposer(tHashSetSearchUsers, this.client.getHabbo().getMessenger().getFriends(lowerCase), this.client.getHabbo()));
        }
    }
}
