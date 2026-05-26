package com.eu.habbo.messages.outgoing.users;

import com.eu.habbo.core.BattlePass;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/**
 * Server -> client: full battle pass state for the active season.
 *
 * Wire layout:
 *   int    seasonId
 *   string seasonName
 *   int    seasonStartUnix
 *   int    seasonEndUnix
 *   int    xp                (current total)
 *   int    xpPerTier
 *   int    currentTier
 *   bool   isPremium
 *   int    premiumCostDiamonds
 *   int    tierCount
 *   for each tier:
 *     int    tier
 *     int    freeKind
 *     int    freeAmount
 *     string freeBadge
 *     int    premiumKind
 *     int    premiumAmount
 *     string premiumBadge
 *     bool   freeClaimed
 *     bool   premiumClaimed
 */
public class BattlePassInfoComposer extends MessageComposer {

    private final BattlePass.SeasonInfo season;
    private final BattlePass.Progress progress;

    public BattlePassInfoComposer(BattlePass.SeasonInfo season, BattlePass.Progress progress) {
        this.season = season;
        this.progress = progress;
    }

    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.BattlePassInfoComposer);
        if (this.season == null) {
            this.response.appendInt(0);
            this.response.appendString("");
            this.response.appendInt(0);
            this.response.appendInt(0);
            this.response.appendInt(0);
            this.response.appendInt(BattlePass.xpPerTier());
            this.response.appendInt(0);
            this.response.appendBoolean(false);
            this.response.appendInt(BattlePass.premiumCostDiamonds());
            this.response.appendInt(0);
            return this.response;
        }
        this.response.appendInt(this.season.id);
        this.response.appendString(this.season.name);
        this.response.appendInt(this.season.startUnix);
        this.response.appendInt(this.season.endUnix);
        this.response.appendInt(this.progress.xp);
        this.response.appendInt(BattlePass.xpPerTier());
        this.response.appendInt(this.progress.currentTier());
        this.response.appendBoolean(this.progress.premium);
        this.response.appendInt(BattlePass.premiumCostDiamonds());
        this.response.appendInt(BattlePass.TIERS.length);
        for (BattlePass.Tier t : BattlePass.TIERS) {
            this.response.appendInt(t.tier);
            this.response.appendInt(t.freeKind);
            this.response.appendInt(t.freeAmount);
            this.response.appendString(t.freeBadge);
            this.response.appendInt(t.premiumKind);
            this.response.appendInt(t.premiumAmount);
            this.response.appendString(t.premiumBadge);
            this.response.appendBoolean(this.progress.claimedFree.contains(t.tier));
            this.response.appendBoolean(this.progress.claimedPremium.contains(t.tier));
        }
        return this.response;
    }
}
