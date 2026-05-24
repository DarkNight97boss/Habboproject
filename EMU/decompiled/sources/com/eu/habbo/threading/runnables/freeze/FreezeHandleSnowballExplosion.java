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

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/freeze/FreezeHandleSnowballExplosion.class */
class FreezeHandleSnowballExplosion implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(FreezeHandleSnowballExplosion.class);
    private final FreezeThrowSnowball thrownData;

    public FreezeHandleSnowballExplosion(FreezeThrowSnowball freezeThrowSnowball) {
        this.thrownData = freezeThrowSnowball;
    }

    @Override // java.lang.Runnable
    public void run() {
        FreezeGamePlayer freezeGamePlayer;
        try {
            if (this.thrownData == null || this.thrownData.habbo.getHabboInfo().getGamePlayer() == null || (freezeGamePlayer = (FreezeGamePlayer) this.thrownData.habbo.getHabboInfo().getGamePlayer()) == null) {
                return;
            }
            freezeGamePlayer.addSnowball();
            THashSet tHashSet = new THashSet();
            FreezeGame freezeGame = (FreezeGame) this.thrownData.room.getGame(FreezeGame.class);
            if (freezeGame == null) {
                return;
            }
            if (freezeGamePlayer.nextHorizontal) {
                tHashSet.addAll(freezeGame.affectedTilesByExplosion(this.thrownData.targetTile.getX(), this.thrownData.targetTile.getY(), this.thrownData.radius + 1));
            }
            if (freezeGamePlayer.nextDiagonal) {
                tHashSet.addAll(freezeGame.affectedTilesByExplosionDiagonal(this.thrownData.targetTile.getX(), this.thrownData.targetTile.getY(), this.thrownData.radius + 1));
                freezeGamePlayer.nextDiagonal = false;
            }
            THashSet tHashSet2 = new THashSet();
            TObjectHashIterator it = tHashSet.iterator();
            while (it.hasNext()) {
                RoomTile roomTile = (RoomTile) it.next();
                TObjectHashIterator it2 = this.thrownData.room.getItemsAt(roomTile).iterator();
                while (it2.hasNext()) {
                    HabboItem habboItem = (HabboItem) it2.next();
                    if ((habboItem instanceof InteractionFreezeTile) || (habboItem instanceof InteractionFreezeBlock)) {
                        int iCeil = (habboItem.getX() == this.thrownData.targetTile.getX() || habboItem.getY() == this.thrownData.targetTile.getY()) ? (int) Math.ceil(this.thrownData.room.getLayout().getTile(this.thrownData.targetTile.getX(), this.thrownData.targetTile.getY()).distance(roomTile)) : Math.abs(habboItem.getX() - this.thrownData.targetTile.getX());
                        if (habboItem instanceof InteractionFreezeTile) {
                            habboItem.setExtradata("11" + String.format("%03d", Integer.valueOf(iCeil * 100)));
                            tHashSet2.add((InteractionFreezeTile) habboItem);
                            this.thrownData.room.updateItem(habboItem);
                            THashSet tHashSet3 = new THashSet();
                            tHashSet3.addAll(this.thrownData.room.getHabbosAt(habboItem.getX(), habboItem.getY()));
                            TObjectHashIterator it3 = tHashSet3.iterator();
                            while (it3.hasNext()) {
                                Habbo habbo = (Habbo) it3.next();
                                if (habbo.getHabboInfo().getGamePlayer() != null && (habbo.getHabboInfo().getGamePlayer() instanceof FreezeGamePlayer)) {
                                    FreezeGamePlayer freezeGamePlayer2 = (FreezeGamePlayer) habbo.getHabboInfo().getGamePlayer();
                                    if (freezeGamePlayer2.canGetFrozen()) {
                                        if (freezeGamePlayer2.getTeamColor().equals(freezeGamePlayer.getTeamColor())) {
                                            freezeGamePlayer.addScore(-FreezeGame.FREEZE_LOOSE_POINTS);
                                        } else {
                                            freezeGamePlayer.addScore(FreezeGame.FREEZE_LOOSE_POINTS);
                                        }
                                        ((FreezeGamePlayer) habbo.getHabboInfo().getGamePlayer()).freeze();
                                        if (this.thrownData.habbo != habbo) {
                                            AchievementManager.progressAchievement(habbo, Emulator.getGameEnvironment().getAchievementManager().getAchievement("EsA"));
                                        }
                                    }
                                }
                            }
                        } else if ((habboItem instanceof InteractionFreezeBlock) && habboItem.getExtradata().equalsIgnoreCase("0")) {
                            freezeGame.explodeBox((InteractionFreezeBlock) habboItem, iCeil * 100);
                            freezeGamePlayer.addScore(FreezeGame.DESTROY_BLOCK_POINTS);
                        }
                    }
                }
            }
            Emulator.getThreading().run(new FreezeResetExplosionTiles(tHashSet2, this.thrownData.room), 1000L);
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
        }
    }
}
