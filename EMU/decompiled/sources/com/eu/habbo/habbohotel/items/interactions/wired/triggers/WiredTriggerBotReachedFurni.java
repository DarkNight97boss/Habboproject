package com.eu.habbo.habbohotel.items.interactions.wired.triggers;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/triggers/WiredTriggerBotReachedFurni.class */
public class WiredTriggerBotReachedFurni extends InteractionWiredTrigger {
    private static final Logger LOGGER = LoggerFactory.getLogger(WiredTriggerBotReachedFurni.class);
    public static final WiredTriggerType type = WiredTriggerType.WALKS_ON_FURNI;
    private THashSet<HabboItem> items;
    private String botName;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/triggers/WiredTriggerBotReachedFurni$JsonData.class */
    static class JsonData {
        String botName;
        List<Integer> itemIds;

        public JsonData(String str, List<Integer> list) {
            this.botName = str;
            this.itemIds = list;
        }
    }

    public WiredTriggerBotReachedFurni(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.botName = Emulator.PREVIEW;
        this.items = new THashSet<>();
    }

    public WiredTriggerBotReachedFurni(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.botName = Emulator.PREVIEW;
        this.items = new THashSet<>();
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger
    public WiredTriggerType getType() {
        return type;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void serializeWiredData(ServerMessage serverMessage, Room room) {
        THashSet tHashSet = new THashSet();
        if (Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId()) == null) {
            tHashSet.addAll(this.items);
        } else {
            TObjectHashIterator it = this.items.iterator();
            while (it.hasNext()) {
                HabboItem habboItem = (HabboItem) it.next();
                if (Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId()).getHabboItem(habboItem.getId()) == null) {
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
        serverMessage.appendString(this.botName);
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(WiredTriggerType.BOT_REACHED_STF.code));
        if (isTriggeredByRoomUnit()) {
            serverMessage.appendInt((Integer) 0);
            return;
        }
        final ArrayList arrayList = new ArrayList();
        room.getRoomSpecialTypes().getEffects(getX(), getY()).forEach(new TObjectProcedure<InteractionWiredEffect>() { // from class: com.eu.habbo.habbohotel.items.interactions.wired.triggers.WiredTriggerBotReachedFurni.1
            public boolean execute(InteractionWiredEffect interactionWiredEffect) {
                if (!interactionWiredEffect.requiresTriggeringUser()) {
                    return true;
                }
                arrayList.add(Integer.valueOf(interactionWiredEffect.getBaseItem().getSpriteId()));
                return true;
            }
        });
        serverMessage.appendInt(Integer.valueOf(arrayList.size()));
        Iterator it4 = arrayList.iterator();
        while (it4.hasNext()) {
            serverMessage.appendInt((Integer) it4.next());
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger
    public boolean saveData(WiredSettings wiredSettings) {
        this.botName = wiredSettings.getStringParam();
        this.items.clear();
        int length = wiredSettings.getFurniIds().length;
        for (int i = 0; i < length; i++) {
            this.items.add(Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId()).getHabboItem(wiredSettings.getFurniIds()[i]));
        }
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public boolean execute(RoomUnit roomUnit, Room room, Object[] objArr) {
        return objArr.length >= 1 && (objArr[0] instanceof HabboItem) && this.items.contains(objArr[0]) && room.getBots(this.botName).stream().anyMatch(bot -> {
            return bot.getRoomUnit() == roomUnit;
        });
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.botName, (List) this.items.stream().map((v0) -> {
            return v0.getId();
        }).collect(Collectors.toList())));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void loadWiredData(ResultSet resultSet, Room room) throws SQLException {
        this.items.clear();
        String string = resultSet.getString("wired_data");
        if (string.startsWith("{")) {
            JsonData jsonData = (JsonData) WiredHandler.getGsonBuilder().create().fromJson(string, JsonData.class);
            this.botName = jsonData.botName;
            Iterator<Integer> it = jsonData.itemIds.iterator();
            while (it.hasNext()) {
                HabboItem habboItem = room.getHabboItem(it.next().intValue());
                if (habboItem != null) {
                    this.items.add(habboItem);
                }
            }
            return;
        }
        String[] strArrSplit = string.split(":");
        if (strArrSplit.length == 1) {
            this.botName = strArrSplit[0];
            return;
        }
        if (strArrSplit.length == 2) {
            this.botName = strArrSplit[0];
            for (String str : strArrSplit[1].split(";")) {
                try {
                    HabboItem habboItem2 = room.getHabboItem(Integer.parseInt(str));
                    if (habboItem2 != null) {
                        this.items.add(habboItem2);
                    }
                } catch (Exception e) {
                    LOGGER.error("Caught exception", e);
                }
            }
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void onPickUp() {
        this.items.clear();
        this.botName = Emulator.PREVIEW;
    }
}
