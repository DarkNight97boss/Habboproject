package com.eu.habbo.habbohotel.achievements;

import gnu.trove.map.hash.THashMap;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/achievements/Achievement.class */
public class Achievement {
    public final int id;
    public final String name;
    public final AchievementCategories category;
    public final THashMap<Integer, AchievementLevel> levels = new THashMap<>();

    public Achievement(ResultSet resultSet) throws SQLException {
        this.id = resultSet.getInt("id");
        this.name = resultSet.getString("name");
        this.category = AchievementCategories.valueOf(resultSet.getString("category").toUpperCase());
        addLevel(new AchievementLevel(resultSet));
    }

    public void addLevel(AchievementLevel achievementLevel) {
        synchronized (this.levels) {
            this.levels.put(Integer.valueOf(achievementLevel.level), achievementLevel);
        }
    }

    public AchievementLevel getLevelForProgress(int i) {
        AchievementLevel achievementLevel = null;
        if (i > 0) {
            for (AchievementLevel achievementLevel2 : this.levels.values()) {
                if (i >= achievementLevel2.progress && (achievementLevel == null || achievementLevel.level <= achievementLevel2.level)) {
                    achievementLevel = achievementLevel2;
                }
            }
        }
        return achievementLevel;
    }

    public AchievementLevel getNextLevel(int i) {
        for (AchievementLevel achievementLevel : this.levels.values()) {
            if (achievementLevel.level == i + 1) {
                return achievementLevel;
            }
        }
        return null;
    }

    public AchievementLevel firstLevel() {
        return (AchievementLevel) this.levels.get(1);
    }

    public void clearLevels() {
        this.levels.clear();
    }
}
