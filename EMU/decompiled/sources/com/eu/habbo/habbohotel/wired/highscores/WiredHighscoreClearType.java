package com.eu.habbo.habbohotel.wired.highscores;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/wired/highscores/WiredHighscoreClearType.class */
public enum WiredHighscoreClearType {
    ALLTIME(0),
    DAILY(1),
    WEEKLY(2),
    MONTHLY(3);

    public final int type;

    WiredHighscoreClearType(int i) {
        this.type = i;
    }
}
