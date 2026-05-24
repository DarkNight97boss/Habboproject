package com.eu.habbo.messages.outgoing.friends;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.messenger.MessengerBuddy;
import com.eu.habbo.habbohotel.users.HabboGender;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.set.hash.THashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/friends/FriendsComposer.class */
public class FriendsComposer extends MessageComposer {
    private static final Logger LOGGER = LoggerFactory.getLogger(FriendsComposer.class);
    private final int totalPages;
    private final int pageIndex;
    private final Collection<MessengerBuddy> friends;

    public FriendsComposer(int i, int i2, Collection<MessengerBuddy> collection) {
        this.totalPages = i;
        this.pageIndex = i2;
        this.friends = collection;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        try {
            this.response.init(Outgoing.FriendsComposer);
            this.response.appendInt(Integer.valueOf(this.totalPages));
            this.response.appendInt(Integer.valueOf(this.pageIndex));
            this.response.appendInt(Integer.valueOf(this.friends.size()));
            for (MessengerBuddy messengerBuddy : this.friends) {
                this.response.appendInt(Integer.valueOf(messengerBuddy.getId()));
                this.response.appendString(messengerBuddy.getUsername());
                this.response.appendInt(Integer.valueOf(messengerBuddy.getGender().equals(HabboGender.M) ? 0 : 1));
                this.response.appendBoolean(Boolean.valueOf(messengerBuddy.getOnline() == 1));
                this.response.appendBoolean(Boolean.valueOf(messengerBuddy.inRoom()));
                this.response.appendString(messengerBuddy.getOnline() == 1 ? messengerBuddy.getLook() : Emulator.PREVIEW);
                this.response.appendInt(Integer.valueOf(messengerBuddy.getCategoryId()));
                this.response.appendString(messengerBuddy.getMotto());
                this.response.appendString(Emulator.PREVIEW);
                this.response.appendString(Emulator.PREVIEW);
                this.response.appendBoolean(false);
                this.response.appendBoolean(false);
                this.response.appendBoolean(false);
                this.response.appendShort(messengerBuddy.getRelation());
            }
            return this.response;
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
            return null;
        }
    }

    public static ArrayList<ServerMessage> getMessagesForBuddyList(Collection<MessengerBuddy> collection) {
        ArrayList<ServerMessage> arrayList = new ArrayList<>();
        THashSet tHashSet = new THashSet();
        int iCeil = (int) Math.ceil(((double) collection.size()) / 750.0d);
        int i = 0;
        Iterator<MessengerBuddy> it = collection.iterator();
        while (it.hasNext()) {
            tHashSet.add(it.next());
            if (tHashSet.size() == 750) {
                arrayList.add(new FriendsComposer(iCeil, i, tHashSet).compose());
                tHashSet.clear();
                i++;
            }
        }
        if (i == 0 || tHashSet.size() > 0) {
            arrayList.add(new FriendsComposer(iCeil, i, tHashSet).compose());
        }
        return arrayList;
    }
}
