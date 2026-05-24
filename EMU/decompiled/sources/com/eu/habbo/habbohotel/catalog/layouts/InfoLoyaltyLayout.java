package com.eu.habbo.habbohotel.catalog.layouts;

import com.eu.habbo.habbohotel.catalog.CatalogPage;
import com.eu.habbo.messages.ServerMessage;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/catalog/layouts/InfoLoyaltyLayout.class */
public class InfoLoyaltyLayout extends CatalogPage {
    public InfoLoyaltyLayout(ResultSet resultSet) throws SQLException {
        super(resultSet);
    }

    @Override // com.eu.habbo.habbohotel.catalog.CatalogPage, com.eu.habbo.messages.ISerialize
    public void serialize(ServerMessage serverMessage) {
        serverMessage.appendString("info_loyalty");
        serverMessage.appendInt((Integer) 1);
        serverMessage.appendString(getHeaderImage());
        serverMessage.appendInt((Integer) 1);
        serverMessage.appendString(getTextOne());
        serverMessage.appendInt((Integer) 0);
    }
}
