package com.eu.habbo.messages.outgoing.habboway.nux;

import com.eu.habbo.habbohotel.items.NewUserGift;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/habboway/nux/NewUserGiftComposer.class */
public class NewUserGiftComposer extends MessageComposer {
    private final List<List<NewUserGift>> options;

    public NewUserGiftComposer(List<List<NewUserGift>> list) {
        this.options = list;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.NewUserGiftComposer);
        this.response.appendInt(Integer.valueOf(this.options.size()));
        for (List<NewUserGift> list : this.options) {
            this.response.appendInt((Integer) 1);
            this.response.appendInt((Integer) 3);
            this.response.appendInt(Integer.valueOf(list.size()));
            Iterator<NewUserGift> it = list.iterator();
            while (it.hasNext()) {
                it.next().serialize(this.response);
            }
        }
        return this.response;
    }
}
