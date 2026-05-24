package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/core/RoomUserPetComposer.class */
public class RoomUserPetComposer extends MessageComposer {
    private final int petType;
    private final int race;
    private final String color;
    private final Habbo habbo;

    public RoomUserPetComposer(int i, int i2, String str, Habbo habbo) {
        this.petType = i;
        this.race = i2;
        this.color = str;
        this.habbo = habbo;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomUsersComposer);
        this.response.appendInt((Integer) 1);
        this.response.appendInt(Integer.valueOf(this.habbo.getHabboInfo().getId()));
        this.response.appendString(this.habbo.getHabboInfo().getUsername());
        this.response.appendString(Emulator.PREVIEW);
        this.response.appendString(this.petType + " " + this.race + " " + this.color + " 2 2 -1 0 3 -1 0");
        this.response.appendInt(Integer.valueOf(this.habbo.getRoomUnit().getId()));
        this.response.appendInt(Short.valueOf(this.habbo.getRoomUnit().getX()));
        this.response.appendInt(Short.valueOf(this.habbo.getRoomUnit().getY()));
        this.response.appendString(this.habbo.getRoomUnit().getZ() + Emulator.PREVIEW);
        this.response.appendInt(Integer.valueOf(this.habbo.getRoomUnit().getBodyRotation().getValue()));
        this.response.appendInt((Integer) 2);
        this.response.appendInt(Integer.valueOf(this.petType));
        this.response.appendInt(Integer.valueOf(this.habbo.getHabboInfo().getId()));
        this.response.appendString(this.habbo.getHabboInfo().getUsername());
        this.response.appendInt((Integer) 1);
        this.response.appendBoolean(false);
        this.response.appendBoolean(true);
        this.response.appendBoolean(true);
        this.response.appendBoolean(true);
        this.response.appendBoolean(true);
        this.response.appendBoolean(true);
        this.response.appendInt((Integer) 0);
        this.response.appendString(Emulator.PREVIEW);
        return this.response;
    }
}
