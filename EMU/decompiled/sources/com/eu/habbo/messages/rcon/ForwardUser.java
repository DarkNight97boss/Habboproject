package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.rooms.ForwardToRoomComposer;
import com.google.gson.Gson;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/ForwardUser.class */
public class ForwardUser extends RCONMessage<ForwardUserJSON> {

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/ForwardUser$ForwardUserJSON.class */
    static class ForwardUserJSON {
        public int user_id;
        public int room_id;

        ForwardUserJSON() {
        }
    }

    public ForwardUser() {
        super(ForwardUserJSON.class);
    }

    @Override // com.eu.habbo.messages.rcon.RCONMessage
    public void handle(Gson gson, ForwardUserJSON forwardUserJSON) {
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(forwardUserJSON.user_id);
        if (habbo != null) {
            if (Emulator.getGameEnvironment().getRoomManager().loadRoom(forwardUserJSON.room_id) != null) {
                if (habbo.getHabboInfo().getCurrentRoom() != null) {
                    Emulator.getGameEnvironment().getRoomManager().leaveRoom(habbo, habbo.getHabboInfo().getCurrentRoom());
                }
                habbo.getClient().sendResponse(new ForwardToRoomComposer(forwardUserJSON.room_id));
                Emulator.getGameEnvironment().getRoomManager().enterRoom(habbo, forwardUserJSON.room_id, Emulator.PREVIEW, true);
            } else {
                this.status = 3;
            }
        }
        this.status = 2;
    }
}
