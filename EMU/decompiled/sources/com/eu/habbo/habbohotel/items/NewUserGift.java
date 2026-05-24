package com.eu.habbo.habbohotel.items;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ISerialize;
import com.eu.habbo.messages.ServerMessage;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/NewUserGift.class */
public class NewUserGift implements ISerialize {
    private final int id;
    private final Type type;
    private final String imageUrl;
    private Map<String, String> items;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/NewUserGift$Type.class */
    public enum Type {
        ITEM,
        ROOM
    }

    public NewUserGift(ResultSet resultSet) throws SQLException {
        this.items = new HashMap();
        this.id = resultSet.getInt("id");
        this.type = Type.valueOf(resultSet.getString("type").toUpperCase());
        this.imageUrl = resultSet.getString("image");
        this.items.put(this.type == Type.ROOM ? Emulator.PREVIEW : resultSet.getString("value"), this.type == Type.ROOM ? resultSet.getString("value") : Emulator.PREVIEW);
    }

    public NewUserGift(int i, Type type, String str, Map<String, String> map) {
        this.items = new HashMap();
        this.id = i;
        this.imageUrl = str;
        this.type = type;
        this.items = map;
    }

    @Override // com.eu.habbo.messages.ISerialize
    public void serialize(ServerMessage serverMessage) {
        serverMessage.appendString(this.imageUrl);
        serverMessage.appendInt(Integer.valueOf(this.items.size()));
        for (Map.Entry<String, String> entry : this.items.entrySet()) {
            serverMessage.appendString(entry.getKey());
            serverMessage.appendString(entry.getValue());
        }
    }

    public void give(Habbo habbo) {
        HabboItem habboItemCreateItem;
        if (this.type != Type.ITEM) {
            if (this.type == Type.ROOM) {
            }
            return;
        }
        Iterator<Map.Entry<String, String>> it = this.items.entrySet().iterator();
        while (it.hasNext()) {
            Item item = Emulator.getGameEnvironment().getItemManager().getItem(it.next().getKey());
            if (item != null && (habboItemCreateItem = Emulator.getGameEnvironment().getItemManager().createItem(habbo.getHabboInfo().getId(), item, 0, 0, Emulator.PREVIEW)) != null) {
                habbo.addFurniture(habboItemCreateItem);
            }
        }
    }

    public int getId() {
        return this.id;
    }

    public Type getType() {
        return this.type;
    }

    public String getImageUrl() {
        return this.imageUrl;
    }

    public Map<String, String> getItems() {
        return this.items;
    }
}
