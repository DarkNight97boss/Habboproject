package com.eu.habbo.messages.outgoing.navigator;

import com.eu.habbo.habbohotel.navigation.EventCategory;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/navigator/NewNavigatorEventCategoriesComposer.class */
public class NewNavigatorEventCategoriesComposer extends MessageComposer {
    public static List<EventCategory> CATEGORIES = new ArrayList();

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.NewNavigatorEventCategoriesComposer);
        this.response.appendInt(Integer.valueOf(CATEGORIES.size()));
        Iterator<EventCategory> it = CATEGORIES.iterator();
        while (it.hasNext()) {
            it.next().serialize(this.response);
        }
        return this.response;
    }
}
