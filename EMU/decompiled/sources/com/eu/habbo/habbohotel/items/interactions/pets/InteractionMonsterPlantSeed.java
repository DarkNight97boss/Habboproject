package com.eu.habbo.habbohotel.items.interactions.pets;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/pets/InteractionMonsterPlantSeed.class */
public class InteractionMonsterPlantSeed extends HabboItem {
    public InteractionMonsterPlantSeed(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        if (getExtradata().isEmpty()) {
            setExtradata(Emulator.PREVIEW + randomRarityLevel());
            needsUpdate(true);
        }
    }

    public InteractionMonsterPlantSeed(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        if (getExtradata().isEmpty()) {
            setExtradata(Emulator.PREVIEW + randomRarityLevel());
            needsUpdate(true);
        }
    }

    public static int randomGoldenRarityLevel() {
        int iNextInt = Emulator.getRandom().nextInt(66);
        int i = 0;
        for (int i2 = 8; i2 < 11; i2++) {
            i += 11 - i2;
            if (iNextInt <= i) {
                return i2;
            }
        }
        return 10;
    }

    public static int randomRarityLevel() {
        int iNextInt = Emulator.getRandom().nextInt(66);
        int i = 0;
        for (int i2 = 1; i2 < 11; i2++) {
            i += 11 - i2;
            if (iNextInt <= i) {
                return i2;
            }
        }
        return 10;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        return false;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return false;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void serializeExtradata(ServerMessage serverMessage) {
        serverMessage.appendInt(Integer.valueOf(1 + (isLimited() ? 256 : 0)));
        serverMessage.appendInt((Integer) 1);
        serverMessage.appendString("rarity");
        serverMessage.appendString(getExtradata());
        super.serializeExtradata(serverMessage);
    }
}
