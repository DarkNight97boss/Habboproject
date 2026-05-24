package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.habboway.nux.NuxAlertComposer;
import gnu.trove.map.hash.THashMap;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionInformationTerminal.class */
public class InteractionInformationTerminal extends InteractionCustomValues {
    public static final THashMap<String, String> defaultValues = new THashMap<String, String>() { // from class: com.eu.habbo.habbohotel.items.interactions.InteractionInformationTerminal.1
        {
            put("internalLink", "habbopages/chat/commands");
        }
    };

    public InteractionInformationTerminal(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item, defaultValues);
    }

    public InteractionInformationTerminal(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4, defaultValues);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionCustomValues, com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        super.onWalk(roomUnit, room, objArr);
        Habbo habbo = room.getHabbo(roomUnit);
        if (habbo == null || !this.values.containsKey("internalLink")) {
            return;
        }
        habbo.getClient().sendResponse(new NuxAlertComposer((String) this.values.get("internalLink")));
    }
}
