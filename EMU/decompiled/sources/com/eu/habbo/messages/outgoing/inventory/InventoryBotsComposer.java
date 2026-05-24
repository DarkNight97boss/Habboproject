package com.eu.habbo.messages.outgoing.inventory;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import gnu.trove.map.hash.THashMap;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/inventory/InventoryBotsComposer.class */
public class InventoryBotsComposer extends MessageComposer {
    private final Habbo habbo;

    public InventoryBotsComposer(Habbo habbo) {
        this.habbo = habbo;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(3086);
        THashMap<Integer, Bot> bots = this.habbo.getInventory().getBotsComponent().getBots();
        this.response.appendInt(Integer.valueOf(bots.size()));
        for (Bot bot : bots.values()) {
            this.response.appendInt(Integer.valueOf(bot.getId()));
            this.response.appendString(bot.getName());
            this.response.appendString(bot.getMotto());
            this.response.appendString(bot.getGender().toString().toLowerCase().charAt(0) + Emulator.PREVIEW);
            this.response.appendString(bot.getFigure());
        }
        return this.response;
    }
}
