package com.eu.habbo.plugin.events.bots;

import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/bots/BotWhisperEvent.class */
public class BotWhisperEvent extends BotChatEvent {
    public Habbo target;

    public BotWhisperEvent(Bot bot, String str, Habbo habbo) {
        super(bot, str);
        this.target = habbo;
    }
}
