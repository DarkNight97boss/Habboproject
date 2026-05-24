package com.eu.habbo.habbohotel.catalog.layouts;

import com.eu.habbo.habbohotel.catalog.CatalogPage;
import com.eu.habbo.messages.ServerMessage;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/catalog/layouts/GuildFrontpageLayout.class */
public class GuildFrontpageLayout extends CatalogPage {
    public GuildFrontpageLayout(ResultSet resultSet) throws SQLException {
        super(resultSet);
    }

    @Override // com.eu.habbo.habbohotel.catalog.CatalogPage, com.eu.habbo.messages.ISerialize
    public void serialize(ServerMessage serverMessage) {
        serverMessage.appendString("guild_frontpage");
        serverMessage.appendInt((Integer) 2);
        serverMessage.appendString(super.getHeaderImage());
        serverMessage.appendString(super.getTeaserImage());
        serverMessage.appendInt((Integer) 3);
        serverMessage.appendString(super.getTextOne());
        serverMessage.appendString(super.getTextDetails());
        serverMessage.appendString(super.getTextTeaser());
    }
}
