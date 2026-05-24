package com.eu.habbo.messages.incoming.guides;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guides.GuideChatMessage;
import com.eu.habbo.habbohotel.guides.GuideTour;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.guides.GuideSessionMessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guides/GuideUserMessageEvent.class */
public class GuideUserMessageEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        GuideTour guideTourByHabbo = Emulator.getGameEnvironment().getGuideManager().getGuideTourByHabbo(this.client.getHabbo());
        if (guideTourByHabbo != null) {
            GuideChatMessage guideChatMessage = new GuideChatMessage(this.client.getHabbo().getHabboInfo().getId(), this.packet.readString(), Emulator.getIntUnixTimestamp());
            guideTourByHabbo.addMessage(guideChatMessage);
            ServerMessage serverMessageCompose = new GuideSessionMessageComposer(guideChatMessage).compose();
            guideTourByHabbo.getHelper().getClient().sendResponse(serverMessageCompose);
            guideTourByHabbo.getNoob().getClient().sendResponse(serverMessageCompose);
        }
    }
}
