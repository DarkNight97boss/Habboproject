package com.eu.habbo.messages.outgoing.rooms.promotions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomPromotion;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/promotions/RoomPromotionMessageComposer.class */
public class RoomPromotionMessageComposer extends MessageComposer {
    private final Room room;
    private final RoomPromotion roomPromotion;

    public RoomPromotionMessageComposer(Room room, RoomPromotion roomPromotion) {
        this.room = room;
        this.roomPromotion = roomPromotion;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(1840);
        if (this.room == null || this.roomPromotion == null) {
            this.response.appendInt((Integer) (-1));
            this.response.appendInt((Integer) (-1));
            this.response.appendString(Emulator.PREVIEW);
            this.response.appendInt((Integer) 0);
            this.response.appendInt((Integer) 0);
            this.response.appendString(Emulator.PREVIEW);
            this.response.appendString(Emulator.PREVIEW);
            this.response.appendInt((Integer) 0);
            this.response.appendInt((Integer) 0);
            this.response.appendInt((Integer) 0);
        } else {
            this.response.appendInt(Integer.valueOf(this.room.getId()));
            this.response.appendInt(Integer.valueOf(this.room.getOwnerId()));
            this.response.appendString(this.room.getOwnerName());
            this.response.appendInt(Integer.valueOf(this.room.getId()));
            this.response.appendInt((Integer) 1);
            this.response.appendString(this.roomPromotion.getTitle());
            this.response.appendString(this.roomPromotion.getDescription());
            this.response.appendInt(Integer.valueOf((Emulator.getIntUnixTimestamp() - this.roomPromotion.getStartTimestamp()) / 60));
            this.response.appendInt(Integer.valueOf((this.roomPromotion.getEndTimestamp() - Emulator.getIntUnixTimestamp()) / 60));
            this.response.appendInt(Integer.valueOf(this.roomPromotion.getCategory()));
        }
        return this.response;
    }
}
