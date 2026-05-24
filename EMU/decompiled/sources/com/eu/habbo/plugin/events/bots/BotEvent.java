package com.eu.habbo.plugin.events.bots;

import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.plugin.Event;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/bots/BotEvent.class */
public abstract class BotEvent extends Event {
    public final Bot bot;

    public BotEvent(Bot bot) {
        this.bot = bot;
    }
}
