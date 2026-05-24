package com.eu.habbo.messages.outgoing.navigator;

import com.eu.habbo.habbohotel.rooms.RoomCategory;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import com.eu.habbo.messages.outgoing.rooms.items.rentablespaces.RentableSpaceInfoComposer;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/navigator/NewNavigatorCategoryUserCountComposer.class */
public class NewNavigatorCategoryUserCountComposer extends MessageComposer {
    public final List<RoomCategory> roomCategories;

    public NewNavigatorCategoryUserCountComposer(List<RoomCategory> list) {
        this.roomCategories = list;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.NewNavigatorCategoryUserCountComposer);
        this.response.appendInt(Integer.valueOf(this.roomCategories.size()));
        for (RoomCategory roomCategory : this.roomCategories) {
            this.response.appendInt((Integer) 0);
            this.response.appendInt((Integer) 0);
            this.response.appendInt(Integer.valueOf(RentableSpaceInfoComposer.NOT_ENOUGH_CREDITS));
        }
        return this.response;
    }
}
