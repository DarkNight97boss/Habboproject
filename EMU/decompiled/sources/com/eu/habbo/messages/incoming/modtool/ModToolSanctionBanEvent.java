package com.eu.habbo.messages.incoming.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ModToolBanType;
import com.eu.habbo.habbohotel.modtool.ModToolSanctionItem;
import com.eu.habbo.habbohotel.modtool.ModToolSanctions;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.messages.incoming.MessageHandler;
import gnu.trove.map.hash.THashMap;
import java.util.ArrayList;
import java.util.Objects;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/modtool/ModToolSanctionBanEvent.class */
public class ModToolSanctionBanEvent extends MessageHandler {
    public static final int BAN_18_HOURS = 3;
    public static final int BAN_7_DAYS = 4;
    public static final int BAN_30_DAYS_STEP_1 = 5;
    public static final int BAN_30_DAYS_STEP_2 = 7;
    public static final int BAN_100_YEARS = 6;
    public static final int BAN_AVATAR_ONLY_100_YEARS = 106;
    public final int DAY_IN_SECONDS = 86400;

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        String string = this.packet.readString();
        int iIntValue2 = this.packet.readInt().intValue();
        int iIntValue3 = this.packet.readInt().intValue();
        this.packet.readBoolean();
        int intUnixTimestamp = 0;
        switch (iIntValue3) {
            case 3:
                intUnixTimestamp = 64800;
                break;
            case 4:
                Objects.requireNonNull(this);
                intUnixTimestamp = 7 * 86400;
                break;
            case 5:
            case 7:
                Objects.requireNonNull(this);
                intUnixTimestamp = 30 * 86400;
                break;
            case 6:
            case BAN_AVATAR_ONLY_100_YEARS /* 106 */:
                intUnixTimestamp = Emulator.getIntUnixTimestamp();
                break;
        }
        if (!this.client.getHabbo().hasPermission(Permission.ACC_SUPPORTTOOL)) {
            ScripterManager.scripterDetected(this.client, Emulator.getTexts().getValue("scripter.warning.modtools.ban").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()));
            return;
        }
        ModToolSanctions modToolSanctions = Emulator.getGameEnvironment().getModToolSanctions();
        if (!Emulator.getConfig().getBoolean("hotel.sanctions.enabled")) {
            Emulator.getGameEnvironment().getModToolManager().ban(iIntValue, this.client.getHabbo(), string, intUnixTimestamp, ModToolBanType.ACCOUNT, iIntValue2);
            return;
        }
        THashMap<Integer, ArrayList<ModToolSanctionItem>> sanctions = Emulator.getGameEnvironment().getModToolSanctions().getSanctions(iIntValue);
        ArrayList arrayList = (ArrayList) sanctions.get(Integer.valueOf(iIntValue));
        if (arrayList == null || sanctions.isEmpty()) {
            modToolSanctions.run(iIntValue, this.client.getHabbo(), 0, iIntValue2, string, 0, false, 0);
            return;
        }
        ModToolSanctionItem modToolSanctionItem = (ModToolSanctionItem) arrayList.get(arrayList.size() - 1);
        if (modToolSanctionItem.probationTimestamp <= 0 || modToolSanctionItem.probationTimestamp < Emulator.getIntUnixTimestamp()) {
            modToolSanctions.run(iIntValue, this.client.getHabbo(), modToolSanctionItem.sanctionLevel, iIntValue2, string, 0, false, 0);
        } else {
            modToolSanctions.run(iIntValue, this.client.getHabbo(), modToolSanctionItem.sanctionLevel, iIntValue2, string, 0, false, 0);
        }
    }
}
