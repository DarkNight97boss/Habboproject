package com.eu.habbo.messages.incoming.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.incoming.MessageHandler;

public class RoomVoteEvent extends MessageHandler {
    @Override
    public int getRatelimit() {
        // First-line defence against rating inflation. Even if the per-user
        // sync + DB UNIQUE catch the dupe, this avoids burning CPU on the
        // composer broadcast 1000x/sec.
        return 5000;
    }

    @Override
    public void handle() throws Exception {
        Emulator.getGameEnvironment().getRoomManager().voteForRoom(this.client.getHabbo(), this.client.getHabbo().getHabboInfo().getCurrentRoom());
    }
}
