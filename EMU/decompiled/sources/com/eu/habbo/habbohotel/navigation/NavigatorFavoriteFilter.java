package com.eu.habbo.habbohotel.navigation;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/navigation/NavigatorFavoriteFilter.class */
public class NavigatorFavoriteFilter extends NavigatorFilter {
    public static final String name = "favorites";

    public NavigatorFavoriteFilter() {
        super(name);
    }

    @Override // com.eu.habbo.habbohotel.navigation.NavigatorFilter
    public List<SearchResultList> getResult(Habbo habbo) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(new SearchResultList(0, name, Emulator.PREVIEW, SearchAction.NONE, habbo.getHabboStats().navigatorWindowSettings.getListModeForCategory(name, ListMode.LIST), habbo.getHabboStats().navigatorWindowSettings.getDisplayModeForCategory("popular", DisplayMode.VISIBLE), Emulator.getGameEnvironment().getNavigatorManager().getRoomsForCategory(name, habbo), true, true, DisplayOrder.ACTIVITY, -1));
        return arrayList;
    }
}
