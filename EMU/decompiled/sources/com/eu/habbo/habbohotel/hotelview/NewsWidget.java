package com.eu.habbo.habbohotel.hotelview;

import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/hotelview/NewsWidget.class */
public class NewsWidget {
    private int id;
    private String title;
    private String message;
    private String buttonMessage;
    private int type;
    private String link;
    private String image;

    public NewsWidget(ResultSet resultSet) throws SQLException {
        this.id = resultSet.getInt("id");
        this.title = resultSet.getString("title");
        this.message = resultSet.getString("text");
        this.buttonMessage = resultSet.getString("button_text");
        this.type = resultSet.getString("button_type").equals("client") ? 1 : 0;
        this.link = resultSet.getString("button_link");
        this.image = resultSet.getString("image");
    }

    public int getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getMessage() {
        return this.message;
    }

    public String getButtonMessage() {
        return this.buttonMessage;
    }

    public int getType() {
        return this.type;
    }

    public String getLink() {
        return this.link;
    }

    public String getImage() {
        return this.image;
    }
}
