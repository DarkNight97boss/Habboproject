package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.messages.ServerMessage;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionVoteCounter.class */
public class InteractionVoteCounter extends HabboItem {
    private boolean frozen;
    private int votes;
    private List<Integer> votedUsers;

    public InteractionVoteCounter(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        if (!getExtradata().contains(",")) {
            setExtradata("1,0");
        }
        String[] strArrSplit = getExtradata().split(",");
        this.frozen = strArrSplit[0].equals("1");
        this.votes = Integer.parseInt(strArrSplit[1]);
        this.votedUsers = new ArrayList();
    }

    public InteractionVoteCounter(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        String[] strArrSplit = (str.contains(",") ? str : "1,0").split(",");
        this.frozen = strArrSplit[0].equals("1");
        this.votes = Integer.parseInt(strArrSplit[1]);
        this.votedUsers = new ArrayList();
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void serializeExtradata(ServerMessage serverMessage) {
        serverMessage.appendInt(Integer.valueOf((isLimited() ? 256 : 0) + 3));
        serverMessage.appendString(this.frozen ? "0" : "1");
        serverMessage.appendInt(Integer.valueOf(this.votes));
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

    private void updateExtradata() {
        setExtradata((this.frozen ? "1" : "0") + "," + this.votes);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        if ((gameClient == null || room == null || !room.hasRights(gameClient.getHabbo())) && (objArr.length < 2 || !(objArr[1] instanceof WiredEffectType))) {
            return;
        }
        this.frozen = !this.frozen;
        if (!this.frozen) {
            this.votes = 0;
            this.votedUsers.clear();
        }
        updateExtradata();
        needsUpdate(true);
        room.updateItem(this);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }

    public void vote(Room room, int i, int i2) {
        if (this.frozen || this.votedUsers.contains(Integer.valueOf(i))) {
            return;
        }
        this.votedUsers.add(Integer.valueOf(i));
        this.votes += i2;
        updateExtradata();
        needsUpdate(true);
        room.updateItem(this);
    }
}
