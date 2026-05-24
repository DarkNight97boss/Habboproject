package com.eu.habbo.messages.outgoing.achievements.talenttrack;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementLevel;
import com.eu.habbo.habbohotel.achievements.TalentTrackLevel;
import com.eu.habbo.habbohotel.achievements.TalentTrackType;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.iterator.hash.TObjectHashIterator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/achievements/talenttrack/TalentTrackComposer.class */
public class TalentTrackComposer extends MessageComposer {
    public final Habbo habbo;
    public final TalentTrackType type;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/achievements/talenttrack/TalentTrackComposer$TalentTrackState.class */
    public enum TalentTrackState {
        LOCKED(0),
        IN_PROGRESS(1),
        COMPLETED(2);

        public final int id;

        TalentTrackState(int i) {
            this.id = i;
        }
    }

    public TalentTrackComposer(Habbo habbo, TalentTrackType talentTrackType) {
        this.habbo = habbo;
        this.type = talentTrackType;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.TalentTrackComposer);
        this.response.appendString(this.type.name().toLowerCase());
        LinkedHashMap<Integer, TalentTrackLevel> talenTrackLevels = Emulator.getGameEnvironment().getAchievementManager().getTalenTrackLevels(this.type);
        if (talenTrackLevels != null) {
            this.response.appendInt(Integer.valueOf(talenTrackLevels.size()));
            Iterator<Map.Entry<Integer, TalentTrackLevel>> it = talenTrackLevels.entrySet().iterator();
            while (it.hasNext()) {
                try {
                    TalentTrackLevel value = it.next().getValue();
                    this.response.appendInt(Integer.valueOf(value.level));
                    TalentTrackState talentTrackState = TalentTrackState.LOCKED;
                    int iTalentTrackLevel = this.habbo.getHabboStats().talentTrackLevel(this.type);
                    if (iTalentTrackLevel + 1 == value.level) {
                        talentTrackState = TalentTrackState.IN_PROGRESS;
                    } else if (iTalentTrackLevel >= value.level) {
                        talentTrackState = TalentTrackState.COMPLETED;
                    }
                    this.response.appendInt(Integer.valueOf(talentTrackState.id));
                    this.response.appendInt(Integer.valueOf(value.achievements.size()));
                    TalentTrackState talentTrackState2 = talentTrackState;
                    value.achievements.forEachEntry((achievement, i) -> {
                        if (achievement == null) {
                            this.response.appendInt((Integer) 0);
                            this.response.appendInt((Integer) 0);
                            this.response.appendString(Emulator.PREVIEW);
                            this.response.appendString(Emulator.PREVIEW);
                            this.response.appendInt((Integer) 0);
                            this.response.appendInt((Integer) 0);
                            this.response.appendInt((Integer) 0);
                            return true;
                        }
                        this.response.appendInt(Integer.valueOf(achievement.id));
                        this.response.appendInt(Integer.valueOf(i));
                        this.response.appendString("ACH_" + achievement.name + i);
                        int iMax = Math.max(0, this.habbo.getHabboStats().getAchievementProgress(achievement));
                        AchievementLevel levelForProgress = achievement.getLevelForProgress(iMax);
                        if (levelForProgress == null) {
                            levelForProgress = achievement.firstLevel();
                        }
                        if (talentTrackState2 == TalentTrackState.LOCKED) {
                            this.response.appendInt((Integer) 0);
                        } else if (levelForProgress == null || levelForProgress.progress > iMax) {
                            this.response.appendInt((Integer) 1);
                        } else {
                            this.response.appendInt((Integer) 2);
                        }
                        this.response.appendInt(Integer.valueOf(iMax));
                        this.response.appendInt(Integer.valueOf(levelForProgress != null ? levelForProgress.progress : 0));
                        return true;
                    });
                    if (value.perks == null || value.perks.length <= 0) {
                        this.response.appendInt((Integer) (-1));
                    } else {
                        this.response.appendInt(Integer.valueOf(value.perks.length));
                        for (String str : value.perks) {
                            this.response.appendString(str);
                        }
                    }
                    if (value.items.isEmpty()) {
                        this.response.appendInt((Integer) (-1));
                    } else {
                        this.response.appendInt(Integer.valueOf(value.items.size()));
                        TObjectHashIterator it2 = value.items.iterator();
                        while (it2.hasNext()) {
                            this.response.appendString(((Item) it2.next()).getName());
                            this.response.appendInt((Integer) 0);
                        }
                    }
                } catch (NoSuchElementException e) {
                    return null;
                }
            }
        } else {
            this.response.appendInt((Integer) 0);
        }
        return this.response;
    }
}
