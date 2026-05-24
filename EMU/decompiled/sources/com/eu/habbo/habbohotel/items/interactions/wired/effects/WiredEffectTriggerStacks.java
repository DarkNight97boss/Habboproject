package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.wired.WiredSaveException;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectTriggerStacks.class */
public class WiredEffectTriggerStacks extends InteractionWiredEffect {
    public static final WiredEffectType type = WiredEffectType.CALL_STACKS;
    private THashSet<HabboItem> items;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectTriggerStacks$JsonData.class */
    static class JsonData {
        int delay;
        List<Integer> itemIds;

        public JsonData(int i, List<Integer> list) {
            this.delay = i;
            this.itemIds = list;
        }
    }

    public WiredEffectTriggerStacks(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.items = new THashSet<>();
    }

    public WiredEffectTriggerStacks(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.items = new THashSet<>();
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void serializeWiredData(ServerMessage serverMessage, Room room) {
        THashSet tHashSet = new THashSet();
        TObjectHashIterator it = this.items.iterator();
        while (it.hasNext()) {
            HabboItem habboItem = (HabboItem) it.next();
            if (habboItem.getRoomId() != getRoomId() || Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId()).getHabboItem(habboItem.getId()) == null) {
                tHashSet.add(habboItem);
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
        serverMessage.appendInt(Integer.valueOf(getDelay()));
        if (!requiresTriggeringUser()) {
            serverMessage.appendInt((Integer) 0);
            return;
        }
        final ArrayList arrayList = new ArrayList();
        room.getRoomSpecialTypes().getTriggers(getX(), getY()).forEach(new TObjectProcedure<InteractionWiredTrigger>() { // from class: com.eu.habbo.habbohotel.items.interactions.wired.effects.WiredEffectTriggerStacks.1
            public boolean execute(InteractionWiredTrigger interactionWiredTrigger) {
                if (interactionWiredTrigger.isTriggeredByRoomUnit()) {
                    return true;
                }
                arrayList.add(Integer.valueOf(interactionWiredTrigger.getBaseItem().getSpriteId()));
                return true;
            }
        });
        serverMessage.appendInt(Integer.valueOf(arrayList.size()));
        Iterator it4 = arrayList.iterator();
        while (it4.hasNext()) {
            serverMessage.appendInt((Integer) it4.next());
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect
    public boolean saveData(WiredSettings wiredSettings, GameClient gameClient) throws WiredSaveException {
        int length = wiredSettings.getFurniIds().length;
        if (length > Emulator.getConfig().getInt("hotel.wired.furni.selection.count")) {
            throw new WiredSaveException("Too many furni selected");
        }
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < length; i++) {
            int i2 = wiredSettings.getFurniIds()[i];
            HabboItem habboItem = Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId()).getHabboItem(i2);
            if (habboItem == null) {
                throw new WiredSaveException(String.format("Item %s not found", Integer.valueOf(i2)));
            }
            arrayList.add(habboItem);
        }
        int delay = wiredSettings.getDelay();
        if (delay > Emulator.getConfig().getInt("hotel.wired.max_delay", 20)) {
            throw new WiredSaveException("Delay too long");
        }
        this.items.clear();
        this.items.addAll(arrayList);
        setDelay(delay);
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public boolean execute(RoomUnit roomUnit, Room room, Object[] objArr) {
        if (objArr == null) {
            return false;
        }
        if (objArr.length >= 1 && (objArr[objArr.length - 1] instanceof WiredEffectTriggerStacks)) {
            return false;
        }
        THashSet tHashSet = new THashSet();
        TObjectHashIterator it = this.items.iterator();
        while (it.hasNext()) {
            HabboItem habboItem = (HabboItem) it.next();
            boolean z = false;
            TObjectHashIterator it2 = tHashSet.iterator();
            while (true) {
                if (!it2.hasNext()) {
                    break;
                }
                RoomTile roomTile = (RoomTile) it2.next();
                if (roomTile.x == habboItem.getX() && roomTile.y == habboItem.getY()) {
                    z = true;
                    break;
                }
            }
            if (!z) {
                tHashSet.add(room.getLayout().getTile(habboItem.getX(), habboItem.getY()));
            }
        }
        Object[] objArr2 = new Object[objArr.length + 1];
        System.arraycopy(objArr, 0, objArr2, 0, objArr.length);
        objArr2[objArr2.length - 1] = this;
        WiredHandler.executeEffectsAtTiles(tHashSet, roomUnit, room, objArr);
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(getDelay(), (List) this.items.stream().map((v0) -> {
            return v0.getId();
        }).collect(Collectors.toList())));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void loadWiredData(ResultSet resultSet, Room room) throws SQLException {
        this.items = new THashSet<>();
        String string = resultSet.getString("wired_data");
        if (string.startsWith("{")) {
            JsonData jsonData = (JsonData) WiredHandler.getGsonBuilder().create().fromJson(string, JsonData.class);
            setDelay(jsonData.delay);
            Iterator<Integer> it = jsonData.itemIds.iterator();
            while (it.hasNext()) {
                HabboItem habboItem = room.getHabboItem(it.next().intValue());
                if (habboItem != null) {
                    this.items.add(habboItem);
                }
            }
            return;
        }
        String[] strArrSplit = string.split("\t");
        if (strArrSplit.length >= 1) {
            setDelay(Integer.parseInt(strArrSplit[0]));
        }
        if (strArrSplit.length == 2 && strArrSplit[1].contains(";")) {
            for (String str : strArrSplit[1].split(";")) {
                HabboItem habboItem2 = room.getHabboItem(Integer.parseInt(str));
                if (habboItem2 != null) {
                    this.items.add(habboItem2);
                }
            }
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void onPickUp() {
        this.items.clear();
        setDelay(0);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect
    public WiredEffectType getType() {
        return type;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    protected long requiredCooldown() {
        return 250L;
    }
}
