package com.eu.habbo.messages.outgoing.friends;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.messenger.MessengerBuddy;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/friends/UserSearchResultComposer.class */
public class UserSearchResultComposer extends MessageComposer {
    private final THashSet<MessengerBuddy> users;
    private final THashSet<MessengerBuddy> friends;
    private final Habbo habbo;
    private static Comparator COMPARATOR = Comparator.comparing(messengerBuddy -> {
        return Integer.valueOf(messengerBuddy.getUsername().length());
    }).thenComparing((messengerBuddy2, messengerBuddy3) -> {
        return messengerBuddy2.getUsername().compareToIgnoreCase(messengerBuddy3.getUsername());
    });

    public UserSearchResultComposer(THashSet<MessengerBuddy> tHashSet, THashSet<MessengerBuddy> tHashSet2, Habbo habbo) {
        this.users = tHashSet;
        this.friends = tHashSet2;
        this.habbo = habbo;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.UserSearchResultComposer);
        ArrayList<MessengerBuddy> arrayList = new ArrayList();
        TObjectHashIterator it = this.users.iterator();
        while (it.hasNext()) {
            MessengerBuddy messengerBuddy = (MessengerBuddy) it.next();
            if (!inFriendList(messengerBuddy)) {
                arrayList.add(messengerBuddy);
            }
        }
        ArrayList arrayList2 = new ArrayList((Collection) this.friends);
        arrayList.sort(COMPARATOR);
        arrayList2.sort(COMPARATOR);
        this.response.appendInt(Integer.valueOf(this.friends.size()));
        TObjectHashIterator it2 = this.friends.iterator();
        while (it2.hasNext()) {
            MessengerBuddy messengerBuddy2 = (MessengerBuddy) it2.next();
            this.response.appendInt(Integer.valueOf(messengerBuddy2.getId()));
            this.response.appendString(messengerBuddy2.getUsername());
            this.response.appendString(messengerBuddy2.getMotto());
            this.response.appendBoolean(false);
            this.response.appendBoolean(false);
            this.response.appendString(Emulator.PREVIEW);
            this.response.appendInt((Integer) 1);
            this.response.appendString(messengerBuddy2.getLook());
            this.response.appendString(Emulator.PREVIEW);
        }
        this.response.appendInt(Integer.valueOf(arrayList.size()));
        for (MessengerBuddy messengerBuddy3 : arrayList) {
            this.response.appendInt(Integer.valueOf(messengerBuddy3.getId()));
            this.response.appendString(messengerBuddy3.getUsername());
            this.response.appendString(messengerBuddy3.getMotto());
            this.response.appendBoolean(false);
            this.response.appendBoolean(false);
            this.response.appendString(Emulator.PREVIEW);
            this.response.appendInt((Integer) 1);
            this.response.appendString(messengerBuddy3.getOnline() == 1 ? messengerBuddy3.getLook() : Emulator.PREVIEW);
            this.response.appendString(Emulator.PREVIEW);
        }
        return this.response;
    }

    private boolean inFriendList(MessengerBuddy messengerBuddy) {
        TObjectHashIterator it = this.friends.iterator();
        while (it.hasNext()) {
            if (((MessengerBuddy) it.next()).getUsername().equals(messengerBuddy.getUsername())) {
                return true;
            }
        }
        return false;
    }
}
