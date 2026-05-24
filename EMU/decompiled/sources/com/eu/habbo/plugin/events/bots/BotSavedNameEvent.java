package com.eu.habbo.plugin.events.bots;

import com.eu.habbo.habbohotel.bots.Bot;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/bots/BotSavedNameEvent.class */
public class BotSavedNameEvent extends BotEvent {
    public String name;

    public BotSavedNameEvent(Bot bot, String str) {
        super(bot);
        this.name = str;
    }
}
