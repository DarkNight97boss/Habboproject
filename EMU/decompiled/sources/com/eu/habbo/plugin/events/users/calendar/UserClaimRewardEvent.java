package com.eu.habbo.plugin.events.users.calendar;

import com.eu.habbo.habbohotel.campaign.calendar.CalendarCampaign;
import com.eu.habbo.habbohotel.campaign.calendar.CalendarRewardObject;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.plugin.events.users.UserEvent;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/calendar/UserClaimRewardEvent.class */
public class UserClaimRewardEvent extends UserEvent {
    public CalendarCampaign campaign;
    public int day;
    public CalendarRewardObject reward;
    public boolean force;

    public UserClaimRewardEvent(Habbo habbo, CalendarCampaign calendarCampaign, int i, CalendarRewardObject calendarRewardObject, boolean z) {
        super(habbo);
        this.campaign = calendarCampaign;
        this.day = i;
        this.reward = calendarRewardObject;
        this.force = z;
    }
}
