package com.eu.habbo.messages.incoming.navigator;

import com.eu.habbo.habbohotel.navigation.DisplayMode;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/navigator/NavigatorUncollapseCategoryEvent.class */
public class NavigatorUncollapseCategoryEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        this.client.getHabbo().getHabboStats().navigatorWindowSettings.setDisplayMode(this.packet.readString(), DisplayMode.VISIBLE);
    }
}
