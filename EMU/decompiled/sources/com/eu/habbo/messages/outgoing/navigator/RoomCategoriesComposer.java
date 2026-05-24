package com.eu.habbo.messages.outgoing.navigator;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.RoomCategory;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/navigator/RoomCategoriesComposer.class */
public class RoomCategoriesComposer extends MessageComposer {
    private final List<RoomCategory> categories;

    public RoomCategoriesComposer(List<RoomCategory> list) {
        this.categories = list;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomCategoriesComposer);
        this.response.appendInt(Integer.valueOf(this.categories.size()));
        for (RoomCategory roomCategory : this.categories) {
            this.response.appendInt(Integer.valueOf(roomCategory.getId()));
            this.response.appendString(roomCategory.getCaption());
            this.response.appendBoolean(true);
            this.response.appendBoolean(false);
            this.response.appendString(roomCategory.getCaption());
            if (roomCategory.getCaption().startsWith("${")) {
                this.response.appendString(Emulator.PREVIEW);
            } else {
                this.response.appendString(roomCategory.getCaption());
            }
            this.response.appendBoolean(false);
        }
        return this.response;
    }
}
