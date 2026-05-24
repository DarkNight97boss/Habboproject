package com.eu.habbo.plugin.events.furniture.wired;

import com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.plugin.events.roomunit.RoomUnitEvent;
import gnu.trove.set.hash.THashSet;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/furniture/wired/WiredStackTriggeredEvent.class */
public class WiredStackTriggeredEvent extends RoomUnitEvent {
    public final InteractionWiredTrigger trigger;
    public final THashSet<InteractionWiredEffect> effects;
    public final THashSet<InteractionWiredCondition> conditions;

    public WiredStackTriggeredEvent(Room room, RoomUnit roomUnit, InteractionWiredTrigger interactionWiredTrigger, THashSet<InteractionWiredEffect> tHashSet, THashSet<InteractionWiredCondition> tHashSet2) {
        super(room, roomUnit);
        this.trigger = interactionWiredTrigger;
        this.effects = tHashSet;
        this.conditions = tHashSet2;
    }
}
