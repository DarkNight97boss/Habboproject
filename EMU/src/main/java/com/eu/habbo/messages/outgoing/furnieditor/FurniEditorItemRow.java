package com.eu.habbo.messages.outgoing.furnieditor;

import com.eu.habbo.messages.ServerMessage;

/**
 * Plain DTO mirroring the columns the staff FurniEditor needs from
 * {@code items_base}. Both {@link FurniEditorSearchResultComposer} (which
 * emits the 14-field base block) and {@link FurniEditorDetailResultComposer}
 * (which extends it with the detail-only fields) read from this struct so
 * the SQL row-mapper in the event handler stays in one place.
 */
public class FurniEditorItemRow {
    public int id;
    public int spriteId;
    public String itemName;
    public String publicName;
    public String type;
    public int width;
    public int length;
    public double stackHeight;
    public boolean allowStack;
    public boolean allowWalk;
    public boolean allowSit;
    public boolean allowLay;
    public String interactionType;
    public int interactionModesCount;

    // detail-only fields (unused by the search composer)
    public boolean allowGift;
    public boolean allowTrade;
    public boolean allowRecycle;
    public boolean allowMarketplaceSell;
    public boolean allowInventoryStack;
    public String vendingIds;
    public String customParams;
    public int effectIdMale;
    public int effectIdFemale;
    public String clothingOnWalk;
    public String multiheight;
    public String description;
    public int usageCount;

    /** Append the 14-field FurniItemData base block. */
    public void serializeBase(ServerMessage response) {
        response.appendInt(this.id);
        response.appendInt(this.spriteId);
        response.appendString(nullToEmpty(this.itemName));
        response.appendString(nullToEmpty(this.publicName));
        response.appendString(nullToEmpty(this.type));
        response.appendInt(this.width);
        response.appendInt(this.length);
        response.appendDouble(this.stackHeight);
        response.appendBoolean(this.allowStack);
        response.appendBoolean(this.allowWalk);
        response.appendBoolean(this.allowSit);
        response.appendBoolean(this.allowLay);
        response.appendString(nullToEmpty(this.interactionType));
        response.appendInt(this.interactionModesCount);
    }

    /** Append the detail-only extension block (FurniDetailData minus base). */
    public void serializeDetailExtension(ServerMessage response) {
        response.appendBoolean(this.allowGift);
        response.appendBoolean(this.allowTrade);
        response.appendBoolean(this.allowRecycle);
        response.appendBoolean(this.allowMarketplaceSell);
        response.appendBoolean(this.allowInventoryStack);
        response.appendString(nullToEmpty(this.vendingIds));
        response.appendString(nullToEmpty(this.customParams));
        response.appendInt(this.effectIdMale);
        response.appendInt(this.effectIdFemale);
        response.appendString(nullToEmpty(this.clothingOnWalk));
        response.appendString(nullToEmpty(this.multiheight));
        // description falls back to publicName when blank (parser-side contract).
        String desc = this.description;
        if (desc == null || desc.isEmpty()) desc = nullToEmpty(this.publicName);
        response.appendString(desc);
        response.appendInt(this.usageCount);
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
