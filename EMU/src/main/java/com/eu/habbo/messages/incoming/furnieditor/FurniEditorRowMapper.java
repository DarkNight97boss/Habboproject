package com.eu.habbo.messages.incoming.furnieditor;

import com.eu.habbo.messages.outgoing.furnieditor.FurniEditorItemRow;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Single source of truth for mapping {@code items_base} rows into the
 * {@link FurniEditorItemRow} DTO. Used by every FurniEditor handler so any
 * column rename only needs to be touched here.
 *
 * The whitelist of columns lives in {@link FurniEditorUpdateEvent#ALLOWED_COLUMNS};
 * the {@code SELECT} list below mirrors it (plus the immutable {@code id}).
 */
final class FurniEditorRowMapper {

    /**
     * Enumerated explicitly — never {@code SELECT *}. Order doesn't matter
     * here because every read uses the column-name overloads.
     */
    static final String COLUMNS =
            "id, sprite_id, item_name, public_name, type, width, length, stack_height, " +
            "allow_stack, allow_sit, allow_lay, allow_walk, allow_gift, allow_trade, " +
            "allow_recycle, allow_marketplace_sell, allow_inventory_stack, " +
            "interaction_type, interaction_modes_count, vending_ids, multiheight, " +
            "customparams, effect_id_male, effect_id_female, clothing_on_walk";

    private FurniEditorRowMapper() {}

    /** Populate the base + detail fields from a result set positioned on a row. */
    static FurniEditorItemRow map(ResultSet set) throws SQLException {
        FurniEditorItemRow row = new FurniEditorItemRow();
        row.id = set.getInt("id");
        row.spriteId = set.getInt("sprite_id");
        row.itemName = nz(set.getString("item_name"));
        row.publicName = nz(set.getString("public_name"));
        row.type = nz(set.getString("type"));
        row.width = set.getInt("width");
        row.length = set.getInt("length");
        row.stackHeight = set.getDouble("stack_height");
        row.allowStack = set.getBoolean("allow_stack");
        row.allowSit = set.getBoolean("allow_sit");
        row.allowLay = set.getBoolean("allow_lay");
        row.allowWalk = set.getBoolean("allow_walk");
        row.allowGift = set.getBoolean("allow_gift");
        row.allowTrade = set.getBoolean("allow_trade");
        row.allowRecycle = set.getBoolean("allow_recycle");
        row.allowMarketplaceSell = set.getBoolean("allow_marketplace_sell");
        row.allowInventoryStack = set.getBoolean("allow_inventory_stack");
        row.interactionType = nz(set.getString("interaction_type"));
        row.interactionModesCount = set.getInt("interaction_modes_count");
        row.vendingIds = nz(set.getString("vending_ids"));
        row.multiheight = nz(set.getString("multiheight"));
        row.customParams = nz(set.getString("customparams"));
        row.effectIdMale = set.getInt("effect_id_male");
        row.effectIdFemale = set.getInt("effect_id_female");
        row.clothingOnWalk = nz(set.getString("clothing_on_walk"));
        // description / usageCount filled by the detail handler — leave defaults.
        return row;
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
