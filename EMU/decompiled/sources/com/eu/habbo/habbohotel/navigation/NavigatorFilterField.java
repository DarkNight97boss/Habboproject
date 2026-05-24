package com.eu.habbo.habbohotel.navigation;

import java.lang.reflect.Method;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/navigation/NavigatorFilterField.class */
public class NavigatorFilterField {
    public final String key;
    public final Method field;
    public final String databaseQuery;
    public final NavigatorFilterComparator comparator;

    public NavigatorFilterField(String str, Method method, String str2, NavigatorFilterComparator navigatorFilterComparator) {
        this.key = str;
        this.field = method;
        this.databaseQuery = str2;
        this.comparator = navigatorFilterComparator;
    }
}
