package com.eu.habbo.messages.outgoing.rooms.pets;

import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/pets/PetPackageComposer.class */
public class PetPackageComposer extends MessageComposer {
    private final HabboItem item;

    public PetPackageComposer(HabboItem habboItem) {
        this.item = habboItem;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.LeprechaunStarterBundleComposer);
        this.response.appendInt(Integer.valueOf(this.item.getId()));
        return this.response;
    }
}
