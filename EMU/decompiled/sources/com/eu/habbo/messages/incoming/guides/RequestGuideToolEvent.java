package com.eu.habbo.messages.incoming.guides;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.guides.GuideToolsComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guides/RequestGuideToolEvent.class */
public class RequestGuideToolEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        boolean z = this.packet.readBoolean();
        if (!z) {
            Emulator.getGameEnvironment().getGuideManager().setOnGuide(this.client.getHabbo(), z);
            Emulator.getGameEnvironment().getGuideManager().setOnGuardian(this.client.getHabbo(), z);
            this.client.sendResponse(new GuideToolsComposer(z));
            return;
        }
        this.packet.readBoolean();
        boolean z2 = this.packet.readBoolean();
        boolean z3 = this.packet.readBoolean();
        if (this.client.getHabbo().hasPermission(Permission.ACC_HELPER_USE_GUIDE_TOOL)) {
            if (z2 && !this.client.getHabbo().hasPermission(Permission.ACC_HELPER_GIVE_GUIDE_TOURS)) {
                z2 = false;
            }
            if (z3 && !this.client.getHabbo().hasPermission(Permission.ACC_HELPER_JUDGE_CHAT_REVIEWS)) {
                z3 = false;
            }
            if (z2) {
                Emulator.getGameEnvironment().getGuideManager().setOnGuide(this.client.getHabbo(), z);
            }
            if (z3) {
                Emulator.getGameEnvironment().getGuideManager().setOnGuardian(this.client.getHabbo(), z);
            }
            this.client.sendResponse(new GuideToolsComposer(z));
        }
    }
}
