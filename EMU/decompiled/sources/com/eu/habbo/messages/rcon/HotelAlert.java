package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.generic.alerts.GenericAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.StaffAlertWithLinkComposer;
import com.google.gson.Gson;
import java.util.Iterator;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/HotelAlert.class */
public class HotelAlert extends RCONMessage<JSONHotelAlert> {

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/HotelAlert$JSONHotelAlert.class */
    static class JSONHotelAlert {
        public String message;
        public String url = Emulator.PREVIEW;

        JSONHotelAlert() {
        }
    }

    public HotelAlert() {
        super(JSONHotelAlert.class);
    }

    @Override // com.eu.habbo.messages.rcon.RCONMessage
    public void handle(Gson gson, JSONHotelAlert jSONHotelAlert) {
        ServerMessage serverMessageCompose = jSONHotelAlert.url.isEmpty() ? new GenericAlertComposer(jSONHotelAlert.message).compose() : new StaffAlertWithLinkComposer(jSONHotelAlert.message, jSONHotelAlert.url).compose();
        if (serverMessageCompose != null) {
            Iterator<Map.Entry<Integer, Habbo>> it = Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().entrySet().iterator();
            while (it.hasNext()) {
                Habbo value = it.next().getValue();
                if (!value.getHabboStats().blockStaffAlerts) {
                    value.getClient().sendResponse(serverMessageCompose);
                }
            }
        }
    }
}
