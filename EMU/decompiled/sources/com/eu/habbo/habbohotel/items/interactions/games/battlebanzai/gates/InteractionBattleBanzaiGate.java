package com.eu.habbo.habbohotel.items.interactions.games.battlebanzai.gates;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.games.Game;
import com.eu.habbo.habbohotel.games.GameState;
import com.eu.habbo.habbohotel.games.GameTeamColors;
import com.eu.habbo.habbohotel.games.battlebanzai.BattleBanzaiGame;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.games.InteractionGameGate;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/games/battlebanzai/gates/InteractionBattleBanzaiGate.class */
public class InteractionBattleBanzaiGate extends InteractionGameGate {
    public InteractionBattleBanzaiGate(ResultSet resultSet, Item item, GameTeamColors gameTeamColors) throws SQLException {
        super(resultSet, item, gameTeamColors);
    }

    public InteractionBattleBanzaiGate(int i, int i2, Item item, String str, int i3, int i4, GameTeamColors gameTeamColors) {
        super(i, i2, item, str, i3, i4, gameTeamColors);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        return room.getGame(BattleBanzaiGame.class) == null || ((BattleBanzaiGame) room.getGame(BattleBanzaiGame.class)).state.equals(GameState.IDLE);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId());
        if (room == null) {
            return false;
        }
        Game game = room.getGame(BattleBanzaiGame.class);
        return game == null || game.getState() == GameState.IDLE;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.games.InteractionGameGate, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        BattleBanzaiGame battleBanzaiGame = (BattleBanzaiGame) room.getGame(BattleBanzaiGame.class);
        if (battleBanzaiGame == null) {
            battleBanzaiGame = (BattleBanzaiGame) BattleBanzaiGame.class.getDeclaredConstructor(Room.class).newInstance(room);
            room.addGame(battleBanzaiGame);
        }
        if (battleBanzaiGame.getTeamForHabbo(room.getHabbo(roomUnit)) != null) {
            battleBanzaiGame.removeHabbo(room.getHabbo(roomUnit));
        } else {
            battleBanzaiGame.addHabbo(room.getHabbo(roomUnit), this.teamColor);
        }
        updateState(battleBanzaiGame, 5);
        super.onWalkOn(roomUnit, room, objArr);
    }
}
