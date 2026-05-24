package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomTileState;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.rooms.RoomUserRotation;
import com.eu.habbo.habbohotel.users.HabboGender;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.threading.runnables.RoomUnitGiveHanditem;
import com.eu.habbo.threading.runnables.RoomUnitWalkToLocation;
import com.eu.habbo.util.pathfinding.Rotation;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InteractionVendingMachine extends HabboItem {
   public InteractionVendingMachine(ResultSet set, Item baseItem) throws SQLException {
      super(set, baseItem);
      this.setExtradata("0");
   }

   public InteractionVendingMachine(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
      super(id, userId, item, extradata, limitedStack, limitedSells);
      this.setExtradata("0");
   }

   public THashSet<RoomTile> getActivatorTiles(Room room) {
      THashSet<RoomTile> tiles = new THashSet();
      RoomTile tileInFront = getSquareInFront(room.getLayout(), this);
      if (tileInFront != null) {
         tiles.add(tileInFront);
      }

      tiles.add(room.getLayout().getTile(this.getX(), this.getY()));
      return tiles;
   }

   @Override
   public void serializeExtradata(ServerMessage serverMessage) {
      serverMessage.appendInt(this.isLimited() ? 256 : 0);
      serverMessage.appendString(this.getExtradata());
      super.serializeExtradata(serverMessage);
   }

   private void tryInteract(GameClient client, Room room, RoomUnit unit) {
      THashSet<RoomTile> activatorTiles = this.getActivatorTiles(room);
      if (activatorTiles.size() != 0) {
         boolean inActivatorSpace = false;
         TObjectHashIterator var6 = activatorTiles.iterator();

         while (var6.hasNext()) {
            RoomTile tile = (RoomTile)var6.next();
            if (unit.getCurrentLocation().is(unit.getX(), unit.getY())) {
               inActivatorSpace = true;
            }
         }

         if (inActivatorSpace) {
            this.useVendingMachine(client, room, unit);
         }
      }
   }

   private void useVendingMachine(GameClient client, Room room, RoomUnit unit) {
      this.setExtradata("1");
      room.updateItem(this);

      try {
         super.onClick(client, room, new Object[]{"TOGGLE_OVERRIDE"});
      } catch (Exception e) {
         e.printStackTrace();
      }

      if (!unit.isWalking() && !unit.hasStatus(RoomUnitStatus.SIT) && !unit.hasStatus(RoomUnitStatus.LAY)) {
         this.rotateToMachine(room, unit);
      }

      Emulator.getThreading().run(() -> {
         this.giveVendingMachineItem(room, unit);
         if (this.getBaseItem().getEffectM() > 0 && client.getHabbo().getHabboInfo().getGender() == HabboGender.M) {
            room.giveEffect(client.getHabbo(), this.getBaseItem().getEffectM(), -1);
         }

         if (this.getBaseItem().getEffectF() > 0 && client.getHabbo().getHabboInfo().getGender() == HabboGender.F) {
            room.giveEffect(client.getHabbo(), this.getBaseItem().getEffectF(), -1);
         }

         Emulator.getThreading().run(this, 500L);
      }, 1500L);
   }

   public void giveVendingMachineItem(Room room, RoomUnit unit) {
      Emulator.getThreading().run(new RoomUnitGiveHanditem(unit, room, this.getBaseItem().getRandomVendingItem()));
   }

   @Override
   public void onClick(GameClient client, Room room, Object[] objects) throws Exception {
      if (client != null) {
         RoomUnit unit = client.getHabbo().getRoomUnit();
         THashSet<RoomTile> activatorTiles = this.getActivatorTiles(room);
         if (activatorTiles.size() != 0) {
            boolean inActivatorSpace = false;
            TObjectHashIterator tileToWalkTo = activatorTiles.iterator();

            while (tileToWalkTo.hasNext()) {
               RoomTile tile = (RoomTile)tileToWalkTo.next();
               if (unit.getCurrentLocation().is(tile.x, tile.y)) {
                  inActivatorSpace = true;
               }
            }

            if (!inActivatorSpace) {
               RoomTile tileToWalkTox = null;
               TObjectHashIterator var11 = activatorTiles.iterator();

               while (var11.hasNext()) {
                  RoomTile tile = (RoomTile)var11.next();
                  if ((tile.state == RoomTileState.OPEN || tile.state == RoomTileState.SIT)
                     && (tileToWalkTox == null || tileToWalkTox.distance(unit.getCurrentLocation()) > tile.distance(unit.getCurrentLocation()))) {
                     tileToWalkTox = tile;
                  }
               }

               if (tileToWalkTox != null) {
                  List<Runnable> onSuccess = new ArrayList<>();
                  List<Runnable> onFail = new ArrayList<>();
                  onSuccess.add(() -> this.tryInteract(client, room, unit));
                  unit.setGoalLocation(tileToWalkTox);
                  Emulator.getThreading().run(new RoomUnitWalkToLocation(unit, tileToWalkTox, room, onSuccess, onFail));
               }
            } else {
               this.useVendingMachine(client, room, unit);
            }
         }
      }
   }

   @Override
   public void onWalk(RoomUnit roomUnit, Room room, Object[] objects) throws Exception {
   }

   @Override
   public void run() {
      super.run();
      if (this.getExtradata().equals("1")) {
         this.setExtradata("0");
         Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(this.getRoomId());
         if (room != null) {
            room.updateItem(this);
         }
      }
   }

   @Override
   public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objects) {
      return true;
   }

   @Override
   public boolean isWalkable() {
      return this.getBaseItem().allowWalk();
   }

   @Override
   public boolean isUsable() {
      return true;
   }

   private void rotateToMachine(Room room, RoomUnit unit) {
      RoomUserRotation rotation = RoomUserRotation.values()[Rotation.Calculate(unit.getX(), unit.getY(), this.getX(), this.getY())];
      if (Math.abs(unit.getBodyRotation().getValue() - rotation.getValue()) > 1) {
         unit.setRotation(rotation);
         unit.statusUpdate(true);
      }
   }
}
