package com.eu.habbo.messages.outgoing.achievements.talenttrack;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementLevel;
import com.eu.habbo.habbohotel.achievements.TalentTrackLevel;
import com.eu.habbo.habbohotel.achievements.TalentTrackType;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

public class TalentTrackComposer extends MessageComposer {
   public final Habbo habbo;
   public final TalentTrackType type;

   public TalentTrackComposer(Habbo habbo, TalentTrackType type) {
      this.habbo = habbo;
      this.type = type;
   }

   @Override
   protected ServerMessage composeInternal() {
      this.response.init(3406);
      this.response.appendString(this.type.name().toLowerCase());
      LinkedHashMap<Integer, TalentTrackLevel> talentTrackLevels = Emulator.getGameEnvironment().getAchievementManager().getTalenTrackLevels(this.type);
      if (talentTrackLevels != null) {
         this.response.appendInt(talentTrackLevels.size());

         for (Entry<Integer, TalentTrackLevel> set : talentTrackLevels.entrySet()) {
            try {
               TalentTrackLevel level = set.getValue();
               this.response.appendInt(level.level);
               TalentTrackComposer.TalentTrackState state = TalentTrackComposer.TalentTrackState.LOCKED;
               int currentLevel = this.habbo.getHabboStats().talentTrackLevel(this.type);
               if (currentLevel + 1 == level.level) {
                  state = TalentTrackComposer.TalentTrackState.IN_PROGRESS;
               } else if (currentLevel >= level.level) {
                  state = TalentTrackComposer.TalentTrackState.COMPLETED;
               }

               this.response.appendInt(state.id);
               this.response.appendInt(level.achievements.size());
               TalentTrackComposer.TalentTrackState finalState = state;
               level.achievements.forEachEntry((achievement, index) -> {
                  if (achievement != null) {
                     this.response.appendInt(achievement.id);
                     this.response.appendInt(index);
                     this.response.appendString("ACH_" + achievement.name + index);
                     int progress = Math.max(0, this.habbo.getHabboStats().getAchievementProgress(achievement));
                     AchievementLevel achievementLevel = achievement.getLevelForProgress(progress);
                     if (achievementLevel == null) {
                        achievementLevel = achievement.firstLevel();
                     }

                     if (finalState != TalentTrackComposer.TalentTrackState.LOCKED) {
                        if (achievementLevel != null && achievementLevel.progress <= progress) {
                           this.response.appendInt(2);
                        } else {
                           this.response.appendInt(1);
                        }
                     } else {
                        this.response.appendInt(0);
                     }

                     this.response.appendInt(progress);
                     this.response.appendInt(achievementLevel != null ? achievementLevel.progress : 0);
                  } else {
                     this.response.appendInt(0);
                     this.response.appendInt(0);
                     this.response.appendString("");
                     this.response.appendString("");
                     this.response.appendInt(0);
                     this.response.appendInt(0);
                     this.response.appendInt(0);
                  }

                  return true;
               });
               if (level.perks != null && level.perks.length > 0) {
                  this.response.appendInt(level.perks.length);

                  for (String perk : level.perks) {
                     this.response.appendString(perk);
                  }
               } else {
                  this.response.appendInt(-1);
               }

               if (!level.items.isEmpty()) {
                  this.response.appendInt(level.items.size());
                  TObjectHashIterator var13 = level.items.iterator();

                  while (var13.hasNext()) {
                     Item item = (Item)var13.next();
                     this.response.appendString(item.getName());
                     this.response.appendInt(0);
                  }
               } else {
                  this.response.appendInt(-1);
               }
            } catch (NoSuchElementException e) {
               return null;
            }
         }
      } else {
         this.response.appendInt(0);
      }

      return this.response;
   }

   public enum TalentTrackState {
      LOCKED(0),
      IN_PROGRESS(1),
      COMPLETED(2);

      public final int id;

      TalentTrackState(int id) {
         this.id = id;
      }
   }
}
