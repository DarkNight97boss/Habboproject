package com.eu.habbo.habbohotel.messenger;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/messenger/MessengerCategory.class */
public class MessengerCategory {
    private int user_id;
    private String name;
    private int id;

    public MessengerCategory(String str, int i, int i2) {
        this.name = str;
        this.user_id = i;
        this.id = i2;
    }

    public String getName() {
        return this.name;
    }

    public int getUserId() {
        return this.user_id;
    }

    public int getId() {
        return this.id;
    }

    public void setName(String str) {
        this.name = str;
    }

    public void setId(int i) {
        this.id = i;
    }
}
