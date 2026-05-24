package com.eu.habbo.habbohotel.games.freeze;

import com.eu.habbo.habbohotel.games.Game;
import com.eu.habbo.habbohotel.games.GamePlayer;
import com.eu.habbo.habbohotel.games.GameTeam;
import com.eu.habbo.habbohotel.games.GameTeamColors;
import com.eu.habbo.habbohotel.items.interactions.games.InteractionGameGate;
import com.eu.habbo.habbohotel.rooms.Room;
import java.util.Iterator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/games/freeze/FreezeGameTeam.class */
public class FreezeGameTeam extends GameTeam {
    public FreezeGameTeam(GameTeamColors gameTeamColors) {
        super(gameTeamColors);
    }

    @Override // com.eu.habbo.habbohotel.games.GameTeam
    public void removeMember(GamePlayer gamePlayer) {
        if (gamePlayer == null || gamePlayer.getHabbo() == null || gamePlayer.getHabbo().getHabboInfo().getCurrentRoom() == null) {
            return;
        }
        Game game = gamePlayer.getHabbo().getHabboInfo().getCurrentRoom().getGame(FreezeGame.class);
        Room room = gamePlayer.getHabbo().getRoomUnit().getRoom();
        gamePlayer.getHabbo().getHabboInfo().getCurrentRoom().giveEffect(gamePlayer.getHabbo(), 0, -1);
        gamePlayer.getHabbo().getRoomUnit().setCanWalk(true);
        super.removeMember(gamePlayer);
        if (room == null || room.getRoomSpecialTypes() == null) {
            return;
        }
        Iterator it = room.getRoomSpecialTypes().getFreezeGates().values().iterator();
        while (it.hasNext()) {
            ((InteractionGameGate) it.next()).updateState(game, 5);
        }
    }

    @Override // com.eu.habbo.habbohotel.games.GameTeam
    public void addMember(GamePlayer gamePlayer) {
        super.addMember(gamePlayer);
        gamePlayer.getHabbo().getHabboInfo().getCurrentRoom().giveEffect(gamePlayer.getHabbo(), 39 + this.teamColor.type, -1);
    }
}
