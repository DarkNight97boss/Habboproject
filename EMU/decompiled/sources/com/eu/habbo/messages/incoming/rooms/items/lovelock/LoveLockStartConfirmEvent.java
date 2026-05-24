package com.eu.habbo.messages.incoming.rooms.items.lovelock;

import com.eu.habbo.habbohotel.items.interactions.InteractionLoveLock;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.items.lovelock.LoveLockFurniFinishedComposer;
import com.eu.habbo.messages.outgoing.rooms.items.lovelock.LoveLockFurniFriendConfirmedComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/lovelock/LoveLockStartConfirmEvent.class */
public class LoveLockStartConfirmEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        HabboItem habboItem;
        Habbo habbo;
        int iIntValue = this.packet.readInt().intValue();
        if (!this.packet.readBoolean() || this.client.getHabbo().getHabboInfo().getCurrentRoom() == null || (habboItem = this.client.getHabbo().getHabboInfo().getCurrentRoom().getHabboItem(iIntValue)) == null || !(habboItem instanceof InteractionLoveLock)) {
            return;
        }
        int i = 0;
        if (((InteractionLoveLock) habboItem).userOneId == this.client.getHabbo().getHabboInfo().getId() && ((InteractionLoveLock) habboItem).userTwoId != 0) {
            i = ((InteractionLoveLock) habboItem).userTwoId;
        } else if (((InteractionLoveLock) habboItem).userOneId != 0 && ((InteractionLoveLock) habboItem).userTwoId == this.client.getHabbo().getHabboInfo().getId()) {
            i = ((InteractionLoveLock) habboItem).userOneId;
        }
        if (i <= 0 || (habbo = this.client.getHabbo().getHabboInfo().getCurrentRoom().getHabbo(i)) == null) {
            return;
        }
        habbo.getClient().sendResponse(new LoveLockFurniFriendConfirmedComposer((InteractionLoveLock) habboItem));
        habbo.getClient().sendResponse(new LoveLockFurniFinishedComposer((InteractionLoveLock) habboItem));
        this.client.sendResponse(new LoveLockFurniFinishedComposer((InteractionLoveLock) habboItem));
        ((InteractionLoveLock) habboItem).lock(habbo, this.client.getHabbo(), this.client.getHabbo().getHabboInfo().getCurrentRoom());
    }
}
