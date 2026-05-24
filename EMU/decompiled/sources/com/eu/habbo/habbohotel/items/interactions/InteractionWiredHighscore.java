package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.habbohotel.wired.highscores.WiredHighscoreClearType;
import com.eu.habbo.habbohotel.wired.highscores.WiredHighscoreRow;
import com.eu.habbo.habbohotel.wired.highscores.WiredHighscoreScoreType;
import com.eu.habbo.messages.ServerMessage;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionWiredHighscore.class */
public class InteractionWiredHighscore extends HabboItem {
    private static final Logger LOGGER = LoggerFactory.getLogger(InteractionWiredHighscore.class);
    public WiredHighscoreScoreType scoreType;
    public WiredHighscoreClearType clearType;
    private List<WiredHighscoreRow> data;

    public InteractionWiredHighscore(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.scoreType = WiredHighscoreScoreType.CLASSIC;
        this.clearType = WiredHighscoreClearType.ALLTIME;
        try {
            String str = getBaseItem().getName().split("_")[1].toUpperCase().split("\\*")[0];
            int iIntValue = Integer.valueOf(getBaseItem().getName().split("\\*")[1]).intValue() - 1;
            this.scoreType = WiredHighscoreScoreType.valueOf(str);
            this.clearType = WiredHighscoreClearType.values()[iIntValue];
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
        }
        reloadData();
    }

    public InteractionWiredHighscore(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.scoreType = WiredHighscoreScoreType.CLASSIC;
        this.clearType = WiredHighscoreClearType.ALLTIME;
        try {
            String str2 = getBaseItem().getName().split("_")[1].toUpperCase().split("\\*")[0];
            int iIntValue = Integer.valueOf(getBaseItem().getName().split("\\*")[1]).intValue() - 1;
            this.scoreType = WiredHighscoreScoreType.valueOf(str2);
            this.clearType = WiredHighscoreClearType.values()[iIntValue];
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
        }
        reloadData();
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        if ((gameClient == null || room == null || !room.hasRights(gameClient.getHabbo())) && (objArr.length < 2 || !(objArr[1] instanceof WiredEffectType))) {
            return;
        }
        if (getExtradata() == null || getExtradata().isEmpty() || getExtradata().length() == 0) {
            setExtradata("0");
        }
        try {
            setExtradata(Math.abs(Integer.valueOf(getExtradata()).intValue() - 1) + Emulator.PREVIEW);
            room.updateItem(this);
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
        }
        if (gameClient != null) {
            if (objArr.length < 2 || !(objArr[1] instanceof WiredEffectType)) {
                WiredHandler.handle(WiredTriggerType.STATE_CHANGED, gameClient.getHabbo().getRoomUnit(), room, new Object[]{this});
            }
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void serializeExtradata(ServerMessage serverMessage) {
        serverMessage.appendInt((Integer) 6);
        serverMessage.appendString(getExtradata());
        serverMessage.appendInt(Integer.valueOf(this.scoreType.type));
        serverMessage.appendInt(Integer.valueOf(this.clearType.type));
        if (this.data != null) {
            int size = this.data.size();
            if (size > 50) {
                size = 50;
            }
            serverMessage.appendInt(Integer.valueOf(size));
            int i = 0;
            for (WiredHighscoreRow wiredHighscoreRow : this.data) {
                if (i < 50) {
                    serverMessage.appendInt(Integer.valueOf(wiredHighscoreRow.getValue()));
                    serverMessage.appendInt(Integer.valueOf(wiredHighscoreRow.getUsers().size()));
                    Iterator<String> it = wiredHighscoreRow.getUsers().iterator();
                    while (it.hasNext()) {
                        serverMessage.appendString(it.next());
                    }
                }
                i++;
            }
        } else {
            serverMessage.appendInt((Integer) 0);
        }
        super.serializeExtradata(serverMessage);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPlace(Room room) {
        reloadData();
        super.onPlace(room);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPickUp(Room room) {
        if (this.data != null) {
            this.data.clear();
        }
    }

    public void reloadData() {
        this.data = Emulator.getGameEnvironment().getItemManager().getHighscoreManager().getHighscoreRowsForItem(getId(), this.clearType, this.scoreType);
    }
}
