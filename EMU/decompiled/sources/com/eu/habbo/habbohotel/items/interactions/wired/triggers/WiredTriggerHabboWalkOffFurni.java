package com.eu.habbo.habbohotel.items.interactions.wired.triggers;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.items.interactions.wired.triggers.WiredTriggerFurniStateToggled;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/triggers/WiredTriggerHabboWalkOffFurni.class */
public class WiredTriggerHabboWalkOffFurni extends InteractionWiredTrigger {
    public static final WiredTriggerType type = WiredTriggerType.WALKS_OFF_FURNI;
    private THashSet<HabboItem> items;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/triggers/WiredTriggerHabboWalkOffFurni$JsonData.class */
    static class JsonData {
        List<Integer> itemIds;

        public JsonData(List<Integer> list) {
            this.itemIds = list;
        }
    }

    public WiredTriggerHabboWalkOffFurni(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.items = new THashSet<>();
    }

    public WiredTriggerHabboWalkOffFurni(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.items = new THashSet<>();
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public boolean execute(RoomUnit roomUnit, Room room, Object[] objArr) {
        if (objArr.length < 1 || !(objArr[0] instanceof HabboItem)) {
            return false;
        }
        return this.items.contains(objArr[0]);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new WiredTriggerFurniStateToggled.JsonData((List) this.items.stream().map((v0) -> {
            return v0.getId();
        }).collect(Collectors.toList())));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void loadWiredData(ResultSet resultSet, Room room) throws SQLException {
        this.items.clear();
        String string = resultSet.getString("wired_data");
        if (string.startsWith("{")) {
            Iterator<Integer> it = ((JsonData) WiredHandler.getGsonBuilder().create().fromJson(string, JsonData.class)).itemIds.iterator();
            while (it.hasNext()) {
                HabboItem habboItem = room.getHabboItem(it.next().intValue());
                if (habboItem != null) {
                    this.items.add(habboItem);
                }
            }
            return;
        }
        if (string.split(":").length >= 3) {
            super.setDelay(Integer.parseInt(string.split(":")[0]));
            if (string.split(":")[2].equals("\t")) {
                return;
            }
            for (String str : string.split(":")[2].split(";")) {
                if (!str.isEmpty()) {
                    try {
                        HabboItem habboItem2 = room.getHabboItem(Integer.parseInt(str));
                        if (habboItem2 != null) {
                            this.items.add(habboItem2);
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void onPickUp() {
        this.items.clear();
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger
    public WiredTriggerType getType() {
        return type;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void serializeWiredData(ServerMessage serverMessage, Room room) {
        THashSet tHashSet = new THashSet();
        if (room == null) {
            tHashSet.addAll(this.items);
        } else {
            TObjectHashIterator it = this.items.iterator();
            while (it.hasNext()) {
                HabboItem habboItem = (HabboItem) it.next();
                if (room.getHabboItem(habboItem.getId()) == null) {
                    tHashSet.add(habboItem);
                }
            }
        }
        TObjectHashIterator it2 = tHashSet.iterator();
        while (it2.hasNext()) {
            this.items.remove((HabboItem) it2.next());
        }
        serverMessage.appendBoolean(false);
        serverMessage.appendInt(Integer.valueOf(WiredHandler.MAXIMUM_FURNI_SELECTION));
        serverMessage.appendInt(Integer.valueOf(this.items.size()));
        TObjectHashIterator it3 = this.items.iterator();
        while (it3.hasNext()) {
            serverMessage.appendInt(Integer.valueOf(((HabboItem) it3.next()).getId()));
        }
        serverMessage.appendInt(Integer.valueOf(getBaseItem().getSpriteId()));
        serverMessage.appendInt(Integer.valueOf(getId()));
        serverMessage.appendString(Emulator.PREVIEW);
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(getType().code));
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt((Integer) 0);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger
    public boolean saveData(WiredSettings wiredSettings) {
        this.items.clear();
        int length = wiredSettings.getFurniIds().length;
        for (int i = 0; i < length; i++) {
            this.items.add(Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId()).getHabboItem(wiredSettings.getFurniIds()[i]));
        }
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger
    public boolean isTriggeredByRoomUnit() {
        return true;
    }
}
