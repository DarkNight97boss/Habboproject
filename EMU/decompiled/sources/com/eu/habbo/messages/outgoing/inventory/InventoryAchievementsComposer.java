package com.eu.habbo.messages.outgoing.inventory;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.Achievement;
import com.eu.habbo.habbohotel.achievements.AchievementLevel;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import gnu.trove.map.hash.THashMap;
import gnu.trove.procedure.TObjectProcedure;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/inventory/InventoryAchievementsComposer.class */
public class InventoryAchievementsComposer extends MessageComposer {
    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(2501);
        synchronized (Emulator.getGameEnvironment().getAchievementManager().getAchievements()) {
            THashMap<String, Achievement> achievements = Emulator.getGameEnvironment().getAchievementManager().getAchievements();
            this.response.appendInt(Integer.valueOf(achievements.size()));
            achievements.forEachValue(new TObjectProcedure<Achievement>() { // from class: com.eu.habbo.messages.outgoing.inventory.InventoryAchievementsComposer.1
                public boolean execute(Achievement achievement) {
                    InventoryAchievementsComposer.this.response.appendString(achievement.name.startsWith("ACH_") ? achievement.name.replace("ACH_", Emulator.PREVIEW) : achievement.name);
                    InventoryAchievementsComposer.this.response.appendInt(Integer.valueOf(achievement.levels.size()));
                    for (AchievementLevel achievementLevel : achievement.levels.values()) {
                        InventoryAchievementsComposer.this.response.appendInt(Integer.valueOf(achievementLevel.level));
                        InventoryAchievementsComposer.this.response.appendInt(Integer.valueOf(achievementLevel.progress));
                    }
                    return true;
                }
            });
        }
        return this.response;
    }
}
