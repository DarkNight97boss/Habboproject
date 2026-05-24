package com.eu.habbo.habbohotel.navigation;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/navigation/NavigatorFilter.class */
public abstract class NavigatorFilter {
    public final String viewName;

    public NavigatorFilter(String str) {
        this.viewName = str;
    }

    public void filter(Method method, Object obj, List<SearchResultList> list) {
        if (method == null) {
            return;
        }
        if ((obj instanceof String) && ((String) obj).isEmpty()) {
            return;
        }
        for (SearchResultList searchResultList : list) {
            if (searchResultList.filter) {
                filterRooms(method, obj, searchResultList.rooms);
            }
        }
    }

    public void filterRooms(Method method, Object obj, List<Room> list) {
        if (method == null) {
            return;
        }
        if ((obj instanceof String) && ((String) obj).isEmpty()) {
            return;
        }
        ArrayList arrayList = new ArrayList();
        try {
            method.setAccessible(true);
            Iterator<Room> it = list.iterator();
            while (it.hasNext()) {
                Room next = it.next();
                Object objInvoke = method.invoke(next, new Object[0]);
                if (objInvoke.getClass() == obj.getClass()) {
                    if (objInvoke instanceof String) {
                        NavigatorFilterComparator navigatorFilterComparatorComperatorForField = Emulator.getGameEnvironment().getNavigatorManager().comperatorForField(method);
                        if (navigatorFilterComparatorComperatorForField == null || !applies(navigatorFilterComparatorComperatorForField, (String) objInvoke, (String) obj)) {
                            arrayList.add(next);
                        }
                    } else if (objInvoke instanceof String[]) {
                        for (String str : (String[]) objInvoke) {
                            NavigatorFilterComparator navigatorFilterComparatorComperatorForField2 = Emulator.getGameEnvironment().getNavigatorManager().comperatorForField(method);
                            if (navigatorFilterComparatorComperatorForField2 != null && !applies(navigatorFilterComparatorComperatorForField2, str, (String) obj)) {
                                arrayList.add(next);
                            }
                        }
                    } else if (objInvoke != obj) {
                        arrayList.add(next);
                    }
                }
            }
        } catch (Exception e) {
        }
        list.removeAll(arrayList);
        arrayList.clear();
    }

    public abstract List<SearchResultList> getResult(Habbo habbo);

    public List<SearchResultList> getResult(Habbo habbo, NavigatorFilterField navigatorFilterField, String str, int i) {
        return getResult(habbo);
    }

    private boolean applies(NavigatorFilterComparator navigatorFilterComparator, String str, String str2) {
        switch (navigatorFilterComparator) {
            case CONTAINS:
                if (StringUtils.containsIgnoreCase(str, str2)) {
                }
                break;
            case EQUALS:
                if (str.equals(str2)) {
                }
                break;
            case EQUALS_IGNORE_CASE:
                if (str.equalsIgnoreCase(str2)) {
                }
                break;
        }
        return true;
    }
}
