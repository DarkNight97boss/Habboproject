package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.FurnitureMovementError;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomTileState;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUserRotation;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.wired.WiredSaveException;
import com.eu.habbo.messages.outgoing.rooms.items.FloorItemOnRollerComposer;
import com.eu.habbo.threading.runnables.WiredCollissionRunnable;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectMoveFurniTowards.class */
public class WiredEffectMoveFurniTowards extends InteractionWiredEffect {
    public static final WiredEffectType type = WiredEffectType.CHASE;
    private THashSet<HabboItem> items;
    private THashMap<Integer, RoomUserRotation> lastDirections;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectMoveFurniTowards$JsonData.class */
    static class JsonData {
        int delay;
        List<Integer> itemIds;

        public JsonData(int i, List<Integer> list) {
            this.delay = i;
            this.itemIds = list;
        }
    }

    public WiredEffectMoveFurniTowards(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.items = new THashSet<>();
        this.lastDirections = new THashMap<>();
    }

    public WiredEffectMoveFurniTowards(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.items = new THashSet<>();
        this.lastDirections = new THashMap<>();
    }

    public List<RoomUserRotation> getAvailableDirections(HabboItem habboItem, Room room) {
        HabboItem topItemAt;
        ArrayList arrayList = new ArrayList();
        RoomLayout layout = room.getLayout();
        RoomTile tile = layout.getTile(habboItem.getX(), habboItem.getY());
        for (RoomUserRotation roomUserRotation : new RoomUserRotation[]{RoomUserRotation.NORTH, RoomUserRotation.EAST, RoomUserRotation.SOUTH, RoomUserRotation.WEST}) {
            RoomTile tileInFront = layout.getTileInFront(tile, roomUserRotation.getValue());
            if (tileInFront != null && tileInFront.state != RoomTileState.BLOCKED && tileInFront.state != RoomTileState.INVALID && layout.tileExists(tileInFront.x, tileInFront.y) && room.furnitureFitsAt(tileInFront, habboItem, habboItem.getRotation()) != FurnitureMovementError.INVALID_MOVE && (((topItemAt = room.getTopItemAt(tileInFront.x, tileInFront.y)) == null || topItemAt.getBaseItem().allowStack()) && tileInFront.getAllowStack())) {
                arrayList.add(roomUserRotation);
            }
        }
        return arrayList;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public boolean execute(RoomUnit roomUnit, Room room, Object[] objArr) {
        THashSet tHashSet = new THashSet();
        TObjectHashIterator it = this.items.iterator();
        while (it.hasNext()) {
            HabboItem habboItem = (HabboItem) it.next();
            if (Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId()).getHabboItem(habboItem.getId()) == null) {
                tHashSet.add(habboItem);
            }
        }
        TObjectHashIterator it2 = tHashSet.iterator();
        while (it2.hasNext()) {
            this.items.remove((HabboItem) it2.next());
        }
        TObjectHashIterator it3 = this.items.iterator();
        while (it3.hasNext()) {
            HabboItem habboItem2 = (HabboItem) it3.next();
            if (habboItem2 != null) {
                RoomUserRotation roomUserRotation = (RoomUserRotation) this.lastDirections.get(Integer.valueOf(habboItem2.getId()));
                RoomUnit next = null;
                RoomLayout layout = room.getLayout();
                boolean z = false;
                if (layout == null) {
                    return true;
                }
                for (int i = 0; i < 3 && next == null; i++) {
                    RoomUserRotation[] roomUserRotationArr = {RoomUserRotation.NORTH, RoomUserRotation.EAST, RoomUserRotation.SOUTH, RoomUserRotation.WEST};
                    int length = roomUserRotationArr.length;
                    int i2 = 0;
                    while (true) {
                        if (i2 < length) {
                            RoomUserRotation roomUserRotation2 = roomUserRotationArr[i2];
                            RoomTile tile = layout.getTile(habboItem2.getX(), habboItem2.getY());
                            for (int i3 = 0; i3 <= i && tile != null; i3++) {
                                tile = layout.getTileInFront(tile, roomUserRotation2.getValue());
                            }
                            if (tile != null && layout.tileExists(tile.x, tile.y)) {
                                Collection<RoomUnit> roomUnitsAt = room.getRoomUnitsAt(tile);
                                if (roomUnitsAt.size() > 0) {
                                    next = roomUnitsAt.iterator().next();
                                    if (i == 0) {
                                        z = true;
                                        Emulator.getThreading().run(new WiredCollissionRunnable(next, room, new Object[]{habboItem2}));
                                    }
                                }
                            }
                            i2++;
                        }
                    }
                }
                if (!z) {
                    RoomUserRotation next2 = next != null ? next.getX() == habboItem2.getX() ? habboItem2.getY() < next.getY() ? RoomUserRotation.SOUTH : RoomUserRotation.NORTH : next.getY() == habboItem2.getY() ? habboItem2.getX() < next.getX() ? RoomUserRotation.EAST : RoomUserRotation.WEST : next.getX() - habboItem2.getX() > next.getY() - habboItem2.getY() ? next.getX() - habboItem2.getX() > 0 ? RoomUserRotation.EAST : RoomUserRotation.WEST : next.getY() - habboItem2.getY() > 0 ? RoomUserRotation.SOUTH : RoomUserRotation.NORTH : null;
                    List<RoomUserRotation> availableDirections = getAvailableDirections(habboItem2, room);
                    if (next2 != null && !availableDirections.contains(next2)) {
                        next2 = null;
                    }
                    if (next2 == null) {
                        if (availableDirections.size() != 0) {
                            if (availableDirections.size() == 1) {
                                next2 = availableDirections.iterator().next();
                            } else if (availableDirections.size() == 2) {
                                next2 = roomUserRotation == null ? availableDirections.get(Emulator.getRandom().nextInt(availableDirections.size())) : availableDirections.get(0) == roomUserRotation.getOpposite() ? availableDirections.get(1) : availableDirections.get(0);
                            } else {
                                if (roomUserRotation != null) {
                                    availableDirections.remove(roomUserRotation.getOpposite());
                                }
                                next2 = availableDirections.get(Emulator.getRandom().nextInt(availableDirections.size()));
                            }
                        }
                    }
                    RoomTile tileInFront = room.getLayout().getTileInFront(room.getLayout().getTile(habboItem2.getX(), habboItem2.getY()), next2.getValue());
                    RoomTile tile2 = room.getLayout().getTile(habboItem2.getX(), habboItem2.getY());
                    double z2 = habboItem2.getZ();
                    if (tileInFront != null) {
                        this.lastDirections.put(Integer.valueOf(habboItem2.getId()), next2);
                        if (tileInFront.state != RoomTileState.INVALID && tileInFront != tile2 && room.furnitureFitsAt(tileInFront, habboItem2, habboItem2.getRotation(), true) == FurnitureMovementError.NONE && room.moveFurniTo(habboItem2, tileInFront, habboItem2.getRotation(), null, false) == FurnitureMovementError.NONE) {
                            room.sendComposer(new FloorItemOnRollerComposer(habboItem2, null, tile2, z2, tileInFront, habboItem2.getZ(), 0.0d, room).compose());
                        }
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
