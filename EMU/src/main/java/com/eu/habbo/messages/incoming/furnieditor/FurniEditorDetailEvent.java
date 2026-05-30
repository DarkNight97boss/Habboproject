package com.eu.habbo.messages.incoming.furnieditor;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.furnieditor.FurniEditorCatalogRef;
import com.eu.habbo.messages.outgoing.furnieditor.FurniEditorDetailResultComposer;
import com.eu.habbo.messages.outgoing.furnieditor.FurniEditorItemRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 10041 FURNI_EDITOR_DETAIL — full row + catalog references + usage count.
 *
 * Catalog lookup uses {@code item_Ids} (CSV column) with the
 * {@code CONCAT(',', col, ',') LIKE '%,id,%'} pattern so partial matches
 * (e.g. id=4 matching "14,42") cannot fire false positives.
 *
 * The {@code furniDataJson} field is always emitted as {@code ""} — the
 * EMU does not host furnidata.xml (the CMS serves it), so the editor UI
 * shows the JDBC snapshot only.
 */
public class FurniEditorDetailEvent extends MessageHandler {

    @Override
    public void handle() throws Exception {
        if (!this.client.getHabbo().hasPermission(Permission.ACC_SUPPORTTOOL)) {
            ScripterManager.scripterDetected(this.client,
                    "Furni editor detail attempted without permission by "
                            + this.client.getHabbo().getHabboInfo().getUsername());
            return;
        }

        int id = this.packet.readInt();
        if (id <= 0) return;

        FurniEditorItemRow row = null;
        List<FurniEditorCatalogRef> refs = new ArrayList<>();

        try (Connection conn = Emulator.getDatabase().getDataSource().getConnection()) {

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT " + FurniEditorRowMapper.COLUMNS
                            + " FROM items_base WHERE id = ? LIMIT 1")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) row = FurniEditorRowMapper.map(rs);
                }
            }
            if (row == null) return;  // silent drop — no row, no reply

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM items WHERE item_id = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) row.usageCount = rs.getInt(1);
                }
            }

            // catalog_items.item_Ids is CSV — wrap with commas to avoid
            // matching "4" inside "14,42".
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT ci.id, ci.catalog_name, ci.cost_credits, ci.cost_points, " +
                    "ci.points_type, ci.page_id, cp.caption " +
                    "FROM catalog_items ci LEFT JOIN catalog_pages cp ON cp.id = ci.page_id " +
                    "WHERE CONCAT(',', REPLACE(ci.item_Ids, ' ', ''), ',') LIKE ? " +
                    "ORDER BY ci.id LIMIT 200")) {
                ps.setString(1, "%," + id + ",%");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        FurniEditorCatalogRef ref = new FurniEditorCatalogRef();
                        ref.id = rs.getInt("id");
                        ref.catalogName = rs.getString("catalog_name");
                        ref.costCredits = rs.getInt("cost_credits");
                        ref.costPoints = rs.getInt("cost_points");
                        ref.pointsType = rs.getInt("points_type");
                        ref.pageId = rs.getInt("page_id");
                        ref.pageName = rs.getString("caption");
                        refs.add(ref);
                    }
                }
            }
        }

        this.client.sendResponse(new FurniEditorDetailResultComposer(row, refs, ""));
    }
}
