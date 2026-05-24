package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboGender;
import com.eu.habbo.messages.outgoing.rooms.items.RemoveFloorItemComposer;
import com.eu.habbo.threading.runnables.QueryDeleteHabboItem;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionFXBox.class */
public class InteractionFXBox extends InteractionDefault {
    public InteractionFXBox(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionFXBox(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        if (gameClient == null || getUserId() != gameClient.getHabbo().getHabboInfo().getId() || getExtradata().equals("1")) {
            return;
        }
        int effectF = -1;
        if (gameClient.getHabbo().getHabboInfo().getGender().equals(HabboGender.M) && getBaseItem().getEffectM() > 0) {
            effectF = getBaseItem().getEffectM();
        }
        if (gameClient.getHabbo().getHabboInfo().getGender().equals(HabboGender.F) && getBaseItem().getEffectF() > 0) {
            effectF = getBaseItem().getEffectF();
        }
        if (effectF >= 0 && !gameClient.getHabbo().getInventory().getEffectsComponent().ownsEffect(effectF)) {
            gameClient.getHabbo().getInventory().getEffectsComponent().createEffect(effectF, 0);
            gameClient.getHabbo().getInventory().getEffectsComponent().enableEffect(effectF);
            setExtradata("1");
            room.updateItemState(this);
            room.removeHabboItem(this);
            Emulator.getThreading().run(() -> {
                new QueryDeleteHabboItem(this.getId()).run();
                room.sendComposer(new RemoveFloorItemComposer(this).compose());
                room.updateTile(room.getLayout().getTile(getX(), getY()));
            }, 500L);
        }
    }
}
