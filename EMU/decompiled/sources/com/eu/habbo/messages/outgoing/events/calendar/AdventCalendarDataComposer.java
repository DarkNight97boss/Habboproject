package com.eu.habbo.messages.outgoing.events.calendar;

import com.eu.habbo.habbohotel.campaign.calendar.CalendarRewardClaimed;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.list.array.TIntArrayList;
import java.util.ArrayList;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/events/calendar/AdventCalendarDataComposer.class */
public class AdventCalendarDataComposer extends MessageComposer {
    private final String eventName;
    private final String campaignImage;
    private final int totalDays;
    private final int currentDay;
    private final ArrayList<CalendarRewardClaimed> unlocked;
    private final boolean lockExpired;

    public AdventCalendarDataComposer(String str, String str2, int i, int i2, ArrayList<CalendarRewardClaimed> arrayList, boolean z) {
        this.eventName = str;
        this.campaignImage = str2;
        this.totalDays = i;
        this.currentDay = i2;
        this.unlocked = arrayList;
        this.lockExpired = z;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.AdventCalendarDataComposer);
        this.response.appendString(this.eventName);
        this.response.appendString(this.campaignImage);
        this.response.appendInt(Integer.valueOf(this.currentDay));
        this.response.appendInt(Integer.valueOf(this.totalDays));
        this.response.appendInt(Integer.valueOf(this.unlocked.size()));
        TIntArrayList tIntArrayList = new TIntArrayList();
        if (this.lockExpired) {
            for (int i = 0; i < this.totalDays; i++) {
                tIntArrayList.add(i);
            }
        }
        tIntArrayList.remove(this.currentDay);
        if (this.currentDay > 1) {
            tIntArrayList.remove(this.currentDay - 2);
        }
        if (this.currentDay > 0) {
            tIntArrayList.remove(this.currentDay - 1);
        }
        this.unlocked.forEach(calendarRewardClaimed -> {
            this.response.appendInt(Integer.valueOf(calendarRewardClaimed.getDay()));
            tIntArrayList.remove(calendarRewardClaimed.getDay());
        });
        if (this.lockExpired) {
            this.response.appendInt(Integer.valueOf(tIntArrayList.size()));
            tIntArrayList.forEach(i2 -> {
                this.response.appendInt(Integer.valueOf(i2));
                return true;
            });
        } else {
            this.response.appendInt((Integer) 0);
        }
        return this.response;
    }
}
