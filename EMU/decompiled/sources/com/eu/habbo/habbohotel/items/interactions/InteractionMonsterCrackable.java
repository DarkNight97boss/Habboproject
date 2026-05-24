package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.ICycleable;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionMonsterCrackable.class */
public class InteractionMonsterCrackable extends InteractionCrackable implements ICycleable {
    private int lastHealthChange;
    private boolean respawn;

    public InteractionMonsterCrackable(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.lastHealthChange = 0;
        this.respawn = false;
    }

    public InteractionMonsterCrackable(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.lastHealthChange = 0;
        this.respawn = false;
    }

    @Override // com.eu.habbo.habbohotel.items.ICycleable
    public void cycle(Room room) {
        if (this.ticks <= 0 || Emulator.getIntUnixTimestamp() - this.lastHealthChange <= 30) {
            return;
        }
        this.lastHealthChange = Emulator.getIntUnixTimestamp();
        this.ticks--;
        room.updateItem(this);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionCrackable, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        if (room.isPublicRoom()) {
            this.respawn = true;
        }
        super.onClick(gameClient, room, objArr);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionCrackable
    public boolean resetable() {
        return this.respawn;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionCrackable
    public void reset(Room room) {
        RoomTile randomWalkableTile = room.getRandomWalkableTile();
        setX(randomWalkableTile.x);
        setY(randomWalkableTile.y);
        setZ(room.getStackHeight(randomWalkableTile.x, randomWalkableTile.y, false));
        super.reset(room);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionCrackable
    public boolean allowAnyone() {
        return this.respawn;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionCrackable, com.eu.habbo.habbohotel.users.HabboItem
    public boolean isUsable() {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionCrackable
    protected boolean placeInRoom() {
        return this.respawn;
    }
}
