package com.eu.habbo.habbohotel.catalog.layouts;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.CatalogFeaturedPage;
import com.eu.habbo.habbohotel.catalog.CatalogPage;
import com.eu.habbo.messages.ServerMessage;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/catalog/layouts/FrontPageFeaturedLayout.class */
public class FrontPageFeaturedLayout extends CatalogPage {
    public FrontPageFeaturedLayout(ResultSet resultSet) throws SQLException {
        super(resultSet);
    }

    @Override // com.eu.habbo.habbohotel.catalog.CatalogPage, com.eu.habbo.messages.ISerialize
    public void serialize(ServerMessage serverMessage) {
        serverMessage.appendString("frontpage_featured");
        String[] strArrSplit = super.getTeaserImage().split(";");
        String[] strArrSplit2 = super.getSpecialImage().split(";");
        serverMessage.appendInt(Integer.valueOf(1 + strArrSplit.length + strArrSplit2.length));
        serverMessage.appendString(super.getHeaderImage());
        for (String str : strArrSplit) {
            serverMessage.appendString(str);
        }
        for (String str2 : strArrSplit2) {
            serverMessage.appendString(str2);
        }
        serverMessage.appendInt((Integer) 3);
        serverMessage.appendString(super.getTextOne());
        serverMessage.appendString(super.getTextDetails());
        serverMessage.appendString(super.getTextTeaser());
    }

    public void serializeExtra(ServerMessage serverMessage) {
        serverMessage.appendInt(Integer.valueOf(Emulator.getGameEnvironment().getCatalogManager().getCatalogFeaturedPages().size()));
        Iterator it = Emulator.getGameEnvironment().getCatalogManager().getCatalogFeaturedPages().valueCollection().iterator();
        while (it.hasNext()) {
            ((CatalogFeaturedPage) it.next()).serialize(serverMessage);
        }
        serverMessage.appendInt((Integer) 1);
        serverMessage.appendString("NUOVO: Affare Stanza di Rilassamento");
        serverMessage.appendString("catalogue/feature_cata_vert_oly16bundle4.png");
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendString(Emulator.PREVIEW);
        serverMessage.appendInt((Integer) (-1));
        serverMessage.appendInt((Integer) 2);
        serverMessage.appendString("Il RITORNO di Habburgers! (TUTTI furni nuovi)");
        serverMessage.appendString("catalogue/feature_cata_hort_habbergerbundle.png");
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendString(Emulator.PREVIEW);
        serverMessage.appendInt((Integer) (-1));
        serverMessage.appendInt((Integer) 3);
        serverMessage.appendString("Habbolympics");
        serverMessage.appendString("catalogue/feature_cata_hort_olympic16.png");
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendString(Emulator.PREVIEW);
        serverMessage.appendInt((Integer) (-1));
        serverMessage.appendInt((Integer) 4);
        serverMessage.appendString("Diventa un Membro HC");
        serverMessage.appendString("catalogue/feature_cata_hort_HC_b.png");
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendString("habbo_club");
        serverMessage.appendInt((Integer) (-1));
    }
}
