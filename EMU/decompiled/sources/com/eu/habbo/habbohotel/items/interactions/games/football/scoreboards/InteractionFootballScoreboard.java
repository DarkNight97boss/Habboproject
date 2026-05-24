package com.eu.habbo.habbohotel.items.interactions.games.football.scoreboards;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.games.GameTeamColors;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.games.InteractionGameScoreboard;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/games/football/scoreboards/InteractionFootballScoreboard.class */
public class InteractionFootballScoreboard extends InteractionGameScoreboard {
    private int score;

    public InteractionFootballScoreboard(ResultSet resultSet, Item item, GameTeamColors gameTeamColors) throws SQLException {
        super(resultSet, item, gameTeamColors);
        try {
            this.score = Integer.parseInt(getExtradata());
        } catch (Exception e) {
            this.score = 0;
        }
    }

    public InteractionFootballScoreboard(int i, int i2, Item item, String str, int i3, int i4, GameTeamColors gameTeamColors) {
        super(i, i2, item, str, i3, i4, gameTeamColors);
        try {
            this.score = Integer.parseInt(str);
        } catch (Exception e) {
            this.score = 0;
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        return false;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return false;
    }

    public int changeScore(int i) {
        this.score += i;
        if (this.score > 99) {
            this.score = 0;
        }
        if (this.score < 0) {
            this.score = 99;
        }
        setExtradata(this.score + Emulator.PREVIEW);
        needsUpdate(true);
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId());
        if (room != null) {
            room.updateItem(this);
        }
        return this.score;
    }

    public int getScore() {
        return this.score;
    }

    public void setScore(int i) {
        this.score = i;
        if (this.score > 99) {
            this.score = 0;
        }
        if (this.score < 0) {
            this.score = 99;
        }
        setExtradata(this.score + Emulator.PREVIEW);
        needsUpdate(true);
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId());
        if (room != null) {
            room.updateItem(this);
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        super.onClick(gameClient, room, objArr);
        if (objArr.length < 1 || !(objArr[0] instanceof Integer) || gameClient == null || (objArr.length >= 2 && (objArr[1] instanceof WiredEffectType))) {
            changeScore(1);
        }
        switch (((Integer) objArr[0]).intValue()) {
            case 1:
                changeScore(1);
                break;
            case 2:
                changeScore(-1);
                break;
            default:
                setScore(0);
                break;
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOff(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }
}
