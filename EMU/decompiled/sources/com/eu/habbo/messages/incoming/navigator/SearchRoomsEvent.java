package com.eu.habbo.messages.incoming.navigator;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.Rank;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.navigator.PrivateRoomsComposer;
import com.eu.habbo.plugin.events.navigator.NavigatorSearchResultEvent;
import gnu.trove.map.hash.THashMap;
import java.util.ArrayList;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/navigator/SearchRoomsEvent.class */
public class SearchRoomsEvent extends MessageHandler {
    public static final THashMap<Rank, THashMap<String, ServerMessage>> cachedResults = new THashMap<>(4);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        ArrayList<Room> roomsWithName;
        String string = this.packet.readString();
        String str = Emulator.PREVIEW;
        String str2 = string;
        ServerMessage serverMessageCompose = null;
        if (cachedResults.containsKey(this.client.getHabbo().getHabboInfo().getRank())) {
            serverMessageCompose = (ServerMessage) ((THashMap) cachedResults.get(this.client.getHabbo().getHabboInfo().getRank())).get((string + "\t" + str2).toLowerCase());
        } else {
            cachedResults.put(this.client.getHabbo().getHabboInfo().getRank(), new THashMap());
        }
        if (serverMessageCompose == null) {
            if (string.startsWith("owner:")) {
                str2 = string.split("owner:")[1];
                str = "owner:";
                roomsWithName = (ArrayList) Emulator.getGameEnvironment().getRoomManager().getRoomsForHabbo(string);
            } else if (string.startsWith("tag:")) {
                str2 = string.split("tag:")[1];
                str = "tag:";
                roomsWithName = Emulator.getGameEnvironment().getRoomManager().getRoomsWithTag(string);
            } else if (string.startsWith("group:")) {
                str2 = string.split("group:")[1];
                str = "group:";
                roomsWithName = Emulator.getGameEnvironment().getRoomManager().getGroupRoomsWithName(string);
            } else {
                roomsWithName = Emulator.getGameEnvironment().getRoomManager().getRoomsWithName(string);
            }
            serverMessageCompose = new PrivateRoomsComposer(roomsWithName).compose();
            THashMap tHashMap = (THashMap) cachedResults.get(this.client.getHabbo().getHabboInfo().getRank());
            if (tHashMap == null) {
                tHashMap = new THashMap(1);
            }
            tHashMap.put((string + "\t" + str2).toLowerCase(), serverMessageCompose);
            cachedResults.put(this.client.getHabbo().getHabboInfo().getRank(), tHashMap);
            if (((NavigatorSearchResultEvent) Emulator.getPluginManager().fireEvent(new NavigatorSearchResultEvent(this.client.getHabbo(), str, str2, roomsWithName))).isCancelled()) {
                return;
            }
        }
        this.client.sendResponse(serverMessageCompose);
    }
}
