package com.eu.habbo.messages.outgoing.rooms.items.rentablespaces;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionRentableSpace;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/items/rentablespaces/RentableSpaceInfoComposer.class */
public class RentableSpaceInfoComposer extends MessageComposer {
    public static final int SPACE_ALREADY_RENTED = 100;
    public static final int SPACE_EXTEND_NOT_RENTED = 101;
    public static final int SPACE_EXTEND_NOT_RENTED_BY_YOU = 102;
    public static final int CAN_RENT_ONLY_ONE_SPACE = 103;
    public static final int NOT_ENOUGH_CREDITS = 200;
    public static final int NOT_ENOUGH_PIXELS = 201;
    public static final int CANT_RENT_NO_PERMISSION = 202;
    public static final int CANT_RENT_NO_HABBO_CLUB = 203;
    public static final int CANT_RENT = 300;
    public static final int CANT_RENT_GENERIC = 400;
    private final Habbo habbo;
    private final HabboItem item;
    private final int errorCode;

    public RentableSpaceInfoComposer(Habbo habbo, HabboItem habboItem) {
        this.habbo = habbo;
        this.item = habboItem;
        this.errorCode = 0;
    }

    public RentableSpaceInfoComposer(Habbo habbo, HabboItem habboItem, int i) {
        this.habbo = habbo;
        this.item = habboItem;
        this.errorCode = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        if (!(this.item instanceof InteractionRentableSpace)) {
            return null;
        }
        this.response.init(3559);
        this.response.appendBoolean(Boolean.valueOf(((InteractionRentableSpace) this.item).isRented()));
        this.response.appendInt(Integer.valueOf(this.errorCode));
        this.response.appendInt(Integer.valueOf(((InteractionRentableSpace) this.item).getRenterId()));
        this.response.appendString(((InteractionRentableSpace) this.item).getRenterName());
        this.response.appendInt(Integer.valueOf(((InteractionRentableSpace) this.item).getEndTimestamp() - Emulator.getIntUnixTimestamp()));
        this.response.appendInt(Integer.valueOf(((InteractionRentableSpace) this.item).rentCost()));
        return this.response;
    }
}
