package com.eu.habbo.habbohotel.navigation;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.users.Habbo;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/navigation/NavigatorPublicFilter.class */
public class NavigatorPublicFilter extends NavigatorFilter {
    public static final String name = "official_view";

    public NavigatorPublicFilter() {
        super(name);
    }

    @Override // com.eu.habbo.habbohotel.navigation.NavigatorFilter
    public List<SearchResultList> getResult(Habbo habbo) {
        boolean z = habbo.hasPermission(Permission.ACC_ENTERANYROOM) || habbo.hasPermission(Permission.ACC_ANYROOMOWNER);
        ArrayList arrayList = new ArrayList();
        arrayList.add(new SearchResultList(0, "official-root", Emulator.PREVIEW, SearchAction.NONE, habbo.getHabboStats().navigatorWindowSettings.getListModeForCategory("official-root", ListMode.THUMBNAILS), habbo.getHabboStats().navigatorWindowSettings.getDisplayModeForCategory("official-root"), Emulator.getGameEnvironment().getNavigatorManager().getRoomsForCategory("official-root", habbo), false, z, DisplayOrder.ORDER_NUM, -1));
        int i = 0 + 1;
        for (NavigatorPublicCategory navigatorPublicCategory : Emulator.getGameEnvironment().getNavigatorManager().publicCategories.values()) {
            if (!navigatorPublicCategory.rooms.isEmpty()) {
                arrayList.add(new SearchResultList(i, Emulator.PREVIEW, navigatorPublicCategory.name, SearchAction.NONE, habbo.getHabboStats().navigatorWindowSettings.getListModeForCategory(navigatorPublicCategory.name, navigatorPublicCategory.image), habbo.getHabboStats().navigatorWindowSettings.getDisplayModeForCategory(navigatorPublicCategory.name), navigatorPublicCategory.rooms, true, z, DisplayOrder.ORDER_NUM, navigatorPublicCategory.order));
                i++;
            }
        }
        return arrayList;
    }
}
