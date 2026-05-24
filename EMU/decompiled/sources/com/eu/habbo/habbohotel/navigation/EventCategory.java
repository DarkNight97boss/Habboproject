package com.eu.habbo.habbohotel.navigation;

import com.eu.habbo.messages.ServerMessage;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/navigation/EventCategory.class */
public class EventCategory {
    private int id;
    private String caption;
    private boolean visible;

    public EventCategory(int i, String str, boolean z) {
        this.id = i;
        this.caption = str;
        this.visible = z;
    }

    public EventCategory(String str) throws Exception {
        String[] strArrSplit = str.split(",");
        if (strArrSplit.length != 3) {
            throw new Exception("A serialized event category should contain 3 fields");
        }
        this.id = Integer.valueOf(strArrSplit[0]).intValue();
        this.caption = strArrSplit[1];
        this.visible = strArrSplit[2].equalsIgnoreCase("true");
    }

    public int getId() {
        return this.id;
    }

    public String getCaption() {
        return this.caption;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void serialize(ServerMessage serverMessage) {
        serverMessage.appendInt(Integer.valueOf(this.id));
        serverMessage.appendString(this.caption);
        serverMessage.appendBoolean(Boolean.valueOf(this.visible));
    }
}
