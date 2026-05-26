package com.eu.habbo.messages.outgoing.users;

import com.eu.habbo.core.DailyStreak;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/**
 * Server -> client: current streak state and the 7-day cycle the client should
 * render in the calendar widget.
 *
 * Wire layout:
 *   int     currentStreak
 *   int     bestStreak
 *   bool    canClaimToday
 *   int     dayInCycle           (1..7, the day that would be claimed next)
 *   int     cycleSize            (always 7 here)
 *   for each day in cycle:
 *     int    day                  (1..7)
 *     int    kind                 (0=credits, 1=pixels, 2=diamonds, 3=badge)
 *     int    amount
 *     string badgeCode
 */
public class DailyStreakInfoComposer extends MessageComposer {

    private final DailyStreak.State state;

    public DailyStreakInfoComposer(DailyStreak.State state) {
        this.state = state;
    }

    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.DailyStreakInfoComposer);
        this.response.appendInt(this.state.currentStreak);
        this.response.appendInt(this.state.bestStreak);
        this.response.appendBoolean(this.state.canClaimToday);
        this.response.appendInt(this.state.dayInCycle);
        this.response.appendInt(DailyStreak.CYCLE.length);
        for (DailyStreak.Reward r : DailyStreak.CYCLE) {
            this.response.appendInt(r.day);
            this.response.appendInt(r.kind);
            this.response.appendInt(r.amount);
            this.response.appendString(r.badgeCode);
        }
        return this.response;
    }
}
