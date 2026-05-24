package com.eu.habbo.habbohotel.navigation;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomState;
import com.eu.habbo.messages.ISerialize;
import com.eu.habbo.messages.ServerMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/navigation/SearchResultList.class */
public class SearchResultList implements ISerialize, Comparable<SearchResultList> {
    public final int order;
    public final String code;
    public final String query;
    public final SearchAction action;
    public final ListMode mode;
    public final DisplayMode hidden;
    public final List<Room> rooms;
    public final boolean filter;
    public final boolean showInvisible;
    public final DisplayOrder displayOrder;
    public final int categoryOrder;

    public SearchResultList(int i, String str, String str2, SearchAction searchAction, ListMode listMode, DisplayMode displayMode, List<Room> list, boolean z, boolean z2, DisplayOrder displayOrder, int i2) {
        this.order = i;
        this.code = str;
        this.query = str2;
        this.action = searchAction;
        this.mode = listMode;
        this.rooms = list;
        this.hidden = displayMode;
        this.filter = z;
        this.showInvisible = z2;
        this.displayOrder = displayOrder;
        this.categoryOrder = i2;
    }

    @Override // com.eu.habbo.messages.ISerialize
    public void serialize(ServerMessage serverMessage) {
        serverMessage.appendString(this.code);
        serverMessage.appendString(this.query);
        serverMessage.appendInt(Integer.valueOf(this.action.type));
        serverMessage.appendBoolean(Boolean.valueOf(this.hidden.equals(DisplayMode.COLLAPSED)));
        serverMessage.appendInt(Integer.valueOf(this.mode.type));
        synchronized (this.rooms) {
            if (!this.showInvisible) {
                ArrayList arrayList = new ArrayList();
                for (Room room : this.rooms) {
                    if (room.getState() == RoomState.INVISIBLE) {
                        arrayList.add(room);
                    }
                }
                this.rooms.removeAll(arrayList);
            }
            serverMessage.appendInt(Integer.valueOf(this.rooms.size()));
            Collections.sort(this.rooms);
            Iterator<Room> it = this.rooms.iterator();
            while (it.hasNext()) {
                it.next().serialize(serverMessage);
            }
        }
    }

    @Override // java.lang.Comparable
    public int compareTo(SearchResultList searchResultList) {
        if (this.displayOrder != DisplayOrder.ACTIVITY) {
            return this.categoryOrder - searchResultList.categoryOrder;
        }
        if (this.code.equalsIgnoreCase("popular")) {
            return -1;
        }
        return this.rooms.size() - searchResultList.rooms.size();
    }
}
