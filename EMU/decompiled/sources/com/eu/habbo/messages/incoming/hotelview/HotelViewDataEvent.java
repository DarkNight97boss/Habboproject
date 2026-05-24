package com.eu.habbo.messages.incoming.hotelview;

import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.hotelview.HotelViewDataComposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/hotelview/HotelViewDataEvent.class */
public class HotelViewDataEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(HotelViewDataEvent.class);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() {
        try {
            String string = this.packet.readString();
            if (string.contains(";")) {
                String[] strArrSplit = string.split(";");
                if (0 < strArrSplit.length) {
                    String str = strArrSplit[0];
                    if (str.contains(",")) {
                        this.client.sendResponse(new HotelViewDataComposer(str, str.split(",")[str.split(",").length - 1]));
                    } else {
                        this.client.sendResponse(new HotelViewDataComposer(string, str));
                    }
                }
            } else {
                this.client.sendResponse(new HotelViewDataComposer(string, string.split(",")[string.split(",").length - 1]));
            }
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
        }
    }
}
