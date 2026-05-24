package com.eu.habbo.habbohotel.catalog.layouts;

import com.eu.habbo.habbohotel.catalog.CatalogPage;
import com.eu.habbo.messages.ServerMessage;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/catalog/layouts/ClubGiftsLayout.class */
public class ClubGiftsLayout extends CatalogPage {
    public ClubGiftsLayout(ResultSet resultSet) throws SQLException {
        super(resultSet);
    }

    @Override // com.eu.habbo.habbohotel.catalog.CatalogPage, com.eu.habbo.messages.ISerialize
    public void serialize(ServerMessage serverMessage) {
        serverMessage.appendString("club_gifts");
        serverMessage.appendInt((Integer) 1);
        serverMessage.appendString(super.getHeaderImage());
        serverMessage.appendInt((Integer) 1);
        serverMessage.appendString(super.getTextOne());
    }
}
