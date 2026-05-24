package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.wired.WiredConditionOperator;
import com.eu.habbo.habbohotel.wired.WiredConditionType;
import com.eu.habbo.messages.outgoing.wired.WiredConditionDataComposer;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionWiredCondition.class */
public abstract class InteractionWiredCondition extends InteractionWired {
    public InteractionWiredCondition(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionWiredCondition(int i, int i2, Item item, String str, int i3, int i4) {
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
        gameClient.sendResponse(new WiredConditionDataComposer(this, room));
        activateBox(room);
    }

    public abstract WiredConditionType getType();

    public abstract boolean saveData(WiredSettings wiredSettings);

    public WiredConditionOperator operator() {
        return WiredConditionOperator.AND;
    }
}
