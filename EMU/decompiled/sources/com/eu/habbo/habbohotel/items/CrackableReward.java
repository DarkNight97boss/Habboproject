package com.eu.habbo.habbohotel.items;

import com.eu.habbo.Emulator;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/CrackableReward.class */
public class CrackableReward {
    private static final Logger LOGGER = LoggerFactory.getLogger(CrackableReward.class);
    public final int itemId;
    public final int count;
    public final Map<Integer, Map.Entry<Integer, Integer>> prizes;
    public final String achievementTick;
    public final String achievementCracked;
    public final int requiredEffect;
    public final int subscriptionDuration;
    public final RedeemableSubscriptionType subscriptionType;
    public int totalChance;

    public CrackableReward(ResultSet resultSet) throws SQLException {
        this.itemId = resultSet.getInt("item_id");
        this.count = resultSet.getInt("count");
        this.achievementTick = resultSet.getString("achievement_tick");
        this.achievementCracked = resultSet.getString("achievement_cracked");
        this.requiredEffect = resultSet.getInt("required_effect");
        this.subscriptionDuration = resultSet.getInt("subscription_duration");
        this.subscriptionType = RedeemableSubscriptionType.fromString(resultSet.getString("subscription_type"));
        String[] strArrSplit = resultSet.getString("prizes").split(";");
        this.prizes = new HashMap();
        if (resultSet.getString("prizes").isEmpty()) {
            return;
        }
        this.totalChance = 0;
        for (String str : strArrSplit) {
            try {
                int iIntValue = 0;
                int iIntValue2 = 100;
                if (str.contains(":") && str.split(":").length == 2) {
                    iIntValue = Integer.valueOf(str.split(":")[0]).intValue();
                    iIntValue2 = Integer.valueOf(str.split(":")[1]).intValue();
                } else if (str.contains(":")) {
                    LOGGER.error("Invalid configuration of crackable prizes (item id: " + this.itemId + "). '" + str + "' format should be itemId:chance.");
                } else {
                    iIntValue = Integer.valueOf(str.replace(":", Emulator.PREVIEW)).intValue();
                }
                this.prizes.put(Integer.valueOf(iIntValue), new AbstractMap.SimpleEntry(Integer.valueOf(this.totalChance), Integer.valueOf(this.totalChance + iIntValue2)));
                this.totalChance += iIntValue2;
            } catch (Exception e) {
                LOGGER.error("Caught exception", e);
            }
        }
    }

    public int getRandomReward() {
        if (this.prizes.size() == 0) {
            return 0;
        }
        int iNextInt = Emulator.getRandom().nextInt(this.totalChance);
        int iIntValue = 0;
        for (Map.Entry<Integer, Map.Entry<Integer, Integer>> entry : this.prizes.entrySet()) {
            iIntValue = entry.getKey().intValue();
            if (iNextInt >= entry.getValue().getKey().intValue() && iNextInt < entry.getValue().getValue().intValue()) {
                return entry.getKey().intValue();
            }
        }
        return iIntValue;
    }
}
