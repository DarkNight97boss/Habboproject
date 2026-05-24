package com.eu.habbo.messages.incoming.users;

import com.eu.habbo.habbohotel.users.subscriptions.SubscriptionHabboClub;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/users/RequestClubCenterEvent.class */
public class RequestClubCenterEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        this.client.sendResponse(SubscriptionHabboClub.calculatePayday(this.client.getHabbo().getHabboInfo()));
    }
}
