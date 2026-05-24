package com.eu.habbo.messages.incoming.rooms.promotions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomPromotion;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.promotions.RoomPromotionMessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/promotions/UpdateRoomPromotionEvent.class */
public class UpdateRoomPromotionEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        RoomPromotion promotion;
        int iIntValue = this.packet.readInt().intValue();
        String string = this.packet.readString();
        String string2 = this.packet.readString();
        Room roomLoadRoom = Emulator.getGameEnvironment().getRoomManager().loadRoom(iIntValue);
        if (roomLoadRoom == null || roomLoadRoom.getOwnerId() != this.client.getHabbo().getHabboInfo().getId() || !this.client.getHabbo().hasPermission(Permission.ACC_ANYROOMOWNER) || (promotion = roomLoadRoom.getPromotion()) == null) {
            return;
        }
        promotion.setTitle(string);
        promotion.setDescription(string2);
        promotion.needsUpdate = true;
        promotion.save();
        roomLoadRoom.sendComposer(new RoomPromotionMessageComposer(roomLoadRoom, promotion).compose());
    }
}
