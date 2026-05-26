package com.eu.habbo.messages.outgoing.users;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/**
 * Server -> client: result of a battle pass claim.
 *
 * Wire layout:
 *   bool   success
 *   int    tier
 *   bool   premium
 *   int    kind        (0=credits, 1=pixels, 2=diamonds, 3=badge)
 *   int    amount
 *   string badgeCode
 *   string message
 */
public class BattlePassClaimedComposer extends MessageComposer {

    private final boolean success;
    private final int tier;
    private final boolean premium;
    private final int kind;
    private final int amount;
    private final String badgeCode;
    private final String message;

    public BattlePassClaimedComposer(boolean success, int tier, boolean premium, int kind, int amount, String badgeCode, String message) {
        this.success = success;
        this.tier = tier;
        this.premium = premium;
        this.kind = kind;
        this.amount = amount;
        this.badgeCode = badgeCode == null ? "" : badgeCode;
        this.message = message == null ? "" : message;
    }

    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.BattlePassClaimedComposer);
        this.response.appendBoolean(this.success);
        this.response.appendInt(this.tier);
        this.response.appendBoolean(this.premium);
        this.response.appendInt(this.kind);
        this.response.appendInt(this.amount);
        this.response.appendString(this.badgeCode);
        this.response.appendString(this.message);
        return this.response;
    }
}
