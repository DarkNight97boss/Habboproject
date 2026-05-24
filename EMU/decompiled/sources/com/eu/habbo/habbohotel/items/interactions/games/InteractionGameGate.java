package com.eu.habbo.habbohotel.items.interactions.games;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.games.Game;
import com.eu.habbo.habbohotel.games.GameTeamColors;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.messages.ServerMessage;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/games/InteractionGameGate.class */
public abstract class InteractionGameGate extends InteractionGameTeamItem {
    public InteractionGameGate(ResultSet resultSet, Item item, GameTeamColors gameTeamColors) throws SQLException {
        super(resultSet, item, gameTeamColors);
        setExtradata("0");
    }

    public InteractionGameGate(int i, int i2, Item item, String str, int i3, int i4, GameTeamColors gameTeamColors) {
        super(i, i2, item, str, i3, i4, gameTeamColors);
        setExtradata("0");
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPickUp(Room room) {
        setExtradata("0");
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        super.onWalkOn(roomUnit, room, objArr);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void serializeExtradata(ServerMessage serverMessage) {
        serverMessage.appendInt(Integer.valueOf(isLimited() ? 256 : 0));
        serverMessage.appendString(getExtradata());
        super.serializeExtradata(serverMessage);
    }

    public void updateState(Game game, int i) {
        int size = 0;
        if (game.getTeam(this.teamColor) != null) {
            size = game.getTeam(this.teamColor).getMembers().size();
        }
        if (size > i) {
            size = i;
        }
        setExtradata(size + Emulator.PREVIEW);
        game.getRoom().updateItem(this);
    }
}
