package com.eu.habbo.habbohotel.items.interactions.wired.extra;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredExtra;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.messages.ServerMessage;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/extra/WiredExtraUnseen.class */
public class WiredExtraUnseen extends InteractionWiredExtra {
    public List<Integer> seenList;

    public WiredExtraUnseen(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.seenList = new ArrayList();
    }

    public WiredExtraUnseen(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.seenList = new ArrayList();
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public boolean execute(RoomUnit roomUnit, Room room, Object[] objArr) {
        return false;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public String getWiredData() {
        return null;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void serializeWiredData(ServerMessage serverMessage, Room room) {
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void loadWiredData(ResultSet resultSet, Room room) throws SQLException {
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void onPickUp() {
        this.seenList.clear();
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem
    public void onMove(Room room, RoomTile roomTile, RoomTile roomTile2) {
        super.onMove(room, roomTile, roomTile2);
        this.seenList.clear();
    }

    public InteractionWiredEffect getUnseenEffect(List<InteractionWiredEffect> list) {
        ArrayList arrayList = new ArrayList();
        for (InteractionWiredEffect interactionWiredEffect : list) {
            if (!this.seenList.contains(Integer.valueOf(interactionWiredEffect.getId()))) {
                arrayList.add(interactionWiredEffect);
            }
        }
        InteractionWiredEffect interactionWiredEffect2 = null;
        if (arrayList.isEmpty()) {
            this.seenList.clear();
            if (!list.isEmpty()) {
                interactionWiredEffect2 = list.get(0);
            }
        } else {
            interactionWiredEffect2 = (InteractionWiredEffect) arrayList.get(0);
        }
        if (interactionWiredEffect2 != null) {
            this.seenList.add(Integer.valueOf(interactionWiredEffect2.getId()));
        }
        return interactionWiredEffect2;
    }
}
