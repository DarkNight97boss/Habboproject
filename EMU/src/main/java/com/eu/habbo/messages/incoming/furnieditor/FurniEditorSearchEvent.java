package com.eu.habbo.messages.incoming.furnieditor;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.furnieditor.FurniEditorItemRow;
import com.eu.habbo.messages.outgoing.furnieditor.FurniEditorSearchResultComposer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 10040 FURNI_EDITOR_SEARCH — staff catalog browse.
 *
 * Args: {@code (string query, string type, int page)}.
 *  - query is trimmed and length-capped at 64 chars (defensive — the
 *    client UI caps at ~32 but a hostile packet could send more).
 *  - type must be one of {@code s|i|e}; anything else (including the
 *    "any" sentinel) means no type filter.
 *  - page is clamped to {@code [1, +inf)}; page size is fixed at 20.
 */
public class FurniEditorSearchEvent extends MessageHandler {

    private static final int PAGE_SIZE = 20;
    private static final int MAX_QUERY_LEN = 64;

    @Override
    public void handle() throws Exception {
        if (!this.client.getHabbo().hasPermission(Permission.ACC_SUPPORTTOOL)) {
            ScripterManager.scripterDetected(this.client,
                    "Furni editor search attempted without permission by "
                            + this.client.getHabbo().getHabboInfo().getUsername());
            return;
        }

        String rawQuery = this.packet.readString();
        String rawType = this.packet.readString();
        int page = this.packet.readInt();

        String query = rawQuery == null ? "" : rawQuery.trim();
        if (query.length() > MAX_QUERY_LEN) query = query.substring(0, MAX_QUERY_LEN);
        if (page < 1) page = 1;

        // type filter: only s/i/e are real items_base.type values. Anything
        // else (empty string, "any", garbage) means "no filter".
        String typeFilter = null;
        if (rawType != null) {
            String t = rawType.trim().toLowerCase();
            if (t.equals("s") || t.equals("i") || t.equals("e")) typeFilter = t;
        }

        List<FurniEditorItemRow> rows = new ArrayList<>();
        int total = 0;

        StringBuilder where = new StringBuilder(" WHERE 1=1");
        if (!query.isEmpty()) where.append(" AND (item_name LIKE ? OR public_name LIKE ?)");
        if (typeFilter != null) where.append(" AND type = ?");

        String countSql = "SELECT COUNT(*) FROM items_base" + where;
        String dataSql = "SELECT " + FurniEditorRowMapper.COLUMNS + " FROM items_base"
                + where + " ORDER BY id DESC LIMIT ? OFFSET ?";

        try (Connection conn = Emulator.getDatabase().getDataSource().getConnection()) {
            // COUNT (for paginator)
            try (PreparedStatement ps = conn.prepareStatement(countSql)) {
                int idx = 1;
                if (!query.isEmpty()) {
                    String like = "%" + query + "%";
                    ps.setString(idx++, like);
                    ps.setString(idx++, like);
                }
                if (typeFilter != null) ps.setString(idx, typeFilter);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) total = rs.getInt(1);
                }
            }

            // DATA page
            try (PreparedStatement ps = conn.prepareStatement(dataSql)) {
                int idx = 1;
                if (!query.isEmpty()) {
                    String like = "%" + query + "%";
                    ps.setString(idx++, like);
                    ps.setString(idx++, like);
                }
                if (typeFilter != null) ps.setString(idx++, typeFilter);
                ps.setInt(idx++, PAGE_SIZE);
                ps.setInt(idx, (page - 1) * PAGE_SIZE);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) rows.add(FurniEditorRowMapper.map(rs));
                }
            }
        }

        this.client.sendResponse(new FurniEditorSearchResultComposer(rows, total, page));
    }
}
