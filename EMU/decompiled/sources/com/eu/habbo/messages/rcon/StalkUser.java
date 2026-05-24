package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.rooms.ForwardToRoomComposer;
import com.google.gson.Gson;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/StalkUser.class */
public class StalkUser extends RCONMessage<StalkUserJSON> {

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/StalkUser$StalkUserJSON.class */
    static class StalkUserJSON {
        public int user_id;
        public int follow_id;

        StalkUserJSON() {
        }
    }

    public StalkUser() {
        super(StalkUserJSON.class);
    }

    @Override // com.eu.habbo.messages.rcon.RCONMessage
    public void handle(Gson gson, StalkUserJSON stalkUserJSON) {
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(stalkUserJSON.user_id);
        if (habbo != null) {
            Habbo habbo2 = Emulator.getGameEnvironment().getHabboManager().getHabbo(stalkUserJSON.follow_id);
            if (habbo2 == null) {
                this.message = Emulator.getTexts().getValue("commands.error.cmd_stalk.not_found").replace("%user%", stalkUserJSON.user_id + Emulator.PREVIEW);
                this.status = 1;
                return;
            }
            if (habbo2.getHabboInfo().getCurrentRoom() == null) {
                this.message = Emulator.getTexts().getValue("commands.error.cmd_stalk.not_room").replace("%user%", stalkUserJSON.user_id + Emulator.PREVIEW);
                this.status = 1;
                return;
            }
            if (habbo2.getHabboInfo().getUsername().equals(habbo.getHabboInfo().getUsername())) {
                this.message = Emulator.getTexts().getValue("commands.generic.cmd_stalk.self").replace("%user%", stalkUserJSON.user_id + Emulator.PREVIEW);
                this.status = 1;
            } else if (habbo2.getHabboInfo().getCurrentRoom() == habbo.getHabboInfo().getCurrentRoom()) {
                this.message = Emulator.getTexts().getValue("commands.generic.cmd_stalk.same_room").replace("%user%", stalkUserJSON.user_id + Emulator.PREVIEW);
                this.status = 1;
            } else if (this.status == 0) {
                habbo.getClient().sendResponse(new ForwardToRoomComposer(habbo2.getHabboInfo().getCurrentRoom().getId()));
            }
        }
    }
}
