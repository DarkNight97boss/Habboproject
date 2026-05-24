package com.eu.habbo.messages.outgoing.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.CatalogPage;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/catalog/CatalogPagesListComposer.class */
public class CatalogPagesListComposer extends MessageComposer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogPagesListComposer.class);
    private final Habbo habbo;
    private final String mode;
    private final boolean hasPermission;

    public CatalogPagesListComposer(Habbo habbo, String str) {
        this.habbo = habbo;
        this.mode = str;
        this.hasPermission = this.habbo.hasPermission(Permission.ACC_CATALOG_IDS);
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        try {
            List<CatalogPage> catalogPages = Emulator.getGameEnvironment().getCatalogManager().getCatalogPages(-1, this.habbo);
            this.response.init(Outgoing.CatalogPagesListComposer);
            this.response.appendBoolean(true);
            this.response.appendInt((Integer) 0);
            this.response.appendInt((Integer) (-1));
            this.response.appendString("root");
            this.response.appendString(Emulator.PREVIEW);
            this.response.appendInt((Integer) 0);
            this.response.appendInt(Integer.valueOf(catalogPages.size()));
            Iterator<CatalogPage> it = catalogPages.iterator();
            while (it.hasNext()) {
                append(it.next());
            }
            this.response.appendBoolean(false);
            this.response.appendString(this.mode);
            return this.response;
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
            return null;
        }
    }

    private void append(CatalogPage catalogPage) {
        List<CatalogPage> catalogPages = Emulator.getGameEnvironment().getCatalogManager().getCatalogPages(catalogPage.getId(), this.habbo);
        this.response.appendBoolean(Boolean.valueOf(catalogPage.isVisible()));
        this.response.appendInt(Integer.valueOf(catalogPage.getIconImage()));
        this.response.appendInt(Integer.valueOf(catalogPage.isEnabled() ? catalogPage.getId() : -1));
        this.response.appendString(catalogPage.getPageName());
        this.response.appendString(catalogPage.getCaption() + (this.hasPermission ? " (" + catalogPage.getId() + ")" : Emulator.PREVIEW));
        this.response.appendInt(Integer.valueOf(catalogPage.getOfferIds().size()));
        for (int i : catalogPage.getOfferIds().toArray()) {
            this.response.appendInt(Integer.valueOf(i));
        }
        this.response.appendInt(Integer.valueOf(catalogPages.size()));
        Iterator<CatalogPage> it = catalogPages.iterator();
        while (it.hasNext()) {
            append(it.next());
        }
    }
}
