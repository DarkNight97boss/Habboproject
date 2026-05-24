package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.messages.incoming.wired.WiredSaveException;
import com.eu.habbo.messages.outgoing.wired.WiredEffectDataComposer;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionWiredEffect.class */
public abstract class InteractionWiredEffect extends InteractionWired {
    private int delay;

    public InteractionWiredEffect(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionWiredEffect(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        if (gameClient == null || !room.hasRights(gameClient.getHabbo())) {
            return;
        }
        gameClient.sendResponse(new WiredEffectDataComposer(this, room));
        activateBox(room);
    }

    public abstract boolean saveData(WiredSettings wiredSettings, GameClient gameClient) throws WiredSaveException;

    public int getDelay() {
        return this.delay;
    }

    protected void setDelay(int i) {
        this.delay = i;
    }

    public abstract WiredEffectType getType();

    public boolean requiresTriggeringUser() {
        return false;
    }
}
