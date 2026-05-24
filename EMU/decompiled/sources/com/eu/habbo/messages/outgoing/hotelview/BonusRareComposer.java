package com.eu.habbo.messages.outgoing.hotelview;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/hotelview/BonusRareComposer.class */
public class BonusRareComposer extends MessageComposer {
    private final Habbo habbo;

    public BonusRareComposer(Habbo habbo) {
        this.habbo = habbo;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(1533);
        this.response.appendString(Emulator.getConfig().getValue("hotelview.promotional.reward.name", "prizetrophy_breed_gold"));
        this.response.appendInt(Integer.valueOf(Emulator.getConfig().getInt("hotelview.promotional.reward.id", 0)));
        this.response.appendInt(Integer.valueOf(Emulator.getConfig().getInt("hotelview.promotional.points", 120)));
        int i = Emulator.getConfig().getInt("hotelview.promotional.points", 120) - this.habbo.getHabboInfo().getBonusRarePoints();
        this.response.appendInt(Integer.valueOf(i < 0 ? 0 : i));
        return this.response;
    }
}
