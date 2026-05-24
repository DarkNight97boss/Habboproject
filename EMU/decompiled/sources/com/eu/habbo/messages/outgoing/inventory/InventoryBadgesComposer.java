package com.eu.habbo.messages.outgoing.inventory;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboBadge;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/inventory/InventoryBadgesComposer.class */
public class InventoryBadgesComposer extends MessageComposer {
    private final Habbo habbo;

    public InventoryBadgesComposer(Habbo habbo) {
        this.habbo = habbo;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        if (this.habbo == null) {
            return null;
        }
        THashSet tHashSet = new THashSet();
        this.response.init(Outgoing.InventoryBadgesComposer);
        this.response.appendInt(Integer.valueOf(this.habbo.getInventory().getBadgesComponent().getBadges().size()));
        TObjectHashIterator it = this.habbo.getInventory().getBadgesComponent().getBadges().iterator();
        while (it.hasNext()) {
            HabboBadge habboBadge = (HabboBadge) it.next();
            this.response.appendInt(Integer.valueOf(habboBadge.getId()));
            this.response.appendString(habboBadge.getCode());
            if (habboBadge.getSlot() > 0) {
                tHashSet.add(habboBadge);
            }
        }
        this.response.appendInt(Integer.valueOf(tHashSet.size()));
        TObjectHashIterator it2 = tHashSet.iterator();
        while (it2.hasNext()) {
            HabboBadge habboBadge2 = (HabboBadge) it2.next();
            this.response.appendInt(Integer.valueOf(habboBadge2.getSlot()));
            this.response.appendString(habboBadge2.getCode());
        }
        return this.response;
    }
}
