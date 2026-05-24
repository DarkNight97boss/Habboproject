package com.eu.habbo.habbohotel.wired;

import com.eu.habbo.habbohotel.rooms.RoomUserRotation;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/wired/WiredChangeDirectionSetting.class */
public class WiredChangeDirectionSetting {
    public final int item_id;
    public int rotation;
    public RoomUserRotation direction;

    public WiredChangeDirectionSetting(int i, int i2, RoomUserRotation roomUserRotation) {
        this.item_id = i;
        this.rotation = i2;
        this.direction = roomUserRotation;
    }
}
