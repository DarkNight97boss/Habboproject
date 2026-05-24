package com.eu.habbo.messages.outgoing.events.calendar;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.campaign.calendar.CalendarManager;
import com.eu.habbo.habbohotel.campaign.calendar.CalendarRewardObject;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/events/calendar/AdventCalendarProductComposer.class */
public class AdventCalendarProductComposer extends MessageComposer {
    public final boolean visible;
    public final CalendarRewardObject rewardObject;
    public final Habbo habbo;

    public AdventCalendarProductComposer(boolean z, CalendarRewardObject calendarRewardObject, Habbo habbo) {
        this.visible = z;
        this.rewardObject = calendarRewardObject;
        this.habbo = habbo;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(2551);
        this.response.appendBoolean(Boolean.valueOf(this.visible));
        String name = Emulator.PREVIEW;
        String strReplace = this.rewardObject.getProductName().replace("%credits%", String.valueOf(this.rewardObject.getCredits())).replace("%pixels%", String.valueOf((int) (((double) this.rewardObject.getPixels()) * (this.habbo.getHabboStats().hasActiveClub() ? CalendarManager.HC_MODIFIER : 1.0d)))).replace("%points%", String.valueOf(this.rewardObject.getPoints())).replace("%points_type%", String.valueOf(this.rewardObject.getPointsType())).replace("%badge%", this.rewardObject.getBadge());
        if (this.rewardObject.getSubscriptionType() != null) {
            strReplace = strReplace.replace("%subscription_type%", this.rewardObject.getSubscriptionType()).replace("%subscription_days%", String.valueOf(this.rewardObject.getSubscriptionDays()));
        }
        if (this.rewardObject.getItem() != null) {
            strReplace = strReplace.replace("%item%", this.rewardObject.getItem().getName());
            name = this.rewardObject.getItem().getName();
        }
        this.response.appendString(strReplace);
        this.response.appendString(this.rewardObject.getCustomImage());
        this.response.appendString(name);
        return this.response;
    }
}
