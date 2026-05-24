package com.eu.habbo.habbohotel.pets;

import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/pets/PetBreedingReward.class */
public class PetBreedingReward {
    public final int petType;
    public final int rarityLevel;
    public final int breed;

    public PetBreedingReward(ResultSet resultSet) throws SQLException {
        this.petType = resultSet.getInt("pet_type");
        this.rarityLevel = resultSet.getInt("rarity_level");
        this.breed = resultSet.getInt("breed");
    }
}
