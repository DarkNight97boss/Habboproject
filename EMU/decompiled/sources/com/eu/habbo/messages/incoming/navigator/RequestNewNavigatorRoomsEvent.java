package com.eu.habbo.messages.incoming.navigator;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.navigation.DisplayMode;
import com.eu.habbo.habbohotel.navigation.DisplayOrder;
import com.eu.habbo.habbohotel.navigation.ListMode;
import com.eu.habbo.habbohotel.navigation.NavigatorFilter;
import com.eu.habbo.habbohotel.navigation.NavigatorFilterField;
import com.eu.habbo.habbohotel.navigation.NavigatorHotelFilter;
import com.eu.habbo.habbohotel.navigation.SearchAction;
import com.eu.habbo.habbohotel.navigation.SearchResultList;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomCategory;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.navigator.NewNavigatorSearchResultsComposer;
import gnu.trove.map.hash.THashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/navigator/RequestNewNavigatorRoomsEvent.class */
public class RequestNewNavigatorRoomsEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestNewNavigatorRoomsEvent.class);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        List<Room> roomsForCategory;
        String string = this.packet.readString();
        String string2 = this.packet.readString();
        if (string.equals("query")) {
            string = NavigatorHotelFilter.name;
        }
        if (string.equals("groups")) {
            string = NavigatorHotelFilter.name;
        }
        NavigatorFilter navigatorFilter = (NavigatorFilter) Emulator.getGameEnvironment().getNavigatorManager().filters.get(string);
        RoomCategory categoryBySafeCaption = Emulator.getGameEnvironment().getRoomManager().getCategoryBySafeCaption(string);
        if (navigatorFilter == null && (roomsForCategory = Emulator.getGameEnvironment().getNavigatorManager().getRoomsForCategory(string, this.client.getHabbo())) != null) {
            ArrayList arrayList = new ArrayList();
            arrayList.add(new SearchResultList(0, string, string2, SearchAction.NONE, this.client.getHabbo().getHabboStats().navigatorWindowSettings.getListModeForCategory(string, ListMode.LIST), this.client.getHabbo().getHabboStats().navigatorWindowSettings.getDisplayModeForCategory(string, DisplayMode.VISIBLE), roomsForCategory, true, true, DisplayOrder.ACTIVITY, -1));
            this.client.sendResponse(new NewNavigatorSearchResultsComposer(string, string2, arrayList));
            return;
        }
        String strReplace = "anything";
        String str = string2;
        NavigatorFilterField navigatorFilterField = Emulator.getGameEnvironment().getNavigatorManager().filterSettings.get(strReplace);
        if (navigatorFilter != null) {
            if (string2.contains(":")) {
                String[] strArrSplit = string2.split(":");
                if (strArrSplit.length > 1) {
                    strReplace = strArrSplit[0];
                    str = strArrSplit[1];
                } else {
                    strReplace = strArrSplit[0].replace(":", Emulator.PREVIEW);
                    if (!Emulator.getGameEnvironment().getNavigatorManager().filterSettings.containsKey(strReplace)) {
                        strReplace = "anything";
                    }
                }
            }
            if (Emulator.getGameEnvironment().getNavigatorManager().filterSettings.get(strReplace) != null) {
                navigatorFilterField = Emulator.getGameEnvironment().getNavigatorManager().filterSettings.get(strReplace);
            }
        }
        if (navigatorFilterField == null || string2.isEmpty()) {
            if (navigatorFilter == null) {
                return;
            }
            List<SearchResultList> result = navigatorFilter.getResult(this.client.getHabbo());
            Collections.sort(result);
            if (!string2.isEmpty()) {
                result = toQueryResults(result);
            }
            this.client.sendResponse(new NewNavigatorSearchResultsComposer(string, string2, result));
            return;
        }
        if (navigatorFilter == null) {
            navigatorFilter = (NavigatorFilter) Emulator.getGameEnvironment().getNavigatorManager().filters.get(NavigatorHotelFilter.name);
        }
        if (categoryBySafeCaption == null) {
            categoryBySafeCaption = Emulator.getGameEnvironment().getRoomManager().getCategoryBySafeCaption(NavigatorHotelFilter.name);
        }
        if (navigatorFilter == null) {
            return;
        }
        try {
            List<SearchResultList> result2 = navigatorFilter.getResult(this.client.getHabbo(), navigatorFilterField, str, categoryBySafeCaption != null ? categoryBySafeCaption.getId() : -1);
            ArrayList arrayList2 = new ArrayList();
            for (SearchResultList searchResultList : result2) {
                ArrayList arrayList3 = new ArrayList();
                arrayList3.addAll(searchResultList.rooms);
                arrayList2.add(new SearchResultList(searchResultList.order, searchResultList.code, searchResultList.query, searchResultList.action, searchResultList.mode, searchResultList.hidden, arrayList3, searchResultList.filter, searchResultList.showInvisible, searchResultList.displayOrder, searchResultList.categoryOrder));
            }
            navigatorFilter.filter(navigatorFilterField.field, str, arrayList2);
            this.client.sendResponse(new NewNavigatorSearchResultsComposer(string, string2, toQueryResults(arrayList2)));
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
        }
    }

    private ArrayList<SearchResultList> toQueryResults(List<SearchResultList> list) {
        ArrayList<SearchResultList> arrayList = new ArrayList<>();
        THashMap tHashMap = new THashMap();
        Iterator<SearchResultList> it = list.iterator();
        while (it.hasNext()) {
            for (Room room : it.next().rooms) {
                tHashMap.put(Integer.valueOf(room.getId()), room);
            }
        }
        arrayList.add(new SearchResultList(0, "query", Emulator.PREVIEW, SearchAction.NONE, ListMode.LIST, DisplayMode.VISIBLE, new ArrayList(tHashMap.values()), true, this.client.getHabbo().hasPermission(Permission.ACC_ENTERANYROOM) || this.client.getHabbo().hasPermission(Permission.ACC_ANYROOMOWNER), DisplayOrder.ACTIVITY, -1));
        return arrayList;
    }

    private void filter(List<SearchResultList> list, NavigatorFilter navigatorFilter, String str) {
        ArrayList arrayList = new ArrayList();
        HashMap map = new HashMap();
        for (NavigatorFilterField navigatorFilterField : Emulator.getGameEnvironment().getNavigatorManager().filterSettings.values()) {
            for (SearchResultList searchResultList : list) {
                if (searchResultList.filter) {
                    ArrayList arrayList2 = new ArrayList(searchResultList.rooms.subList(0, searchResultList.rooms.size()));
                    navigatorFilter.filterRooms(navigatorFilterField.field, str, arrayList2);
                    if (!map.containsKey(Integer.valueOf(searchResultList.order))) {
                        map.put(Integer.valueOf(searchResultList.order), new HashMap());
                    }
                    for (Room room : arrayList2) {
                        ((HashMap) map.get(Integer.valueOf(searchResultList.order))).put(Integer.valueOf(room.getId()), room);
                    }
                }
            }
        }
        for (Map.Entry entry : map.entrySet()) {
            for (SearchResultList searchResultList2 : list) {
                if (searchResultList2.filter) {
                    searchResultList2.rooms.clear();
                    searchResultList2.rooms.addAll(((HashMap) entry.getValue()).values());
                    if (searchResultList2.rooms.isEmpty()) {
                        arrayList.add(searchResultList2);
                    }
                }
            }
        }
        list.removeAll(arrayList);
    }
}
