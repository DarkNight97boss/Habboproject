package com.eu.habbo.threading.runnables;

import com.eu.habbo.habbohotel.guides.GuardianTicket;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/GuardianVotingFinish.class */
public class GuardianVotingFinish implements Runnable {
    private final GuardianTicket ticket;
    private int checkSum;

    public GuardianVotingFinish(GuardianTicket guardianTicket) {
        this.ticket = guardianTicket;
        this.checkSum = this.ticket.getCheckSum();
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.ticket.inProgress() && this.ticket.getCheckSum() == this.checkSum) {
            this.ticket.finish();
        }
    }
}
