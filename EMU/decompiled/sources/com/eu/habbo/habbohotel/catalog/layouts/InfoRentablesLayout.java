package com.eu.habbo.habbohotel.catalog.layouts;

import com.eu.habbo.habbohotel.catalog.CatalogPage;
import com.eu.habbo.messages.ServerMessage;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/catalog/layouts/InfoRentablesLayout.class */
public class InfoRentablesLayout extends CatalogPage {
    public InfoRentablesLayout(ResultSet resultSet) throws SQLException {
        super(resultSet);
    }

    @Override // com.eu.habbo.habbohotel.catalog.CatalogPage, com.eu.habbo.messages.ISerialize
    public void serialize(ServerMessage serverMessage) {
        String[] strArrSplit = getTextOne().split("\\|\\|");
        serverMessage.appendString("info_rentables");
        serverMessage.appendInt((Integer) 1);
        serverMessage.appendString(getHeaderImage());
        serverMessage.appendInt(Integer.valueOf(strArrSplit.length));
        for (String str : strArrSplit) {
            serverMessage.appendString(str);
        }
        serverMessage.appendInt((Integer) 0);
    }
}
