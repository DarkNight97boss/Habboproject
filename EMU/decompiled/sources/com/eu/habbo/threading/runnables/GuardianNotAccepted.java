package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guides.GuardianTicket;
import com.eu.habbo.habbohotel.guides.GuardianVote;
import com.eu.habbo.habbohotel.guides.GuardianVoteType;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/GuardianNotAccepted.class */
public class GuardianNotAccepted implements Runnable {
    private final GuardianTicket ticket;
    private final Habbo habbo;

    public GuardianNotAccepted(GuardianTicket guardianTicket, Habbo habbo) {
        this.ticket = guardianTicket;
        this.habbo = habbo;
    }

    @Override // java.lang.Runnable
    public void run() {
        GuardianVote voteForGuardian = this.ticket.getVoteForGuardian(this.habbo);
        if (voteForGuardian == null || voteForGuardian.type != GuardianVoteType.SEARCHING) {
            return;
        }
        Emulator.getGameEnvironment().getGuideManager().acceptTicket(this.habbo, false);
    }
}
