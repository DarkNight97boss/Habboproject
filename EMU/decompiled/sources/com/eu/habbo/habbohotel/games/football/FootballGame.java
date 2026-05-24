package com.eu.habbo.habbohotel.games.football;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.games.Game;
import com.eu.habbo.habbohotel.games.GameTeamColors;
import com.eu.habbo.habbohotel.items.interactions.games.football.scoreboards.InteractionFootballScoreboard;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUserAction;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserActionComposer;
import java.util.Iterator;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/games/football/FootballGame.class */
public class FootballGame extends Game {
    private Room room;

    public FootballGame(Room room) {
        super(null, null, room, true);
        this.room = room;
    }

    @Override // com.eu.habbo.habbohotel.games.Game
    public void initialise() {
    }

    @Override // com.eu.habbo.habbohotel.games.Game, java.lang.Runnable
    public void run() {
    }

    public void onScore(RoomUnit roomUnit, GameTeamColors gameTeamColors) {
        if (this.room == null || !this.room.isLoaded()) {
            return;
        }
        Habbo habbo = this.room.getHabbo(roomUnit);
        if (habbo != null) {
            AchievementManager.progressAchievement(habbo, Emulator.getGameEnvironment().getAchievementManager().getAchievement("FootballGoalScored"));
            if (habbo.getHabboInfo().getId() != this.room.getOwnerId()) {
                AchievementManager.progressAchievement(this.room.getOwnerId(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("FootballGoalScoredInRoom"));
            }
        }
        this.room.sendComposer(new RoomUserActionComposer(roomUnit, RoomUserAction.WAVE).compose());
        Iterator it = this.room.getRoomSpecialTypes().getFootballScoreboards(gameTeamColors).entrySet().iterator();
        while (it.hasNext()) {
            ((InteractionFootballScoreboard) ((Map.Entry) it.next()).getValue()).changeScore(1);
        }
    }
}
