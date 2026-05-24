package com.eu.habbo.plugin.events.users;

import com.eu.habbo.habbohotel.items.interactions.wired.effects.WiredEffectGiveReward;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/UserWiredRewardReceived.class */
public class UserWiredRewardReceived extends UserEvent {
    public final WiredEffectGiveReward wiredEffectGiveReward;
    public final String type;
    public String value;

    public UserWiredRewardReceived(Habbo habbo, WiredEffectGiveReward wiredEffectGiveReward, String str, String str2) {
        super(habbo);
        this.wiredEffectGiveReward = wiredEffectGiveReward;
        this.type = str;
        this.value = str2;
    }
}
