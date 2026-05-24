package com.eu.habbo.plugin.events.trading;

import com.eu.habbo.habbohotel.rooms.RoomTradeUser;
import com.eu.habbo.plugin.Event;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/trading/TradeConfirmEvent.class */
public class TradeConfirmEvent extends Event {
    public final RoomTradeUser userOne;
    public final RoomTradeUser userTwo;

    public TradeConfirmEvent(RoomTradeUser roomTradeUser, RoomTradeUser roomTradeUser2) {
        this.userOne = roomTradeUser;
        this.userTwo = roomTradeUser2;
    }
}
