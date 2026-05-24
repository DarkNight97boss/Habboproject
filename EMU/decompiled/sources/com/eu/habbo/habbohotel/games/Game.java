package com.eu.habbo.habbohotel.games;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredHighscore;
import com.eu.habbo.habbohotel.items.interactions.games.InteractionGameTimer;
import com.eu.habbo.habbohotel.items.interactions.wired.extra.WiredBlob;
import com.eu.habbo.habbohotel.items.interactions.wired.triggers.WiredTriggerTeamLoses;
import com.eu.habbo.habbohotel.items.interactions.wired.triggers.WiredTriggerTeamWins;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.highscores.WiredHighscoreDataEntry;
import com.eu.habbo.messages.outgoing.guides.GuideSessionPartnerIsPlayingComposer;
import com.eu.habbo.plugin.events.games.GameHabboJoinEvent;
import com.eu.habbo.plugin.events.games.GameHabboLeaveEvent;
import com.eu.habbo.plugin.events.games.GameStartedEvent;
import com.eu.habbo.plugin.events.games.GameStoppedEvent;
import com.eu.habbo.threading.runnables.SaveScoreForTeam;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.hash.THashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/games/Game.class */
public abstract class Game implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Game.class);
    protected final Room room;
    private final Class<? extends GameTeam> gameTeamClazz;
    private final Class<? extends GamePlayer> gamePlayerClazz;
    private final boolean countsAchievements;
    public boolean isRunning;
    private int startTime;
    private int endTime;
    protected final THashMap<GameTeamColors, GameTeam> teams = new THashMap<>();
    public GameState state = GameState.IDLE;

    public Game(Class<? extends GameTeam> cls, Class<? extends GamePlayer> cls2, Room room, boolean z) {
        this.gameTeamClazz = cls;
        this.gamePlayerClazz = cls2;
        this.room = room;
        this.countsAchievements = z;
    }

    public abstract void initialise();

    /* JADX WARN: Multi-variable type inference failed */
    public boolean addHabbo(Habbo habbo, GameTeamColors gameTeamColors) {
        if (habbo == null) {
            return false;
        }
        try {
            if (Emulator.getPluginManager().isRegistered(GameHabboJoinEvent.class, true)) {
                GameHabboJoinEvent gameHabboJoinEvent = new GameHabboJoinEvent(this, habbo);
                Emulator.getPluginManager().fireEvent(gameHabboJoinEvent);
                if (gameHabboJoinEvent.isCancelled()) {
                    return false;
                }
            }
            synchronized (this.teams) {
                GameTeam team = getTeam(gameTeamColors);
                if (team == null) {
                    team = this.gameTeamClazz.getDeclaredConstructor(GameTeamColors.class).newInstance(gameTeamColors);
                    addTeam(team);
                }
                GamePlayer gamePlayerNewInstance = this.gamePlayerClazz.getDeclaredConstructor(Habbo.class, GameTeamColors.class).newInstance(habbo, gameTeamColors);
                team.addMember(gamePlayerNewInstance);
                habbo.getHabboInfo().setCurrentGame(getClass());
                habbo.getHabboInfo().setGamePlayer(gamePlayerNewInstance);
            }
            habbo.getClient().sendResponse(new GuideSessionPartnerIsPlayingComposer(true));
            return true;
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
            return false;
        }
    }

    public void removeHabbo(Habbo habbo) {
        if (habbo != null) {
            if (Emulator.getPluginManager().isRegistered(GameHabboLeaveEvent.class, true)) {
                GameHabboLeaveEvent gameHabboLeaveEvent = new GameHabboLeaveEvent(this, habbo);
                Emulator.getPluginManager().fireEvent(gameHabboLeaveEvent);
                if (gameHabboLeaveEvent.isCancelled()) {
                    return;
                }
            }
            GameTeam teamForHabbo = getTeamForHabbo(habbo);
            if (teamForHabbo == null || !teamForHabbo.isMember(habbo)) {
                return;
            }
            if (habbo.getHabboInfo().getGamePlayer() != null) {
                teamForHabbo.removeMember(habbo.getHabboInfo().getGamePlayer());
                if (habbo.getHabboInfo().getGamePlayer() != null) {
                    habbo.getHabboInfo().getGamePlayer().reset();
                }
            }
            habbo.getHabboInfo().setCurrentGame(null);
            habbo.getHabboInfo().setGamePlayer(null);
            habbo.getClient().sendResponse(new GuideSessionPartnerIsPlayingComposer(false));
            if (!this.countsAchievements || this.endTime <= this.startTime) {
                return;
            }
            AchievementManager.progressAchievement(habbo, Emulator.getGameEnvironment().getAchievementManager().getAchievement("GamePlayed"));
        }
    }

    public void start() {
        this.isRunning = false;
        this.state = GameState.RUNNING;
        this.startTime = Emulator.getIntUnixTimestamp();
        if (Emulator.getPluginManager().isRegistered(GameStartedEvent.class, true)) {
            Emulator.getPluginManager().fireEvent(new GameStartedEvent(this));
        }
        TObjectHashIterator it = this.room.getRoomSpecialTypes().getItemsOfType(WiredBlob.class).iterator();
        while (it.hasNext()) {
            ((WiredBlob) ((HabboItem) it.next())).onGameStart(this.room);
        }
        Iterator it2 = this.teams.values().iterator();
        while (it2.hasNext()) {
            ((GameTeam) it2.next()).resetScores();
        }
    }

    public void onEnd() {
        this.endTime = Emulator.getIntUnixTimestamp();
        saveScores();
        int iSum = this.teams.values().stream().mapToInt((v0) -> {
            return v0.getTotalScore();
        }).sum();
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(this.room.getOwnerId());
        if (habbo != null) {
            AchievementManager.progressAchievement(habbo, Emulator.getGameEnvironment().getAchievementManager().getAchievement("GameAuthorExperience"), iSum);
        }
        GameTeam gameTeam = null;
        if (iSum > 0) {
            for (GameTeam gameTeam2 : this.teams.values()) {
                if (gameTeam == null || gameTeam2.getTotalScore() > gameTeam.getTotalScore()) {
                    gameTeam = gameTeam2;
                }
            }
        }
        if (gameTeam != null) {
            TObjectHashIterator it = gameTeam.getMembers().iterator();
            while (it.hasNext()) {
                GamePlayer gamePlayer = (GamePlayer) it.next();
                WiredHandler.handleCustomTrigger(WiredTriggerTeamWins.class, gamePlayer.getHabbo().getRoomUnit(), this.room, new Object[]{this});
                if (gamePlayer.getHabbo() != null) {
                    AchievementManager.progressAchievement(habbo, Emulator.getGameEnvironment().getAchievementManager().getAchievement("GamePlayerExperience"));
                }
            }
            if (gameTeam.getMembers().size() > 0) {
                TObjectHashIterator it2 = this.room.getRoomSpecialTypes().getItemsOfType(InteractionWiredHighscore.class).iterator();
                while (it2.hasNext()) {
                    Emulator.getGameEnvironment().getItemManager().getHighscoreManager().addHighscoreData(new WiredHighscoreDataEntry(((HabboItem) it2.next()).getId(), (List) gameTeam.getMembers().stream().map(gamePlayer2 -> {
                        return Integer.valueOf(gamePlayer2.getHabbo().getHabboInfo().getId());
                    }).collect(Collectors.toList()), gameTeam.getTotalScore(), true, Emulator.getIntUnixTimestamp()));
                }
            }
            for (GameTeam gameTeam3 : this.teams.values()) {
                if (gameTeam3 != gameTeam) {
                    TObjectHashIterator it3 = gameTeam3.getMembers().iterator();
                    while (it3.hasNext()) {
                        WiredHandler.handleCustomTrigger(WiredTriggerTeamLoses.class, ((GamePlayer) it3.next()).getHabbo().getRoomUnit(), this.room, new Object[]{this});
                    }
                    if (gameTeam3.getMembers().size() > 0 && gameTeam3.getTotalScore() > 0) {
                        TObjectHashIterator it4 = this.room.getRoomSpecialTypes().getItemsOfType(InteractionWiredHighscore.class).iterator();
                        while (it4.hasNext()) {
                            Emulator.getGameEnvironment().getItemManager().getHighscoreManager().addHighscoreData(new WiredHighscoreDataEntry(((HabboItem) it4.next()).getId(), (List) gameTeam3.getMembers().stream().map(gamePlayer3 -> {
                                return Integer.valueOf(gamePlayer3.getHabbo().getHabboInfo().getId());
                            }).collect(Collectors.toList()), gameTeam3.getTotalScore(), false, Emulator.getIntUnixTimestamp()));
                        }
                    }
                }
            }
        }
        TObjectHashIterator it5 = this.room.getRoomSpecialTypes().getItemsOfType(InteractionWiredHighscore.class).iterator();
        while (it5.hasNext()) {
            HabboItem habboItem = (HabboItem) it5.next();
            ((InteractionWiredHighscore) habboItem).reloadData();
            this.room.updateItem(habboItem);
        }
        TObjectHashIterator it6 = this.room.getRoomSpecialTypes().getItemsOfType(WiredBlob.class).iterator();
        while (it6.hasNext()) {
            ((WiredBlob) ((HabboItem) it6.next())).onGameEnd(this.room);
        }
    }

    @Override // java.lang.Runnable
    public abstract void run();

    public void pause() {
        if (this.state.equals(GameState.RUNNING)) {
            this.state = GameState.PAUSED;
        }
    }

    public void unpause() {
        if (this.state.equals(GameState.PAUSED)) {
            this.state = GameState.RUNNING;
        }
    }

    public void stop() {
        this.state = GameState.IDLE;
        boolean z = false;
        TObjectHashIterator it = this.room.getFloorItems().iterator();
        while (it.hasNext()) {
            HabboItem habboItem = (HabboItem) it.next();
            if ((habboItem instanceof InteractionGameTimer) && ((InteractionGameTimer) habboItem).isRunning()) {
                z = true;
            }
        }
        if (!z && Emulator.getPluginManager().isRegistered(GameStoppedEvent.class, true)) {
            Emulator.getPluginManager().fireEvent(new GameStoppedEvent(this));
        }
    }

    public void dispose() {
        Iterator it = this.teams.values().iterator();
        while (it.hasNext()) {
            ((GameTeam) it.next()).clearMembers();
        }
        this.teams.clear();
        stop();
    }

    private void saveScores() {
        if (this.room == null) {
            return;
        }
        THashMap tHashMap = new THashMap();
        tHashMap.putAll(this.teams);
        Iterator it = tHashMap.entrySet().iterator();
        while (it.hasNext()) {
            Emulator.getThreading().run(new SaveScoreForTeam((GameTeam) ((Map.Entry) it.next()).getValue(), this));
        }
    }

    public GameTeam getTeamForHabbo(Habbo habbo) {
        if (habbo == null) {
            return null;
        }
        synchronized (this.teams) {
            for (GameTeam gameTeam : this.teams.values()) {
                if (gameTeam.isMember(habbo)) {
                    return gameTeam;
                }
            }
            return null;
        }
    }

    public GameTeam getTeam(GameTeamColors gameTeamColors) {
        GameTeam gameTeam;
        synchronized (this.teams) {
            gameTeam = (GameTeam) this.teams.get(gameTeamColors);
        }
        return gameTeam;
    }

    public void addTeam(GameTeam gameTeam) {
        synchronized (this.teams) {
            this.teams.put(gameTeam.teamColor, gameTeam);
        }
    }

    public Room getRoom() {
        return this.room;
    }

    public int getStartTime() {
        return this.startTime;
    }

    public Class<? extends GameTeam> getGameTeamClass() {
        return this.gameTeamClazz;
    }

    public Class<? extends GamePlayer> getGamePlayerClass() {
        return this.gamePlayerClazz;
    }

    public THashMap<GameTeamColors, GameTeam> getTeams() {
        return this.teams;
    }

    public boolean isCountsAchievements() {
        return this.countsAchievements;
    }

    public GameState getState() {
        return this.state;
    }
}
