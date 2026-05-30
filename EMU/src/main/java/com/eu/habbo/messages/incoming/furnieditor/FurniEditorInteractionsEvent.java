package com.eu.habbo.messages.incoming.furnieditor;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.furnieditor.FurniEditorInteractionsResultComposer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 10043 FURNI_EDITOR_INTERACTIONS — distinct interaction_type values from
 * items_base, used by the editor's "interaction" dropdown.
 *
 * No args. Result is fixed-size (~150 interactions on a full install),
 * so we read the whole list and stream it back in one composer call.
 */
public class FurniEditorInteractionsEvent extends MessageHandler {

    @Override
    public void handle() throws Exception {
        if (!this.client.getHabbo().hasPermission(Permission.ACC_SUPPORTTOOL)) {
            ScripterManager.scripterDetected(this.client,
                    "Furni editor interactions list attempted without permission by "
                            + this.client.getHabbo().getHabboInfo().getUsername());
            return;
        }

        List<String> interactions = new ArrayList<>();

        try (Connection conn = Emulator.getDatabase().getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT DISTINCT interaction_type FROM items_base " +
                             "WHERE interaction_type IS NOT NULL AND interaction_type <> '' " +
                             "ORDER BY interaction_type");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) interactions.add(rs.getString(1));
        }

        this.client.sendResponse(new FurniEditorInteractionsResultComposer(interactions));
    }
}
