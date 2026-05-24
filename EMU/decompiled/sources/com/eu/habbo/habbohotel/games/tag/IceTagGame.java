package com.eu.habbo.habbohotel.games.tag;

import com.eu.habbo.habbohotel.games.GameTeam;
import com.eu.habbo.habbohotel.items.interactions.games.tag.InteractionTagPole;
import com.eu.habbo.habbohotel.items.interactions.games.tag.icetag.InteractionIceTagPole;
import com.eu.habbo.habbohotel.rooms.Room;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/games/tag/IceTagGame.class */
public class IceTagGame extends TagGame {
    private static final int MALE_SKATES = 38;
    private static final int FEMALE_SKATES = 39;

    public IceTagGame(Room room) {
        super(GameTeam.class, TagGamePlayer.class, room);
    }

    @Override // com.eu.habbo.habbohotel.games.tag.TagGame
    public Class<? extends InteractionTagPole> getTagPole() {
        return InteractionIceTagPole.class;
    }

    @Override // com.eu.habbo.habbohotel.games.tag.TagGame
    public int getMaleEffect() {
        return MALE_SKATES;
    }

    @Override // com.eu.habbo.habbohotel.games.tag.TagGame
    public int getMaleTaggerEffect() {
        return 45;
    }

    @Override // com.eu.habbo.habbohotel.games.tag.TagGame
    public int getFemaleEffect() {
        return 39;
    }

    @Override // com.eu.habbo.habbohotel.games.tag.TagGame
    public int getFemaleTaggerEffect() {
        return 46;
    }
}
