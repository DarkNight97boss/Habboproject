package com.eu.habbo.messages.incoming.furnieditor;

import com.eu.habbo.Emulator;
import com.eu.habbo.core.AuditLog;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.furnieditor.FurniEditorResultComposer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 10044 FURNI_EDITOR_UPDATE — apply a partial UPDATE on items_base.
 *
 * Args: {@code (int id, string jsonFields)}. The JSON object is a flat
 * map of {column → value}. Only columns in {@link #ALLOWED_COLUMNS} are
 * applied; everything else is silently dropped (no error to avoid leaking
 * the whitelist). After the SQL UPDATE we re-load the row via {@code
 * Item.update(ResultSet)} so the in-memory ItemManager cache stays in
 * sync — without this, rooms keep using the stale base item until the
 * EMU restarts.
 *
 * Type filter on 'type' field: must be one of {@code s|i|e}. Boolean
 * fields accept {@code true/false} or {@code 0/1}. Numeric fields are
 * coerced via Gson's number handling. Strings are stored verbatim
 * (the underlying columns are TEXT/VARCHAR).
 */
public class FurniEditorUpdateEvent extends MessageHandler {

    /**
     * Whitelist of writable columns. Mirrors the task spec exactly.
     * Any field name outside this set in the incoming JSON is dropped
     * before the SQL is built — defense in depth against future column
     * additions that might be sensitive (e.g. price tables).
     */
    static final Set<String> ALLOWED_COLUMNS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "sprite_id", "public_name", "item_name", "type", "width", "length",
            "stack_height", "allow_stack", "allow_sit", "allow_lay", "allow_walk",
            "allow_gift", "allow_trade", "allow_recycle", "allow_marketplace_sell",
            "allow_inventory_stack", "interaction_type", "interaction_modes_count",
            "vending_ids", "multiheight", "customparams", "effect_id_male",
            "effect_id_female", "clothing_on_walk"
    )));

    /** Columns that store a boolean tinyint(1). */
    private static final Set<String> BOOL_COLUMNS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "allow_stack", "allow_sit", "allow_lay", "allow_walk", "allow_gift",
            "allow_trade", "allow_recycle", "allow_marketplace_sell", "allow_inventory_stack"
    )));

    /** Integer-valued columns (everything else in the whitelist is string or stack_height). */
    private static final Set<String> INT_COLUMNS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "sprite_id", "width", "length", "interaction_modes_count",
            "effect_id_male", "effect_id_female"
    )));

    @Override
    public void handle() throws Exception {
        if (!this.client.getHabbo().hasPermission(Permission.ACC_SUPPORTTOOL)) {
            ScripterManager.scripterDetected(this.client,
                    "Furni editor update attempted without permission by "
                            + this.client.getHabbo().getHabboInfo().getUsername());
            return;
        }

        int id = this.packet.readInt();
        String json = this.packet.readString();
        if (id <= 0 || json == null || json.isEmpty()) {
            reply(false, "Invalid request", id);
            return;
        }

        JsonObject obj;
        try {
            JsonElement parsed = JsonParser.parseString(json);
            if (!parsed.isJsonObject()) {
                reply(false, "Payload must be a JSON object", id);
                return;
            }
            obj = parsed.getAsJsonObject();
        } catch (JsonSyntaxException e) {
            reply(false, "Malformed JSON", id);
            return;
        }

        // Build the SET clause from whitelisted fields only.
        List<String> setFragments = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            String col = entry.getKey();
            if (!ALLOWED_COLUMNS.contains(col)) continue;
            JsonElement v = entry.getValue();
            if (v == null || v.isJsonNull()) continue;

            Object coerced = coerce(col, v);
            if (coerced == null && !v.isJsonPrimitive()) continue;

            setFragments.add(col + " = ?");
            params.add(coerced);
        }

        if (setFragments.isEmpty()) {
            reply(false, "No allowed fields to update", id);
            return;
        }

        String sql = "UPDATE items_base SET " + String.join(", ", setFragments) + " WHERE id = ?";

        int affected;
        try (Connection conn = Emulator.getDatabase().getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            for (Object p : params) {
                if (p instanceof Integer) ps.setInt(idx++, (Integer) p);
                else if (p instanceof Double) ps.setDouble(idx++, (Double) p);
                else if (p instanceof Boolean) ps.setBoolean(idx++, (Boolean) p);
                else ps.setString(idx++, p == null ? "" : p.toString());
            }
            ps.setInt(idx, id);
            affected = ps.executeUpdate();
        }

        if (affected == 0) {
            reply(false, "Item not found", id);
            return;
        }

        // Refresh in-memory Item cache so live rooms see the new metadata
        // without a server restart. We re-SELECT the row and feed it to
        // Item.update / new Item depending on whether ItemManager already
        // had the id.
        refreshItemCache(id);

        AuditLog.record(
                this.client.getHabbo().getHabboInfo().getId(),
                this.client.getHabbo().getHabboInfo().getUsername(),
                "FURNI_EDITOR_UPDATE",
                "items_base:" + id,
                "fields=" + String.join(",", obj.keySet())
        );

        reply(true, "Updated", id);
    }

    /**
     * Coerce a JSON value to the right Java type for the target column.
     * Tolerant of JSON-as-string ("1" / "true") because the staff UI may
     * round-trip via form inputs.
     */
    private Object coerce(String col, JsonElement v) {
        if (BOOL_COLUMNS.contains(col)) {
            if (v.isJsonPrimitive() && v.getAsJsonPrimitive().isBoolean()) return v.getAsBoolean();
            if (v.isJsonPrimitive() && v.getAsJsonPrimitive().isNumber()) return v.getAsInt() != 0;
            if (v.isJsonPrimitive() && v.getAsJsonPrimitive().isString()) {
                String s = v.getAsString().trim().toLowerCase();
                return s.equals("1") || s.equals("true") || s.equals("yes");
            }
            return false;
        }
        if (INT_COLUMNS.contains(col)) {
            try {
                if (v.isJsonPrimitive() && v.getAsJsonPrimitive().isNumber()) return v.getAsInt();
                if (v.isJsonPrimitive() && v.getAsJsonPrimitive().isString()) {
                    String s = v.getAsString().trim();
                    return s.isEmpty() ? 0 : Integer.parseInt(s);
                }
            } catch (NumberFormatException e) {
                return 0;
            }
            return 0;
        }
        if (col.equals("stack_height")) {
            try {
                if (v.isJsonPrimitive() && v.getAsJsonPrimitive().isNumber()) return v.getAsDouble();
                if (v.isJsonPrimitive() && v.getAsJsonPrimitive().isString()) {
                    String s = v.getAsString().trim();
                    return s.isEmpty() ? 0.0d : Double.parseDouble(s);
                }
            } catch (NumberFormatException e) {
                return 0.0d;
            }
            return 0.0d;
        }
        if (col.equals("type")) {
            String s = v.isJsonPrimitive() ? v.getAsString().trim().toLowerCase() : "s";
            // safe default to 's' on unknown — UI shouldn't ever send anything else.
            return (s.equals("s") || s.equals("i") || s.equals("e")) ? s : "s";
        }
        // remaining columns are TEXT/VARCHAR
        if (v.isJsonPrimitive()) return v.getAsString();
        return null;
    }

    /** Re-load the cached {@link Item} for {@code id} from the DB. */
    private void refreshItemCache(int id) {
        try (Connection conn = Emulator.getDatabase().getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM items_base WHERE id = ? LIMIT 1")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return;
                Item existing = Emulator.getGameEnvironment().getItemManager().getItem(id);
                if (existing != null) {
                    existing.update(rs);
                } else {
                    Emulator.getGameEnvironment().getItemManager().getItems().put(id, new Item(rs));
                }
            }
        } catch (SQLException ignored) {
            // cache refresh is best-effort — the DB UPDATE already
            // succeeded, so report success to the staff client either way.
        }
    }

    private void reply(boolean ok, String msg, int id) {
        this.client.sendResponse(new FurniEditorResultComposer(ok, msg, id));
    }
}
