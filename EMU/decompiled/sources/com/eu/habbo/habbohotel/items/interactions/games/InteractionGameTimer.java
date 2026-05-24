package com.eu.habbo.habbohotel.items.interactions.games;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.games.Game;
import com.eu.habbo.habbohotel.games.GameState;
import com.eu.habbo.habbohotel.games.wired.WiredGame;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.threading.runnables.games.GameTimer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/games/InteractionGameTimer.class */
public class InteractionGameTimer extends HabboItem implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(InteractionGameTimer.class);
    private int[] TIMER_INTERVAL_STEPS;
    private int baseTime;
    private int timeNow;
    private boolean isRunning;
    private boolean isPaused;
    private boolean threadActive;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/games/InteractionGameTimer$InteractionGameTimerAction.class */
    public enum InteractionGameTimerAction {
        START_STOP(1),
        INCREASE_TIME(2);

        private int action;

        InteractionGameTimerAction(int i) {
            this.action = i;
        }

        public int getAction() {
            return this.action;
        }

        public static InteractionGameTimerAction getByAction(int i) {
            if (i != 1 && i == 2) {
                return INCREASE_TIME;
            }
            return START_STOP;
        }
    }

    public InteractionGameTimer(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.TIMER_INTERVAL_STEPS = new int[]{30, 60, 120, 180, 300, 600};
        this.baseTime = 0;
        this.timeNow = 0;
        this.isRunning = false;
        this.isPaused = false;
        this.threadActive = false;
        parseCustomParams(item);
        try {
            String[] strArrSplit = resultSet.getString("extra_data").split("\t");
            if (strArrSplit.length >= 2) {
                this.baseTime = Integer.parseInt(strArrSplit[1]);
                this.timeNow = this.baseTime;
            }
            if (strArrSplit.length >= 1) {
                setExtradata(strArrSplit[0] + "\t0");
            }
        } catch (Exception e) {
            this.baseTime = this.TIMER_INTERVAL_STEPS[0];
            this.timeNow = this.baseTime;
        }
    }

    public InteractionGameTimer(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.TIMER_INTERVAL_STEPS = new int[]{30, 60, 120, 180, 300, 600};
        this.baseTime = 0;
        this.timeNow = 0;
        this.isRunning = false;
        this.isPaused = false;
        this.threadActive = false;
        parseCustomParams(item);
    }

    private void parseCustomParams(Item item) {
        try {
            this.TIMER_INTERVAL_STEPS = Arrays.stream(item.getCustomParams().split(",")).mapToInt(str -> {
                try {
                    return Integer.parseInt(str);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }).toArray();
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
        }
    }

    public void endGame(Room room) {
        endGame(room, false);
    }

    public void endGame(Room room, boolean z) {
        this.isRunning = false;
        this.isPaused = false;
        for (Game game : room.getGames()) {
            if (!game.getState().equals(GameState.IDLE) && (!z || !(game instanceof WiredGame))) {
                game.onEnd();
                game.stop();
            }
        }
    }

    private void createNewGame(Room room) {
        for (Class<? extends Game> cls : Emulator.getGameEnvironment().getRoomManager().getGameTypes()) {
            Game game = room.getGame(cls);
            if (game != null) {
                game.initialise();
            } else {
                try {
                    Game gameNewInstance = cls.getDeclaredConstructor(Room.class).newInstance(room);
                    room.addGame(gameNewInstance);
                    gameNewInstance.initialise();
                } catch (Exception e) {
                    LOGGER.error("Caught exception", e);
                }
            }
        }
    }

    private void pause(Room room) {
        Iterator it = room.getGames().iterator();
        while (it.hasNext()) {
            ((Game) it.next()).pause();
        }
    }

    private void unpause(Room room) {
        Iterator it = room.getGames().iterator();
        while (it.hasNext()) {
            ((Game) it.next()).unpause();
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, java.lang.Runnable
    public void run() {
        if (needsUpdate() || needsDelete()) {
            super.run();
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPickUp(Room room) {
        endGame(room);
        setExtradata(this.baseTime + "\t" + this.baseTime);
        needsUpdate(true);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPlace(Room room) {
        if (this.baseTime < this.TIMER_INTERVAL_STEPS[0]) {
            this.baseTime = this.TIMER_INTERVAL_STEPS[0];
        }
        this.timeNow = this.baseTime;
        setExtradata(this.timeNow + "\t" + this.baseTime);
        room.updateItem(this);
        needsUpdate(true);
        super.onPlace(room);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void serializeExtradata(ServerMessage serverMessage) {
        serverMessage.appendInt(Integer.valueOf(isLimited() ? 256 : 0));
        serverMessage.appendString(Emulator.PREVIEW + this.timeNow);
        super.serializeExtradata(serverMessage);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        return false;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return false;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        if (getExtradata().isEmpty()) {
            setExtradata("0\t" + this.TIMER_INTERVAL_STEPS[0]);
        }
        if (objArr.length >= 2 && (objArr[1] instanceof WiredEffectType)) {
            if (this.isRunning && !this.isPaused) {
                return;
            }
            boolean z = this.isPaused;
            endGame(room, true);
            if (z) {
                WiredHandler.handle(WiredTriggerType.GAME_ENDS, (RoomUnit) null, room, new Object[0]);
            }
            createNewGame(room);
            this.timeNow = this.baseTime;
            this.isRunning = true;
            this.isPaused = false;
            room.updateItem(this);
            WiredHandler.handle(WiredTriggerType.GAME_STARTS, (RoomUnit) null, room, new Object[0]);
            if (!this.threadActive) {
                this.threadActive = true;
                Emulator.getThreading().run(new GameTimer(this), 1000L);
            }
        } else if (gameClient != null) {
            if (room.hasRights(gameClient.getHabbo()) || gameClient.getHabbo().hasPermission(Permission.ACC_ANYROOMOWNER)) {
                InteractionGameTimerAction byAction = InteractionGameTimerAction.START_STOP;
                if (objArr.length >= 1 && (objArr[0] instanceof Integer)) {
                    byAction = InteractionGameTimerAction.getByAction(((Integer) objArr[0]).intValue());
                }
                switch (byAction) {
                    case START_STOP:
                        if (!this.isRunning) {
                            this.isPaused = false;
                            this.isRunning = true;
                            this.timeNow = this.baseTime;
                            room.updateItem(this);
                            createNewGame(room);
                            WiredHandler.handle(WiredTriggerType.GAME_STARTS, (RoomUnit) null, room, new Object[]{this});
                            if (!this.threadActive) {
                                this.threadActive = true;
                                Emulator.getThreading().run(new GameTimer(this), 1000L);
                            }
                        } else {
                            this.isPaused = !this.isPaused;
                            if (!this.isPaused) {
                                unpause(room);
                                if (!this.threadActive) {
                                    this.threadActive = true;
                                    Emulator.getThreading().run(new GameTimer(this));
                                }
                            } else {
                                pause(room);
                            }
                        }
                        break;
                    case INCREASE_TIME:
                        if (!this.isRunning) {
                            increaseTimer(room);
                        } else if (this.isPaused) {
                            endGame(room);
                            increaseTimer(room);
                            WiredHandler.handle(WiredTriggerType.GAME_ENDS, (RoomUnit) null, room, new Object[0]);
                        }
                        break;
                }
            } else {
                return;
            }
        }
        super.onClick(gameClient, room, objArr);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }

    private void increaseTimer(Room room) {
        if (this.isRunning) {
            return;
        }
        int i = -1;
        if (this.timeNow != this.baseTime) {
            i = this.baseTime;
        } else {
            int[] iArr = this.TIMER_INTERVAL_STEPS;
            int length = iArr.length;
            int i2 = 0;
            while (true) {
                if (i2 >= length) {
                    break;
                }
                int i3 = iArr[i2];
                if (this.timeNow < i3) {
                    i = i3;
                    break;
                }
                i2++;
            }
            if (i == -1) {
                i = this.TIMER_INTERVAL_STEPS[0];
            }
        }
        this.baseTime = i;
        setExtradata(this.timeNow + "\t" + this.baseTime);
        this.timeNow = this.baseTime;
        room.updateItem(this);
        needsUpdate(true);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public String getDatabaseExtraData() {
        return getExtradata();
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean allowWiredResetState() {
        return true;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void setRunning(boolean z) {
        this.isRunning = z;
    }

    public void setThreadActive(boolean z) {
        this.threadActive = z;
    }

    public boolean isPaused() {
        return this.isPaused;
    }

    public void reduceTime() {
        this.timeNow--;
    }

    public int getTimeNow() {
        return this.timeNow;
    }

    public void setTimeNow(int i) {
        this.timeNow = i;
    }
}
