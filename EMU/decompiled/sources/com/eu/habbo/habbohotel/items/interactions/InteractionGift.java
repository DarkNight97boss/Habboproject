package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionGift.class */
public class InteractionGift extends HabboItem {
    private static final Logger LOGGER = LoggerFactory.getLogger(InteractionGift.class);
    public boolean explode;
    private int[] itemId;
    private int colorId;
    private int ribbonId;
    private boolean showSender;
    private String message;
    private String sender;
    private String look;

    public InteractionGift(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.explode = false;
        this.colorId = 0;
        this.ribbonId = 0;
        this.showSender = false;
        this.message = Emulator.PREVIEW;
        this.sender = Emulator.PREVIEW;
        this.look = Emulator.PREVIEW;
        try {
            loadData();
        } catch (Exception e) {
            LOGGER.warn("Incorrect extradata for gift with ID " + getId());
        }
    }

    public InteractionGift(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.explode = false;
        this.colorId = 0;
        this.ribbonId = 0;
        this.showSender = false;
        this.message = Emulator.PREVIEW;
        this.sender = Emulator.PREVIEW;
        this.look = Emulator.PREVIEW;
        try {
            loadData();
        } catch (Exception e) {
            LOGGER.warn("Incorrect extradata for gift with ID " + getId());
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void serializeExtradata(ServerMessage serverMessage) {
        serverMessage.appendInt((Integer) 1);
        serverMessage.appendInt((Integer) 6);
        serverMessage.appendString("EXTRA_PARAM");
        serverMessage.appendString(Emulator.PREVIEW);
        serverMessage.appendString("MESSAGE");
        serverMessage.appendString(this.message);
        serverMessage.appendString("PURCHASER_NAME");
        serverMessage.appendString(this.showSender ? this.sender : Emulator.PREVIEW);
        serverMessage.appendString("PURCHASER_FIGURE");
        serverMessage.appendString(this.showSender ? this.look : Emulator.PREVIEW);
        serverMessage.appendString("PRODUCT_CODE");
        serverMessage.appendString(Emulator.PREVIEW);
        serverMessage.appendString("state");
        serverMessage.appendString(this.explode ? "1" : "0");
        super.serializeExtradata(serverMessage);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        return false;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return false;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }

    private void loadData() throws NumberFormatException {
        String[] strArrSplit = getExtradata().contains("\t") ? getExtradata().split("\t") : null;
        if (strArrSplit == null || strArrSplit.length < 5) {
            this.itemId = new int[0];
            this.colorId = 0;
            this.ribbonId = 0;
            this.showSender = false;
            this.message = "Please delete this present. Thanks!";
            return;
        }
        int iIntValue = Integer.valueOf(strArrSplit[0]).intValue();
        this.itemId = new int[iIntValue];
        for (int i = 0; i < iIntValue; i++) {
            this.itemId[i] = Integer.valueOf(strArrSplit[i + 1]).intValue();
        }
        this.colorId = Integer.valueOf(strArrSplit[iIntValue + 1]).intValue();
        this.ribbonId = Integer.valueOf(strArrSplit[iIntValue + 2]).intValue();
        this.showSender = strArrSplit[iIntValue + 3].equalsIgnoreCase("1");
        this.message = strArrSplit[iIntValue + 4];
        if (strArrSplit.length - iIntValue < 7 || !this.showSender) {
            return;
        }
        this.sender = strArrSplit[iIntValue + 5];
        this.look = strArrSplit[iIntValue + 6];
    }

    public int getColorId() {
        return this.colorId;
    }

    public int getRibbonId() {
        return this.ribbonId;
    }

    public THashSet<HabboItem> loadItems() {
        THashSet<HabboItem> tHashSet = new THashSet<>();
        for (int i : this.itemId) {
            if (i != 0) {
                tHashSet.add(Emulator.getGameEnvironment().getItemManager().loadHabboItem(i));
            }
        }
        return tHashSet;
    }
}
