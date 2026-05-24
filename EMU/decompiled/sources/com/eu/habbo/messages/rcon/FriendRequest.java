package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.messenger.Messenger;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboManager;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import com.eu.habbo.messages.outgoing.friends.FriendRequestComposer;
import com.google.gson.Gson;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/FriendRequest.class */
public class FriendRequest extends RCONMessage<JSON> {

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/FriendRequest$JSON.class */
    static class JSON {
        public int user_id;
        public int target_id;

        JSON() {
        }
    }

    public FriendRequest() {
        super(JSON.class);
    }

    @Override // com.eu.habbo.messages.rcon.RCONMessage
    public void handle(Gson gson, JSON json) {
        if (Messenger.friendRequested(json.user_id, json.target_id)) {
            return;
        }
        Messenger.makeFriendRequest(json.user_id, json.target_id);
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(json.target_id);
        if (habbo != null) {
            Habbo habbo2 = Emulator.getGameEnvironment().getHabboManager().getHabbo(json.user_id);
            if (habbo2 != null) {
                habbo.getClient().sendResponse(new FriendRequestComposer(habbo2));
                return;
            }
            final HabboInfo offlineHabboInfo = HabboManager.getOfflineHabboInfo(json.user_id);
            if (offlineHabboInfo != null) {
                habbo.getClient().sendResponse(new MessageComposer() { // from class: com.eu.habbo.messages.rcon.FriendRequest.1
                    @Override // com.eu.habbo.messages.outgoing.MessageComposer
                    protected ServerMessage composeInternal() {
                        this.response.init(Outgoing.FriendRequestComposer);
                        this.response.appendInt(Integer.valueOf(offlineHabboInfo.getId()));
                        this.response.appendString(offlineHabboInfo.getUsername());
                        this.response.appendString(offlineHabboInfo.getLook());
                        return this.response;
                    }
                });
            }
        }
    }
}
