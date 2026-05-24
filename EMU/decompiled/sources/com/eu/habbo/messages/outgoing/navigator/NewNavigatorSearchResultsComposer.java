package com.eu.habbo.messages.outgoing.navigator;

import com.eu.habbo.habbohotel.navigation.SearchResultList;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/navigator/NewNavigatorSearchResultsComposer.class */
public class NewNavigatorSearchResultsComposer extends MessageComposer {
    private final String searchCode;
    private final String searchQuery;
    private final List<SearchResultList> resultList;

    public NewNavigatorSearchResultsComposer(String str, String str2, List<SearchResultList> list) {
        this.searchCode = str;
        this.searchQuery = str2;
        this.resultList = list;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.NewNavigatorSearchResultsComposer);
        this.response.appendString(this.searchCode);
        this.response.appendString(this.searchQuery);
        this.response.appendInt(Integer.valueOf(this.resultList.size()));
        Iterator<SearchResultList> it = this.resultList.iterator();
        while (it.hasNext()) {
            it.next().serialize(this.response);
        }
        return this.response;
    }
}
