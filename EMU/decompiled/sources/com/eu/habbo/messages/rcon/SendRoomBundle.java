package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.CatalogPage;
import com.eu.habbo.habbohotel.catalog.layouts.RoomBundleLayout;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboManager;
import com.google.gson.Gson;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/SendRoomBundle.class */
public class SendRoomBundle extends RCONMessage<JSON> {

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/SendRoomBundle$JSON.class */
    static class JSON {
        public int user_id;
        public int catalog_page;

        JSON() {
        }
    }

    public SendRoomBundle() {
        super(JSON.class);
    }

    @Override // com.eu.habbo.messages.rcon.RCONMessage
    public void handle(Gson gson, JSON json) {
        if (json.catalog_page <= 0 || json.user_id <= 0) {
            return;
        }
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(json.user_id);
        CatalogPage catalogPage = Emulator.getGameEnvironment().getCatalogManager().getCatalogPage(json.catalog_page);
        if (catalogPage instanceof RoomBundleLayout) {
            if (habbo != null) {
                ((RoomBundleLayout) catalogPage).buyRoom(habbo);
                return;
            }
            HabboInfo offlineHabboInfo = HabboManager.getOfflineHabboInfo(json.user_id);
            if (offlineHabboInfo != null) {
                ((RoomBundleLayout) catalogPage).buyRoom(null, json.user_id, offlineHabboInfo.getUsername());
            }
        }
    }
}
