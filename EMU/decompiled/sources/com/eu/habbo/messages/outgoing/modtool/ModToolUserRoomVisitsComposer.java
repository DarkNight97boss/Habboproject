package com.eu.habbo.messages.outgoing.modtool;

import com.eu.habbo.habbohotel.modtool.ModToolRoomVisit;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.util.Calendar;
import java.util.TimeZone;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/modtool/ModToolUserRoomVisitsComposer.class */
public class ModToolUserRoomVisitsComposer extends MessageComposer {
    private final HabboInfo habboInfo;
    private final THashSet<ModToolRoomVisit> roomVisits;

    public ModToolUserRoomVisitsComposer(HabboInfo habboInfo, THashSet<ModToolRoomVisit> tHashSet) {
        this.habboInfo = habboInfo;
        this.roomVisits = tHashSet;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(1752);
        this.response.appendInt(Integer.valueOf(this.habboInfo.getId()));
        this.response.appendString(this.habboInfo.getUsername());
        this.response.appendInt(Integer.valueOf(this.roomVisits.size()));
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        TObjectHashIterator it = this.roomVisits.iterator();
        while (it.hasNext()) {
            ModToolRoomVisit modToolRoomVisit = (ModToolRoomVisit) it.next();
            calendar.setTimeInMillis(modToolRoomVisit.timestamp * Outgoing.CraftableProductsComposer);
            this.response.appendInt(Integer.valueOf(modToolRoomVisit.roomId));
            this.response.appendString(modToolRoomVisit.roomName);
            this.response.appendInt(Integer.valueOf(calendar.get(10)));
            this.response.appendInt(Integer.valueOf(calendar.get(12)));
        }
        return this.response;
    }
}
