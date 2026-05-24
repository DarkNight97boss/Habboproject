package com.eu.habbo.messages.outgoing.navigator;

import com.eu.habbo.habbohotel.navigation.NavigatorHotelFilter;
import com.eu.habbo.habbohotel.navigation.NavigatorPublicFilter;
import com.eu.habbo.habbohotel.navigation.NavigatorRoomAdsFilter;
import com.eu.habbo.habbohotel.navigation.NavigatorUserFilter;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/navigator/NewNavigatorMetaDataComposer.class */
public class NewNavigatorMetaDataComposer extends MessageComposer {
    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.NewNavigatorMetaDataComposer);
        this.response.appendInt((Integer) 4);
        this.response.appendString(NavigatorPublicFilter.name);
        this.response.appendInt((Integer) 0);
        this.response.appendString(NavigatorHotelFilter.name);
        this.response.appendInt((Integer) 0);
        this.response.appendString(NavigatorRoomAdsFilter.name);
        this.response.appendInt((Integer) 0);
        this.response.appendString(NavigatorUserFilter.name);
        this.response.appendInt((Integer) 0);
        return this.response;
    }
}
