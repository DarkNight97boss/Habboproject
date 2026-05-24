package com.eu.habbo.messages.incoming.floorplaneditor;

import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.floorplaneditor.FloorPlanEditorBlockedTilesComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/floorplaneditor/FloorPlanEditorRequestBlockedTilesEvent.class */
public class FloorPlanEditorRequestBlockedTilesEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (this.client.getHabbo().getHabboInfo().getCurrentRoom() == null) {
            return;
        }
        this.client.sendResponse(new FloorPlanEditorBlockedTilesComposer(this.client.getHabbo().getHabboInfo().getCurrentRoom()));
    }
}
