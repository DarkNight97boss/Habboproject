package com.eu.habbo.messages.outgoing.floorplaneditor;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/floorplaneditor/FloorPlanEditorDoorSettingsComposer.class */
public class FloorPlanEditorDoorSettingsComposer extends MessageComposer {
    private final Room room;

    public FloorPlanEditorDoorSettingsComposer(Room room) {
        this.room = room;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.FloorPlanEditorDoorSettingsComposer);
        this.response.appendInt(Short.valueOf(this.room.getLayout().getDoorX()));
        this.response.appendInt(Short.valueOf(this.room.getLayout().getDoorY()));
        this.response.appendInt(Integer.valueOf(this.room.getLayout().getDoorDirection()));
        return this.response;
    }
}
