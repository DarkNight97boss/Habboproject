package com.eu.habbo.plugin.events.bots;

import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/bots/BotServerItemEvent.class */
public class BotServerItemEvent extends BotEvent {
    public Habbo habbo;
    public int itemId;

    public BotServerItemEvent(Bot bot, Habbo habbo, int i) {
        super(bot);
        this.habbo = habbo;
        this.itemId = i;
    }
}
