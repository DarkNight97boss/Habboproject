package com.eu.habbo.habbohotel.pets;

import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/pets/PetRace.class */
public class PetRace {
    public final int race;
    public final int colorOne;
    public final int colorTwo;
    public final boolean hasColorOne;
    public final boolean hasColorTwo;

    public PetRace(ResultSet resultSet) throws SQLException {
        this.race = resultSet.getInt("race");
        this.colorOne = resultSet.getInt("color_one");
        this.colorTwo = resultSet.getInt("color_two");
        this.hasColorOne = resultSet.getString("has_color_one").equals("1");
        this.hasColorTwo = resultSet.getString("has_color_two").equals("1");
    }
}
