package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.FurnitureMovementError;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomTileState;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.wired.WiredSaveException;
import com.eu.habbo.messages.outgoing.rooms.items.FloorItemOnRollerComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectMoveFurniAway.class */
public class WiredEffectMoveFurniAway extends InteractionWiredEffect {
    public static final WiredEffectType type = WiredEffectType.FLEE;
    private THashSet<HabboItem> items;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectMoveFurniAway$JsonData.class */
    static class JsonData {
        int delay;
        List<Integer> itemIds;

        public JsonData(int i, List<Integer> list) {
            this.delay = i;
            this.itemIds = list;
        }
    }

    public WiredEffectMoveFurniAway(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.items = new THashSet<>();
    }

    public WiredEffectMoveFurniAway(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.items = new THashSet<>();
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public boolean execute(RoomUnit roomUnit, Room room, Object[] objArr) {
        THashSet tHashSet = new THashSet();
        TObjectHashIterator it = this.items.iterator();
        while (it.hasNext()) {
            HabboItem habboItem = (HabboItem) it.next();
            if (habboItem.getRoomId() == 0) {
                tHashSet.add(habboItem);
            }
        }
        this.items.removeAll(tHashSet);
        TObjectHashIterator it2 = this.items.iterator();
        while (it2.hasNext()) {
            HabboItem habboItem2 = (HabboItem) it2.next();
            RoomTile tile = room.getLayout().getTile(habboItem2.getX(), habboItem2.getY());
            RoomUnit roomUnit2 = (RoomUnit) room.getRoomUnits().stream().min(Comparator.comparingDouble(roomUnit3 -> {
                return roomUnit3.getCurrentLocation().distance(tile);
            })).orElse(null);
            if (roomUnit2 != null) {
                if (roomUnit2.getCurrentLocation().distance(tile) <= 1.0d) {
                    Emulator.getThreading().run(() -> {
                        WiredHandler.handle(WiredTriggerType.COLLISION, roomUnit2, room, new Object[]{habboItem2});
                    }, 500L);
                } else {
                    int i = 0;
                    int i2 = 0;
                    if (roomUnit2.getX() == habboItem2.getX()) {
                        i2 = habboItem2.getY() < roomUnit2.getY() ? 0 - 1 : 0 + 1;
                    } else if (roomUnit2.getY() == habboItem2.getY()) {
                        i = habboItem2.getX() < roomUnit2.getX() ? 0 - 1 : 0 + 1;
                    } else if (roomUnit2.getX() - habboItem2.getX() > roomUnit2.getY() - habboItem2.getY()) {
                        i = roomUnit2.getX() - habboItem2.getX() > 0 ? 0 - 1 : 0 + 1;
                    } else {
                        i2 = roomUnit2.getY() - habboItem2.getY() > 0 ? 0 - 1 : 0 + 1;
                    }
                    RoomTile tile2 = room.getLayout().getTile((short) (habboItem2.getX() + i), (short) (habboItem2.getY() + i2));
                    RoomTile tile3 = room.getLayout().getTile(habboItem2.getX(), habboItem2.getY());
                    double z = habboItem2.getZ();
                    if (tile2 != null && tile2.state != RoomTileState.INVALID && tile2 != tile3 && room.furnitureFitsAt(tile2, habboItem2, habboItem2.getRotation(), true) == FurnitureMovementError.NONE && room.moveFurniTo(habboItem2, tile2, habboItem2.getRotation(), null, false) == FurnitureMovementError.NONE) {
                        room.sendComposer(new FloorItemOnRollerComposer(habboItem2, null, tile3, z, tile2, habboItem2.getZ(), 0.0d, room).compose());
                    }
                }
            }
        }
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
        serverMessage.appendInt((Integer) 0);
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
    protected long requiredCooldown() {
        return 495L;
    }
}
