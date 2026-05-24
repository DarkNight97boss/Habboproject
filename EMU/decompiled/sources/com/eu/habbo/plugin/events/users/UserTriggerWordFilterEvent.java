package com.eu.habbo.plugin.events.users;

import com.eu.habbo.habbohotel.modtool.WordFilterWord;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/UserTriggerWordFilterEvent.class */
public class UserTriggerWordFilterEvent extends UserEvent {
    public final WordFilterWord word;

    public UserTriggerWordFilterEvent(Habbo habbo, WordFilterWord wordFilterWord) {
        super(habbo);
        this.word = wordFilterWord;
    }
}
