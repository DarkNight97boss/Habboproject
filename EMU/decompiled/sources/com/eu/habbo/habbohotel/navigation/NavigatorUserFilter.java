package com.eu.habbo.habbohotel.navigation;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/navigation/NavigatorUserFilter.class */
public class NavigatorUserFilter extends NavigatorFilter {
    public static final String name = "myworld_view";

    public NavigatorUserFilter() {
        super(name);
    }

    @Override // com.eu.habbo.habbohotel.navigation.NavigatorFilter
    public List<SearchResultList> getResult(Habbo habbo) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(new SearchResultList(0, "my", Emulator.PREVIEW, SearchAction.NONE, habbo.getHabboStats().navigatorWindowSettings.getListModeForCategory("my"), habbo.getHabboStats().navigatorWindowSettings.getDisplayModeForCategory("my"), Emulator.getGameEnvironment().getNavigatorManager().getRoomsForCategory("my", habbo), true, true, DisplayOrder.ORDER_NUM, 0));
        int i = 0 + 1;
        List<Room> roomsForCategory = Emulator.getGameEnvironment().getNavigatorManager().getRoomsForCategory(NavigatorFavoriteFilter.name, habbo);
        if (!roomsForCategory.isEmpty()) {
            arrayList.add(new SearchResultList(i, NavigatorFavoriteFilter.name, Emulator.PREVIEW, SearchAction.NONE, habbo.getHabboStats().navigatorWindowSettings.getListModeForCategory(NavigatorFavoriteFilter.name), habbo.getHabboStats().navigatorWindowSettings.getDisplayModeForCategory(NavigatorFavoriteFilter.name), roomsForCategory, true, true, DisplayOrder.ORDER_NUM, i));
            i++;
        }
        List<Room> roomsForCategory2 = Emulator.getGameEnvironment().getNavigatorManager().getRoomsForCategory("history_freq", habbo);
        if (!roomsForCategory2.isEmpty()) {
            arrayList.add(new SearchResultList(i, "history_freq", Emulator.PREVIEW, SearchAction.NONE, habbo.getHabboStats().navigatorWindowSettings.getListModeForCategory("history_freq"), habbo.getHabboStats().navigatorWindowSettings.getDisplayModeForCategory("history_freq"), roomsForCategory2, true, true, DisplayOrder.ORDER_NUM, i));
            i++;
        }
        List<Room> roomsForCategory3 = Emulator.getGameEnvironment().getNavigatorManager().getRoomsForCategory("my_groups", habbo);
        if (!roomsForCategory3.isEmpty()) {
            arrayList.add(new SearchResultList(i, "my_groups", Emulator.PREVIEW, SearchAction.NONE, habbo.getHabboStats().navigatorWindowSettings.getListModeForCategory("my_groups"), habbo.getHabboStats().navigatorWindowSettings.getDisplayModeForCategory("my_groups"), roomsForCategory3, true, true, DisplayOrder.ORDER_NUM, i));
            i++;
        }
        List<Room> roomsForCategory4 = Emulator.getGameEnvironment().getNavigatorManager().getRoomsForCategory("with_friends", habbo);
        if (!roomsForCategory4.isEmpty()) {
            arrayList.add(new SearchResultList(i, "with_friends", Emulator.PREVIEW, SearchAction.NONE, habbo.getHabboStats().navigatorWindowSettings.getListModeForCategory("with_friends"), habbo.getHabboStats().navigatorWindowSettings.getDisplayModeForCategory("with_friends"), roomsForCategory4, true, true, DisplayOrder.ORDER_NUM, i));
            i++;
        }
        List<Room> roomsForCategory5 = Emulator.getGameEnvironment().getNavigatorManager().getRoomsForCategory("with_rights", habbo);
        if (!roomsForCategory5.isEmpty()) {
            arrayList.add(new SearchResultList(i, "with_rights", Emulator.PREVIEW, SearchAction.NONE, habbo.getHabboStats().navigatorWindowSettings.getListModeForCategory("with_rights"), habbo.getHabboStats().navigatorWindowSettings.getDisplayModeForCategory("with_rights"), roomsForCategory5, true, true, DisplayOrder.ORDER_NUM, i));
        }
        return arrayList;
    }
}
