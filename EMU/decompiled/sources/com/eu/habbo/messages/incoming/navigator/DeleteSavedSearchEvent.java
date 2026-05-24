package com.eu.habbo.messages.incoming.navigator;

import com.eu.habbo.habbohotel.navigation.NavigatorSavedSearch;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.navigator.NewNavigatorSavedSearchesComposer;
import java.util.Iterator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/navigator/DeleteSavedSearchEvent.class */
public class DeleteSavedSearchEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        NavigatorSavedSearch navigatorSavedSearch = null;
        Iterator<NavigatorSavedSearch> it = this.client.getHabbo().getHabboInfo().getSavedSearches().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            NavigatorSavedSearch next = it.next();
            if (next.getId() == iIntValue) {
                navigatorSavedSearch = next;
                break;
            }
        }
        if (navigatorSavedSearch == null) {
            return;
        }
        this.client.getHabbo().getHabboInfo().deleteSavedSearch(navigatorSavedSearch);
        this.client.sendResponse(new NewNavigatorSavedSearchesComposer(this.client.getHabbo().getHabboInfo().getSavedSearches()));
    }
}
