package com.eu.habbo.habbohotel.games.wired;

import com.eu.habbo.habbohotel.games.Game;
import com.eu.habbo.habbohotel.games.GamePlayer;
import com.eu.habbo.habbohotel.games.GameState;
import com.eu.habbo.habbohotel.games.GameTeam;
import com.eu.habbo.habbohotel.games.GameTeamColors;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import java.util.Iterator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/games/wired/WiredGame.class */
public class WiredGame extends Game {
    public GameState state;

    public WiredGame(Room room) {
        super(GameTeam.class, GamePlayer.class, room, false);
        this.state = GameState.RUNNING;
    }

    @Override // com.eu.habbo.habbohotel.games.Game
    public void initialise() {
        this.state = GameState.RUNNING;
        Iterator it = this.teams.values().iterator();
        while (it.hasNext()) {
            ((GameTeam) it.next()).resetScores();
        }
    }

    @Override // com.eu.habbo.habbohotel.games.Game, java.lang.Runnable
    public void run() {
        this.state = GameState.RUNNING;
    }

    @Override // com.eu.habbo.habbohotel.games.Game
    public boolean addHabbo(Habbo habbo, GameTeamColors gameTeamColors) {
        this.room.giveEffect(habbo, 39 + gameTeamColors.type, -1);
        return super.addHabbo(habbo, gameTeamColors);
    }

    @Override // com.eu.habbo.habbohotel.games.Game
    public void removeHabbo(Habbo habbo) {
        super.removeHabbo(habbo);
        this.room.giveEffect(habbo, 0, -1);
    }

    @Override // com.eu.habbo.habbohotel.games.Game
    public void stop() {
        this.state = GameState.RUNNING;
    }

    @Override // com.eu.habbo.habbohotel.games.Game
    public GameState getState() {
        return GameState.RUNNING;
    }
}
