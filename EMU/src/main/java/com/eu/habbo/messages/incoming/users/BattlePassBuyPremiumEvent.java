package com.eu.habbo.messages.incoming.users;

import com.eu.habbo.core.BattlePass;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.users.BattlePassInfoComposer;
import com.eu.habbo.messages.outgoing.users.BattlePassPremiumPurchasedComposer;

/**
 * Client -> server: purchase the premium track for the current season (paid in
 * diamonds). No payload.
 */
public class BattlePassBuyPremiumEvent extends MessageHandler {

    @Override
    public int getRatelimit() {
        return 3000;
    }

    @Override
    public void handle() throws Exception {
        if (this.client == null || this.client.getHabbo() == null) return;
        if (!BattlePass.isEnabled()) return;

        boolean ok = BattlePass.buyPremium(this.client.getHabbo());
        if (!ok) {
            this.client.sendResponse(new BattlePassPremiumPurchasedComposer(false, "Not enough diamonds, or already premium."));
            return;
        }
        this.client.sendResponse(new BattlePassPremiumPurchasedComposer(true, "Premium pass unlocked!"));
        BattlePass.SeasonInfo season = BattlePass.activeSeason();
        BattlePass.Progress p = BattlePass.loadProgress(this.client.getHabbo().getHabboInfo().getId(), season.id);
        this.client.sendResponse(new BattlePassInfoComposer(season, p));
    }
}
