package com.eu.habbo.messages.outgoing.trading;

import com.eu.habbo.habbohotel.items.FurnitureType;
import com.eu.habbo.habbohotel.rooms.RoomTrade;
import com.eu.habbo.habbohotel.rooms.RoomTradeUser;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.iterator.hash.TObjectHashIterator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/trading/TradeUpdateComposer.class */
public class TradeUpdateComposer extends MessageComposer {
    private final RoomTrade roomTrade;

    public TradeUpdateComposer(RoomTrade roomTrade) {
        this.roomTrade = roomTrade;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.TradeUpdateComposer);
        for (RoomTradeUser roomTradeUser : this.roomTrade.getRoomTradeUsers()) {
            this.response.appendInt(Integer.valueOf(roomTradeUser.getUserId()));
            this.response.appendInt(Integer.valueOf(roomTradeUser.getItems().size()));
            TObjectHashIterator it = roomTradeUser.getItems().iterator();
            while (it.hasNext()) {
                HabboItem habboItem = (HabboItem) it.next();
                this.response.appendInt(Integer.valueOf(habboItem.getId()));
                this.response.appendString(habboItem.getBaseItem().getType().code);
                this.response.appendInt(Integer.valueOf(habboItem.getId()));
                this.response.appendInt(Integer.valueOf(habboItem.getBaseItem().getSpriteId()));
                this.response.appendInt((Integer) 0);
                this.response.appendBoolean(Boolean.valueOf(habboItem.getBaseItem().allowInventoryStack() && !habboItem.isLimited()));
                habboItem.serializeExtradata(this.response);
                this.response.appendInt((Integer) 0);
                this.response.appendInt((Integer) 0);
                this.response.appendInt((Integer) 0);
                if (habboItem.getBaseItem().getType() == FurnitureType.FLOOR) {
                    this.response.appendInt((Integer) 0);
                }
            }
            this.response.appendInt(Integer.valueOf(roomTradeUser.getItems().size()));
            this.response.appendInt(Integer.valueOf(roomTradeUser.getItems().stream().mapToInt(RoomTrade::getCreditsByItem).sum()));
        }
        return this.response;
    }
}
