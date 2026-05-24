package com.eu.habbo.habbohotel.navigation;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.users.Habbo;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/navigation/NavigatorRoomAdsFilter.class */
public class NavigatorRoomAdsFilter extends NavigatorFilter {
    public static final String name = "roomads_view";

    public NavigatorRoomAdsFilter() {
        super(name);
    }

    @Override // com.eu.habbo.habbohotel.navigation.NavigatorFilter
    public List<SearchResultList> getResult(Habbo habbo) {
        boolean z = habbo.hasPermission(Permission.ACC_ENTERANYROOM) || habbo.hasPermission(Permission.ACC_ANYROOMOWNER);
        ArrayList arrayList = new ArrayList();
        arrayList.add(new SearchResultList(0, "categories", Emulator.PREVIEW, SearchAction.NONE, habbo.getHabboStats().navigatorWindowSettings.getListModeForCategory("categories", ListMode.LIST), habbo.getHabboStats().navigatorWindowSettings.getDisplayModeForCategory("official-root", DisplayMode.VISIBLE), Emulator.getGameEnvironment().getNavigatorManager().getRoomsForCategory("categories", habbo), false, z, DisplayOrder.ACTIVITY, 0));
        return arrayList;
    }
}
