package com.eu.habbo.habbohotel.games;

import com.eu.habbo.habbohotel.users.Habbo;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/games/GameTeam.class */
public class GameTeam {
    public final GameTeamColors teamColor;
    private final THashSet<GamePlayer> members = new THashSet<>();
    private int teamScore;

    public GameTeam(GameTeamColors gameTeamColors) {
        this.teamColor = gameTeamColors;
    }

    public void initialise() {
        TObjectHashIterator it = this.members.iterator();
        while (it.hasNext()) {
            ((GamePlayer) it.next()).reset();
        }
        this.teamScore = 0;
    }

    public void reset() {
        this.members.clear();
    }

    public void addTeamScore(int i) {
        this.teamScore += i;
    }

    public int getTeamScore() {
        return this.teamScore;
    }

    public synchronized int getTotalScore() {
        int score = this.teamScore;
        TObjectHashIterator it = this.members.iterator();
        while (it.hasNext()) {
            score += ((GamePlayer) it.next()).getScore();
        }
        return score;
    }

    public void addMember(GamePlayer gamePlayer) {
        synchronized (this.members) {
            this.members.add(gamePlayer);
        }
    }

    public void removeMember(GamePlayer gamePlayer) {
        synchronized (this.members) {
            this.members.remove(gamePlayer);
        }
    }

    public void clearMembers() {
        TObjectHashIterator it = this.members.iterator();
        while (it.hasNext()) {
            GamePlayer gamePlayer = (GamePlayer) it.next();
            if (gamePlayer != null && gamePlayer.getHabbo() != null) {
                if (gamePlayer.getHabbo().getHabboInfo().getGamePlayer() != null) {
                    gamePlayer.getHabbo().getHabboInfo().getGamePlayer().reset();
                }
                gamePlayer.getHabbo().getHabboInfo().setCurrentGame(null);
                gamePlayer.getHabbo().getHabboInfo().setGamePlayer(null);
            }
        }
        this.members.clear();
    }

    public void resetScores() {
        TObjectHashIterator it = this.members.iterator();
        while (it.hasNext()) {
            GamePlayer gamePlayer = (GamePlayer) it.next();
            if (gamePlayer != null) {
                gamePlayer.reset();
            }
        }
        this.teamScore = 0;
    }

    public THashSet<GamePlayer> getMembers() {
        return this.members;
    }

    public boolean isMember(Habbo habbo) {
        TObjectHashIterator it = this.members.iterator();
        while (it.hasNext()) {
            if (((GamePlayer) it.next()).getHabbo().equals(habbo)) {
                return true;
            }
        }
        return false;
    }

    @Deprecated
    public GamePlayer getPlayerForHabbo(Habbo habbo) {
        TObjectHashIterator it = this.members.iterator();
        while (it.hasNext()) {
            GamePlayer gamePlayer = (GamePlayer) it.next();
            if (gamePlayer.getHabbo().equals(habbo)) {
                return gamePlayer;
            }
        }
        return null;
    }
}
