package com.eu.habbo.messages.outgoing.friends;

import com.eu.habbo.habbohotel.messenger.Messenger;
import com.eu.habbo.habbohotel.messenger.MessengerCategory;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/friends/MessengerInitComposer.class */
public class MessengerInitComposer extends MessageComposer {
    private final Habbo habbo;

    public MessengerInitComposer(Habbo habbo) {
        this.habbo = habbo;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.MessengerInitComposer);
        if (this.habbo.hasPermission("acc_infinite_friends")) {
            this.response.appendInt((Integer) Integer.MAX_VALUE);
            this.response.appendInt((Integer) 1337);
            this.response.appendInt((Integer) Integer.MAX_VALUE);
        } else {
            this.response.appendInt(Integer.valueOf(Messenger.MAXIMUM_FRIENDS));
            this.response.appendInt((Integer) 1337);
            this.response.appendInt(Integer.valueOf(Messenger.MAXIMUM_FRIENDS_HC));
        }
        if (this.habbo.getHabboInfo().getMessengerCategories().isEmpty()) {
            this.response.appendInt((Integer) 0);
        } else {
            List<MessengerCategory> messengerCategories = this.habbo.getHabboInfo().getMessengerCategories();
            this.response.appendInt(Integer.valueOf(messengerCategories.size()));
            for (MessengerCategory messengerCategory : messengerCategories) {
                this.response.appendInt(Integer.valueOf(messengerCategory.getId()));
                this.response.appendString(messengerCategory.getName());
            }
        }
        return this.response;
    }
}
