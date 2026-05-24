package com.eu.habbo.messages.incoming.trading;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTrade;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.trading.TradeStartFailComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/trading/TradeStartEvent.class */
public class TradeStartEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (((long) Emulator.getIntUnixTimestamp()) - this.client.getHabbo().getHabboStats().lastTradeTimestamp > 10) {
            this.client.getHabbo().getHabboStats().lastTradeTimestamp = Emulator.getIntUnixTimestamp();
            int iIntValue = this.packet.readInt().intValue();
            Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
            if (currentRoom == null || iIntValue < 0 || iIntValue == this.client.getHabbo().getRoomUnit().getId()) {
                return;
            }
            Habbo habboByRoomUnitId = currentRoom.getHabboByRoomUnitId(iIntValue);
            boolean zHasPermission = this.client.getHabbo().hasPermission(Permission.ACC_TRADE_ANYWHERE);
            if (!RoomTrade.TRADING_ENABLED && !zHasPermission) {
                this.client.sendResponse(new TradeStartFailComposer(1));
                return;
            }
            if ((currentRoom.getTradeMode() == 0 || (currentRoom.getTradeMode() == 1 && this.client.getHabbo().getHabboInfo().getId() != currentRoom.getOwnerId())) && !zHasPermission) {
                this.client.sendResponse(new TradeStartFailComposer(6));
                return;
            }
            if (habboByRoomUnitId == null || habboByRoomUnitId.getHabboStats().userIgnored(this.client.getHabbo().getHabboInfo().getId())) {
                return;
            }
            if (this.client.getHabbo().getRoomUnit().hasStatus(RoomUnitStatus.TRADING)) {
                this.client.sendResponse(new TradeStartFailComposer(7));
                return;
            }
            if (!this.client.getHabbo().getHabboStats().allowTrade()) {
                this.client.sendResponse(new TradeStartFailComposer(2));
                return;
            }
            if (habboByRoomUnitId.getRoomUnit().hasStatus(RoomUnitStatus.TRADING)) {
                this.client.sendResponse(new TradeStartFailComposer(8, habboByRoomUnitId.getHabboInfo().getUsername()));
            } else if (habboByRoomUnitId.getHabboStats().allowTrade()) {
                currentRoom.startTrade(this.client.getHabbo(), habboByRoomUnitId);
            } else {
                this.client.sendResponse(new TradeStartFailComposer(4, habboByRoomUnitId.getHabboInfo().getUsername()));
            }
        }
    }
}
