package com.eu.habbo.habbohotel.rooms;

import com.eu.habbo.Emulator;
import gnu.trove.set.hash.THashSet;
import java.awt.Rectangle;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoomLayout {
   private static final Logger LOGGER = LoggerFactory.getLogger(RoomLayout.class);
   protected static final int BASICMOVEMENTCOST = 10;
   protected static final int DIAGONALMOVEMENTCOST = 14;
   public static double MAXIMUM_STEP_HEIGHT = 1.1;
   public static boolean ALLOW_FALLING = true;
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

   public RoomLayout(ResultSet set, Room room) throws SQLException {
      this.room = room;

      try {
         this.name = set.getString("name");
         this.doorX = set.getShort("door_x");
         this.doorY = set.getShort("door_y");
         this.doorDirection = set.getInt("door_dir");
         this.heightmap = set.getString("heightmap");
         this.parse();
      } catch (Exception e) {
         LOGGER.error("Caught exception", e);
      }
   }

   public static boolean squareInSquare(Rectangle outerSquare, Rectangle innerSquare) {
      if (outerSquare.x > innerSquare.x) {
         return false;
      } else if (outerSquare.y > innerSquare.y) {
         return false;
      } else {
         return outerSquare.x + outerSquare.width < innerSquare.x + innerSquare.width
            ? false
            : outerSquare.y + outerSquare.height >= innerSquare.y + innerSquare.height;
      }
   }

   public static boolean tileInSquare(Rectangle square, RoomTile tile) {
      return square.contains(tile.x, tile.y);
   }

   public static boolean pointInSquare(int x1, int y1, int x2, int y2, int pointX, int pointY) {
      return pointX >= x1 && pointY >= y1 && pointX <= x2 && pointY <= y2;
   }

   public static boolean tilesAdjecent(RoomTile one, RoomTile two) {
      return one != null && two != null && Math.abs(one.x - two.x) <= 1 && Math.abs(one.y - two.y) <= 1;
   }

   public static Rectangle getRectangle(int x, int y, int width, int length, int rotation) {
      rotation %= 8;
      return rotation != 2 && rotation != 6 ? new Rectangle(x, y, width, length) : new Rectangle(x, y, length, width);
   }

   public static boolean tilesAdjecent(RoomTile tile, RoomTile comparator, int width, int length, int rotation) {
      Rectangle rectangle = getRectangle(comparator.x, comparator.y, width, length, rotation);
      rectangle = new Rectangle(rectangle.x - 1, rectangle.y - 1, rectangle.width + 2, rectangle.height + 2);
      return rectangle.contains(tile.x, tile.y);
   }

   public void parse() {
      String[] modelTemp = this.heightmap.replace("\n", "").split(Character.toString('\r'));
      this.mapSize = 0;
      this.mapSizeX = modelTemp[0].length();
      this.mapSizeY = modelTemp.length;
      this.roomTiles = new RoomTile[this.mapSizeX][this.mapSizeY];

      for (short y = 0; y < this.mapSizeY; y++) {
         if (!modelTemp[y].isEmpty() && !modelTemp[y].equalsIgnoreCase("\r")) {
            for (short x = 0; x < this.mapSizeX && modelTemp[y].length() == this.mapSizeX; x++) {
               String square = modelTemp[y].substring(x, x + 1).trim().toLowerCase();
               RoomTileState state = RoomTileState.OPEN;
               short height = 0;
               if (square.equalsIgnoreCase("x")) {
                  state = RoomTileState.INVALID;
               } else if (square.isEmpty()) {
                  height = 0;
               } else if (Emulator.isNumeric(square)) {
                  height = Short.parseShort(square);
               } else {
                  height = (short)(10 + "ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(square.toUpperCase()));
               }

               this.mapSize++;
               this.roomTiles[x][y] = new RoomTile(x, y, height, state, true);
            }
         }
      }

      this.doorTile = this.roomTiles[this.doorX][this.doorY];
      if (this.doorTile != null) {
         this.doorTile.setAllowStack(false);
         RoomTile doorFrontTile = this.getTileInFront(this.doorTile, this.doorDirection);
         if (doorFrontTile != null
            && this.tileExists(doorFrontTile.x, doorFrontTile.y)
            && this.roomTiles[doorFrontTile.x][doorFrontTile.y].state != RoomTileState.INVALID
            && (
               this.doorZ != this.roomTiles[doorFrontTile.x][doorFrontTile.y].z
                  || this.roomTiles[this.doorX][this.doorY].state != this.roomTiles[doorFrontTile.x][doorFrontTile.y].state
            )) {
            this.doorZ = this.roomTiles[doorFrontTile.x][doorFrontTile.y].z;
            this.roomTiles[this.doorX][this.doorY].state = RoomTileState.OPEN;
         }
      }
   }

   public String getName() {
      return this.name;
   }

   public short getDoorX() {
      return this.doorX;
   }

   public void setDoorX(short doorX) {
      this.doorX = doorX;
   }

   public short getDoorY() {
      return this.doorY;
   }

   public void setDoorY(short doorY) {
      this.doorY = doorY;
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

   public void setDoorDirection(int doorDirection) {
      this.doorDirection = doorDirection;
   }

   public String getHeightmap() {
      return this.heightmap;
   }

   public void setHeightmap(String heightMap) {
      this.heightmap = heightMap;
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

   public short getHeightAtSquare(int x, int y) {
      return x >= 0 && y >= 0 && x < this.getMapSizeX() && y < this.getMapSizeY() ? this.roomTiles[x][y].z : 0;
   }

   public double getStackHeightAtSquare(int x, int y) {
      return x >= 0 && y >= 0 && x < this.getMapSizeX() && y < this.getMapSizeY() ? this.roomTiles[x][y].getStackHeight() : 0.0;
   }

   public double getRelativeHeightAtSquare(int x, int y) {
      return x >= 0 && y >= 0 && x < this.getMapSizeX() && y < this.getMapSizeY() ? this.roomTiles[x][y].relativeHeight() : 0.0;
   }

   public RoomTile getTile(short x, short y) {
      return this.tileExists(x, y) ? this.roomTiles[x][y] : null;
   }

   public boolean tileExists(short x, short y) {
      return x >= 0 && y >= 0 && x < this.getMapSizeX() && y < this.getMapSizeY();
   }

   public boolean tileWalkable(short x, short y) {
      return this.tileExists(x, y) && this.roomTiles[x][y].state == RoomTileState.OPEN && this.roomTiles[x][y].isWalkable();
   }

   public boolean isVoidTile(short x, short y) {
      return !this.tileExists(x, y) ? true : this.roomTiles[x][y].state == RoomTileState.INVALID;
   }

   public String getRelativeMap() {
      return this.heightmap.replace("\r\n", "\r");
   }

   public final Deque<RoomTile> findPath(RoomTile oldTile, RoomTile newTile, RoomTile goalLocation, RoomUnit roomUnit) {
      return this.findPath(oldTile, newTile, goalLocation, roomUnit, false);
   }

   public final Deque<RoomTile> findPath(RoomTile oldTile, RoomTile newTile, RoomTile goalLocation, RoomUnit roomUnit, boolean isWalktroughRetry) {
      if (this.room != null && this.room.isLoaded() && oldTile != null && newTile != null && !oldTile.equals(newTile) && newTile.state != RoomTileState.INVALID
         )
       {
         LinkedList<RoomTile> openList = new LinkedList<>();
         List<RoomTile> closedList = new LinkedList<>();
         openList.add(oldTile.copy());
         RoomTile doorTile = this.room.getLayout().getDoorTile();
         long startMillis = System.currentTimeMillis();

         while (!openList.isEmpty()) {
            if (System.currentTimeMillis() - startMillis > Emulator.getConfig().getInt("pathfinder.execution_time.milli", 25)
               && Emulator.getConfig().getBoolean("pathfinder.max_execution_time.enabled", false)) {
               return null;
            }

            RoomTile current = this.lowestFInOpen(openList);
            if (current.x == newTile.x && current.y == newTile.y) {
               return this.calcPath(this.findTile(openList, oldTile.x, oldTile.y), current);
            }

            closedList.add(current);
            openList.remove(current);

            for (RoomTile currentAdj : this.getAdjacent(openList, current, newTile, roomUnit)) {
               if (!closedList.contains(currentAdj)) {
                  if (roomUnit.canOverrideTile(currentAdj)) {
                     currentAdj.setPrevious(current);
                     currentAdj.sethCosts(this.findTile(openList, newTile.x, newTile.y));
                     currentAdj.setgCosts(current);
                     openList.add(currentAdj);
                  } else if (currentAdj.state != RoomTileState.BLOCKED
                     && (currentAdj.state != RoomTileState.SIT && currentAdj.state != RoomTileState.LAY || currentAdj.equals(goalLocation))) {
                     double height = currentAdj.getStackHeight() - current.getStackHeight();
                     if ((ALLOW_FALLING || !(height < -MAXIMUM_STEP_HEIGHT)) && (currentAdj.state != RoomTileState.OPEN || !(height > MAXIMUM_STEP_HEIGHT))) {
                        if (!currentAdj.hasUnits()
                           || !(doorTile.distance(currentAdj) > 2.0)
                           || isWalktroughRetry && this.room.isAllowWalkthrough() && !currentAdj.equals(goalLocation)) {
                           if (!openList.contains(currentAdj)) {
                              currentAdj.setPrevious(current);
                              currentAdj.sethCosts(this.findTile(openList, newTile.x, newTile.y));
                              currentAdj.setgCosts(current);
                              openList.add(currentAdj);
                           } else if (currentAdj.getgCosts() > currentAdj.calculategCosts(current)) {
                              currentAdj.setPrevious(current);
                              currentAdj.setgCosts(current);
                           }
                        } else {
                           closedList.add(currentAdj);
                           openList.remove(currentAdj);
                        }
                     } else {
                        closedList.add(currentAdj);
                        openList.remove(currentAdj);
                     }
                  } else {
                     closedList.add(currentAdj);
                     openList.remove(currentAdj);
                  }
               }
            }
         }

         return this.room.isAllowWalkthrough() && !isWalktroughRetry ? this.findPath(oldTile, newTile, goalLocation, roomUnit, true) : null;
      } else {
         return new LinkedList<>();
      }
   }

   private RoomTile findTile(List<RoomTile> tiles, short x, short y) {
      for (RoomTile tile : tiles) {
         if (x == tile.x && y == tile.y) {
            return tile;
         }
      }

      RoomTile tile = this.getTile(x, y);
      return tile != null ? tile.copy() : null;
   }

   public Deque<RoomTile> calcPath(RoomTile start, RoomTile goal) {
      LinkedList<RoomTile> path = new LinkedList<>();
      if (start == null) {
         return path;
      }

      RoomTile curr = goal;

      while (curr != null) {
         path.addFirst(this.getTile(curr.x, curr.y));
         curr = curr.getPrevious();
         if (curr != null && curr.equals(start)) {
            return path;
         }
      }

      return path;
   }

   private RoomTile lowestFInOpen(List<RoomTile> openList) {
      if (openList == null) {
         return null;
      }

      RoomTile cheapest = openList.get(0);

      for (RoomTile anOpenList : openList) {
         if (anOpenList.getfCosts() < cheapest.getfCosts()) {
            cheapest = anOpenList;
         }
      }

      return cheapest;
   }

   private List<RoomTile> getAdjacent(List<RoomTile> openList, RoomTile node, RoomTile nextTile, RoomUnit unit) {
      short x = node.x;
      short y = node.y;
      List<RoomTile> adj = new LinkedList<>();
      if (x > 0) {
         RoomTile temp = this.findTile(openList, (short)(x - 1), y);
         if (this.canWalkOn(temp, unit) && (temp.state != RoomTileState.SIT || nextTile.getStackHeight() - node.getStackHeight() <= 2.0)) {
            temp.isDiagonally(false);
            if (!adj.contains(temp)) {
               adj.add(temp);
            }
         }
      }

      if (x < this.mapSizeX) {
         RoomTile temp = this.findTile(openList, (short)(x + 1), y);
         if (this.canWalkOn(temp, unit) && (temp.state != RoomTileState.SIT || nextTile.getStackHeight() - node.getStackHeight() <= 2.0)) {
            temp.isDiagonally(false);
            if (!adj.contains(temp)) {
               adj.add(temp);
            }
         }
      }

      if (y > 0) {
         RoomTile temp = this.findTile(openList, x, (short)(y - 1));
         if (this.canWalkOn(temp, unit) && (temp.state != RoomTileState.SIT || nextTile.getStackHeight() - node.getStackHeight() <= 2.0)) {
            temp.isDiagonally(false);
            if (!adj.contains(temp)) {
               adj.add(temp);
            }
         }
      }

      if (y < this.mapSizeY) {
         RoomTile temp = this.findTile(openList, x, (short)(y + 1));
         if (this.canWalkOn(temp, unit) && (temp.state != RoomTileState.SIT || nextTile.getStackHeight() - node.getStackHeight() <= 2.0)) {
            temp.isDiagonally(false);
            if (!adj.contains(temp)) {
               adj.add(temp);
            }
         }
      }

      if (this.CANMOVEDIAGONALY) {
         if (x < this.mapSizeX && y < this.mapSizeY) {
            RoomTile temp = this.findTile(openList, (short)(x + 1), (short)(y + 1));
            if (this.canWalkOn(temp, unit) && (temp.state != RoomTileState.SIT || nextTile.getStackHeight() - node.getStackHeight() <= 2.0)) {
               temp.isDiagonally(true);
               if (!adj.contains(temp)) {
                  adj.add(temp);
               }
            }
         }

         if (x > 0 && y > 0) {
            RoomTile temp = this.findTile(openList, (short)(x - 1), (short)(y - 1));
            if (this.canWalkOn(temp, unit) && (temp.state != RoomTileState.SIT || nextTile.getStackHeight() - node.getStackHeight() <= 2.0)) {
               temp.isDiagonally(true);
               if (!adj.contains(temp)) {
                  adj.add(temp);
               }
            }
         }

         if (x > 0 && y < this.mapSizeY) {
            RoomTile temp = this.findTile(openList, (short)(x - 1), (short)(y + 1));
            if (this.canWalkOn(temp, unit) && (temp.state != RoomTileState.SIT || nextTile.getStackHeight() - node.getStackHeight() <= 2.0)) {
               temp.isDiagonally(true);
               if (!adj.contains(temp)) {
                  adj.add(temp);
               }
            }
         }

         if (x < this.mapSizeX && y > 0) {
            RoomTile temp = this.findTile(openList, (short)(x + 1), (short)(y - 1));
            if (this.canWalkOn(temp, unit) && (temp.state != RoomTileState.SIT || nextTile.getStackHeight() - node.getStackHeight() <= 2.0)) {
               temp.isDiagonally(true);
               if (!adj.contains(temp)) {
                  adj.add(temp);
               }
            }
         }
      }

      return adj;
   }

   private boolean canWalkOn(RoomTile tile, RoomUnit unit) {
      return tile != null && (unit.canOverrideTile(tile) || tile.state != RoomTileState.BLOCKED && tile.state != RoomTileState.INVALID);
   }

   public void moveDiagonally(boolean value) {
      this.CANMOVEDIAGONALY = value;
   }

   public RoomTile getTileInFront(RoomTile tile, int rotation) {
      return this.getTileInFront(tile, rotation, 0);
   }

   public RoomTile getTileInFront(RoomTile tile, int rotation, int offset) {
      int offsetX = 0;
      int offsetY = 0;
      rotation %= 8;
      switch (rotation) {
         case 0:
            offsetY--;
            break;
         case 1:
            offsetX++;
            offsetY--;
            break;
         case 2:
            offsetX++;
            break;
         case 3:
            offsetX++;
            offsetY++;
            break;
         case 4:
            offsetY++;
            break;
         case 5:
            offsetX--;
            offsetY++;
            break;
         case 6:
            offsetX--;
            break;
         case 7:
            offsetX--;
            offsetY--;
      }

      short x = tile.x;
      short y = tile.y;

      for (int i = 0; i <= offset; i++) {
         x = (short)(x + offsetX);
         y = (short)(y + offsetY);
      }

      return this.getTile(x, y);
   }

   public List<RoomTile> getTilesInFront(RoomTile tile, int rotation, int amount) {
      List<RoomTile> tiles = new ArrayList<>(amount);
      RoomTile previous = tile;

      for (int i = 0; i < amount; i++) {
         RoomTile t = this.getTileInFront(previous, rotation, i);
         if (t == null) {
            break;
         }

         tiles.add(t);
      }

      return tiles;
   }

   public List<RoomTile> getTilesAround(RoomTile tile) {
      return this.getTilesAround(tile, 0);
   }

   public List<RoomTile> getTilesAround(RoomTile tile, int directionOffset) {
      return this.getTilesAround(tile, directionOffset, true);
   }

   public List<RoomTile> getTilesAround(RoomTile tile, int directionOffset, boolean diagonal) {
      List<RoomTile> tiles = new ArrayList<>(diagonal ? 8 : 4);
      if (tile != null) {
         for (int i = 0; i < 8; i += diagonal ? 1 : 2) {
            RoomTile t = this.getTileInFront(tile, (i + directionOffset) % 8);
            if (t != null) {
               tiles.add(t);
            }
         }
      }

      return tiles;
   }

   public List<RoomTile> getWalkableTilesAround(RoomTile tile) {
      return this.getWalkableTilesAround(tile, 0);
   }

   public List<RoomTile> getWalkableTilesAround(RoomTile tile, int directionOffset) {
      List<RoomTile> availableTiles = new ArrayList<>(this.getTilesAround(tile, directionOffset));
      List<RoomTile> toRemove = new ArrayList<>();

      for (RoomTile t : availableTiles) {
         if (t == null || t.state != RoomTileState.OPEN || !t.isWalkable()) {
            toRemove.add(t);
         }
      }

      for (RoomTile t : toRemove) {
         availableTiles.remove(t);
      }

      return availableTiles;
   }

   public boolean fitsOnMap(RoomTile tile, int width, int length, int rotation) {
      if (tile != null) {
         if (rotation != 0 && rotation != 4) {
            if (rotation == 2 || rotation == 6) {
               for (short i = tile.x; i <= tile.x + (length - 1); i++) {
                  for (short j = tile.y; j <= tile.y + (width - 1); j++) {
                     RoomTile t = this.getTile(i, j);
                     if (t == null || t.state == RoomTileState.INVALID) {
                        return false;
                     }
                  }
               }
            }
         } else {
            for (short i = tile.x; i <= tile.x + (width - 1); i++) {
               for (short j = tile.y; j <= tile.y + (length - 1); j++) {
                  RoomTile t = this.getTile(i, j);
                  if (t == null || t.state == RoomTileState.INVALID) {
                     return false;
                  }
               }
            }
         }
      }

      return true;
   }

   public THashSet<RoomTile> getTilesAt(RoomTile tile, int width, int length, int rotation) {
      THashSet<RoomTile> pointList = new THashSet(width * length, 0.1F);
      if (tile != null) {
         if (rotation != 0 && rotation != 4) {
            if (rotation == 2 || rotation == 6) {
               for (short i = tile.x; i <= tile.x + (length - 1); i++) {
                  for (short j = tile.y; j <= tile.y + (width - 1); j++) {
                     RoomTile t = this.getTile(i, j);
                     if (t != null) {
                        pointList.add(t);
                     }
                  }
               }
            }
         } else {
            for (short i = tile.x; i <= tile.x + (width - 1); i++) {
               for (short j = tile.y; j <= tile.y + (length - 1); j++) {
                  RoomTile t = this.getTile(i, j);
                  if (t != null) {
                     pointList.add(t);
                  }
               }
            }
         }
      }

      return pointList;
   }
}
