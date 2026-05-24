package com.eu.habbo.threading.runnables;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserHandItemComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserReceivedHandItemComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/HabboGiveHandItemToHabbo.class */
public class HabboGiveHandItemToHabbo implements Runnable {
    private final Habbo target;
    private final Habbo from;

    public HabboGiveHandItemToHabbo(Habbo habbo, Habbo habbo2) {
        this.target = habbo2;
        this.from = habbo;
    }

    @Override // java.lang.Runnable
    public void run() {
        int handItem;
        if (this.from.getHabboInfo().getCurrentRoom() == null || this.target.getHabboInfo().getCurrentRoom() == null || this.from.getHabboInfo().getCurrentRoom() != this.target.getHabboInfo().getCurrentRoom() || (handItem = this.from.getRoomUnit().getHandItem()) <= 0) {
            return;
        }
        this.from.getRoomUnit().setHandItem(0);
        this.from.getHabboInfo().getCurrentRoom().sendComposer(new RoomUserHandItemComposer(this.from.getRoomUnit()).compose());
        this.target.getRoomUnit().lookAtPoint(this.from.getRoomUnit().getCurrentLocation());
        this.target.getRoomUnit().statusUpdate(true);
        this.target.getClient().sendResponse(new RoomUserReceivedHandItemComposer(this.from.getRoomUnit(), handItem));
        this.target.getRoomUnit().setHandItem(handItem);
        this.target.getHabboInfo().getCurrentRoom().sendComposer(new RoomUserHandItemComposer(this.target.getRoomUnit()).compose());
    }
}
