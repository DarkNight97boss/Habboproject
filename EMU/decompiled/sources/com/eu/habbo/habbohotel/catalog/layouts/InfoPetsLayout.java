package com.eu.habbo.habbohotel.catalog.layouts;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.CatalogPage;
import com.eu.habbo.messages.ServerMessage;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/catalog/layouts/InfoPetsLayout.class */
public class InfoPetsLayout extends CatalogPage {
    public InfoPetsLayout(ResultSet resultSet) throws SQLException {
        super(resultSet);
    }

    @Override // com.eu.habbo.habbohotel.catalog.CatalogPage, com.eu.habbo.messages.ISerialize
    public void serialize(ServerMessage serverMessage) {
        serverMessage.appendString("info_pets");
        serverMessage.appendInt((Integer) 2);
        serverMessage.appendString(getHeaderImage());
        serverMessage.appendString(getTeaserImage());
        serverMessage.appendInt((Integer) 3);
        serverMessage.appendString(getTextOne());
        serverMessage.appendString(Emulator.PREVIEW);
        serverMessage.appendString(getTextTeaser());
        serverMessage.appendInt((Integer) 0);
    }
}
