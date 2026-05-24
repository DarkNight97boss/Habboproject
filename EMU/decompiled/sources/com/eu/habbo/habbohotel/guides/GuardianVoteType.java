package com.eu.habbo.habbohotel.guides;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/guides/GuardianVoteType.class */
public enum GuardianVoteType {
    FORWARDED(-1),
    WAITING(0),
    ACCEPTABLY(1),
    BADLY(2),
    AWFULLY(3),
    NOT_VOTED(4),
    SEARCHING(5);

    private final int type;

    GuardianVoteType(int i) {
        this.type = i;
    }

    public int getType() {
        return this.type;
    }
}
