package com.eu.habbo.habbohotel.achievements;

import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/achievements/AchievementLevel.class */
public class AchievementLevel {
    public final int level;
    public final int rewardAmount;
    public final int rewardType;
    public final int points;
    public final int progress;

    public AchievementLevel(ResultSet resultSet) throws SQLException {
        this.level = resultSet.getInt("level");
        this.rewardAmount = resultSet.getInt("reward_amount");
        this.rewardType = resultSet.getInt("reward_type");
        this.points = resultSet.getInt("points");
        this.progress = resultSet.getInt("progress_needed");
    }
}
