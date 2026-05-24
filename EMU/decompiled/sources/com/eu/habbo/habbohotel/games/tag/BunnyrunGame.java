package com.eu.habbo.habbohotel.games.tag;

import com.eu.habbo.habbohotel.games.GameTeam;
import com.eu.habbo.habbohotel.items.interactions.games.tag.InteractionTagPole;
import com.eu.habbo.habbohotel.items.interactions.games.tag.bunnyrun.InteractionBunnyrunPole;
import com.eu.habbo.habbohotel.rooms.Room;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/games/tag/BunnyrunGame.class */
public class BunnyrunGame extends TagGame {
    public BunnyrunGame(Room room) {
        super(GameTeam.class, TagGamePlayer.class, room);
    }

    @Override // com.eu.habbo.habbohotel.games.tag.TagGame
    public Class<? extends InteractionTagPole> getTagPole() {
        return InteractionBunnyrunPole.class;
    }

    @Override // com.eu.habbo.habbohotel.games.tag.TagGame
    public int getMaleEffect() {
        return 0;
    }

    @Override // com.eu.habbo.habbohotel.games.tag.TagGame
    public int getMaleTaggerEffect() {
        return 68;
    }

    @Override // com.eu.habbo.habbohotel.games.tag.TagGame
    public int getFemaleEffect() {
        return 0;
    }

    @Override // com.eu.habbo.habbohotel.games.tag.TagGame
    public int getFemaleTaggerEffect() {
        return 68;
    }
}
