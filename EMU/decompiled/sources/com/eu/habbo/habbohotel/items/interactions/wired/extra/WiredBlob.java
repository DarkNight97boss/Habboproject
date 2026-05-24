package com.eu.habbo.habbohotel.items.interactions.wired.extra;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.games.GamePlayer;
import com.eu.habbo.habbohotel.games.GameState;
import com.eu.habbo.habbohotel.games.battlebanzai.BattleBanzaiGame;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionDefault;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/extra/WiredBlob.class */
public class WiredBlob extends InteractionDefault {
    private static final Logger LOGGER = LoggerFactory.getLogger(WiredBlob.class);
    private int POINTS_REWARD;
    private boolean RESETS_WITH_GAME;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/extra/WiredBlob$WiredBlobState.class */
    enum WiredBlobState {
        ACTIVE("0"),
        USED("1");

        private String state;

        WiredBlobState(String str) {
            this.state = str;
        }

        public String getState() {
            return this.state;
        }
    }

    public WiredBlob(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.POINTS_REWARD = 0;
        this.RESETS_WITH_GAME = true;
        parseCustomParams();
    }

    public WiredBlob(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.POINTS_REWARD = 0;
        this.RESETS_WITH_GAME = true;
        parseCustomParams();
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPlace(Room room) {
        super.onPlace(room);
        setExtradata(WiredBlobState.USED.getState());
        room.updateItem(this);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        Habbo habbo;
        GamePlayer gamePlayer;
        super.onWalkOn(roomUnit, room, objArr);
        if (!getExtradata().equals(WiredBlobState.ACTIVE.getState()) || (habbo = room.getHabbo(roomUnit)) == null || (gamePlayer = habbo.getHabboInfo().getGamePlayer()) == null) {
            return;
        }
        gamePlayer.addScore(this.POINTS_REWARD, true);
        BattleBanzaiGame battleBanzaiGame = (BattleBanzaiGame) room.getGame(BattleBanzaiGame.class);
        if (battleBanzaiGame != null && battleBanzaiGame.getState() != GameState.IDLE) {
            battleBanzaiGame.refreshCounters(habbo.getHabboInfo().getGamePlayer().getTeamColor());
        }
        setExtradata(WiredBlobState.USED.getState());
        room.updateItem(this);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        if (!this.RESETS_WITH_GAME && objArr != null && objArr.length == 2 && objArr[1].equals(WiredEffectType.TOGGLE_STATE) && room.getGames().stream().anyMatch(game -> {
            return game.getState().equals(GameState.RUNNING) || game.getState().equals(GameState.PAUSED);
        })) {
            setExtradata(getExtradata().equals(WiredBlobState.ACTIVE.getState()) ? WiredBlobState.USED.getState() : WiredBlobState.ACTIVE.getState());
            room.updateItem(this);
        }
    }

    public void onGameStart(Room room) {
        if (this.RESETS_WITH_GAME) {
            setExtradata(WiredBlobState.ACTIVE.getState());
            room.updateItem(this);
        }
    }

    public void onGameEnd(Room room) {
        setExtradata(WiredBlobState.USED.getState());
        room.updateItem(this);
    }

    private void parseCustomParams() {
        String[] strArrSplit = getBaseItem().getCustomParams().split(",");
        if (strArrSplit.length != 2) {
            LOGGER.error("Wired blobs should have customparams with two parameters (points,resetsWithGame)");
            return;
        }
        try {
            this.POINTS_REWARD = Integer.parseInt(strArrSplit[0]);
            this.RESETS_WITH_GAME = strArrSplit[1].equalsIgnoreCase("true");
        } catch (NumberFormatException e) {
            LOGGER.error("Wired blobs should have customparams with the first parameter being the amount of points (number)");
        }
    }
}
