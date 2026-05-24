package com.eu.habbo.habbohotel.rooms;

import com.eu.habbo.Emulator;
import gnu.trove.set.hash.THashSet;
import java.awt.Rectangle;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/rooms/RoomLayout.class */
public class RoomLayout {
    protected static final int BASICMOVEMENTCOST = 10;
    protected static final int DIAGONALMOVEMENTCOST = 14;
    public boolean CANMOVEDIAGONALY = true;
    private String name;
    private short doorX;
    private short doorY;
    private short doorZ;
    private int doorDirection;
    private String heightmap;
    private int mapSize;
    private int mapSizeX;
    private int mapSizeY;
    private RoomTile[][] roomTiles;
    private RoomTile doorTile;
    private Room room;
    private static final Logger LOGGER = LoggerFactory.getLogger(RoomLayout.class);
    public static double MAXIMUM_STEP_HEIGHT = 1.1d;
    public static boolean ALLOW_FALLING = true;

    public RoomLayout(ResultSet resultSet, Room room) throws SQLException {
        this.room = room;
        try {
            this.name = resultSet.getString("name");
            this.doorX = resultSet.getShort("door_x");
            this.doorY = resultSet.getShort("door_y");
            this.doorDirection = resultSet.getInt("door_dir");
            this.heightmap = resultSet.getString("heightmap");
            parse();
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
        }
    }

    public static boolean squareInSquare(Rectangle rectangle, Rectangle rectangle2) {
        return rectangle.x <= rectangle2.x && rectangle.y <= rectangle2.y && rectangle.x + rectangle.width >= rectangle2.x + rectangle2.width && rectangle.y + rectangle.height >= rectangle2.y + rectangle2.height;
    }

    public static boolean tileInSquare(Rectangle rectangle, RoomTile roomTile) {
        return rectangle.contains(roomTile.x, roomTile.y);
    }

    public static boolean pointInSquare(int i, int i2, int i3, int i4, int i5, int i6) {
        return i5 >= i && i6 >= i2 && i5 <= i3 && i6 <= i4;
    }

    public static boolean tilesAdjecent(RoomTile roomTile, RoomTile roomTile2) {
        return roomTile != null && roomTile2 != null && Math.abs(roomTile.x - roomTile2.x) <= 1 && Math.abs(roomTile.y - roomTile2.y) <= 1;
    }

    public static Rectangle getRectangle(int i, int i2, int i3, int i4, int i5) {
        int i6 = i5 % 8;
        return (i6 == 2 || i6 == 6) ? new Rectangle(i, i2, i4, i3) : new Rectangle(i, i2, i3, i4);
    }

    public static boolean tilesAdjecent(RoomTile roomTile, RoomTile roomTile2, int i, int i2, int i3) {
        Rectangle rectangle = getRectangle(roomTile2.x, roomTile2.y, i, i2, i3);
        return new Rectangle(rectangle.x - 1, rectangle.y - 1, rectangle.width + 2, rectangle.height + 2).contains(roomTile.x, roomTile.y);
    }

    public void parse() {
        String[] strArrSplit = this.heightmap.replace("\n", Emulator.PREVIEW).split(Character.toString('\r'));
        this.mapSize = 0;
        this.mapSizeX = strArrSplit[0].length();
        this.mapSizeY = strArrSplit.length;
        this.roomTiles = new RoomTile[this.mapSizeX][this.mapSizeY];
        short s = 0;
        while (true) {
            short s2 = s;
            if (s2 >= this.mapSizeY) {
                break;
            }
            if (!strArrSplit[s2].isEmpty() && !strArrSplit[s2].equalsIgnoreCase("\r")) {
                short s3 = 0;
                while (true) {
                    short s4 = s3;
                    if (s4 >= this.mapSizeX || strArrSplit[s2].length() != this.mapSizeX) {
                        break;
                    }
                    String lowerCase = strArrSplit[s2].substring(s4, s4 + 1).trim().toLowerCase();
                    RoomTileState roomTileState = RoomTileState.OPEN;
                    short s5 = 0;
                    if (lowerCase.equalsIgnoreCase("x")) {
                        roomTileState = RoomTileState.INVALID;
                    } else {
                        s5 = lowerCase.isEmpty() ? (short) 0 : Emulator.isNumeric(lowerCase) ? Short.parseShort(lowerCase) : (short) (10 + "ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(lowerCase.toUpperCase()));
                    }
                    this.mapSize++;
                    this.roomTiles[s4][s2] = new RoomTile(s4, s2, s5, roomTileState, true);
                    s3 = (short) (s4 + 1);
                }
            }
            s = (short) (s2 + 1);
        }
        this.doorTile = this.roomTiles[this.doorX][this.doorY];
        if (this.doorTile != null) {
            this.doorTile.setAllowStack(false);
            RoomTile tileInFront = getTileInFront(this.doorTile, this.doorDirection);
            if (tileInFront == null || !tileExists(tileInFront.x, tileInFront.y) || this.roomTiles[tileInFront.x][tileInFront.y].state == RoomTileState.INVALID) {
                return;
            }
            if (this.doorZ == this.roomTiles[tileInFront.x][tileInFront.y].z && this.roomTiles[this.doorX][this.doorY].state == this.roomTiles[tileInFront.x][tileInFront.y].state) {
                return;
            }
            this.doorZ = this.roomTiles[tileInFront.x][tileInFront.y].z;
            this.roomTiles[this.doorX][this.doorY].state = RoomTileState.OPEN;
        }
    }

    public String getName() {
        return this.name;
    }

    public short getDoorX() {
        return this.doorX;
    }

    public void setDoorX(short s) {
        this.doorX = s;
    }

    public short getDoorY() {
        return this.doorY;
    }

    public void setDoorY(short s) {
        this.doorY = s;
    }

    public int getDoorZ() {
        return this.doorZ;
    }

    public RoomTile getDoorTile() {
        return this.doorTile;
    }

    public int getDoorDirection() {
        return this.doorDirection;
    }

    public void setDoorDirection(int i) {
        this.doorDirection = i;
    }

    public String getHeightmap() {
        return this.heightmap;
    }

    public void setHeightmap(String str) {
        this.heightmap = str;
    }

    public int getMapSize() {
        return this.mapSize;
    }

    public int getMapSizeX() {
        return this.mapSizeX;
    }

    public int getMapSizeY() {
        return this.mapSizeY;
    }

    public short getHeightAtSquare(int i, int i2) {
        if (i < 0 || i2 < 0 || i >= getMapSizeX() || i2 >= getMapSizeY()) {
            return (short) 0;
        }
        return this.roomTiles[i][i2].z;
    }

    public double getStackHeightAtSquare(int i, int i2) {
        if (i < 0 || i2 < 0 || i >= getMapSizeX() || i2 >= getMapSizeY()) {
            return 0.0d;
        }
        return this.roomTiles[i][i2].getStackHeight();
    }

    public double getRelativeHeightAtSquare(int i, int i2) {
        if (i < 0 || i2 < 0 || i >= getMapSizeX() || i2 >= getMapSizeY()) {
            return 0.0d;
        }
        return this.roomTiles[i][i2].relativeHeight();
    }

    public RoomTile getTile(short s, short s2) {
        if (tileExists(s, s2)) {
            return this.roomTiles[s][s2];
        }
        return null;
    }

    public boolean tileExists(short s, short s2) {
        return s >= 0 && s2 >= 0 && s < getMapSizeX() && s2 < getMapSizeY();
    }

    public boolean tileWalkable(short s, short s2) {
        return tileExists(s, s2) && this.roomTiles[s][s2].state == RoomTileState.OPEN && this.roomTiles[s][s2].isWalkable();
    }

    public boolean isVoidTile(short s, short s2) {
        return !tileExists(s, s2) || this.roomTiles[s][s2].state == RoomTileState.INVALID;
    }

    public String getRelativeMap() {
        return this.heightmap.replace("\r\n", "\r");
    }

    public final Deque<RoomTile> findPath(RoomTile roomTile, RoomTile roomTile2, RoomTile roomTile3, RoomUnit roomUnit) {
        return findPath(roomTile, roomTile2, roomTile3, roomUnit, false);
    }

    public final Deque<RoomTile> findPath(RoomTile roomTile, RoomTile roomTile2, RoomTile roomTile3, RoomUnit roomUnit, boolean z) {
        if (this.room == null || !this.room.isLoaded() || roomTile == null || roomTile2 == null || roomTile.equals(roomTile2) || roomTile2.state == RoomTileState.INVALID) {
            return new LinkedList();
        }
        LinkedList linkedList = new LinkedList();
        LinkedList linkedList2 = new LinkedList();
        linkedList.add(roomTile.copy());
        RoomTile doorTile = this.room.getLayout().getDoorTile();
        long jCurrentTimeMillis = System.currentTimeMillis();
        while (!linkedList.isEmpty()) {
            if (System.currentTimeMillis() - jCurrentTimeMillis > Emulator.getConfig().getInt("pathfinder.execution_time.milli", 25) && Emulator.getConfig().getBoolean("pathfinder.max_execution_time.enabled", false)) {
                return null;
            }
            RoomTile roomTileLowestFInOpen = lowestFInOpen(linkedList);
            if (roomTileLowestFInOpen.x == roomTile2.x && roomTileLowestFInOpen.y == roomTile2.y) {
                return calcPath(findTile(linkedList, roomTile.x, roomTile.y), roomTileLowestFInOpen);
            }
            linkedList2.add(roomTileLowestFInOpen);
            linkedList.remove(roomTileLowestFInOpen);
            for (RoomTile roomTile4 : getAdjacent(linkedList, roomTileLowestFInOpen, roomTile2, roomUnit)) {
                if (!linkedList2.contains(roomTile4)) {
                    if (roomUnit.canOverrideTile(roomTile4)) {
                        roomTile4.setPrevious(roomTileLowestFInOpen);
                        roomTile4.sethCosts(findTile(linkedList, roomTile2.x, roomTile2.y));
                        roomTile4.setgCosts(roomTileLowestFInOpen);
                        linkedList.add(roomTile4);
                    } else if (roomTile4.state == RoomTileState.BLOCKED || ((roomTile4.state == RoomTileState.SIT || roomTile4.state == RoomTileState.LAY) && !roomTile4.equals(roomTile3))) {
                        linkedList2.add(roomTile4);
                        linkedList.remove(roomTile4);
                    } else {
                        double stackHeight = roomTile4.getStackHeight() - roomTileLowestFInOpen.getStackHeight();
                        if ((!ALLOW_FALLING && stackHeight < (-MAXIMUM_STEP_HEIGHT)) || (roomTile4.state == RoomTileState.OPEN && stackHeight > MAXIMUM_STEP_HEIGHT)) {
                            linkedList2.add(roomTile4);
                            linkedList.remove(roomTile4);
                        } else if (roomTile4.hasUnits() && doorTile.distance(roomTile4) > 2.0d && !(z && this.room.isAllowWalkthrough() && !roomTile4.equals(roomTile3))) {
                            linkedList2.add(roomTile4);
                            linkedList.remove(roomTile4);
                        } else if (!linkedList.contains(roomTile4)) {
                            roomTile4.setPrevious(roomTileLowestFInOpen);
                            roomTile4.sethCosts(findTile(linkedList, roomTile2.x, roomTile2.y));
                            roomTile4.setgCosts(roomTileLowestFInOpen);
                            linkedList.add(roomTile4);
                        } else if (roomTile4.getgCosts() > roomTile4.calculategCosts(roomTileLowestFInOpen)) {
                            roomTile4.setPrevious(roomTileLowestFInOpen);
                            roomTile4.setgCosts(roomTileLowestFInOpen);
                        }
                    }
                }
            }
        }
        if (!this.room.isAllowWalkthrough() || z) {
            return null;
        }
        return findPath(roomTile, roomTile2, roomTile3, roomUnit, true);
    }

    private RoomTile findTile(List<RoomTile> list, short s, short s2) {
        for (RoomTile roomTile : list) {
            if (s == roomTile.x && s2 == roomTile.y) {
                return roomTile;
            }
        }
        RoomTile tile = getTile(s, s2);
        if (tile != null) {
            return tile.copy();
        }
        return null;
    }

    public Deque<RoomTile> calcPath(RoomTile roomTile, RoomTile roomTile2) {
        LinkedList linkedList = new LinkedList();
        if (roomTile == null) {
            return linkedList;
        }
        RoomTile previous = roomTile2;
        while (previous != null) {
            linkedList.addFirst(getTile(previous.x, previous.y));
            previous = previous.getPrevious();
            if (previous != null && previous.equals(roomTile)) {
                return linkedList;
            }
        }
        return linkedList;
    }

    private RoomTile lowestFInOpen(List<RoomTile> list) {
        if (list == null) {
            return null;
        }
        RoomTile roomTile = list.get(0);
        for (RoomTile roomTile2 : list) {
            if (roomTile2.getfCosts() < roomTile.getfCosts()) {
                roomTile = roomTile2;
            }
        }
        return roomTile;
    }

    private List<RoomTile> getAdjacent(List<RoomTile> list, RoomTile roomTile, RoomTile roomTile2, RoomUnit roomUnit) {
        short s = roomTile.x;
        short s2 = roomTile.y;
        LinkedList linkedList = new LinkedList();
        if (s > 0) {
            RoomTile roomTileFindTile = findTile(list, (short) (s - 1), s2);
            if (canWalkOn(roomTileFindTile, roomUnit) && (roomTileFindTile.state != RoomTileState.SIT || roomTile2.getStackHeight() - roomTile.getStackHeight() <= 2.0d)) {
                roomTileFindTile.isDiagonally(false);
                if (!linkedList.contains(roomTileFindTile)) {
                    linkedList.add(roomTileFindTile);
                }
            }
        }
        if (s < this.mapSizeX) {
            RoomTile roomTileFindTile2 = findTile(list, (short) (s + 1), s2);
            if (canWalkOn(roomTileFindTile2, roomUnit) && (roomTileFindTile2.state != RoomTileState.SIT || roomTile2.getStackHeight() - roomTile.getStackHeight() <= 2.0d)) {
                roomTileFindTile2.isDiagonally(false);
                if (!linkedList.contains(roomTileFindTile2)) {
                    linkedList.add(roomTileFindTile2);
                }
            }
        }
        if (s2 > 0) {
            RoomTile roomTileFindTile3 = findTile(list, s, (short) (s2 - 1));
            if (canWalkOn(roomTileFindTile3, roomUnit) && (roomTileFindTile3.state != RoomTileState.SIT || roomTile2.getStackHeight() - roomTile.getStackHeight() <= 2.0d)) {
                roomTileFindTile3.isDiagonally(false);
                if (!linkedList.contains(roomTileFindTile3)) {
                    linkedList.add(roomTileFindTile3);
                }
            }
        }
        if (s2 < this.mapSizeY) {
            RoomTile roomTileFindTile4 = findTile(list, s, (short) (s2 + 1));
            if (canWalkOn(roomTileFindTile4, roomUnit) && (roomTileFindTile4.state != RoomTileState.SIT || roomTile2.getStackHeight() - roomTile.getStackHeight() <= 2.0d)) {
                roomTileFindTile4.isDiagonally(false);
                if (!linkedList.contains(roomTileFindTile4)) {
                    linkedList.add(roomTileFindTile4);
                }
            }
        }
        if (this.CANMOVEDIAGONALY) {
            if (s < this.mapSizeX && s2 < this.mapSizeY) {
                RoomTile roomTileFindTile5 = findTile(list, (short) (s + 1), (short) (s2 + 1));
                if (canWalkOn(roomTileFindTile5, roomUnit) && (roomTileFindTile5.state != RoomTileState.SIT || roomTile2.getStackHeight() - roomTile.getStackHeight() <= 2.0d)) {
                    roomTileFindTile5.isDiagonally(true);
                    if (!linkedList.contains(roomTileFindTile5)) {
                        linkedList.add(roomTileFindTile5);
                    }
                }
            }
            if (s > 0 && s2 > 0) {
                RoomTile roomTileFindTile6 = findTile(list, (short) (s - 1), (short) (s2 - 1));
                if (canWalkOn(roomTileFindTile6, roomUnit) && (roomTileFindTile6.state != RoomTileState.SIT || roomTile2.getStackHeight() - roomTile.getStackHeight() <= 2.0d)) {
                    roomTileFindTile6.isDiagonally(true);
                    if (!linkedList.contains(roomTileFindTile6)) {
                        linkedList.add(roomTileFindTile6);
                    }
                }
            }
            if (s > 0 && s2 < this.mapSizeY) {
                RoomTile roomTileFindTile7 = findTile(list, (short) (s - 1), (short) (s2 + 1));
                if (canWalkOn(roomTileFindTile7, roomUnit) && (roomTileFindTile7.state != RoomTileState.SIT || roomTile2.getStackHeight() - roomTile.getStackHeight() <= 2.0d)) {
                    roomTileFindTile7.isDiagonally(true);
                    if (!linkedList.contains(roomTileFindTile7)) {
                        linkedList.add(roomTileFindTile7);
                    }
                }
            }
            if (s < this.mapSizeX && s2 > 0) {
                RoomTile roomTileFindTile8 = findTile(list, (short) (s + 1), (short) (s2 - 1));
                if (canWalkOn(roomTileFindTile8, roomUnit) && (roomTileFindTile8.state != RoomTileState.SIT || roomTile2.getStackHeight() - roomTile.getStackHeight() <= 2.0d)) {
                    roomTileFindTile8.isDiagonally(true);
                    if (!linkedList.contains(roomTileFindTile8)) {
                        linkedList.add(roomTileFindTile8);
                    }
                }
            }
        }
        return linkedList;
    }

    private boolean canWalkOn(RoomTile roomTile, RoomUnit roomUnit) {
        return roomTile != null && (roomUnit.canOverrideTile(roomTile) || !(roomTile.state == RoomTileState.BLOCKED || roomTile.state == RoomTileState.INVALID));
    }

    public void moveDiagonally(boolean z) {
        this.CANMOVEDIAGONALY = z;
    }

    public RoomTile getTileInFront(RoomTile roomTile, int i) {
        return getTileInFront(roomTile, i, 0);
    }

    public RoomTile getTileInFront(RoomTile roomTile, int i, int i2) {
        int i3 = 0;
        int i4 = 0;
        switch (i % 8) {
            case 0:
                i4 = 0 - 1;
                break;
            case 1:
                i3 = 0 + 1;
                i4 = 0 - 1;
                break;
            case 2:
                i3 = 0 + 1;
                break;
            case 3:
                i3 = 0 + 1;
                i4 = 0 + 1;
                break;
            case 4:
                i4 = 0 + 1;
                break;
            case 5:
                i3 = 0 - 1;
                i4 = 0 + 1;
                break;
            case 6:
                i3 = 0 - 1;
                break;
            case 7:
                i3 = 0 - 1;
                i4 = 0 - 1;
                break;
        }
        short s = roomTile.x;
        short s2 = roomTile.y;
        for (int i5 = 0; i5 <= i2; i5++) {
            s = (short) (s + i3);
            s2 = (short) (s2 + i4);
        }
        return getTile(s, s2);
    }

    public List<RoomTile> getTilesInFront(RoomTile roomTile, int i, int i2) {
        RoomTile tileInFront;
        ArrayList arrayList = new ArrayList(i2);
        for (int i3 = 0; i3 < i2 && (tileInFront = getTileInFront(roomTile, i, i3)) != null; i3++) {
            arrayList.add(tileInFront);
        }
        return arrayList;
    }

    public List<RoomTile> getTilesAround(RoomTile roomTile) {
        return getTilesAround(roomTile, 0);
    }

    public List<RoomTile> getTilesAround(RoomTile roomTile, int i) {
        return getTilesAround(roomTile, i, true);
    }

    public List<RoomTile> getTilesAround(RoomTile roomTile, int i, boolean z) {
        ArrayList arrayList = new ArrayList(z ? 8 : 4);
        if (roomTile != null) {
            int i2 = 0;
            while (true) {
                int i3 = i2;
                if (i3 >= 8) {
                    break;
                }
                RoomTile tileInFront = getTileInFront(roomTile, (i3 + i) % 8);
                if (tileInFront != null) {
                    arrayList.add(tileInFront);
                }
                i2 = i3 + (z ? 1 : 2);
            }
        }
        return arrayList;
    }

    public List<RoomTile> getWalkableTilesAround(RoomTile roomTile) {
        return getWalkableTilesAround(roomTile, 0);
    }

    public List<RoomTile> getWalkableTilesAround(RoomTile roomTile, int i) {
        ArrayList<RoomTile> arrayList = new ArrayList(getTilesAround(roomTile, i));
        ArrayList arrayList2 = new ArrayList();
        for (RoomTile roomTile2 : arrayList) {
            if (roomTile2 == null || roomTile2.state != RoomTileState.OPEN || !roomTile2.isWalkable()) {
                arrayList2.add(roomTile2);
            }
        }
        Iterator it = arrayList2.iterator();
        while (it.hasNext()) {
            arrayList.remove((RoomTile) it.next());
        }
        return arrayList;
    }

    public boolean fitsOnMap(RoomTile roomTile, int i, int i2, int i3) {
        if (roomTile == null) {
            return true;
        }
        if (i3 == 0 || i3 == 4) {
            short s = roomTile.x;
            while (true) {
                short s2 = s;
                if (s2 > roomTile.x + (i - 1)) {
                    return true;
                }
                short s3 = roomTile.y;
                while (true) {
                    short s4 = s3;
                    if (s4 <= roomTile.y + (i2 - 1)) {
                        RoomTile tile = getTile(s2, s4);
                        if (tile == null || tile.state == RoomTileState.INVALID) {
                            return false;
                        }
                        s3 = (short) (s4 + 1);
                    }
                }
                s = (short) (s2 + 1);
            }
        } else {
            if (i3 != 2 && i3 != 6) {
                return true;
            }
            short s5 = roomTile.x;
            while (true) {
                short s6 = s5;
                if (s6 > roomTile.x + (i2 - 1)) {
                    return true;
                }
                short s7 = roomTile.y;
                while (true) {
                    short s8 = s7;
                    if (s8 <= roomTile.y + (i - 1)) {
                        RoomTile tile2 = getTile(s6, s8);
                        if (tile2 == null || tile2.state == RoomTileState.INVALID) {
                            return false;
                        }
                        s7 = (short) (s8 + 1);
                    }
                }
                s5 = (short) (s6 + 1);
            }
        }
    }

    public THashSet<RoomTile> getTilesAt(RoomTile roomTile, int i, int i2, int i3) {
        THashSet<RoomTile> tHashSet = new THashSet<>(i * i2, 0.1f);
        if (roomTile != null) {
            if (i3 == 0 || i3 == 4) {
                short s = roomTile.x;
                while (true) {
                    short s2 = s;
                    if (s2 > roomTile.x + (i - 1)) {
                        break;
                    }
                    short s3 = roomTile.y;
                    while (true) {
                        short s4 = s3;
                        if (s4 <= roomTile.y + (i2 - 1)) {
                            RoomTile tile = getTile(s2, s4);
                            if (tile != null) {
                                tHashSet.add(tile);
                            }
                            s3 = (short) (s4 + 1);
                        }
                    }
                    s = (short) (s2 + 1);
                }
            } else if (i3 == 2 || i3 == 6) {
                short s5 = roomTile.x;
                while (true) {
                    short s6 = s5;
                    if (s6 > roomTile.x + (i2 - 1)) {
                        break;
                    }
                    short s7 = roomTile.y;
                    while (true) {
                        short s8 = s7;
                        if (s8 <= roomTile.y + (i - 1)) {
                            RoomTile tile2 = getTile(s6, s8);
                            if (tile2 != null) {
                                tHashSet.add(tile2);
                            }
                            s7 = (short) (s8 + 1);
                        }
                    }
                    s5 = (short) (s6 + 1);
                }
            }
        }
        return tHashSet;
    }
}
