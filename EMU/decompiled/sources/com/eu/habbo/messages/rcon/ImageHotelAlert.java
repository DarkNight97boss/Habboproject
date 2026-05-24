package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.google.gson.Gson;
import gnu.trove.map.hash.THashMap;
import java.util.Iterator;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/ImageHotelAlert.class */
public class ImageHotelAlert extends RCONMessage<JSON> {

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/ImageHotelAlert$JSON.class */
    static class JSON {
        public String bubble_key = Emulator.PREVIEW;
        public String message = Emulator.PREVIEW;
        public String url = Emulator.PREVIEW;
        public String url_message = Emulator.PREVIEW;
        public String title = Emulator.PREVIEW;
        public String display_type = Emulator.PREVIEW;
        public String image = Emulator.PREVIEW;

        JSON() {
        }
    }

    public ImageHotelAlert() {
        super(JSON.class);
    }

    @Override // com.eu.habbo.messages.rcon.RCONMessage
    public void handle(Gson gson, JSON json) {
        THashMap tHashMap = new THashMap();
        if (!json.message.isEmpty()) {
            tHashMap.put("message", json.message);
        }
        if (!json.url.isEmpty()) {
            tHashMap.put("linkUrl", json.url);
        }
        if (!json.url_message.isEmpty()) {
            tHashMap.put("linkTitle", json.url_message);
        }
        if (!json.title.isEmpty()) {
            tHashMap.put("title", json.title);
        }
        if (!json.display_type.isEmpty()) {
            tHashMap.put("display", json.display_type);
        }
        if (!json.image.isEmpty()) {
            tHashMap.put("image", json.image);
        }
        ServerMessage serverMessageCompose = new BubbleAlertComposer(json.bubble_key, (THashMap<String, String>) tHashMap).compose();
        Iterator<Map.Entry<Integer, Habbo>> it = Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().entrySet().iterator();
        while (it.hasNext()) {
            Habbo value = it.next().getValue();
            if (!value.getHabboStats().blockStaffAlerts) {
                value.getClient().sendResponse(serverMessageCompose);
            }
        }
    }
}
