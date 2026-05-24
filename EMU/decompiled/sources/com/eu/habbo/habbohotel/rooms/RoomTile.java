package com.eu.habbo.habbohotel.rooms;

import com.eu.habbo.habbohotel.items.Item;
import gnu.trove.set.hash.THashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/rooms/RoomTile.class */
public class RoomTile {
    public final short x;
    public final short y;
    public final short z;
    private final THashSet<RoomUnit> units;
    public RoomTileState state;
    private double stackHeight;
    private boolean allowStack;
    private RoomTile previous;
    private boolean diagonally;
    private short gCosts;
    private short hCosts;

    public RoomTile(short s, short s2, short s3, RoomTileState roomTileState, boolean z) {
        this.allowStack = true;
        this.previous = null;
        this.x = s;
        this.y = s2;
        this.z = s3;
        this.stackHeight = s3;
        this.state = roomTileState;
        setAllowStack(z);
        this.units = new THashSet<>();
    }

    public RoomTile(RoomTile roomTile) {
        this.allowStack = true;
        this.previous = null;
        this.x = roomTile.x;
        this.y = roomTile.y;
        this.z = roomTile.z;
        this.stackHeight = roomTile.stackHeight;
        this.state = roomTile.state;
        this.allowStack = roomTile.allowStack;
        this.diagonally = roomTile.diagonally;
        this.gCosts = roomTile.gCosts;
        this.hCosts = roomTile.hCosts;
        if (this.state == RoomTileState.INVALID) {
            this.allowStack = false;
        }
        this.units = roomTile.units;
    }

    public RoomTile() {
        this.allowStack = true;
        this.previous = null;
        this.x = (short) 0;
        this.y = (short) 0;
        this.z = (short) 0;
        this.stackHeight = 0.0d;
        this.state = RoomTileState.INVALID;
        this.allowStack = false;
        this.diagonally = false;
        this.gCosts = (short) 0;
        this.hCosts = (short) 0;
        this.units = null;
    }

    public double getStackHeight() {
        return this.stackHeight;
    }

    public void setStackHeight(double d) {
        if (this.state == RoomTileState.INVALID) {
            this.stackHeight = 32767.0d;
            this.allowStack = false;
        } else if (d < 0.0d || d == 32767.0d) {
            this.allowStack = false;
            this.stackHeight = this.z;
        } else {
            this.stackHeight = d;
            this.allowStack = true;
        }
    }

    public boolean getAllowStack() {
        if (this.state == RoomTileState.INVALID) {
            return false;
        }
        return this.allowStack;
    }

    public void setAllowStack(boolean z) {
        this.allowStack = z;
    }

    public short relativeHeight() {
        if (this.state == RoomTileState.INVALID) {
            return Short.MAX_VALUE;
        }
        if ((this.allowStack || !(this.state == RoomTileState.BLOCKED || this.state == RoomTileState.SIT)) && this.allowStack) {
            return (short) (getStackHeight() * 256.0d);
        }
        return (short) 16384;
    }

    public boolean equals(Object obj) {
        return (obj instanceof RoomTile) && ((RoomTile) obj).x == this.x && ((RoomTile) obj).y == this.y;
    }

    public RoomTile copy() {
        return new RoomTile(this);
    }

    public double distance(RoomTile roomTile) {
        double d = this.x - roomTile.x;
        double d2 = this.y - roomTile.y;
        return Math.sqrt((d * d) + (d2 * d2));
    }

    public void isDiagonally(boolean z) {
        this.diagonally = z;
    }

    public RoomTile getPrevious() {
        return this.previous;
    }

    public void setPrevious(RoomTile roomTile) {
        this.previous = roomTile;
    }

    public int getfCosts() {
        return this.gCosts + this.hCosts;
    }

    public int getgCosts() {
        return this.gCosts;
    }

    public void setgCosts(RoomTile roomTile) {
        setgCosts(roomTile, this.diagonally ? 14 : 10);
    }

    private void setgCosts(short s) {
        this.gCosts = s;
    }

    void setgCosts(RoomTile roomTile, int i) {
        setgCosts((short) (roomTile.getgCosts() + i));
    }

    public int calculategCosts(RoomTile roomTile) {
        return this.diagonally ? roomTile.getgCosts() + 14 : roomTile.getgCosts() + 10;
    }

    public void sethCosts(RoomTile roomTile) {
        this.hCosts = (short) ((Math.abs(this.x - roomTile.x) + Math.abs(this.y - roomTile.y)) * (roomTile.diagonally ? 14 : 10));
    }

    public String toString() {
        return "RoomTile (" + ((int) this.x) + ", " + ((int) this.y) + ", " + ((int) this.z) + "): h: " + ((int) this.hCosts) + " g: " + ((int) this.gCosts) + " f: " + getfCosts();
    }

    public boolean isWalkable() {
        return this.state == RoomTileState.OPEN;
    }

    public RoomTileState getState() {
        return this.state;
    }

    public void setState(RoomTileState roomTileState) {
        this.state = roomTileState;
    }

    public boolean is(short s, short s2) {
        return this.x == s && this.y == s2;
    }

    public List<RoomUnit> getUnits() {
        ArrayList arrayList;
        synchronized (this.units) {
            arrayList = new ArrayList((Collection) this.units);
        }
        return arrayList;
    }

    public void addUnit(RoomUnit roomUnit) {
        synchronized (this.units) {
            if (!this.units.contains(roomUnit)) {
                this.units.add(roomUnit);
            }
        }
    }

    public void removeUnit(RoomUnit roomUnit) {
        synchronized (this.units) {
            this.units.remove(roomUnit);
        }
    }

    public boolean hasUnits() {
        boolean z;
        synchronized (this.units) {
            z = this.units.size() > 0;
        }
        return z;
    }

    public boolean unitIsOnFurniOnTile(RoomUnit roomUnit, Item item) {
        return roomUnit.getX() >= this.x && roomUnit.getX() < this.x + item.getLength() && roomUnit.getY() >= this.y && roomUnit.getY() < this.y + item.getWidth();
    }
}
