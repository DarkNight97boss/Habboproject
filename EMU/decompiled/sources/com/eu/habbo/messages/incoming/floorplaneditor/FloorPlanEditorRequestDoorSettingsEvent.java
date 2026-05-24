package com.eu.habbo.messages.incoming.floorplaneditor;

import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.floorplaneditor.FloorPlanEditorDoorSettingsComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomFloorThicknessUpdatedComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/floorplaneditor/FloorPlanEditorRequestDoorSettingsEvent.class */
public class FloorPlanEditorRequestDoorSettingsEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (this.client.getHabbo().getHabboInfo().getCurrentRoom() == null) {
            return;
        }
        this.client.sendResponse(new FloorPlanEditorDoorSettingsComposer(this.client.getHabbo().getHabboInfo().getCurrentRoom()));
        this.client.sendResponse(new RoomFloorThicknessUpdatedComposer(this.client.getHabbo().getHabboInfo().getCurrentRoom()));
    }
}
