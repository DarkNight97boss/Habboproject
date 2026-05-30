package com.eu.habbo.messages.incoming.furnieditor;

import com.eu.habbo.Emulator;
import com.eu.habbo.core.AuditLog;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.furnieditor.FurniEditorResultComposer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 10045 FURNI_EDITOR_DELETE — remove a row from items_base.
 *
 * Safety: refused if any row in {@code items} still references the
 * base id. The {@code items} table has no DB-level FK in stock
 * Arcturus schemas, so a careless DELETE here would leave dangling
 * floor items pointing at a nonexistent base — they'd start NPE'ing
 * on room load. The pre-check makes the failure mode explicit (the
 * staff client sees "still in use by N items") instead of silent
 * corruption.
 */
public class FurniEditorDeleteEvent extends MessageHandler {

    @Override
    public void handle() throws Exception {
        if (!this.client.getHabbo().hasPermission(Permission.ACC_SUPPORTTOOL)) {
            ScripterManager.scripterDetected(this.client,
                    "Furni editor delete attempted without permission by "
                            + this.client.getHabbo().getHabboInfo().getUsername());
            return;
        }

        int id = this.packet.readInt();
        if (id <= 0) {
            reply(false, "Invalid id", id);
            return;
        }

        try (Connection conn = Emulator.getDatabase().getDataSource().getConnection()) {

            int usage = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM items WHERE item_id = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) usage = rs.getInt(1);
                }
            }
            if (usage > 0) {
                reply(false, "Cannot delete: still in use by " + usage + " item(s)", id);
                return;
            }

            int affected;
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM items_base WHERE id = ?")) {
                ps.setInt(1, id);
                affected = ps.executeUpdate();
            }
            if (affected == 0) {
                reply(false, "Item not found", id);
                return;
            }
        }

        // drop from the in-memory cache so future getItem(id) returns null
        try {
            Emulator.getGameEnvironment().getItemManager().getItems().remove(id);
        } catch (Throwable ignored) {
            // best-effort
        }

        AuditLog.record(
                this.client.getHabbo().getHabboInfo().getId(),
                this.client.getHabbo().getHabboInfo().getUsername(),
                "FURNI_EDITOR_DELETE",
                "items_base:" + id,
                ""
        );

        reply(true, "Deleted", id);
    }

    private void reply(boolean ok, String msg, int id) {
        this.client.sendResponse(new FurniEditorResultComposer(ok, msg, id));
    }
}
