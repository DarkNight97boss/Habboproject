package com.eu.habbo.messages.outgoing.furnieditor;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

import java.util.List;

/**
 * 10040 FURNI_EDITOR_SEARCH_RESULT — paginated catalog metadata page.
 *
 * Wire shape (must match Nitro {@code FurniEditorSearchResultMessageParser}
 * + {@code FurniItemData}):
 *   int count
 *   repeat count:
 *     int id, int sprite_id, str item_name, str public_name, str type,
 *     int width, int length, double stack_height,
 *     bool allow_stack, bool allow_walk, bool allow_sit, bool allow_lay,
 *     str interaction_type, int interaction_modes_count
 *   int total, int page
 */
public class FurniEditorSearchResultComposer extends MessageComposer {

    private final List<FurniEditorItemRow> rows;
    private final int total;
    private final int page;

    public FurniEditorSearchResultComposer(List<FurniEditorItemRow> rows, int total, int page) {
        this.rows = rows;
        this.total = total;
        this.page = page;
    }

    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.FurniEditorSearchResultComposer);
        this.response.appendInt(this.rows.size());
        for (FurniEditorItemRow row : this.rows) {
            row.serializeBase(this.response);
        }
        this.response.appendInt(this.total);
        this.response.appendInt(this.page);
        return this.response;
    }
}
