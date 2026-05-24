package com.eu.habbo.messages.outgoing.navigator;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.navigation.NavigatorSavedSearch;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/navigator/NewNavigatorSavedSearchesComposer.class */
public class NewNavigatorSavedSearchesComposer extends MessageComposer {
    private final List<NavigatorSavedSearch> searches;

    public NewNavigatorSavedSearchesComposer(List<NavigatorSavedSearch> list) {
        this.searches = list;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.NewNavigatorSavedSearchesComposer);
        this.response.appendInt(Integer.valueOf(this.searches.size()));
        for (NavigatorSavedSearch navigatorSavedSearch : this.searches) {
            this.response.appendInt(Integer.valueOf(navigatorSavedSearch.getId()));
            this.response.appendString(navigatorSavedSearch.getSearchCode());
            this.response.appendString(navigatorSavedSearch.getFilter() == null ? Emulator.PREVIEW : navigatorSavedSearch.getFilter());
            this.response.appendString(Emulator.PREVIEW);
        }
        return this.response;
    }
}
