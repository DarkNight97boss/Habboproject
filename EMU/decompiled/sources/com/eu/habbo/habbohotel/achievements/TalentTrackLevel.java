package com.eu.habbo.habbohotel.achievements;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/achievements/TalentTrackLevel.class */
public class TalentTrackLevel {
    private static final Logger LOGGER = LoggerFactory.getLogger(TalentTrackLevel.class);
    public TalentTrackType type;
    public int level;
    public TObjectIntMap<Achievement> achievements = new TObjectIntHashMap();
    public THashSet<Item> items = new THashSet<>();
    public String[] perks;
    public String[] badges;

    public TalentTrackLevel(ResultSet resultSet) throws SQLException {
        this.type = TalentTrackType.valueOf(resultSet.getString("type").toUpperCase());
        this.level = resultSet.getInt("level");
        String[] strArrSplit = resultSet.getString("achievement_ids").split(",");
        String[] strArrSplit2 = resultSet.getString("achievement_levels").split(",");
        if (strArrSplit2.length == strArrSplit.length) {
            for (int i = 0; i < strArrSplit.length; i++) {
                if (!strArrSplit[i].isEmpty() && !strArrSplit2[i].isEmpty()) {
                    Achievement achievement = Emulator.getGameEnvironment().getAchievementManager().getAchievement(Integer.valueOf(strArrSplit[i]).intValue());
                    if (achievement != null) {
                        this.achievements.put(achievement, Integer.valueOf(strArrSplit2[i]).intValue());
                    } else {
                        LOGGER.error("Could not find achievement with ID " + strArrSplit[i] + " for talenttrack level " + this.level + " of type " + this.type);
                    }
                }
            }
        }
        for (String str : resultSet.getString("reward_furni").split(",")) {
            Item item = Emulator.getGameEnvironment().getItemManager().getItem(Integer.valueOf(str).intValue());
            if (item != null) {
                this.items.add(item);
            } else {
                LOGGER.error("Incorrect reward furni (ID: " + str + ") for talent track level " + this.level);
            }
        }
        if (!resultSet.getString("reward_perks").isEmpty()) {
            this.perks = resultSet.getString("reward_perks").split(",");
        }
        if (resultSet.getString("reward_badges").isEmpty()) {
            return;
        }
        this.badges = resultSet.getString("reward_badges").split(",");
    }
}
