package com.eu.habbo.messages.outgoing.trading;

import com.eu.habbo.habbohotel.rooms.RoomTrade;
import com.eu.habbo.habbohotel.rooms.RoomTradeUser;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.Iterator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/trading/TradeStartComposer.class */
public class TradeStartComposer extends MessageComposer {
    private final RoomTrade roomTrade;
    private final int state;

    public TradeStartComposer(RoomTrade roomTrade) {
        this.roomTrade = roomTrade;
        this.state = 1;
    }

    public TradeStartComposer(RoomTrade roomTrade, int i) {
        this.roomTrade = roomTrade;
        this.state = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.TradeStartComposer);
        Iterator<RoomTradeUser> it = this.roomTrade.getRoomTradeUsers().iterator();
        while (it.hasNext()) {
            this.response.appendInt(Integer.valueOf(it.next().getHabbo().getHabboInfo().getId()));
            this.response.appendInt(Integer.valueOf(this.state));
        }
        return this.response;
    }
}
