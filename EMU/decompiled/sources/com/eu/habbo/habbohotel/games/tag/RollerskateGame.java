package com.eu.habbo.habbohotel.games.tag;

import com.eu.habbo.habbohotel.games.GameTeam;
import com.eu.habbo.habbohotel.items.interactions.games.tag.InteractionTagPole;
import com.eu.habbo.habbohotel.rooms.Room;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/games/tag/RollerskateGame.class */
public class RollerskateGame extends TagGame {
    public RollerskateGame(Room room) {
        super(GameTeam.class, TagGamePlayer.class, room);
    }

    @Override // com.eu.habbo.habbohotel.games.tag.TagGame
    public Class<? extends InteractionTagPole> getTagPole() {
        return null;
    }

    @Override // com.eu.habbo.habbohotel.games.tag.TagGame
    public int getMaleEffect() {
        return 55;
    }

    @Override // com.eu.habbo.habbohotel.games.tag.TagGame
    public int getMaleTaggerEffect() {
        return 57;
    }

    @Override // com.eu.habbo.habbohotel.games.tag.TagGame
    public int getFemaleEffect() {
        return 56;
    }

    @Override // com.eu.habbo.habbohotel.games.tag.TagGame
    public int getFemaleTaggerEffect() {
        return 58;
    }
}
