package com.eu.habbo.habbohotel.items;

import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/SoundTrack.class */
public class SoundTrack {
    private int id;
    private String name;
    private String author;
    private String code;
    private String data;
    private int length;

    public SoundTrack(ResultSet resultSet) throws SQLException {
        this.id = resultSet.getInt("id");
        this.name = resultSet.getString("name");
        this.author = resultSet.getString("author");
        this.code = resultSet.getString("code");
        this.data = resultSet.getString("track");
        this.length = resultSet.getInt("length");
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getCode() {
        return this.code;
    }

    public String getData() {
        return this.data;
    }

    public int getLength() {
        return this.length;
    }
}
