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
 * 10042 FURNI_EDITOR_BY_SPRITE — reverse lookup: "which items_base rows
 * use this sprite_id?". Same wire shape as {@link FurniEditorSearchEvent}
 * (paginated search result) with {@code page=1, total=rows.size()} since
 * sprite collisions are bounded and we return all matches in one shot.
 */
public class FurniEditorBySpriteEvent extends MessageHandler {

    @Override
    public void handle() throws Exception {
        if (!this.client.getHabbo().hasPermission(Permission.ACC_SUPPORTTOOL)) {
            ScripterManager.scripterDetected(this.client,
                    "Furni editor by-sprite attempted without permission by "
                            + this.client.getHabbo().getHabboInfo().getUsername());
            return;
        }

        int spriteId = this.packet.readInt();
        if (spriteId <= 0) return;

        List<FurniEditorItemRow> rows = new ArrayList<>();

        try (Connection conn = Emulator.getDatabase().getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT " + FurniEditorRowMapper.COLUMNS
                             + " FROM items_base WHERE sprite_id = ? ORDER BY id LIMIT 100")) {
            ps.setInt(1, spriteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) rows.add(FurniEditorRowMapper.map(rs));
            }
        }

        this.client.sendResponse(new FurniEditorSearchResultComposer(rows, rows.size(), 1));
    }
}
