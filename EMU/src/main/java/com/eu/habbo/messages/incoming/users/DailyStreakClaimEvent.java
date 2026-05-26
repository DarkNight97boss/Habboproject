package com.eu.habbo.messages.incoming.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.core.DailyStreak;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.users.DailyStreakClaimedComposer;
import com.eu.habbo.messages.outgoing.users.DailyStreakInfoComposer;

/**
 * Client -> server: user clicked "Claim" on the daily streak widget. No payload.
 * Server validates eligibility, grants the reward, and replies with a
 * {@link DailyStreakClaimedComposer} + a fresh {@link DailyStreakInfoComposer}.
 *
 * Rate-limited to one attempt every 2s as a safety net (the underlying DB row
 * already enforces single-use per day).
 */
public class DailyStreakClaimEvent extends MessageHandler {

    @Override
    public int getRatelimit() {
        return 2000;
    }

    @Override
    public void handle() throws Exception {
        if (this.client == null || this.client.getHabbo() == null) return;

        DailyStreak.Reward reward = DailyStreak.claim(this.client.getHabbo());
        if (reward == null) {
            // Already claimed today, feature off, or DB error: send a failure
            // result and let the client refresh state.
            this.client.sendResponse(new DailyStreakClaimedComposer(false, 0, 0, 0, 0, "", false, ""));
            DailyStreak.State state = DailyStreak.load(this.client.getHabbo().getHabboInfo().getId());
            this.client.sendResponse(new DailyStreakInfoComposer(state));
            return;
        }

        DailyStreak.State state = DailyStreak.load(this.client.getHabbo().getHabboInfo().getId());
        boolean milestone = state.currentStreak > 0 && state.currentStreak % 7 == 0;
        String milestoneBadge = milestone ? Emulator.getConfig().getValue("daily_streak.milestone_badge", "ACH_Login7") : "";

        this.client.sendResponse(new DailyStreakClaimedComposer(true, state.currentStreak, reward.day,
                reward.kind, reward.amount, reward.badgeCode, milestone, milestoneBadge));
        this.client.sendResponse(new DailyStreakInfoComposer(state));
    }
}
