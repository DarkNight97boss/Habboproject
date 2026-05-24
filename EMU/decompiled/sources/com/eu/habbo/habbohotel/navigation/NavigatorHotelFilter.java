package com.eu.habbo.habbohotel.navigation;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomCategory;
import com.eu.habbo.habbohotel.users.Habbo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/navigation/NavigatorHotelFilter.class */
public class NavigatorHotelFilter extends NavigatorFilter {
    public static final String name = "hotel_view";

    public NavigatorHotelFilter() {
        super(name);
    }

    @Override // com.eu.habbo.habbohotel.navigation.NavigatorFilter
    public List<SearchResultList> getResult(Habbo habbo) {
        boolean z = habbo.hasPermission(Permission.ACC_ENTERANYROOM) || habbo.hasPermission(Permission.ACC_ANYROOMOWNER);
        ArrayList arrayList = new ArrayList();
        arrayList.add(new SearchResultList(0, "popular", Emulator.PREVIEW, SearchAction.NONE, habbo.getHabboStats().navigatorWindowSettings.getListModeForCategory("popular", ListMode.fromType(Emulator.getConfig().getInt("hotel.navigator.popular.listtype"))), habbo.getHabboStats().navigatorWindowSettings.getDisplayModeForCategory("popular"), Emulator.getGameEnvironment().getNavigatorManager().getRoomsForCategory("popular", habbo), false, z, DisplayOrder.ORDER_NUM, -1));
        int i = 0 + 1;
        for (Map.Entry<Integer, List<Room>> entry : Emulator.getGameEnvironment().getRoomManager().getPopularRoomsByCategory(Emulator.getConfig().getInt("hotel.navigator.popular.category.maxresults")).entrySet()) {
            if (!entry.getValue().isEmpty()) {
                RoomCategory category = Emulator.getGameEnvironment().getRoomManager().getCategory(entry.getKey().intValue());
                if (category != null) {
                    arrayList.add(new SearchResultList(i, category.getCaption(), category.getCaption(), SearchAction.MORE, habbo.getHabboStats().navigatorWindowSettings.getListModeForCategory(category.getCaptionSave()), habbo.getHabboStats().navigatorWindowSettings.getDisplayModeForCategory(category.getCaptionSave()), entry.getValue(), true, z, DisplayOrder.ORDER_NUM, category.getOrder()));
                }
                i++;
            }
        }
        return arrayList;
    }

    @Override // com.eu.habbo.habbohotel.navigation.NavigatorFilter
    public List<SearchResultList> getResult(Habbo habbo, NavigatorFilterField navigatorFilterField, String str, int i) {
        boolean z = habbo.hasPermission(Permission.ACC_ENTERANYROOM) || habbo.hasPermission(Permission.ACC_ANYROOMOWNER);
        if (navigatorFilterField.databaseQuery.isEmpty()) {
            return getResult(habbo);
        }
        ArrayList arrayList = new ArrayList();
        int i2 = 0;
        for (Map.Entry entry : Emulator.getGameEnvironment().getRoomManager().findRooms(navigatorFilterField, str, i, z).entrySet()) {
            if (!((List) entry.getValue()).isEmpty()) {
                RoomCategory category = Emulator.getGameEnvironment().getRoomManager().getCategory(((Integer) entry.getKey()).intValue());
                if (category != null) {
                    arrayList.add(new SearchResultList(i2, category.getCaptionSave(), category.getCaption(), SearchAction.MORE, habbo.getHabboStats().navigatorWindowSettings.getListModeForCategory(category.getCaptionSave()), habbo.getHabboStats().navigatorWindowSettings.getDisplayModeForCategory(category.getCaptionSave()), (List) entry.getValue(), true, z, DisplayOrder.ACTIVITY, category.getOrder()));
                }
                i2++;
            }
        }
        return arrayList;
    }
}
