package com.eu.habbo.habbohotel.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.ISerialize;
import com.eu.habbo.messages.ServerMessage;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/catalog/CatalogFeaturedPage.class */
public class CatalogFeaturedPage implements ISerialize {
    private final int slotId;
    private final String caption;
    private final String image;
    private final Type type;
    private final int expireTimestamp;
    private final String pageName;
    private final int pageId;
    private final String productName;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/catalog/CatalogFeaturedPage$Type.class */
    public enum Type {
        PAGE_NAME(0),
        PAGE_ID(1),
        PRODUCT_NAME(2);

        public final int type;

        Type(int i) {
            this.type = i;
        }
    }

    public CatalogFeaturedPage(int i, String str, String str2, Type type, int i2, String str3, int i3, String str4) {
        this.slotId = i;
        this.caption = str;
        this.image = str2;
        this.type = type;
        this.expireTimestamp = i2;
        this.pageName = str3;
        this.pageId = i3;
        this.productName = str4;
    }

    @Override // com.eu.habbo.messages.ISerialize
    public void serialize(ServerMessage serverMessage) {
        serverMessage.appendInt(Integer.valueOf(this.slotId));
        serverMessage.appendString(this.caption);
        serverMessage.appendString(this.image);
        serverMessage.appendInt(Integer.valueOf(this.type.type));
        switch (this.type) {
            case PAGE_NAME:
                serverMessage.appendString(this.pageName);
                break;
            case PAGE_ID:
                serverMessage.appendInt(Integer.valueOf(this.pageId));
                break;
            case PRODUCT_NAME:
                serverMessage.appendString(this.productName);
                break;
        }
        serverMessage.appendInt(Integer.valueOf(Emulator.getIntUnixTimestamp() - this.expireTimestamp));
    }
}
