package com.eu.habbo.messages.outgoing.hotelview;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/hotelview/HotelViewConcurrentUsersComposer.class */
public class HotelViewConcurrentUsersComposer extends MessageComposer {
    public static final int ACTIVE = 0;
    public static final int HIDDEN = 2;
    public static final int ACHIEVED = 3;
    private final int state;
    private final int userCount;
    private final int goal;

    public HotelViewConcurrentUsersComposer(int i, int i2, int i3) {
        this.state = i;
        this.userCount = i2;
        this.goal = i3;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.HotelViewConcurrentUsersComposer);
        this.response.appendInt(Integer.valueOf(this.state));
        this.response.appendInt(Integer.valueOf(this.userCount));
        this.response.appendInt(Integer.valueOf(this.goal));
        return this.response;
    }
}
