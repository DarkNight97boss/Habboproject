package com.eu.habbo.messages.incoming.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.HabboBadge;
import com.eu.habbo.habbohotel.users.inventory.BadgesComponent;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.users.UserBadgesComposer;
import java.util.ArrayList;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/users/UserWearBadgeEvent.class */
public class UserWearBadgeEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        HabboBadge badge;
        BadgesComponent.resetSlots(this.client.getHabbo());
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        for (int i = 0; i < 5; i++) {
            int iIntValue = this.packet.readInt().intValue();
            if (iIntValue < 1 || iIntValue > 5) {
                return;
            }
            String string = this.packet.readString();
            if (string.length() != 0 && (badge = this.client.getHabbo().getInventory().getBadgesComponent().getBadge(string)) != null && !arrayList.contains(badge) && !arrayList2.contains(Integer.valueOf(iIntValue))) {
                arrayList2.add(Integer.valueOf(iIntValue));
                badge.setSlot(iIntValue);
                badge.needsUpdate(true);
                Emulator.getThreading().run(badge);
                arrayList.add(badge);
            }
        }
        if (this.client.getHabbo().getHabboInfo().getCurrentRoom() != null) {
            this.client.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new UserBadgesComposer(arrayList, this.client.getHabbo().getHabboInfo().getId()).compose());
        } else {
            this.client.sendResponse(new UserBadgesComposer(arrayList, this.client.getHabbo().getHabboInfo().getId()));
        }
    }
}
