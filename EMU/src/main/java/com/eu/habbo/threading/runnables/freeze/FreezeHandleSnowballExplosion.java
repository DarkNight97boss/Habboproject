package com.eu.habbo.threading.runnables.freeze;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.games.freeze.FreezeGame;
import com.eu.habbo.habbohotel.games.freeze.FreezeGamePlayer;
import com.eu.habbo.habbohotel.items.interactions.games.freeze.InteractionFreezeBlock;
import com.eu.habbo.habbohotel.items.interactions.games.freeze.InteractionFreezeTile;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FreezeHandleSnowballExplosion implements Runnable {
   private static final Logger LOGGER = LoggerFactory.getLogger(FreezeHandleSnowballExplosion.class);
   private final FreezeThrowSnowball thrownData;

   public FreezeHandleSnowballExplosion(FreezeThrowSnowball thrownData) {
      this.thrownData = thrownData;
   }

   @Override
   public void run() {
      try {
         if (this.thrownData == null || this.thrownData.habbo.getHabboInfo().getGamePlayer() == null) {
            return;
         }

         FreezeGamePlayer player = (FreezeGamePlayer)this.thrownData.habbo.getHabboInfo().getGamePlayer();
         if (player == null) {
            return;
         }

         player.addSnowball();
         THashSet<RoomTile> tiles = new THashSet();
         FreezeGame game = (FreezeGame)this.thrownData.room.getGame(FreezeGame.class);
         if (game == null) {
            return;
         }

         if (player.nextHorizontal) {
            tiles.addAll(game.affectedTilesByExplosion(this.thrownData.targetTile.getX(), this.thrownData.targetTile.getY(), this.thrownData.radius + 1));
         }

         if (player.nextDiagonal) {
            tiles.addAll(
               game.affectedTilesByExplosionDiagonal(this.thrownData.targetTile.getX(), this.thrownData.targetTile.getY(), this.thrownData.radius + 1)
            );
            player.nextDiagonal = false;
         }

         THashSet<InteractionFreezeTile> freezeTiles = new THashSet();
         TObjectHashIterator var5 = tiles.iterator();

         while (var5.hasNext()) {
            RoomTile roomTile = (RoomTile)var5.next();
            THashSet<HabboItem> items = this.thrownData.room.getItemsAt(roomTile);
            TObjectHashIterator var8 = items.iterator();

            while (var8.hasNext()) {
               HabboItem freezeTile = (HabboItem)var8.next();
               if (freezeTile instanceof InteractionFreezeTile || freezeTile instanceof InteractionFreezeBlock) {
                  int distance = 0;
                  if (freezeTile.getX() != this.thrownData.targetTile.getX() && freezeTile.getY() != this.thrownData.targetTile.getY()) {
                     distance = Math.abs(freezeTile.getX() - this.thrownData.targetTile.getX());
                  } else {
                     distance = (int)Math.ceil(
                        this.thrownData.room.getLayout().getTile(this.thrownData.targetTile.getX(), this.thrownData.targetTile.getY()).distance(roomTile)
                     );
                  }

                  if (freezeTile instanceof InteractionFreezeTile) {
                     freezeTile.setExtradata("11" + String.format("%03d", distance * 100));
                     freezeTiles.add((InteractionFreezeTile)freezeTile);
                     this.thrownData.room.updateItem(freezeTile);
                     THashSet<Habbo> habbos = new THashSet();
                     habbos.addAll(this.thrownData.room.getHabbosAt(freezeTile.getX(), freezeTile.getY()));
                     TObjectHashIterator var12 = habbos.iterator();

                     while (var12.hasNext()) {
                        Habbo habbo = (Habbo)var12.next();
                        if (habbo.getHabboInfo().getGamePlayer() != null && habbo.getHabboInfo().getGamePlayer() instanceof FreezeGamePlayer) {
                           FreezeGamePlayer hPlayer = (FreezeGamePlayer)habbo.getHabboInfo().getGamePlayer();
                           if (hPlayer.canGetFrozen()) {
                              if (hPlayer.getTeamColor().equals(player.getTeamColor())) {
                                 player.addScore(-FreezeGame.FREEZE_LOOSE_POINTS);
                              } else {
                                 player.addScore(FreezeGame.FREEZE_LOOSE_POINTS);
                              }

                              ((FreezeGamePlayer)habbo.getHabboInfo().getGamePlayer()).freeze();
                              if (this.thrownData.habbo != habbo) {
                                 AchievementManager.progressAchievement(habbo, Emulator.getGameEnvironment().getAchievementManager().getAchievement("EsA"));
                              }
                           }
                        }
                     }
                  } else if (freezeTile instanceof InteractionFreezeBlock && freezeTile.getExtradata().equalsIgnoreCase("0")) {
                     game.explodeBox((InteractionFreezeBlock)freezeTile, distance * 100);
                     player.addScore(FreezeGame.DESTROY_BLOCK_POINTS);
                  }
               }
            }
         }

         Emulator.getThreading().run(new FreezeResetExplosionTiles(freezeTiles, this.thrownData.room), 1000L);
      } catch (Exception e) {
         LOGGER.error("Caught exception", e);
      }
   }
}
