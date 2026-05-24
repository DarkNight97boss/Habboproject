package com.eu.habbo.habbohotel.hotelview;

import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/hotelview/HallOfFameWinner.class */
public class HallOfFameWinner implements Comparable<HallOfFameWinner> {
    private int id;
    private String username;
    private String look;
    private int points;

    public HallOfFameWinner(ResultSet resultSet) throws SQLException {
        this.id = resultSet.getInt("id");
        this.username = resultSet.getString("username");
        this.look = resultSet.getString("look");
        this.points = resultSet.getInt("hof_points");
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

    public int getPoints() {
        return this.points;
    }

    @Override // java.lang.Comparable
    public int compareTo(HallOfFameWinner hallOfFameWinner) {
        return hallOfFameWinner.getPoints() - this.points;
    }
}
