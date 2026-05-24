package com.eu.habbo.messages.incoming.hotelview;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.hotelview.HotelViewNextLTDAvailableComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/hotelview/HotelViewRequestLTDAvailabilityEvent.class */
public class HotelViewRequestLTDAvailabilityEvent extends MessageHandler {
    public static boolean ENABLED = false;
    public static int TIMESTAMP;
    public static int ITEM_ID;
    public static int PAGE_ID;
    public static String ITEM_NAME;

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (ENABLED) {
            int iMax = Math.max(TIMESTAMP - Emulator.getIntUnixTimestamp(), -1);
            this.client.sendResponse(new HotelViewNextLTDAvailableComposer(iMax, iMax > 0 ? -1 : ITEM_ID, iMax > 0 ? -1 : PAGE_ID, iMax > 0 ? Emulator.PREVIEW : ITEM_NAME));
        }
    }
}
