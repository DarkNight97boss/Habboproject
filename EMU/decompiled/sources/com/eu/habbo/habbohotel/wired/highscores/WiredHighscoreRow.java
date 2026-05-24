package com.eu.habbo.habbohotel.wired.highscores;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/wired/highscores/WiredHighscoreRow.class */
public class WiredHighscoreRow implements Comparable<WiredHighscoreRow> {
    public static final Comparator<WiredHighscoreRow> COMPARATOR = Comparator.comparing((v0) -> {
        return v0.getValue();
    }).reversed();
    private final List<String> users;
    private final int value;

    public WiredHighscoreRow(List<String> list, int i) {
        Collections.sort(list);
        this.users = list;
        this.value = i;
    }

    public List<String> getUsers() {
        return this.users;
    }

    public int getValue() {
        return this.value;
    }

    @Override // java.lang.Comparable
    public int compareTo(WiredHighscoreRow wiredHighscoreRow) {
        return COMPARATOR.compare(this, wiredHighscoreRow);
    }
}
