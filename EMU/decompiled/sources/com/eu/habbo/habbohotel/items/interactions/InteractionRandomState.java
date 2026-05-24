package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.RandomStateParams;
import com.eu.habbo.habbohotel.rooms.Room;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionRandomState.class */
public class InteractionRandomState extends InteractionDefault {
    public InteractionRandomState(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionRandomState(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPlace(Room room) {
        super.onPlace(room);
        setExtradata(Emulator.PREVIEW);
        room.updateItemState(this);
    }

    public void onRandomStateClick(GameClient gameClient, Room room) throws Exception {
        RandomStateParams randomStateParams = new RandomStateParams(getBaseItem().getCustomParams());
        setExtradata(Emulator.PREVIEW);
        room.updateItemState(this);
        int iNextInt = Emulator.getRandom().nextInt(randomStateParams.getStates()) + 1;
        Emulator.getThreading().run(() -> {
            setExtradata(iNextInt + Emulator.PREVIEW);
            room.updateItemState(this);
        }, randomStateParams.getDelay());
    }
}
