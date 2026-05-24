package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.games.GameTeamColors;
import com.eu.habbo.habbohotel.items.interactions.games.battlebanzai.InteractionBattleBanzaiSphere;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.rooms.items.ItemsDataUpdateComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/BattleBanzaiTilesFlicker.class */
public class BattleBanzaiTilesFlicker implements Runnable {
    private final THashSet<HabboItem> items;
    private final GameTeamColors color;
    private final Room room;
    private boolean on = false;
    private int count = 0;

    public BattleBanzaiTilesFlicker(THashSet<HabboItem> tHashSet, GameTeamColors gameTeamColors, Room room) {
        this.items = tHashSet;
        this.color = gameTeamColors;
        this.room = room;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.items == null || this.room == null) {
            return;
        }
        int i = 0;
        if (this.on) {
            i = (this.color.type * 3) + 2;
            this.on = false;
        } else {
            this.on = true;
        }
        TObjectHashIterator it = this.items.iterator();
        while (it.hasNext()) {
            ((HabboItem) it.next()).setExtradata(i + Emulator.PREVIEW);
        }
        this.room.sendComposer(new ItemsDataUpdateComposer(this.items).compose());
        if (this.count != 9) {
            this.count++;
            Emulator.getThreading().run(this, 500L);
            return;
        }
        TObjectHashIterator it2 = this.room.getRoomSpecialTypes().getItemsOfType(InteractionBattleBanzaiSphere.class).iterator();
        while (it2.hasNext()) {
            HabboItem habboItem = (HabboItem) it2.next();
            habboItem.setExtradata("0");
            this.room.updateItemState(habboItem);
        }
    }
}
