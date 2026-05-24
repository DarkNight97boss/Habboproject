package com.eu.habbo.messages.outgoing.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ModToolSanctionItem;
import com.eu.habbo.habbohotel.modtool.ModToolSanctionLevelItem;
import com.eu.habbo.habbohotel.modtool.ModToolSanctions;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.ArrayList;
import java.util.Date;
import org.joda.time.DateTime;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/modtool/ModToolSanctionInfoComposer.class */
public class ModToolSanctionInfoComposer extends MessageComposer {
    private final Habbo habbo;

    public ModToolSanctionInfoComposer(Habbo habbo) {
        this.habbo = habbo;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        ModToolSanctions modToolSanctions = Emulator.getGameEnvironment().getModToolSanctions();
        if (Emulator.getConfig().getBoolean("hotel.sanctions.enabled")) {
            ArrayList arrayList = (ArrayList) Emulator.getGameEnvironment().getModToolSanctions().getSanctions(this.habbo.getHabboInfo().getId()).get(Integer.valueOf(this.habbo.getHabboInfo().getId()));
            if (arrayList == null || arrayList.size() <= 0) {
                return cleanResponse();
            }
            ModToolSanctionItem modToolSanctionItem = (ModToolSanctionItem) arrayList.get(arrayList.size() - 1);
            ModToolSanctionItem modToolSanctionItem2 = null;
            if (arrayList.size() > 1 && arrayList.get(arrayList.size() - 2) != null) {
                modToolSanctionItem2 = (ModToolSanctionItem) arrayList.get(arrayList.size() - 2);
            }
            ModToolSanctionLevelItem sanctionLevelItem = modToolSanctions.getSanctionLevelItem(modToolSanctionItem.sanctionLevel);
            ModToolSanctionLevelItem sanctionLevelItem2 = modToolSanctions.getSanctionLevelItem(modToolSanctionItem.sanctionLevel + 1);
            if (modToolSanctionItem.probationTimestamp <= 0) {
                return cleanResponse();
            }
            Date date = new DateTime(new Date(((long) modToolSanctionItem.probationTimestamp) * 1000)).minusDays(modToolSanctions.getProbationDays(sanctionLevelItem)).toDate();
            Date date2 = null;
            if (modToolSanctionItem.tradeLockedUntil > 0) {
                date2 = new Date(((long) modToolSanctionItem.tradeLockedUntil) * 1000);
            }
            this.response.init(Outgoing.ModToolSanctionInfoComposer);
            this.response.appendBoolean(Boolean.valueOf(modToolSanctionItem2 != null && modToolSanctionItem2.probationTimestamp > 0));
            this.response.appendBoolean(Boolean.valueOf(modToolSanctionItem.probationTimestamp >= Emulator.getIntUnixTimestamp()));
            this.response.appendString(modToolSanctions.getSanctionType(sanctionLevelItem));
            this.response.appendInt(Integer.valueOf(modToolSanctions.getTimeOfSanction(sanctionLevelItem)));
            this.response.appendInt((Integer) 30);
            this.response.appendString(modToolSanctionItem.reason.equals(Emulator.PREVIEW) ? "cfh.reason.EMPTY" : modToolSanctionItem.reason);
            this.response.appendString(date == null ? Emulator.getDate().toString() : date.toString());
            this.response.appendInt((Integer) 0);
            this.response.appendString(modToolSanctions.getSanctionType(sanctionLevelItem2));
            this.response.appendInt(Integer.valueOf(modToolSanctions.getTimeOfSanction(sanctionLevelItem2)));
            this.response.appendInt((Integer) 30);
            this.response.appendBoolean(Boolean.valueOf(modToolSanctionItem.isMuted));
            this.response.appendString(date2 == null ? Emulator.PREVIEW : date2.toString());
        }
        return this.response;
    }

    private ServerMessage cleanResponse() {
        this.response.init(Outgoing.ModToolSanctionInfoComposer);
        this.response.appendBoolean(false);
        this.response.appendBoolean(false);
        this.response.appendString("ALERT");
        this.response.appendInt((Integer) 0);
        this.response.appendInt((Integer) 30);
        this.response.appendString("cfh.reason.EMPTY");
        this.response.appendString(Emulator.getDate().toString());
        this.response.appendInt((Integer) 0);
        this.response.appendString("ALERT");
        this.response.appendInt((Integer) 0);
        this.response.appendInt((Integer) 30);
        this.response.appendBoolean(false);
        this.response.appendString(Emulator.PREVIEW);
        return this.response;
    }
}
