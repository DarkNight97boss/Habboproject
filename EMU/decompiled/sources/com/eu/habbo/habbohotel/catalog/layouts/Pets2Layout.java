package com.eu.habbo.habbohotel.catalog.layouts;

import com.eu.habbo.habbohotel.catalog.CatalogPage;
import com.eu.habbo.messages.ServerMessage;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/catalog/layouts/Pets2Layout.class */
public class Pets2Layout extends CatalogPage {
    public Pets2Layout(ResultSet resultSet) throws SQLException {
        super(resultSet);
    }

    @Override // com.eu.habbo.habbohotel.catalog.CatalogPage, com.eu.habbo.messages.ISerialize
    public void serialize(ServerMessage serverMessage) {
        serverMessage.appendString("pets2");
        serverMessage.appendInt((Integer) 2);
        serverMessage.appendString(super.getHeaderImage());
        serverMessage.appendString(super.getTeaserImage());
        serverMessage.appendInt((Integer) 4);
        serverMessage.appendString(super.getTextOne());
        serverMessage.appendString(super.getTextTwo());
        serverMessage.appendString(super.getTextDetails());
        serverMessage.appendString(super.getTextTeaser());
    }
}
