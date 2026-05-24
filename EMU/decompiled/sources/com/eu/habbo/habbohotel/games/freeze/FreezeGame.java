package com.eu.habbo.habbohotel.games.freeze;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.games.Game;
import com.eu.habbo.habbohotel.games.GamePlayer;
import com.eu.habbo.habbohotel.games.GameState;
import com.eu.habbo.habbohotel.games.GameTeam;
import com.eu.habbo.habbohotel.games.GameTeamColors;
import com.eu.habbo.habbohotel.items.interactions.games.freeze.InteractionFreezeBlock;
import com.eu.habbo.habbohotel.items.interactions.games.freeze.InteractionFreezeExitTile;
import com.eu.habbo.habbohotel.items.interactions.games.freeze.InteractionFreezeTile;
import com.eu.habbo.habbohotel.items.interactions.games.freeze.gates.InteractionFreezeGate;
import com.eu.habbo.habbohotel.items.interactions.games.freeze.scoreboards.InteractionFreezeScoreboard;
import com.eu.habbo.habbohotel.items.interactions.wired.effects.WiredEffectTeleport;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUserAction;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserActionComposer;
import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.events.emulator.EmulatorConfigUpdatedEvent;
import com.eu.habbo.threading.runnables.freeze.FreezeClearEffects;
import com.eu.habbo.threading.runnables.freeze.FreezeThrowSnowball;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.hash.THashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/games/freeze/FreezeGame.class */
public class FreezeGame extends Game {
    private static final Logger LOGGER = LoggerFactory.getLogger(FreezeGame.class);
    public static final int effectId = 39;
    public static int POWER_UP_POINTS;
    public static int POWER_UP_CHANCE;
    public static int POWER_UP_PROTECT_TIME;
    public static int DESTROY_BLOCK_POINTS;
    public static int FREEZE_TIME;
    public static int FREEZE_LOOSE_SNOWBALL;
    public static int FREEZE_LOOSE_BOOST;
    public static int MAX_LIVES;
    public static int MAX_SNOWBALLS;
    public static int FREEZE_LOOSE_POINTS;
    public static boolean POWERUP_STACK;

    public FreezeGame(Room room) {
        super(FreezeGameTeam.class, FreezeGamePlayer.class, room, true);
        room.setAllowEffects(true);
    }

    @EventHandler
    public static void onConfigurationUpdated(EmulatorConfigUpdatedEvent emulatorConfigUpdatedEvent) {
        POWER_UP_POINTS = Emulator.getConfig().getInt("hotel.freeze.points.effect");
        POWER_UP_CHANCE = Emulator.getConfig().getInt("hotel.freeze.powerup.chance");
        POWER_UP_PROTECT_TIME = Emulator.getConfig().getInt("hotel.freeze.powerup.protection.time");
        DESTROY_BLOCK_POINTS = Emulator.getConfig().getInt("hotel.freeze.points.block");
        FREEZE_TIME = Emulator.getConfig().getInt("hotel.freeze.onfreeze.time.frozen");
        FREEZE_LOOSE_SNOWBALL = Emulator.getConfig().getInt("hotel.freeze.onfreeze.loose.snowballs");
        FREEZE_LOOSE_BOOST = Emulator.getConfig().getInt("hotel.freeze.onfreeze.loose.explosionboost");
        MAX_LIVES = Emulator.getConfig().getInt("hotel.freeze.powerup.max.lives");
        MAX_SNOWBALLS = Emulator.getConfig().getInt("hotel.freeze.powerup.max.snowballs");
        FREEZE_LOOSE_POINTS = Emulator.getConfig().getInt("hotel.freeze.points.freeze");
        POWERUP_STACK = Emulator.getConfig().getBoolean("hotel.freeze.powerup.protection.stack");
    }

    @Override // com.eu.habbo.habbohotel.games.Game
    public synchronized void initialise() {
        if (this.state == GameState.RUNNING) {
            return;
        }
        resetMap();
        Iterator it = this.teams.values().iterator();
        while (it.hasNext()) {
            ((GameTeam) it.next()).initialise();
        }
        start();
    }

    synchronized void resetMap() {
        TObjectHashIterator it = this.room.getFloorItems().iterator();
        while (it.hasNext()) {
            HabboItem habboItem = (HabboItem) it.next();
            if ((habboItem instanceof InteractionFreezeBlock) || (habboItem instanceof InteractionFreezeScoreboard)) {
                habboItem.setExtradata("0");
                this.room.updateItemState(habboItem);
            }
        }
    }

    public void throwBall(Habbo habbo, InteractionFreezeTile interactionFreezeTile) {
        if (this.state.equals(GameState.RUNNING) && habbo.getHabboInfo().isInGame() && habbo.getHabboInfo().getCurrentGame() == getClass()) {
            if ((interactionFreezeTile.getExtradata().equalsIgnoreCase("0") || interactionFreezeTile.getExtradata().isEmpty()) && RoomLayout.tilesAdjecent(habbo.getRoomUnit().getCurrentLocation(), this.room.getLayout().getTile(interactionFreezeTile.getX(), interactionFreezeTile.getY())) && ((FreezeGamePlayer) habbo.getHabboInfo().getGamePlayer()).canThrowSnowball()) {
                Emulator.getThreading().run(new FreezeThrowSnowball(habbo, interactionFreezeTile, this.room));
            }
        }
    }

    public THashSet<RoomTile> affectedTilesByExplosion(short s, short s2, int i) {
        THashSet<RoomTile> tHashSet = new THashSet<>();
        tHashSet.add(this.room.getLayout().getTile(s, s2));
        for (int i2 = 0; i2 < 8; i2 += 2) {
            for (int i3 = 0; i3 < i; i3++) {
                RoomTile tileInFront = this.room.getLayout().getTileInFront(this.room.getLayout().getTile(s, s2), i2, i3);
                if (tileInFront != null && tileInFront.x >= 0 && tileInFront.y >= 0 && tileInFront.x < this.room.getLayout().getMapSizeX() && tileInFront.y < this.room.getLayout().getMapSizeY()) {
                    tHashSet.add(tileInFront);
                }
            }
        }
        return tHashSet;
    }

    public THashSet<RoomTile> affectedTilesByExplosionDiagonal(short s, short s2, int i) {
        THashSet<RoomTile> tHashSet = new THashSet<>();
        for (int i2 = 1; i2 < 9; i2 += 2) {
            this.room.getLayout().getTile(s, s2);
            for (int i3 = 0; i3 < i; i3++) {
                RoomTile tileInFront = this.room.getLayout().getTileInFront(this.room.getLayout().getTile(s, s2), i2, i3);
                if (tileInFront != null && tileInFront.x >= 0 && tileInFront.y >= 0 && tileInFront.x < this.room.getLayout().getMapSizeX() && tileInFront.y < this.room.getLayout().getMapSizeY()) {
                    tHashSet.add(tileInFront);
                }
            }
        }
        return tHashSet;
    }

    public synchronized void explodeBox(InteractionFreezeBlock interactionFreezeBlock, int i) {
        int iNextInt = 0;
        if (Emulator.getRandom().nextInt(100) + 1 <= POWER_UP_CHANCE) {
            iNextInt = 0 + Emulator.getRandom().nextInt(6) + 1;
        }
        interactionFreezeBlock.setExtradata((iNextInt + 1) + String.format("%3d", Integer.valueOf(i)));
        this.room.updateItemState(interactionFreezeBlock);
    }

    public synchronized void givePowerUp(FreezeGamePlayer freezeGamePlayer, int i) {
        freezeGamePlayer.addScore(POWER_UP_POINTS);
        switch (i) {
            case 2:
                freezeGamePlayer.increaseExplosion();
                break;
            case 3:
                freezeGamePlayer.addSnowball();
                break;
            case 4:
                freezeGamePlayer.nextDiagonal = true;
                break;
            case 5:
                freezeGamePlayer.nextHorizontal = true;
                freezeGamePlayer.nextDiagonal = true;
                freezeGamePlayer.tempMassiveExplosion = true;
                break;
            case 6:
                freezeGamePlayer.addLife();
                break;
            case 7:
                freezeGamePlayer.addProtection();
                break;
        }
    }

    public synchronized void playerDies(GamePlayer gamePlayer) {
        Emulator.getThreading().run(new FreezeClearEffects(gamePlayer.getHabbo()), 1000L);
        if (this.room.getRoomSpecialTypes().hasFreezeExitTile()) {
            InteractionFreezeExitTile randomFreezeExitTile = this.room.getRoomSpecialTypes().getRandomFreezeExitTile();
            randomFreezeExitTile.setExtradata("1");
            this.room.updateItemState(randomFreezeExitTile);
            this.room.teleportHabboToItem(gamePlayer.getHabbo(), randomFreezeExitTile);
        }
        removeHabbo(gamePlayer.getHabbo());
    }

    @Override // com.eu.habbo.habbohotel.games.Game
    public void start() {
        if (this.state != GameState.IDLE) {
            return;
        }
        super.start();
        if (this.room.getRoomSpecialTypes().hasFreezeExitTile()) {
            for (Habbo habbo : this.room.getHabbos()) {
                if (getTeamForHabbo(habbo) == null) {
                    TObjectHashIterator it = this.room.getItemsAt(habbo.getRoomUnit().getCurrentLocation()).iterator();
                    while (it.hasNext()) {
                        if (((HabboItem) it.next()) instanceof InteractionFreezeTile) {
                            InteractionFreezeExitTile randomFreezeExitTile = this.room.getRoomSpecialTypes().getRandomFreezeExitTile();
                            WiredEffectTeleport.teleportUnitToTile(habbo.getRoomUnit(), this.room.getLayout().getTile(randomFreezeExitTile.getX(), randomFreezeExitTile.getY()));
                        }
                    }
                }
            }
        }
        refreshGates();
        setFreezeTileState("1");
        run();
    }

    @Override // com.eu.habbo.habbohotel.games.Game, java.lang.Runnable
    public synchronized void run() {
        try {
            if (this.state.equals(GameState.IDLE)) {
                return;
            }
            Emulator.getThreading().run(this, 1000L);
            if (this.state.equals(GameState.PAUSED)) {
                return;
            }
            for (GameTeam gameTeam : this.teams.values()) {
                TObjectHashIterator it = gameTeam.getMembers().iterator();
                while (it.hasNext()) {
                    ((FreezeGamePlayer) ((GamePlayer) it.next())).cycle();
                }
                int totalScore = gameTeam.getTotalScore();
                for (InteractionFreezeScoreboard interactionFreezeScoreboard : this.room.getRoomSpecialTypes().getFreezeScoreboards(gameTeam.teamColor).values()) {
                    if (interactionFreezeScoreboard.getExtradata().isEmpty()) {
                        interactionFreezeScoreboard.setExtradata("0");
                    }
                    if (Integer.valueOf(interactionFreezeScoreboard.getExtradata()).intValue() != totalScore) {
                        interactionFreezeScoreboard.setExtradata(totalScore + Emulator.PREVIEW);
                        this.room.updateItemState(interactionFreezeScoreboard);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
        }
    }

    @Override // com.eu.habbo.habbohotel.games.Game
    public void stop() {
        super.stop();
        GameTeam gameTeam = null;
        for (GameTeam gameTeam2 : this.teams.values()) {
            if (gameTeam == null || gameTeam2.getTotalScore() > gameTeam.getTotalScore()) {
                gameTeam = gameTeam2;
            }
        }
        for (GameTeam gameTeam3 : this.teams.values()) {
            THashSet tHashSet = new THashSet();
            tHashSet.addAll(gameTeam3.getMembers());
            TObjectHashIterator it = tHashSet.iterator();
            while (it.hasNext()) {
                GamePlayer gamePlayer = (GamePlayer) it.next();
                if (gamePlayer.getScoreAchievementValue() > 0) {
                    if (gameTeam3.equals(gameTeam)) {
                        AchievementManager.progressAchievement(gamePlayer.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("FreezeWinner"), gamePlayer.getScoreAchievementValue());
                        this.room.sendComposer(new RoomUserActionComposer(gamePlayer.getHabbo().getRoomUnit(), RoomUserAction.WAVE).compose());
                    }
                    AchievementManager.progressAchievement(gamePlayer.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("FreezePlayer"));
                }
            }
        }
        HashMap map = new HashMap();
        for (Map.Entry entry : this.teams.entrySet()) {
            map.put((GameTeamColors) entry.getKey(), Integer.valueOf(((GameTeam) entry.getValue()).getMembers().size()));
        }
        for (Map.Entry entry2 : this.room.getRoomSpecialTypes().getFreezeGates().entrySet()) {
            if (map.containsKey(((InteractionFreezeGate) entry2.getValue()).teamColor)) {
                int iMin = Math.min(((Integer) map.get(((InteractionFreezeGate) entry2.getValue()).teamColor)).intValue(), 5);
                ((InteractionFreezeGate) entry2.getValue()).setExtradata(iMin + Emulator.PREVIEW);
                map.put(((InteractionFreezeGate) entry2.getValue()).teamColor, Integer.valueOf(((Integer) map.get(((InteractionFreezeGate) entry2.getValue()).teamColor)).intValue() - iMin));
                this.room.updateItemState((HabboItem) entry2.getValue());
            }
        }
        refreshGates();
        setFreezeTileState("0");
    }

    public void setFreezeTileState(final String str) {
        this.room.getRoomSpecialTypes().getFreezeExitTiles().forEachValue(new TObjectProcedure<InteractionFreezeExitTile>() { // from class: com.eu.habbo.habbohotel.games.freeze.FreezeGame.1
            public boolean execute(InteractionFreezeExitTile interactionFreezeExitTile) {
                interactionFreezeExitTile.setExtradata(str);
                FreezeGame.this.room.updateItemState(interactionFreezeExitTile);
                return true;
            }
        });
    }

    private void refreshGates() {
        THashSet<RoomTile> tHashSet = new THashSet<>();
        for (HabboItem habboItem : this.room.getRoomSpecialTypes().getFreezeGates().values()) {
            tHashSet.add(this.room.getLayout().getTile(habboItem.getX(), habboItem.getY()));
        }
        this.room.updateTiles(tHashSet);
    }
}
