package com.eu.habbo.habbohotel.items.interactions.games.football.goals;

import com.eu.habbo.habbohotel.games.GameTeamColors;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.games.InteractionGameTeamItem;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.messages.ServerMessage;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/games/football/goals/InteractionFootballGoal.class */
public class InteractionFootballGoal extends InteractionGameTeamItem {
    public InteractionFootballGoal(ResultSet resultSet, Item item, GameTeamColors gameTeamColors) throws SQLException {
        super(resultSet, item, gameTeamColors);
    }

    public InteractionFootballGoal(int i, int i2, Item item, String str, int i3, int i4, GameTeamColors gameTeamColors) {
        super(i, i2, item, str, i3, i4, gameTeamColors);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return getBaseItem().allowWalk();
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void serializeExtradata(ServerMessage serverMessage) {
        serverMessage.appendInt(Integer.valueOf(isLimited() ? 256 : 0));
        serverMessage.appendString(getExtradata());
        super.serializeExtradata(serverMessage);
    }
}
