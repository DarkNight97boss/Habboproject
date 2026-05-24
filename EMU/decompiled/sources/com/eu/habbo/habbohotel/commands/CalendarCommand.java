package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.campaign.calendar.CalendarCampaign;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.messages.outgoing.events.calendar.AdventCalendarDataComposer;
import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/CalendarCommand.class */
public class CalendarCommand extends Command {
    public CalendarCommand() {
        super("cmd_calendar", Emulator.getTexts().getValue("commands.keys.cmd_calendar").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (!Emulator.getConfig().getBoolean("hotel.calendar.enabled")) {
            return true;
        }
        String value = Emulator.getConfig().getValue("hotel.calendar.default");
        if (strArr.length > 1 && gameClient.getHabbo().hasPermission("cmd_calendar_staff")) {
            value = strArr[1];
        }
        CalendarCampaign calendarCampaign = Emulator.getGameEnvironment().getCalendarManager().getCalendarCampaign(value);
        if (calendarCampaign == null) {
            return false;
        }
        int iBetween = (int) ChronoUnit.DAYS.between(new Timestamp(((long) calendarCampaign.getStartTimestamp().intValue()) * 1000).toInstant(), new Date().toInstant());
        if (iBetween < 0) {
            return true;
        }
        gameClient.sendResponse(new AdventCalendarDataComposer(calendarCampaign.getName(), calendarCampaign.getImage(), calendarCampaign.getTotalDays(), iBetween, gameClient.getHabbo().getHabboStats().calendarRewardsClaimed, calendarCampaign.getLockExpired()));
        return true;
    }
}
