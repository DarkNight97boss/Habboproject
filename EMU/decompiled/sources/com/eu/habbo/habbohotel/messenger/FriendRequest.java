package com.eu.habbo.habbohotel.messenger;

import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/messenger/FriendRequest.class */
public class FriendRequest {
    private int id;
    private String username;
    private String look;

    public FriendRequest(ResultSet resultSet) throws SQLException {
        this.id = resultSet.getInt("id");
        this.username = resultSet.getString("username");
        this.look = resultSet.getString("look");
    }

    public FriendRequest(int i, String str, String str2) {
        this.id = i;
        this.username = str;
        this.look = str2;
    }

    public int getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

    public String getLook() {
        return this.look;
    }
}
