package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guides.GuardianTicket;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/GuardianTicketFindMoreSlaves.class */
public class GuardianTicketFindMoreSlaves implements Runnable {
    private final GuardianTicket ticket;

    public GuardianTicketFindMoreSlaves(GuardianTicket guardianTicket) {
        this.ticket = guardianTicket;
    }

    @Override // java.lang.Runnable
    public void run() {
        Emulator.getGameEnvironment().getGuideManager().findGuardians(this.ticket);
    }
}
