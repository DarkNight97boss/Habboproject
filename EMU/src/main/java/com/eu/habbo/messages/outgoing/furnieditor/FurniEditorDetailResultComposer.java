package com.eu.habbo.messages.outgoing.furnieditor;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

import java.util.List;

/**
 * 10041 FURNI_EDITOR_DETAIL_RESULT — single item + linked catalog rows.
 *
 * Wire shape (must match Nitro {@code FurniEditorDetailResultMessageParser}
 * + {@code FurniDetailData} + {@code CatalogRefData}):
 *   FurniItemData base block (14 fields)
 *   FurniDetailData extension (13 fields)
 *   int catalogCount
 *   repeat catalogCount: CatalogRefData (7 fields)
 *   str furniDataJson  (empty string — EMU does not host furnidata.xml)
 */
public class FurniEditorDetailResultComposer extends MessageComposer {

    private final FurniEditorItemRow row;
    private final List<FurniEditorCatalogRef> catalogRefs;
    private final String furniDataJson;

    public FurniEditorDetailResultComposer(FurniEditorItemRow row,
                                           List<FurniEditorCatalogRef> catalogRefs,
                                           String furniDataJson) {
        this.row = row;
        this.catalogRefs = catalogRefs;
        this.furniDataJson = furniDataJson == null ? "" : furniDataJson;
    }

    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.FurniEditorDetailResultComposer);
        this.row.serializeBase(this.response);
        this.row.serializeDetailExtension(this.response);
        this.response.appendInt(this.catalogRefs.size());
        for (FurniEditorCatalogRef ref : this.catalogRefs) {
            ref.serialize(this.response);
        }
        this.response.appendString(this.furniDataJson);
        return this.response;
    }
}
