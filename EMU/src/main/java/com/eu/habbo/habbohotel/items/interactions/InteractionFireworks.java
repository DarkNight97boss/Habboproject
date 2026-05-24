package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.threading.runnables.RoomUnitWalkToLocation;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InteractionFireworks extends InteractionDefault {
   private static final Logger LOGGER = LoggerFactory.getLogger(InteractionFireworks.class);
   private static final String STATE_EMPTY = "0";
   private static final String STATE_CHARGED = "1";
   private static final String STATE_EXPLOSION = "2";

   public InteractionFireworks(ResultSet set, Item baseItem) throws SQLException {
      super(set, baseItem);
   }

   public InteractionFireworks(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
      super(id, userId, item, extradata, limitedStack, limitedSells);
   }

   @Override
   public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objects) {
      return false;
   }

   @Override
   public void onClick(GameClient client, Room room, Object[] objects) throws Exception {
      if (room != null) {
         if (objects.length >= 2 && objects[1] instanceof WiredEffectType && objects[1] == WiredEffectType.TOGGLE_STATE) {
            if (this.getExtradata().equalsIgnoreCase("1")) {
               super.onClick(client, room, objects);
               if (this.getExtradata().equalsIgnoreCase("2")) {
                  this.reCharge(room);
               }
            }
         } else if (client != null) {
            if (!this.canToggle(client.getHabbo(), room)) {
               RoomTile closestTile = null;

               for (RoomTile tile : room.getLayout().getTilesAround(room.getLayout().getTile(this.getX(), this.getY()))) {
                  if (tile.isWalkable()
                     && (
                        closestTile == null
                           || closestTile.distance(client.getHabbo().getRoomUnit().getCurrentLocation())
                              > tile.distance(client.getHabbo().getRoomUnit().getCurrentLocation())
                     )) {
                     closestTile = tile;
                  }
               }

               if (closestTile != null && !closestTile.equals(client.getHabbo().getRoomUnit().getCurrentLocation())) {
                  List<Runnable> onSuccess = new ArrayList<>();
                  onSuccess.add(() -> {
                     try {
                        this.onClick(client, room, objects);
                     } catch (Exception e) {
                        e.printStackTrace();
                     }
                  });
                  client.getHabbo().getRoomUnit().setGoalLocation(closestTile);
                  Emulator.getThreading().run(new RoomUnitWalkToLocation(client.getHabbo().getRoomUnit(), closestTile, room, onSuccess, new ArrayList<>()));
               }
            }

            if (this.getExtradata().equalsIgnoreCase("1")) {
               super.onClick(client, room, objects);
               if (this.getExtradata().equalsIgnoreCase("2")) {
                  this.reCharge(room);
                  AchievementManager.progressAchievement(
                     client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("FireworksCharger")
                  );
               }
            }
         }
      }
   }

   @Override
   public boolean allowWiredResetState() {
      return false;
   }

   @Override
   public void onPlace(Room room) {
      super.onPlace(room);
      this.setExtradata("1");
   }

   @Override
   public boolean canToggle(Habbo habbo, Room room) {
      return room.hasRights(habbo) || RoomLayout.tilesAdjecent(room.getLayout().getTile(this.getX(), this.getY()), habbo.getRoomUnit().getCurrentLocation());
   }

   private void reCharge(Room room) {
      int explodeDuration = 5000;
      if (!this.getBaseItem().getCustomParams().isEmpty()) {
         try {
            explodeDuration = Integer.parseInt(this.getBaseItem().getCustomParams());
         } catch (NumberFormatException e) {
            LOGGER.error(
               "Incorrect customparams ("
                  + this.getBaseItem().getCustomParams()
                  + ") for base item ID ("
                  + this.getBaseItem().getId()
                  + ") of type ("
                  + this.getBaseItem().getName()
                  + ")"
            );
         }
      }

      Emulator.getThreading().run(() -> {
         this.setExtradata("1");
         this.needsUpdate(true);
         room.updateItemState(this);
      }, explodeDuration);
   }
}
