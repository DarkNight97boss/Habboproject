package com.eu.habbo.messages.incoming.friends;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.friends.RoomInviteComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/friends/InviteFriendsEvent.class */
public class InviteFriendsEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Habbo habbo;
        if (this.client.getHabbo().getHabboStats().allowTalk()) {
            int[] iArr = new int[this.packet.readInt().intValue()];
            for (int i = 0; i < iArr.length; i++) {
                iArr[i] = this.packet.readInt().intValue();
            }
            String strFilter = Emulator.getGameEnvironment().getWordFilter().filter(this.packet.readString(), this.client.getHabbo());
            for (int i2 : iArr) {
                if (i2 != 0 && (habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(i2)) != null && !habbo.getHabboStats().blockRoomInvites) {
                    habbo.getClient().sendResponse(new RoomInviteComposer(this.client.getHabbo().getHabboInfo().getId(), strFilter));
                }
            }
        }
    }
}
