package com.eu.habbo.plugin.events.bots;

import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.users.HabboGender;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/bots/BotSavedLookEvent.class */
public class BotSavedLookEvent extends BotEvent {
    public HabboGender gender;
    public String newLook;
    public int effect;

    public BotSavedLookEvent(Bot bot, HabboGender habboGender, String str, int i) {
        super(bot);
        this.gender = habboGender;
        this.newLook = str;
        this.effect = i;
    }
}
