package com.eu.habbo.messages.incoming.users;

import com.eu.habbo.core.BattlePass;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.users.BattlePassInfoComposer;

/**
 * Client -> server: send me the current battle pass state. No payload.
 */
public class BattlePassRequestInfoEvent extends MessageHandler {

    @Override
    public int getRatelimit() {
        return 500;
    }

    @Override
    public void handle() throws Exception {
        if (this.client == null || this.client.getHabbo() == null) return;
        if (!BattlePass.isEnabled()) return;

        BattlePass.SeasonInfo season = BattlePass.activeSeason();
        if (season == null) {
            BattlePass.ensureActiveSeason();
            season = BattlePass.activeSeason();
        }
        BattlePass.Progress progress = season == null
                ? new BattlePass.Progress(0, false, new java.util.HashSet<>(), new java.util.HashSet<>())
                : BattlePass.loadProgress(this.client.getHabbo().getHabboInfo().getId(), season.id);

        this.client.sendResponse(new BattlePassInfoComposer(season, progress));
    }
}
