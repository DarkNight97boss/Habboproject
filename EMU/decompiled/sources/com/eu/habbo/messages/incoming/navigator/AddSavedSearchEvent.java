package com.eu.habbo.messages.incoming.navigator;

import com.eu.habbo.habbohotel.navigation.NavigatorSavedSearch;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.navigator.NewNavigatorSavedSearchesComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/navigator/AddSavedSearchEvent.class */
public class AddSavedSearchEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        String string = this.packet.readString();
        String string2 = this.packet.readString();
        if (string.length() > 255) {
            string = string.substring(0, 255);
        }
        if (string2.length() > 255) {
            string2 = string2.substring(0, 255);
        }
        this.client.getHabbo().getHabboInfo().addSavedSearch(new NavigatorSavedSearch(string, string2));
        this.client.sendResponse(new NewNavigatorSavedSearchesComposer(this.client.getHabbo().getHabboInfo().getSavedSearches()));
    }
}
