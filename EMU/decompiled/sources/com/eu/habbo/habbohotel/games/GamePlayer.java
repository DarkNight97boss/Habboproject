package com.eu.habbo.habbohotel.games;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/games/GamePlayer.class */
public class GamePlayer {
    private final Habbo habbo;
    private GameTeamColors teamColor;
    private int score;
    private int wiredScore;

    public GamePlayer(Habbo habbo, GameTeamColors gameTeamColors) {
        this.habbo = habbo;
        this.teamColor = gameTeamColors;
    }

    public void reset() {
        this.score = 0;
        this.wiredScore = 0;
    }

    public synchronized void addScore(int i) {
        addScore(i, false);
    }

    public synchronized void addScore(int i, boolean z) {
        if (this.habbo.getHabboInfo().getGamePlayer() == null || this.habbo.getHabboInfo().getCurrentGame() == null || this.habbo.getHabboInfo().getCurrentRoom().getGame(this.habbo.getHabboInfo().getCurrentGame()).getTeamForHabbo(this.habbo) == null) {
            return;
        }
        this.score += i;
        if (this.score < 0) {
            this.score = 0;
        }
        if (z && this.score > 0) {
            this.wiredScore += i;
        }
        WiredHandler.handle(WiredTriggerType.SCORE_ACHIEVED, this.habbo.getRoomUnit(), this.habbo.getHabboInfo().getCurrentRoom(), new Object[]{Integer.valueOf(this.habbo.getHabboInfo().getCurrentRoom().getGame(this.habbo.getHabboInfo().getCurrentGame()).getTeamForHabbo(this.habbo).getTotalScore()), Integer.valueOf(i)});
    }

    public Habbo getHabbo() {
        return this.habbo;
    }

    public GameTeamColors getTeamColor() {
        return this.teamColor;
    }

    public int getScore() {
        return this.score;
    }

    public int getScoreAchievementValue() {
        return this.score - this.wiredScore;
    }
}
