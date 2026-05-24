package com.eu.habbo.habbohotel.wired.highscores;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/wired/highscores/WiredHighscoreDataEntry.class */
public class WiredHighscoreDataEntry {
    private final int itemId;
    private final List<Integer> userIds;
    private final int score;
    private final boolean isWin;
    private final int timestamp;

    public WiredHighscoreDataEntry(int i, List<Integer> list, int i2, boolean z, int i3) {
        this.itemId = i;
        this.userIds = list;
        this.score = i2;
        this.isWin = z;
        this.timestamp = i3;
    }

    public WiredHighscoreDataEntry(ResultSet resultSet) throws SQLException {
        this.itemId = resultSet.getInt("item_id");
        this.userIds = (List) Arrays.stream(resultSet.getString("user_ids").split(",")).map(Integer::valueOf).collect(Collectors.toList());
        this.score = resultSet.getInt("score");
        this.isWin = resultSet.getInt("is_win") == 1;
        this.timestamp = resultSet.getInt("timestamp");
    }

    public int getItemId() {
        return this.itemId;
    }

    public List<Integer> getUserIds() {
        return this.userIds;
    }

    public int getScore() {
        return this.score;
    }

    public boolean isWin() {
        return this.isWin;
    }

    public int getTimestamp() {
        return this.timestamp;
    }
}
