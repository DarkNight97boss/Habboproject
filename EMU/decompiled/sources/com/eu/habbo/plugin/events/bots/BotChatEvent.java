package com.eu.habbo.plugin.events.bots;

import com.eu.habbo.habbohotel.bots.Bot;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/bots/BotChatEvent.class */
public abstract class BotChatEvent extends BotEvent {
    public String message;

    public BotChatEvent(Bot bot, String str) {
        super(bot);
        this.message = str;
    }
}
