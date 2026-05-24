package com.eu.habbo.habbohotel.bots;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ModToolRoomVisit;
import com.eu.habbo.habbohotel.rooms.RoomChatMessage;
import com.eu.habbo.habbohotel.users.Habbo;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/bots/VisitorBot.class */
public class VisitorBot extends Bot {
    private static SimpleDateFormat DATE_FORMAT;
    private boolean showedLog;
    private THashSet<ModToolRoomVisit> visits;

    public VisitorBot(ResultSet resultSet) throws SQLException {
        super(resultSet);
        this.showedLog = false;
        this.visits = new THashSet<>(3);
    }

    public VisitorBot(Bot bot) {
        super(bot);
        this.showedLog = false;
        this.visits = new THashSet<>(3);
    }

    public static void initialise() {
        DATE_FORMAT = new SimpleDateFormat(Emulator.getConfig().getValue("bots.visitor.dateformat"));
    }

    @Override // com.eu.habbo.habbohotel.bots.Bot
    public void onUserSay(RoomChatMessage roomChatMessage) {
        if (this.showedLog || !roomChatMessage.getMessage().equalsIgnoreCase(Emulator.getTexts().getValue("generic.yes"))) {
            return;
        }
        this.showedLog = true;
        String strReplace = Emulator.getTexts().getValue("bots.visitor.list").replace("%count%", this.visits.size() + Emulator.PREVIEW);
        StringBuilder sb = new StringBuilder();
        TObjectHashIterator it = this.visits.iterator();
        while (it.hasNext()) {
            ModToolRoomVisit modToolRoomVisit = (ModToolRoomVisit) it.next();
            sb.append("\r");
            sb.append(modToolRoomVisit.roomName).append(" ");
            sb.append(Emulator.getTexts().getValue("generic.time.at")).append(" ");
            sb.append(DATE_FORMAT.format(new Date(((long) modToolRoomVisit.timestamp) * 1000)));
        }
        talk(strReplace.replace("%list%", sb.toString()));
        this.visits.clear();
    }

    public void onUserEnter(Habbo habbo) {
        if (this.showedLog || habbo.getHabboInfo().getCurrentRoom() == null) {
            return;
        }
        this.visits = Emulator.getGameEnvironment().getModToolManager().getVisitsForRoom(habbo.getHabboInfo().getCurrentRoom(), 10, true, habbo.getHabboInfo().getLastOnline(), Emulator.getIntUnixTimestamp(), habbo.getHabboInfo().getCurrentRoom().getOwnerName());
        if (this.visits.isEmpty()) {
            talk(Emulator.getTexts().getValue("bots.visitor.no_visits"));
        } else {
            talk(Emulator.getTexts().getValue("bots.visitor.visits").replace("%count%", this.visits.size() + Emulator.PREVIEW).replace("%positive%", Emulator.getTexts().getValue("generic.yes")));
        }
    }
}
