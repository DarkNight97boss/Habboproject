package com.eu.habbo.messages.incoming.hotelview;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.hotelview.HotelViewSecondsUntilComposer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/hotelview/HotelViewRequestSecondsUntilEvent.class */
public class HotelViewRequestSecondsUntilEvent extends MessageHandler {
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm");

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        String string = this.packet.readString();
        this.client.sendResponse(new HotelViewSecondsUntilComposer(string, Math.max(0, ((int) (dateFormat.parse(string).getTime() / 1000)) - Emulator.getIntUnixTimestamp())));
    }
}
