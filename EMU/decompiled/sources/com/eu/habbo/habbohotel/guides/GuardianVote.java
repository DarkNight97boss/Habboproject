package com.eu.habbo.habbohotel.guides;

import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/guides/GuardianVote.class */
public class GuardianVote implements Comparable<GuardianVote> {
    public final int id;
    final Habbo guardian;
    public GuardianVoteType type = GuardianVoteType.SEARCHING;
    boolean ignore = false;

    public GuardianVote(int i, Habbo habbo) {
        this.id = i;
        this.guardian = habbo;
    }

    @Override // java.lang.Comparable
    public int compareTo(GuardianVote guardianVote) {
        return this.id - guardianVote.id;
    }

    public boolean equals(Object obj) {
        return (obj instanceof GuardianVote) && ((GuardianVote) obj).id == this.id && ((GuardianVote) obj).guardian == this.guardian && ((GuardianVote) obj).type == this.type;
    }

    public void ignore() {
        this.ignore = true;
    }
}
