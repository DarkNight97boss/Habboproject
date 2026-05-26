package com.eu.habbo.messages.incoming.users;

import com.eu.habbo.core.DailyStreak;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.users.DailyStreakInfoComposer;

/**
 * Client -> server: the widget asks for the current streak state (called once
 * on mount and after every claim). No payload.
 */
public class DailyStreakRequestInfoEvent extends MessageHandler {

    @Override
    public int getRatelimit() {
        return 500;
    }

    @Override
    public void handle() throws Exception {
        if (this.client == null || this.client.getHabbo() == null) return;

        DailyStreak.State state = DailyStreak.load(this.client.getHabbo().getHabboInfo().getId());
        this.client.sendResponse(new DailyStreakInfoComposer(state));
    }
}
