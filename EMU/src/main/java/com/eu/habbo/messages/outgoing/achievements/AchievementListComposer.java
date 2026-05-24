package com.eu.habbo.messages.outgoing.achievements;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.Achievement;
import com.eu.habbo.habbohotel.achievements.AchievementLevel;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AchievementListComposer extends MessageComposer {
   private static final Logger LOGGER = LoggerFactory.getLogger(AchievementListComposer.class);
   private final Habbo habbo;

   public AchievementListComposer(Habbo habbo) {
      this.habbo = habbo;
   }

   @Override
   protected ServerMessage composeInternal() {
      this.response.init(305);

      try {
         this.response.appendInt(Emulator.getGameEnvironment().getAchievementManager().getAchievements().size());

         for (Achievement achievement : Emulator.getGameEnvironment().getAchievementManager().getAchievements().values()) {
            int achievementProgress = this.habbo.getHabboStats().getAchievementProgress(achievement);
            AchievementLevel currentLevel = achievement.getLevelForProgress(achievementProgress);
            AchievementLevel nextLevel = achievement.getNextLevel(currentLevel != null ? currentLevel.level : 0);
            this.response.appendInt(achievement.id);
            this.response.appendInt(nextLevel != null ? nextLevel.level : (currentLevel != null ? currentLevel.level : 0));
            this.response.appendString("ACH_" + achievement.name + (nextLevel != null ? nextLevel.level : (currentLevel != null ? currentLevel.level : 0)));
            this.response.appendInt(currentLevel != null ? currentLevel.progress : 0);
            this.response.appendInt(nextLevel != null ? nextLevel.progress : -1);
            this.response.appendInt(nextLevel != null ? nextLevel.rewardAmount : -1);
            this.response.appendInt(nextLevel != null ? nextLevel.rewardType : -1);
            this.response.appendInt(achievementProgress <= 0 ? 0 : achievementProgress);
            this.response.appendBoolean(AchievementManager.hasAchieved(this.habbo, achievement));
            this.response.appendString(achievement.category.toString().toLowerCase());
            this.response.appendString("");
            this.response.appendInt(achievement.levels.size());
            this.response.appendInt(AchievementManager.hasAchieved(this.habbo, achievement) ? 1 : 0);
         }

         this.response.appendString("");
      } catch (Exception e) {
         LOGGER.error("Caught exception", e);
      }

      return this.response;
   }
}
