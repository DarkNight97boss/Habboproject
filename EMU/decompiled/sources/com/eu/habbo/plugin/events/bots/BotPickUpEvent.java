package com.eu.habbo.plugin.events.bots;

import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/bots/BotPickUpEvent.class */
public class BotPickUpEvent extends BotEvent {
    public final Habbo picker;

    public BotPickUpEvent(Bot bot, Habbo habbo) {
        super(bot);
        this.picker = habbo;
    }
}
