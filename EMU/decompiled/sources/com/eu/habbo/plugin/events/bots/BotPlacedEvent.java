package com.eu.habbo.plugin.events.bots;

import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/bots/BotPlacedEvent.class */
public class BotPlacedEvent extends BotEvent {
    public final RoomTile location;
    public final Habbo placer;

    public BotPlacedEvent(Bot bot, RoomTile roomTile, Habbo habbo) {
        super(bot);
        this.location = roomTile;
        this.placer = habbo;
    }
}
