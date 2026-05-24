package com.eu.habbo.messages.incoming.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ModToolSanctionItem;
import com.eu.habbo.habbohotel.modtool.ModToolSanctions;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.modtool.ModToolIssueHandledComposer;
import java.util.ArrayList;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/modtool/ModToolSanctionAlertEvent.class */
public class ModToolSanctionAlertEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        String string = this.packet.readString();
        int iIntValue2 = this.packet.readInt().intValue();
        if (this.client.getHabbo().hasPermission(Permission.ACC_SUPPORTTOOL)) {
            Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(iIntValue);
            if (habbo == null) {
                this.client.sendResponse(new ModToolIssueHandledComposer(Emulator.getTexts().getValue("generic.user.not_found").replace("%user%", Emulator.getConfig().getValue("hotel.player.name"))));
                return;
            }
            ModToolSanctions modToolSanctions = Emulator.getGameEnvironment().getModToolSanctions();
            if (!Emulator.getConfig().getBoolean("hotel.sanctions.enabled")) {
                habbo.alert(string);
                return;
            }
            ArrayList arrayList = (ArrayList) Emulator.getGameEnvironment().getModToolSanctions().getSanctions(habbo.getHabboInfo().getId()).get(Integer.valueOf(habbo.getHabboInfo().getId()));
            if (arrayList == null || arrayList.isEmpty()) {
                modToolSanctions.run(iIntValue, this.client.getHabbo(), 0, iIntValue2, string, 0, false, 0);
                return;
            }
            ModToolSanctionItem modToolSanctionItem = (ModToolSanctionItem) arrayList.get(arrayList.size() - 1);
            if (modToolSanctionItem == null || modToolSanctionItem.probationTimestamp <= 0 || modToolSanctionItem.probationTimestamp < Emulator.getIntUnixTimestamp()) {
                modToolSanctions.run(iIntValue, this.client.getHabbo(), modToolSanctionItem.sanctionLevel, iIntValue2, string, 0, false, 0);
            } else {
                modToolSanctions.run(iIntValue, this.client.getHabbo(), modToolSanctionItem.sanctionLevel, iIntValue2, string, 0, false, 0);
            }
        }
    }
}
