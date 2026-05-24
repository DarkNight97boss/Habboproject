package com.eu.habbo.habbohotel.items.interactions.totems;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionDefault;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/totems/InteractionTotemPlanet.class */
public class InteractionTotemPlanet extends InteractionDefault {
    public InteractionTotemPlanet(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionTotemPlanet(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    public TotemPlanetType getPlanetType() {
        int i;
        try {
            i = Integer.parseInt(getExtradata());
        } catch (NumberFormatException e) {
            i = 0;
        }
        return TotemPlanetType.fromInt(i);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        if (gameClient.getHabbo().getHabboInfo().getId() != getUserId()) {
            super.onClick(gameClient, room, objArr);
            return;
        }
        InteractionTotemLegs interactionTotemLegs = null;
        InteractionTotemHead interactionTotemHead = null;
        THashSet<HabboItem> itemsAt = room.getItemsAt(room.getLayout().getTile(getX(), getY()));
        TObjectHashIterator it = itemsAt.iterator();
        while (it.hasNext()) {
            HabboItem habboItem = (HabboItem) it.next();
            if ((habboItem instanceof InteractionTotemLegs) && habboItem.getZ() < getZ()) {
                interactionTotemLegs = (InteractionTotemLegs) habboItem;
            }
        }
        if (interactionTotemLegs == null) {
            super.onClick(gameClient, room, objArr);
            return;
        }
        TObjectHashIterator it2 = itemsAt.iterator();
        while (it2.hasNext()) {
            HabboItem habboItem2 = (HabboItem) it2.next();
            if ((habboItem2 instanceof InteractionTotemHead) && habboItem2.getZ() > interactionTotemLegs.getZ()) {
                interactionTotemHead = (InteractionTotemHead) habboItem2;
            }
        }
        if (interactionTotemHead == null) {
            super.onClick(gameClient, room, objArr);
            return;
        }
        int i = 0;
        if (getPlanetType() == TotemPlanetType.SUN && interactionTotemHead.getTotemType() == TotemType.BIRD && interactionTotemLegs.getTotemType() == TotemType.BIRD && interactionTotemLegs.getTotemColor() == TotemColor.RED) {
            i = 25;
        } else if (getPlanetType() == TotemPlanetType.EARTH && interactionTotemHead.getTotemType() == TotemType.TROLL && interactionTotemLegs.getTotemType() == TotemType.TROLL && interactionTotemLegs.getTotemColor() == TotemColor.YELLOW) {
            i = 23;
        } else if (getPlanetType() == TotemPlanetType.EARTH && interactionTotemHead.getTotemType() == TotemType.SNAKE && interactionTotemLegs.getTotemType() == TotemType.BIRD && interactionTotemLegs.getTotemColor() == TotemColor.YELLOW) {
            i = 26;
        } else if (getPlanetType() == TotemPlanetType.MOON && interactionTotemHead.getTotemType() == TotemType.SNAKE && interactionTotemLegs.getTotemType() == TotemType.SNAKE && interactionTotemLegs.getTotemColor() == TotemColor.BLUE) {
            i = 24;
        }
        if (i <= 0) {
            super.onClick(gameClient, room, objArr);
        } else if (gameClient.getHabbo().getInventory().getEffectsComponent().ownsEffect(i)) {
            gameClient.getHabbo().getInventory().getEffectsComponent().enableEffect(i);
        } else {
            gameClient.getHabbo().getInventory().getEffectsComponent().createEffect(i);
            gameClient.getHabbo().getInventory().getEffectsComponent().enableEffect(i);
        }
    }
}
