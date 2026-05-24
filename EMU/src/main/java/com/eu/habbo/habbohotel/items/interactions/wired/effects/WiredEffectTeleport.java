package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomTileState;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.wired.WiredSaveException;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserEffectComposer;
import com.eu.habbo.threading.runnables.RoomUnitTeleport;
import com.eu.habbo.threading.runnables.SendRoomUnitEffectComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class WiredEffectTeleport extends InteractionWiredEffect {
   public static final WiredEffectType type = WiredEffectType.TELEPORT;
   protected List<HabboItem> items = new ArrayList<>();

   public WiredEffectTeleport(ResultSet set, Item baseItem) throws SQLException {
      super(set, baseItem);
   }

   public WiredEffectTeleport(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
      super(id, userId, item, extradata, limitedStack, limitedSells);
   }

   public static void teleportUnitToTile(RoomUnit roomUnit, RoomTile tile) {
      if (roomUnit != null && tile != null && !roomUnit.isWiredTeleporting) {
         Room room = roomUnit.getRoom();
         if (room != null) {
            roomUnit.getRoom().unIdle(roomUnit.getRoom().getHabbo(roomUnit));
            room.sendComposer(new RoomUserEffectComposer(roomUnit, 4).compose());
            Emulator.getThreading().run(new SendRoomUnitEffectComposer(room, roomUnit), WiredHandler.TELEPORT_DELAY + 1000);
            if (tile != roomUnit.getCurrentLocation()) {
               if (tile.state == RoomTileState.INVALID || tile.state == RoomTileState.BLOCKED) {
                  RoomTile alternativeTile = null;
                  List<RoomTile> optionalTiles = room.getLayout().getTilesAround(tile);
                  Collections.reverse(optionalTiles);

                  for (RoomTile optionalTile : optionalTiles) {
                     if (optionalTile.state != RoomTileState.INVALID && optionalTile.state != RoomTileState.BLOCKED) {
                        alternativeTile = optionalTile;
                        break;
                     }
                  }

                  if (alternativeTile != null) {
                     tile = alternativeTile;
                  }
               }

               Emulator.getThreading().run(() -> roomUnit.isWiredTeleporting = true, Math.max(0, WiredHandler.TELEPORT_DELAY - 500));
               Emulator.getThreading()
                  .run(
                     new RoomUnitTeleport(
                        roomUnit, room, tile.x, tile.y, tile.getStackHeight() + (tile.state == RoomTileState.SIT ? -0.5 : 0.0), roomUnit.getEffectId()
                     ),
                     WiredHandler.TELEPORT_DELAY
                  );
            }
         }
      }
   }

   @Override
   public void serializeWiredData(ServerMessage message, Room room) {
      THashSet<HabboItem> items = new THashSet();

      for (HabboItem item : this.items) {
         if (item.getRoomId() != this.getRoomId()
            || Emulator.getGameEnvironment().getRoomManager().getRoom(this.getRoomId()).getHabboItem(item.getId()) == null) {
            items.add(item);
         }
      }

      TObjectHashIterator var7 = items.iterator();

      while (var7.hasNext()) {
         HabboItem item = (HabboItem)var7.next();
         this.items.remove(item);
      }

      message.appendBoolean(false);
      message.appendInt(WiredHandler.MAXIMUM_FURNI_SELECTION);
      message.appendInt(this.items.size());

      for (HabboItem item : this.items) {
         message.appendInt(item.getId());
      }

      message.appendInt(this.getBaseItem().getSpriteId());
      message.appendInt(this.getId());
      message.appendString("");
      message.appendInt(0);
      message.appendInt(0);
      message.appendInt(this.getType().code);
      message.appendInt(this.getDelay());
      if (this.requiresTriggeringUser()) {
         final List<Integer> invalidTriggers = new ArrayList<>();
         room.getRoomSpecialTypes().getTriggers(this.getX(), this.getY()).forEach(new TObjectProcedure<InteractionWiredTrigger>() {
            public boolean execute(InteractionWiredTrigger object) {
               if (!object.isTriggeredByRoomUnit()) {
                  invalidTriggers.add(object.getId());
               }

               return true;
            }
         });
         message.appendInt(invalidTriggers.size());

         for (Integer i : invalidTriggers) {
            message.appendInt(i);
         }
      } else {
         message.appendInt(0);
      }
   }

   @Override
   public boolean saveData(WiredSettings settings, GameClient gameClient) throws WiredSaveException {
      int itemsCount = settings.getFurniIds().length;
      if (itemsCount > Emulator.getConfig().getInt("hotel.wired.furni.selection.count")) {
         throw new WiredSaveException("Too many furni selected");
      }

      List<HabboItem> newItems = new ArrayList<>();

      for (int i = 0; i < itemsCount; i++) {
         int itemId = settings.getFurniIds()[i];
         HabboItem it = Emulator.getGameEnvironment().getRoomManager().getRoom(this.getRoomId()).getHabboItem(itemId);
         if (it == null) {
            throw new WiredSaveException(String.format("Item %s not found", itemId));
         }

         newItems.add(it);
      }

      int delay = settings.getDelay();
      if (delay > Emulator.getConfig().getInt("hotel.wired.max_delay", 20)) {
         throw new WiredSaveException("Delay too long");
      }

      this.items.clear();
      this.items.addAll(newItems);
      this.setDelay(delay);
      return true;
   }

   @Override
   public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
      this.items
         .removeIf(
            itemx -> itemx == null
               || itemx.getRoomId() != this.getRoomId()
               || Emulator.getGameEnvironment().getRoomManager().getRoom(this.getRoomId()).getHabboItem(itemx.getId()) == null
         );
      if (!this.items.isEmpty()) {
         int i = Emulator.getRandom().nextInt(this.items.size());
         HabboItem item = this.items.get(i);
         teleportUnitToTile(roomUnit, room.getLayout().getTile(item.getX(), item.getY()));
         return true;
      } else {
         return false;
      }
   }

   @Override
   public String getWiredData() {
      return WiredHandler.getGsonBuilder()
         .create()
         .toJson(new WiredEffectTeleport.JsonData(this.getDelay(), this.items.stream().map(HabboItem::getId).collect(Collectors.toList())));
   }

   @Override
   public void loadWiredData(ResultSet set, Room room) throws SQLException {
      this.items = new ArrayList<>();
      String wiredData = set.getString("wired_data");
      if (wiredData.startsWith("{")) {
         WiredEffectTeleport.JsonData data = (WiredEffectTeleport.JsonData)WiredHandler.getGsonBuilder()
            .create()
            .fromJson(wiredData, WiredEffectTeleport.JsonData.class);
         this.setDelay(data.delay);

         for (Integer id : data.itemIds) {
            HabboItem item = room.getHabboItem(id);
            if (item != null) {
               this.items.add(item);
            }
         }
      } else {
         String[] wiredDataOld = wiredData.split("\t");
         if (wiredDataOld.length >= 1) {
            this.setDelay(Integer.parseInt(wiredDataOld[0]));
         }

         if (wiredDataOld.length == 2 && wiredDataOld[1].contains(";")) {
            for (String s : wiredDataOld[1].split(";")) {
               HabboItem item = room.getHabboItem(Integer.parseInt(s));
               if (item != null) {
                  this.items.add(item);
               }
            }
         }
      }
   }

   @Override
   public void onPickUp() {
      this.items.clear();
      this.setDelay(0);
   }

   @Override
   public WiredEffectType getType() {
      return type;
   }

   @Override
   public boolean requiresTriggeringUser() {
      return true;
   }

   @Override
   protected long requiredCooldown() {
      return 50L;
   }

   static class JsonData {
      int delay;
      List<Integer> itemIds;

      public JsonData(int delay, List<Integer> itemIds) {
         this.delay = delay;
         this.itemIds = itemIds;
      }
   }
}
