package com.eu.habbo.messages.outgoing.furnieditor;

import com.eu.habbo.messages.ServerMessage;

/**
 * One row from {@code catalog_items} whose {@code item_ids} CSV contains
 * the furniture id being inspected. Serialized inside
 * {@link FurniEditorDetailResultComposer} so the staff UI can show "this
 * sprite is sold from page X for Y credits".
 */
public class FurniEditorCatalogRef {
    public int id;
    public String catalogName;
    public int costCredits;
    public int costPoints;
    public int pointsType;
    public int pageId;
    public String pageName;

    public void serialize(ServerMessage response) {
        response.appendInt(this.id);
        response.appendString(this.catalogName == null ? "" : this.catalogName);
        response.appendInt(this.costCredits);
        response.appendInt(this.costPoints);
        response.appendInt(this.pointsType);
        response.appendInt(this.pageId);
        response.appendString(this.pageName == null ? "" : this.pageName);
    }
}
