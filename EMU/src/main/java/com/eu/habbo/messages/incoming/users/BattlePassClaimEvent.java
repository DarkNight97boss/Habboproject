package com.eu.habbo.messages.incoming.users;

import com.eu.habbo.core.BattlePass;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.users.BattlePassClaimedComposer;
import com.eu.habbo.messages.outgoing.users.BattlePassInfoComposer;

/**
 * Client -> server: claim a tier reward.
 *
 * Wire layout:
 *   int  tier
 *   bool premium
 */
public class BattlePassClaimEvent extends MessageHandler {

    @Override
    public int getRatelimit() {
        return 1000;
    }

    @Override
    public void handle() throws Exception {
        if (this.client == null || this.client.getHabbo() == null) return;
        if (!BattlePass.isEnabled()) return;

        int tier = this.packet.readInt();
        boolean premium = this.packet.readBoolean();

        BattlePass.Tier t = BattlePass.claim(this.client.getHabbo(), tier, premium);
        if (t == null) {
            this.client.sendResponse(new BattlePassClaimedComposer(false, tier, premium, 0, 0, "", "Not available."));
            return;
        }
        int kind = premium ? t.premiumKind : t.freeKind;
        int amount = premium ? t.premiumAmount : t.freeAmount;
        String badge = premium ? t.premiumBadge : t.freeBadge;
        this.client.sendResponse(new BattlePassClaimedComposer(true, tier, premium, kind, amount, badge, "Reward claimed!"));

        // Refresh info so the client UI marks the tier as claimed.
        BattlePass.SeasonInfo season = BattlePass.activeSeason();
        BattlePass.Progress p = BattlePass.loadProgress(this.client.getHabbo().getHabboInfo().getId(), season.id);
        this.client.sendResponse(new BattlePassInfoComposer(season, p));
    }
}
