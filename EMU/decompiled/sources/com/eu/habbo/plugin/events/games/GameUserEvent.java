package com.eu.habbo.plugin.events.games;

import com.eu.habbo.habbohotel.games.Game;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/games/GameUserEvent.class */
public abstract class GameUserEvent extends GameEvent {
    public final Habbo habbo;

    public GameUserEvent(Game game, Habbo habbo) {
        super(game);
        this.habbo = habbo;
    }
}
