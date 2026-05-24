package com.eu.habbo.habbohotel.catalog;

import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/catalog/ClothItem.class */
public class ClothItem {
    public int id;
    public String name;
    public int[] setId;

    public ClothItem(ResultSet resultSet) throws SQLException {
        this.id = resultSet.getInt("id");
        this.name = resultSet.getString("name");
        String[] strArrSplit = resultSet.getString("setid").split(",");
        this.setId = new int[strArrSplit.length];
        for (int i = 0; i < this.setId.length; i++) {
            this.setId[i] = Integer.valueOf(strArrSplit[i]).intValue();
        }
    }
}
