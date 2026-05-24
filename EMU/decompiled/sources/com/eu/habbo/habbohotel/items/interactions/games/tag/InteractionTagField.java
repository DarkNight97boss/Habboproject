package com.eu.habbo.habbohotel.items.interactions.games.tag;

import com.eu.habbo.habbohotel.games.Game;
import com.eu.habbo.habbohotel.games.tag.TagGame;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/games/tag/InteractionTagField.class */
public abstract class InteractionTagField extends HabboItem {
    public Class<? extends Game> gameClazz;

    public InteractionTagField(ResultSet resultSet, Item item, Class<? extends Game> cls) throws SQLException {
        super(resultSet, item);
        this.gameClazz = cls;
    }

    public InteractionTagField(int i, int i2, Item item, String str, int i3, int i4, Class<? extends Game> cls) {
        super(i, i2, item, str, i3, i4);
        this.gameClazz = cls;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        Habbo habbo = room.getHabbo(roomUnit);
        if (habbo != null) {
            return habbo.getHabboInfo().getCurrentGame() == null || habbo.getHabboInfo().getCurrentGame() == this.gameClazz;
        }
        return false;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        Habbo habbo = room.getHabbo(roomUnit);
        if (habbo == null || habbo.getHabboInfo().getCurrentGame() != null) {
            return;
        }
        TagGame tagGame = (TagGame) room.getGame(this.gameClazz);
        if (tagGame == null) {
            tagGame = (TagGame) this.gameClazz.getDeclaredConstructor(Room.class).newInstance(room);
            room.addGame(tagGame);
        }
        tagGame.addHabbo(habbo, null);
        habbo.getHabboInfo().setCurrentGame(this.gameClazz);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOff(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void serializeExtradata(ServerMessage serverMessage) {
        serverMessage.appendInt(Integer.valueOf(isLimited() ? 256 : 0));
        serverMessage.appendString(getExtradata());
        super.serializeExtradata(serverMessage);
    }
}
