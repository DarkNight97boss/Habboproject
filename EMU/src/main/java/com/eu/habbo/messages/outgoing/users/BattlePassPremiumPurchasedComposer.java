package com.eu.habbo.messages.outgoing.users;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/**
 * Server -> client: result of a buy-premium request.
 *
 * Wire layout:
 *   bool   success
 *   string message
 */
public class BattlePassPremiumPurchasedComposer extends MessageComposer {

    private final boolean success;
    private final String message;

    public BattlePassPremiumPurchasedComposer(boolean success, String message) {
        this.success = success;
        this.message = message == null ? "" : message;
    }

    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.BattlePassPremiumPurchasedComposer);
        this.response.appendBoolean(this.success);
        this.response.appendString(this.message);
        return this.response;
    }
}
