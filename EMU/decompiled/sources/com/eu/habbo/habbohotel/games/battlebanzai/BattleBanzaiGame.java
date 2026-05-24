package com.eu.habbo.habbohotel.games.battlebanzai;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.games.Game;
import com.eu.habbo.habbohotel.games.GamePlayer;
import com.eu.habbo.habbohotel.games.GameState;
import com.eu.habbo.habbohotel.games.GameTeam;
import com.eu.habbo.habbohotel.games.GameTeamColors;
import com.eu.habbo.habbohotel.items.interactions.games.InteractionGameTimer;
import com.eu.habbo.habbohotel.items.interactions.games.battlebanzai.InteractionBattleBanzaiSphere;
import com.eu.habbo.habbohotel.items.interactions.games.battlebanzai.InteractionBattleBanzaiTile;
import com.eu.habbo.habbohotel.items.interactions.games.battlebanzai.scoreboards.InteractionBattleBanzaiScoreboard;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUserAction;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserActionComposer;
import com.eu.habbo.threading.runnables.BattleBanzaiTilesFlicker;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/games/battlebanzai/BattleBanzaiGame.class */
public class BattleBanzaiGame extends Game {
    public static final int effectId = 32;
    private final THashMap<GameTeamColors, THashSet<HabboItem>> lockedTiles;
    private final THashMap<Integer, HabboItem> gameTiles;
    private int tileCount;
    private int countDown;
    private int countDown2;
    private static final Logger LOGGER = LoggerFactory.getLogger(BattleBanzaiGame.class);
    public static final int POINTS_HIJACK_TILE = Emulator.getConfig().getInt("hotel.banzai.points.tile.steal", 0);
    public static final int POINTS_FILL_TILE = Emulator.getConfig().getInt("hotel.banzai.points.tile.fill", 0);
    public static final int POINTS_LOCK_TILE = Emulator.getConfig().getInt("hotel.banzai.points.tile.lock", 1);
    private static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Emulator.getConfig().getInt("hotel.banzai.fill.threads", 2));

    public BattleBanzaiGame(Room room) {
        super(BattleBanzaiGameTeam.class, BattleBanzaiGamePlayer.class, room, true);
        this.lockedTiles = new THashMap<>();
        this.gameTiles = new THashMap<>();
        room.setAllowEffects(true);
    }

    @Override // com.eu.habbo.habbohotel.games.Game
    public void initialise() {
        if (this.state.equals(GameState.IDLE)) {
            this.countDown = 3;
            this.countDown2 = 2;
            resetMap();
            synchronized (this.teams) {
                Iterator it = this.teams.values().iterator();
                while (it.hasNext()) {
                    ((GameTeam) it.next()).initialise();
                }
            }
            TObjectHashIterator it2 = this.room.getRoomSpecialTypes().getItemsOfType(InteractionBattleBanzaiSphere.class).iterator();
            while (it2.hasNext()) {
                HabboItem habboItem = (HabboItem) it2.next();
                habboItem.setExtradata("1");
                this.room.updateItemState(habboItem);
            }
            start();
        }
    }

    @Override // com.eu.habbo.habbohotel.games.Game
    public boolean addHabbo(Habbo habbo, GameTeamColors gameTeamColors) {
        return super.addHabbo(habbo, gameTeamColors);
    }

    @Override // com.eu.habbo.habbohotel.games.Game
    public void start() {
        if (this.state.equals(GameState.IDLE)) {
            super.start();
            refreshGates();
            Emulator.getThreading().run(this, 0L);
        }
    }

    @Override // com.eu.habbo.habbohotel.games.Game, java.lang.Runnable
    public void run() {
        try {
            if (this.state.equals(GameState.IDLE)) {
                return;
            }
            if (this.countDown > 0) {
                this.countDown--;
                if (this.countDown == 0) {
                    TObjectHashIterator it = this.room.getRoomSpecialTypes().getItemsOfType(InteractionBattleBanzaiSphere.class).iterator();
                    while (it.hasNext()) {
                        HabboItem habboItem = (HabboItem) it.next();
                        habboItem.setExtradata("1");
                        this.room.updateItemState(habboItem);
                        if (this.countDown2 > 0) {
                            this.countDown2--;
                            if (this.countDown2 == 0) {
                                habboItem.setExtradata("2");
                                this.room.updateItemState(habboItem);
                            }
                        }
                    }
                }
                if (this.countDown > 1) {
                    Emulator.getThreading().run(this, 500L);
                    return;
                }
            }
            Emulator.getThreading().run(this, 1000L);
            if (this.state.equals(GameState.PAUSED)) {
                return;
            }
            int size = 0;
            synchronized (this.lockedTiles) {
                Iterator it2 = this.lockedTiles.entrySet().iterator();
                while (it2.hasNext()) {
                    size += ((THashSet) ((Map.Entry) it2.next()).getValue()).size();
                }
            }
            GameTeam gameTeam = null;
            synchronized (this.teams) {
                for (Map.Entry entry : this.teams.entrySet()) {
                    if (gameTeam == null || gameTeam.getTotalScore() < ((GameTeam) entry.getValue()).getTotalScore()) {
                        gameTeam = (GameTeam) entry.getValue();
                    }
                }
            }
            if (gameTeam != null) {
                TObjectHashIterator it3 = this.room.getRoomSpecialTypes().getItemsOfType(InteractionBattleBanzaiSphere.class).iterator();
                while (it3.hasNext()) {
                    HabboItem habboItem2 = (HabboItem) it3.next();
                    habboItem2.setExtradata((gameTeam.teamColor.type + 2) + Emulator.PREVIEW);
                    this.room.updateItemState(habboItem2);
                }
            }
            if (size >= this.tileCount && this.tileCount != 0) {
                for (InteractionGameTimer interactionGameTimer : this.room.getRoomSpecialTypes().getGameTimers().values()) {
                    if (interactionGameTimer.isRunning()) {
                        interactionGameTimer.endGame(this.room);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
        }
    }

    @Override // com.eu.habbo.habbohotel.games.Game
    public void onEnd() {
        GameTeam gameTeam = null;
        boolean z = this.teams.values().stream().filter(gameTeam2 -> {
            return gameTeam2.getMembers().size() > 0;
        }).count() == 1;
        for (GameTeam gameTeam3 : this.teams.values()) {
            if (!z) {
                TObjectHashIterator it = gameTeam3.getMembers().iterator();
                while (it.hasNext()) {
                    GamePlayer gamePlayer = (GamePlayer) it.next();
                    if (gamePlayer.getScoreAchievementValue() > 0) {
                        AchievementManager.progressAchievement(gamePlayer.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("BattleBallPlayer"));
                    }
                }
            }
            if (gameTeam == null || gameTeam3.getTotalScore() > gameTeam.getTotalScore()) {
                gameTeam = gameTeam3;
            }
        }
        if (gameTeam != null) {
            if (!z) {
                TObjectHashIterator it2 = gameTeam.getMembers().iterator();
                while (it2.hasNext()) {
                    GamePlayer gamePlayer2 = (GamePlayer) it2.next();
                    if (gamePlayer2.getScoreAchievementValue() > 0) {
                        this.room.sendComposer(new RoomUserActionComposer(gamePlayer2.getHabbo().getRoomUnit(), RoomUserAction.WAVE).compose());
                        AchievementManager.progressAchievement(gamePlayer2.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("BattleBallWinner"));
                    }
                }
            }
            TObjectHashIterator it3 = this.room.getRoomSpecialTypes().getItemsOfType(InteractionBattleBanzaiSphere.class).iterator();
            while (it3.hasNext()) {
                HabboItem habboItem = (HabboItem) it3.next();
                habboItem.setExtradata((6 + gameTeam.teamColor.type) + Emulator.PREVIEW);
                this.room.updateItemState(habboItem);
            }
            synchronized (this.lockedTiles) {
                Emulator.getThreading().run(new BattleBanzaiTilesFlicker((THashSet) this.lockedTiles.get(gameTeam.teamColor), gameTeam.teamColor, this.room));
            }
        }
        super.onEnd();
    }

    @Override // com.eu.habbo.habbohotel.games.Game
    public void stop() {
        super.stop();
        refreshGates();
        for (HabboItem habboItem : this.gameTiles.values()) {
            if (habboItem.getExtradata().equals("1")) {
                habboItem.setExtradata("0");
                this.room.updateItem(habboItem);
            }
        }
        synchronized (this.lockedTiles) {
            this.lockedTiles.clear();
        }
    }

    private synchronized void resetMap() {
        this.tileCount = 0;
        TObjectHashIterator it = this.room.getFloorItems().iterator();
        while (it.hasNext()) {
            HabboItem habboItem = (HabboItem) it.next();
            if (habboItem instanceof InteractionBattleBanzaiTile) {
                habboItem.setExtradata("1");
                this.room.updateItemState(habboItem);
                this.tileCount++;
                this.gameTiles.put(Integer.valueOf(habboItem.getId()), habboItem);
            }
            if (habboItem instanceof InteractionBattleBanzaiScoreboard) {
                habboItem.setExtradata("0");
                this.room.updateItemState(habboItem);
            }
        }
    }

    public void tileLocked(GameTeamColors gameTeamColors, HabboItem habboItem, Habbo habbo) {
        tileLocked(gameTeamColors, habboItem, habbo, false);
    }

    public void tileLocked(GameTeamColors gameTeamColors, HabboItem habboItem, Habbo habbo, boolean z) {
        synchronized (this.lockedTiles) {
            if (habboItem instanceof InteractionBattleBanzaiTile) {
                if (!this.lockedTiles.containsKey(gameTeamColors)) {
                    this.lockedTiles.put(gameTeamColors, new THashSet());
                }
                ((THashSet) this.lockedTiles.get(gameTeamColors)).add(habboItem);
            }
            if (habbo != null) {
                AchievementManager.progressAchievement(habbo, Emulator.getGameEnvironment().getAchievementManager().getAchievement("BattleBallTilesLocked"));
            }
            if (z) {
                return;
            }
            short x = habboItem.getX();
            short y = habboItem.getY();
            ArrayList arrayList = new ArrayList();
            THashSet tHashSet = new THashSet((Collection) this.lockedTiles.get(gameTeamColors));
            executor.execute(() -> {
                arrayList.add(floodFill(x, y - 1, tHashSet, new ArrayList(), gameTeamColors));
                arrayList.add(floodFill(x, y + 1, tHashSet, new ArrayList(), gameTeamColors));
                arrayList.add(floodFill(x - 1, y, tHashSet, new ArrayList(), gameTeamColors));
                arrayList.add(floodFill(x + 1, y, tHashSet, new ArrayList(), gameTeamColors));
                Optional optionalMax = arrayList.stream().filter((v0) -> {
                    return Objects.nonNull(v0);
                }).max(Comparator.comparing((v0) -> {
                    return v0.size();
                }));
                if (optionalMax.isPresent()) {
                    for (RoomTile roomTile : (List) optionalMax.get()) {
                        this.gameTiles.values().stream().filter(habboItem2 -> {
                            return habboItem2.getX() == roomTile.x && habboItem2.getY() == roomTile.y && (habboItem2 instanceof InteractionBattleBanzaiTile);
                        }).findAny().ifPresent(habboItem3 -> {
                            tileLocked(gameTeamColors, habboItem3, habbo, true);
                            habboItem3.setExtradata((2 + (gameTeamColors.type * 3)) + Emulator.PREVIEW);
                            this.room.updateItem(habboItem3);
                        });
                    }
                    refreshCounters(gameTeamColors);
                    if (habbo != null) {
                        habbo.getHabboInfo().getGamePlayer().addScore(POINTS_LOCK_TILE * ((List) optionalMax.get()).size());
                    }
                }
            });
        }
    }

    private List<RoomTile> floodFill(int i, int i2, THashSet<HabboItem> tHashSet, List<RoomTile> list, GameTeamColors gameTeamColors) {
        if (isOutOfBounds(i, i2) || isForeignLockedTile(i, i2, gameTeamColors)) {
            return null;
        }
        RoomTile tile = this.room.getLayout().getTile((short) i, (short) i2);
        if (hasLockedTileAtCoordinates(i, i2, tHashSet) || list.contains(tile)) {
            return list;
        }
        list.add(tile);
        ArrayList arrayList = new ArrayList();
        arrayList.add(floodFill(i, i2 - 1, tHashSet, list, gameTeamColors));
        arrayList.add(floodFill(i, i2 + 1, tHashSet, list, gameTeamColors));
        arrayList.add(floodFill(i - 1, i2, tHashSet, list, gameTeamColors));
        arrayList.add(floodFill(i + 1, i2, tHashSet, list, gameTeamColors));
        if (arrayList.contains(null)) {
            return null;
        }
        return (List) arrayList.stream().max(Comparator.comparing((v0) -> {
            return v0.size();
        })).orElse(null);
    }

    private boolean hasLockedTileAtCoordinates(int i, int i2, THashSet<HabboItem> tHashSet) {
        TObjectHashIterator it = tHashSet.iterator();
        while (it.hasNext()) {
            HabboItem habboItem = (HabboItem) it.next();
            if (habboItem.getX() == i && habboItem.getY() == i2) {
                return true;
            }
        }
        return false;
    }

    private boolean isOutOfBounds(int i, int i2) {
        for (HabboItem habboItem : this.gameTiles.values()) {
            if (habboItem.getX() == i && habboItem.getY() == i2) {
                return false;
            }
        }
        return true;
    }

    private boolean isForeignLockedTile(int i, int i2, GameTeamColors gameTeamColors) {
        for (Map.Entry entry : this.lockedTiles.entrySet()) {
            if (entry.getKey() != gameTeamColors) {
                TObjectHashIterator it = ((THashSet) entry.getValue()).iterator();
                while (it.hasNext()) {
                    HabboItem habboItem = (HabboItem) it.next();
                    if (habboItem.getX() == i && habboItem.getY() == i2) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void refreshCounters() {
        for (GameTeam gameTeam : this.teams.values()) {
            if (!gameTeam.getMembers().isEmpty()) {
                refreshCounters(gameTeam.teamColor);
            }
        }
    }

    public void refreshCounters(GameTeamColors gameTeamColors) {
        if (this.teams.containsKey(gameTeamColors)) {
            int totalScore = ((GameTeam) this.teams.get(gameTeamColors)).getTotalScore();
            for (InteractionBattleBanzaiScoreboard interactionBattleBanzaiScoreboard : this.room.getRoomSpecialTypes().getBattleBanzaiScoreboards(gameTeamColors).values()) {
                if (interactionBattleBanzaiScoreboard.getExtradata().isEmpty()) {
                    interactionBattleBanzaiScoreboard.setExtradata("0");
                }
                if (Integer.valueOf(interactionBattleBanzaiScoreboard.getExtradata()).intValue() != totalScore) {
                    interactionBattleBanzaiScoreboard.setExtradata(totalScore + Emulator.PREVIEW);
                    this.room.updateItemState(interactionBattleBanzaiScoreboard);
                }
            }
        }
    }

    private void refreshGates() {
        Collection<HabboItem> collectionValues = this.room.getRoomSpecialTypes().getBattleBanzaiGates().values();
        THashSet<RoomTile> tHashSet = new THashSet<>(collectionValues.size());
        for (HabboItem habboItem : collectionValues) {
            tHashSet.add(this.room.getLayout().getTile(habboItem.getX(), habboItem.getY()));
        }
        this.room.updateTiles(tHashSet);
    }

    public void markTile(Habbo habbo, InteractionBattleBanzaiTile interactionBattleBanzaiTile, int i) {
        int i2;
        if (this.gameTiles.contains(Integer.valueOf(interactionBattleBanzaiTile.getId()))) {
            int i3 = i - (habbo.getHabboInfo().getGamePlayer().getTeamColor().type * 3);
            if (i3 == 0 || i3 == 1) {
                i2 = i + 1;
                if (i2 % 3 == 2) {
                    habbo.getHabboInfo().getGamePlayer().addScore(POINTS_LOCK_TILE);
                    tileLocked(habbo.getHabboInfo().getGamePlayer().getTeamColor(), interactionBattleBanzaiTile, habbo);
                } else {
                    habbo.getHabboInfo().getGamePlayer().addScore(POINTS_FILL_TILE);
                }
            } else {
                i2 = habbo.getHabboInfo().getGamePlayer().getTeamColor().type * 3;
                habbo.getHabboInfo().getGamePlayer().addScore(POINTS_HIJACK_TILE);
            }
            refreshCounters(habbo.getHabboInfo().getGamePlayer().getTeamColor());
            interactionBattleBanzaiTile.setExtradata(i2 + Emulator.PREVIEW);
            this.room.updateItem(interactionBattleBanzaiTile);
        }
    }
}
