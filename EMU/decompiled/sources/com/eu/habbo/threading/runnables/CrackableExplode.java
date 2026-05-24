package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.FurnitureType;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionCrackable;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.inventory.AddHabboItemComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import com.eu.habbo.messages.outgoing.rooms.items.AddFloorItemComposer;
import com.eu.habbo.messages.outgoing.rooms.items.RemoveFloorItemComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/CrackableExplode.class */
public class CrackableExplode implements Runnable {
    private final Room room;
    private final InteractionCrackable habboItem;
    private final Habbo habbo;
    private final boolean toInventory;
    private final short x;
    private final short y;

    public CrackableExplode(Room room, InteractionCrackable interactionCrackable, Habbo habbo, boolean z, short s, short s2) {
        this.room = room;
        this.habboItem = interactionCrackable;
        this.habbo = habbo;
        this.toInventory = z;
        this.x = s;
        this.y = s2;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.habboItem.getRoomId() == 0) {
            return;
        }
        if (this.habboItem.resetable()) {
            this.habboItem.reset(this.room);
        } else {
            this.room.removeHabboItem(this.habboItem);
            this.room.sendComposer(new RemoveFloorItemComposer(this.habboItem, true).compose());
            this.habboItem.setRoomId(0);
            Emulator.getGameEnvironment().getItemManager().deleteItem(this.habboItem);
        }
        Item crackableReward = Emulator.getGameEnvironment().getItemManager().getCrackableReward(this.habboItem.getBaseItem().getId());
        if (crackableReward != null) {
            HabboItem habboItemCreateItem = Emulator.getGameEnvironment().getItemManager().createItem(this.habboItem.allowAnyone() ? this.habbo.getHabboInfo().getId() : this.habboItem.getUserId(), crackableReward, 0, 0, Emulator.PREVIEW);
            if (habboItemCreateItem != null) {
                if (this.toInventory || habboItemCreateItem.getBaseItem().getType() == FurnitureType.WALL) {
                    this.habbo.getInventory().getItemsComponent().addItem(habboItemCreateItem);
                    this.habbo.getClient().sendResponse(new AddHabboItemComposer(habboItemCreateItem));
                    this.habbo.getClient().sendResponse(new InventoryRefreshComposer());
                } else {
                    habboItemCreateItem.setX(this.x);
                    habboItemCreateItem.setY(this.y);
                    habboItemCreateItem.setZ(this.room.getStackHeight(this.x, this.y, false));
                    habboItemCreateItem.setRoomId(this.room.getId());
                    habboItemCreateItem.needsUpdate(true);
                    this.room.addHabboItem(habboItemCreateItem);
                    this.room.updateItem(habboItemCreateItem);
                    this.room.sendComposer(new AddFloorItemComposer(habboItemCreateItem, (String) this.room.getFurniOwnerNames().get(habboItemCreateItem.getUserId())).compose());
                }
            }
        }
        this.room.updateTile(this.room.getLayout().getTile(this.x, this.y));
    }
}
