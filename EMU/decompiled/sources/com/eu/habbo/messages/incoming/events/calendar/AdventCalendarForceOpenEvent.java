package com.eu.habbo.messages.incoming.events.calendar;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/events/calendar/AdventCalendarForceOpenEvent.class */
public class AdventCalendarForceOpenEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Emulator.getGameEnvironment().getCalendarManager().claimCalendarReward(this.client.getHabbo(), this.packet.readString(), this.packet.readInt().intValue(), true);
    }
}
