package com.eu.habbo.messages.outgoing.users;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/**
 * Server -> client: outcome of a {@code DailyStreakClaim} attempt.
 *
 * Wire layout:
 *   bool   success
 *   int    newStreak
 *   int    dayClaimed          (1..7)
 *   int    kind                (0=credits, 1=pixels, 2=diamonds, 3=badge)
 *   int    amount
 *   string badgeCode           (empty unless kind == BADGE)
 *   bool   milestoneAwarded    (true if a milestone badge was added on top)
 *   string milestoneBadge      (the badge code, or empty)
 */
public class DailyStreakClaimedComposer extends MessageComposer {

    private final boolean success;
    private final int newStreak;
    private final int dayClaimed;
    private final int kind;
    private final int amount;
    private final String badgeCode;
    private final boolean milestoneAwarded;
    private final String milestoneBadge;

    public DailyStreakClaimedComposer(boolean success, int newStreak, int dayClaimed, int kind, int amount,
                                      String badgeCode, boolean milestoneAwarded, String milestoneBadge) {
        this.success = success;
        this.newStreak = newStreak;
        this.dayClaimed = dayClaimed;
        this.kind = kind;
        this.amount = amount;
        this.badgeCode = badgeCode == null ? "" : badgeCode;
        this.milestoneAwarded = milestoneAwarded;
        this.milestoneBadge = milestoneBadge == null ? "" : milestoneBadge;
    }

    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.DailyStreakClaimedComposer);
        this.response.appendBoolean(this.success);
        this.response.appendInt(this.newStreak);
        this.response.appendInt(this.dayClaimed);
        this.response.appendInt(this.kind);
        this.response.appendInt(this.amount);
        this.response.appendString(this.badgeCode);
        this.response.appendBoolean(this.milestoneAwarded);
        this.response.appendString(this.milestoneBadge);
        return this.response;
    }
}
