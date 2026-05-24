package com.eu.habbo.habbohotel.navigation;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/navigation/NavigatorSavedSearch.class */
public class NavigatorSavedSearch {
    private String searchCode;
    private String filter;
    private int id;

    public NavigatorSavedSearch(String str, String str2) {
        this.searchCode = str;
        this.filter = str2;
    }

    public NavigatorSavedSearch(String str, String str2, int i) {
        this.searchCode = str;
        this.filter = str2;
        this.id = i;
    }

    public String getSearchCode() {
        return this.searchCode;
    }

    public String getFilter() {
        return this.filter;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int i) {
        this.id = i;
    }
}
