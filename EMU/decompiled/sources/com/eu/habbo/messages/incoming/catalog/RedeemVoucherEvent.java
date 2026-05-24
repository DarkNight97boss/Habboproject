package com.eu.habbo.messages.incoming.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.catalog.RedeemVoucherErrorComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.HotelWillCloseInMinutesComposer;
import com.eu.habbo.threading.runnables.ShutdownEmulator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/catalog/RedeemVoucherEvent.class */
public class RedeemVoucherEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (ShutdownEmulator.timestamp > 0) {
            this.client.sendResponse(new HotelWillCloseInMinutesComposer((ShutdownEmulator.timestamp - Emulator.getIntUnixTimestamp()) / 60));
            return;
        }
        String string = this.packet.readString();
        if (string.contains(" ")) {
            this.client.sendResponse(new RedeemVoucherErrorComposer(1));
        } else {
            Emulator.getGameEnvironment().getCatalogManager().redeemVoucher(this.client, string);
        }
    }
}
