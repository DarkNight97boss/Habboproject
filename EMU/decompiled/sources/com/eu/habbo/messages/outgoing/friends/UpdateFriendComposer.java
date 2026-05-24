package com.eu.habbo.messages.outgoing.friends;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.messenger.MessengerBuddy;
import com.eu.habbo.habbohotel.messenger.MessengerCategory;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboGender;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/friends/UpdateFriendComposer.class */
public class UpdateFriendComposer extends MessageComposer {
    private Collection<MessengerBuddy> buddies;
    private Habbo habbo;
    private int action;

    public UpdateFriendComposer(Habbo habbo, MessengerBuddy messengerBuddy, Integer num) {
        this.habbo = habbo;
        this.buddies = Collections.singletonList(messengerBuddy);
        this.action = num.intValue();
    }

    public UpdateFriendComposer(Habbo habbo, Collection<MessengerBuddy> collection, Integer num) {
        this.habbo = habbo;
        this.buddies = collection;
        this.action = num.intValue();
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.UpdateFriendComposer);
        if (this.habbo == null || this.habbo.getHabboInfo().getMessengerCategories().isEmpty()) {
            this.response.appendInt((Integer) 0);
        } else {
            List<MessengerCategory> messengerCategories = this.habbo.getHabboInfo().getMessengerCategories();
            this.response.appendInt(Integer.valueOf(messengerCategories.size()));
            for (MessengerCategory messengerCategory : messengerCategories) {
                this.response.appendInt(Integer.valueOf(messengerCategory.getId()));
                this.response.appendString(messengerCategory.getName());
            }
        }
        this.response.appendInt(Integer.valueOf(this.buddies.size()));
        for (MessengerBuddy messengerBuddy : this.buddies) {
            if (messengerBuddy != null) {
                this.response.appendInt(Integer.valueOf(this.action));
                this.response.appendInt(Integer.valueOf(messengerBuddy.getId()));
                if (this.action != -1) {
                    this.response.appendString(messengerBuddy.getUsername());
                    this.response.appendInt(Integer.valueOf(messengerBuddy.getGender().equals(HabboGender.M) ? 0 : 1));
                    this.response.appendBoolean(Boolean.valueOf(messengerBuddy.getOnline() == 1));
                    this.response.appendBoolean(Boolean.valueOf(messengerBuddy.inRoom()));
                    this.response.appendString(messengerBuddy.getLook());
                    this.response.appendInt(Integer.valueOf(messengerBuddy.getCategoryId()));
                    this.response.appendString(messengerBuddy.getMotto());
                    this.response.appendString(Emulator.PREVIEW);
                    this.response.appendString(Emulator.PREVIEW);
                    this.response.appendBoolean(false);
                    this.response.appendBoolean(false);
                    this.response.appendBoolean(false);
                    this.response.appendShort(messengerBuddy.getRelation());
                }
            }
        }
        return this.response;
    }
}
