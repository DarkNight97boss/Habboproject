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
import com.eu.habbo.habbohotel.rooms.RoomUserRotation;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredChangeDirectionSetting;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.wired.WiredSaveException;
import com.eu.habbo.messages.outgoing.rooms.items.FloorItemOnRollerComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectChangeFurniDirection.class */
public class WiredEffectChangeFurniDirection extends InteractionWiredEffect {
    public static final int ACTION_WAIT = 0;
    public static final int ACTION_TURN_RIGHT_45 = 1;
    public static final int ACTION_TURN_RIGHT_90 = 2;
    public static final int ACTION_TURN_LEFT_45 = 3;
    public static final int ACTION_TURN_LEFT_90 = 4;
    public static final int ACTION_TURN_BACK = 5;
    public static final int ACTION_TURN_RANDOM = 6;
    public static final WiredEffectType type = WiredEffectType.MOVE_DIRECTION;
    private final THashMap<HabboItem, WiredChangeDirectionSetting> items;
    private RoomUserRotation startRotation;
    private int blockedAction;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectChangeFurniDirection$JsonData.class */
    static class JsonData {
        RoomUserRotation start_direction;
        int blocked_action;
        List<WiredChangeDirectionSetting> items;
        int delay;

        public JsonData(RoomUserRotation roomUserRotation, int i, List<WiredChangeDirectionSetting> list, int i2) {
            this.start_direction = roomUserRotation;
            this.blocked_action = i;
            this.items = list;
            this.delay = i2;
        }
    }

    public WiredEffectChangeFurniDirection(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.items = new THashMap<>(0);
        this.startRotation = RoomUserRotation.NORTH;
        this.blockedAction = 0;
    }

    public WiredEffectChangeFurniDirection(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.items = new THashMap<>(0);
        this.startRotation = RoomUserRotation.NORTH;
        this.blockedAction = 0;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public boolean execute(RoomUnit roomUnit, Room room, Object[] objArr) {
        THashSet tHashSet = new THashSet();
        for (HabboItem habboItem : this.items.keySet()) {
            if (Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId()).getHabboItem(habboItem.getId()) == null) {
                tHashSet.add(habboItem);
            }
        }
        TObjectHashIterator it = tHashSet.iterator();
        while (it.hasNext()) {
            this.items.remove((HabboItem) it.next());
        }
        if (this.items.isEmpty()) {
            return false;
        }
        for (Map.Entry entry : this.items.entrySet()) {
            HabboItem habboItem2 = (HabboItem) entry.getKey();
            RoomTile tileInFront = room.getLayout().getTileInFront(room.getLayout().getTile(habboItem2.getX(), habboItem2.getY()), ((WiredChangeDirectionSetting) entry.getValue()).direction.getValue());
            int i = 1;
            while (true) {
                if ((tileInFront == null || tileInFront.state == RoomTileState.INVALID || room.furnitureFitsAt(tileInFront, habboItem2, habboItem2.getRotation(), false) != FurnitureMovementError.NONE) && i < 8) {
                    ((WiredChangeDirectionSetting) entry.getValue()).direction = nextRotation(((WiredChangeDirectionSetting) entry.getValue()).direction);
                    RoomTile tileInFront2 = room.getLayout().getTileInFront(room.getLayout().getTile(habboItem2.getX(), habboItem2.getY()), ((WiredChangeDirectionSetting) entry.getValue()).direction.getValue());
                    if (tileInFront2 != null && tileInFront2.state != RoomTileState.INVALID) {
                        tileInFront = tileInFront2;
                    }
                    i++;
                }
            }
        }
        for (Map.Entry entry2 : this.items.entrySet()) {
            HabboItem habboItem3 = (HabboItem) entry2.getKey();
            RoomTile tileInFront3 = room.getLayout().getTileInFront(room.getLayout().getTile(habboItem3.getX(), habboItem3.getY()), ((WiredChangeDirectionSetting) entry2.getValue()).direction.getValue());
            if (habboItem3.getRotation() != ((WiredChangeDirectionSetting) entry2.getValue()).rotation) {
                if (room.furnitureFitsAt(tileInFront3, habboItem3, ((WiredChangeDirectionSetting) entry2.getValue()).rotation, false) == FurnitureMovementError.NONE) {
                    room.moveFurniTo((HabboItem) entry2.getKey(), tileInFront3, ((WiredChangeDirectionSetting) entry2.getValue()).rotation, null, true);
                }
            }
            boolean z = false;
            TObjectHashIterator it2 = room.getLayout().getTilesAt(tileInFront3, habboItem3.getBaseItem().getWidth(), habboItem3.getBaseItem().getLength(), habboItem3.getRotation()).iterator();
            while (it2.hasNext()) {
                TObjectHashIterator it3 = room.getRoomUnits((RoomTile) it2.next()).iterator();
                while (true) {
                    if (it3.hasNext()) {
                        RoomUnit roomUnit2 = (RoomUnit) it3.next();
                        z = true;
                        if (roomUnit2.getCurrentLocation() == tileInFront3) {
                            Emulator.getThreading().run(() -> {
                                WiredHandler.handle(WiredTriggerType.COLLISION, roomUnit2, room, new Object[]{entry2.getKey()});
                            });
                            break;
                        }
                    }
                }
            }
            if (tileInFront3 != null && tileInFront3.state != RoomTileState.INVALID && room.furnitureFitsAt(tileInFront3, habboItem3, habboItem3.getRotation(), false) == FurnitureMovementError.NONE && !z) {
                RoomTile tile = room.getLayout().getTile(((HabboItem) entry2.getKey()).getX(), ((HabboItem) entry2.getKey()).getY());
                double z2 = ((HabboItem) entry2.getKey()).getZ();
                if (room.moveFurniTo((HabboItem) entry2.getKey(), tileInFront3, habboItem3.getRotation(), null, false) == FurnitureMovementError.NONE) {
                    room.sendComposer(new FloorItemOnRollerComposer((HabboItem) entry2.getKey(), null, tile, z2, tileInFront3, ((HabboItem) entry2.getKey()).getZ(), 0.0d, room).compose());
                }
            }
        }
        return false;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.startRotation, this.blockedAction, new ArrayList(this.items.values()), getDelay()));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void loadWiredData(ResultSet resultSet, Room room) throws SQLException {
        HabboItem habboItem;
        this.items.clear();
        String string = resultSet.getString("wired_data");
        if (string.startsWith("{")) {
            JsonData jsonData = (JsonData) WiredHandler.getGsonBuilder().create().fromJson(string, JsonData.class);
            setDelay(jsonData.delay);
            this.startRotation = jsonData.start_direction;
            this.blockedAction = jsonData.blocked_action;
            for (WiredChangeDirectionSetting wiredChangeDirectionSetting : jsonData.items) {
                HabboItem habboItem2 = room.getHabboItem(wiredChangeDirectionSetting.item_id);
                if (habboItem2 != null) {
                    this.items.put(habboItem2, wiredChangeDirectionSetting);
                }
            }
            return;
        }
        String[] strArrSplit = string.split("\t");
        if (strArrSplit.length >= 4) {
            setDelay(Integer.parseInt(strArrSplit[0]));
            this.startRotation = RoomUserRotation.fromValue(Integer.parseInt(strArrSplit[1]));
            this.blockedAction = Integer.parseInt(strArrSplit[2]);
            if (Integer.parseInt(strArrSplit[3]) > 0) {
                for (int i = 4; i < strArrSplit.length; i++) {
                    String[] strArrSplit2 = strArrSplit[i].split(":");
                    if (strArrSplit2.length >= 2 && (habboItem = room.getHabboItem(Integer.parseInt(strArrSplit2[0]))) != null) {
                        int rotation = habboItem.getRotation();
                        if (strArrSplit2.length > 2) {
                            rotation = Integer.parseInt(strArrSplit2[2]);
                        }
                        this.items.put(habboItem, new WiredChangeDirectionSetting(habboItem.getId(), rotation, RoomUserRotation.fromValue(Integer.parseInt(strArrSplit2[1]))));
                    }
                }
            }
        }
        needsUpdate(true);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void onPickUp() {
        setDelay(0);
        this.items.clear();
        this.blockedAction = 0;
        this.startRotation = RoomUserRotation.NORTH;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect
    public WiredEffectType getType() {
        return type;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void serializeWiredData(ServerMessage serverMessage, Room room) {
        serverMessage.appendBoolean(false);
        serverMessage.appendInt(Integer.valueOf(WiredHandler.MAXIMUM_FURNI_SELECTION));
        serverMessage.appendInt(Integer.valueOf(this.items.size()));
        Iterator it = this.items.entrySet().iterator();
        while (it.hasNext()) {
            serverMessage.appendInt(Integer.valueOf(((HabboItem) ((Map.Entry) it.next()).getKey()).getId()));
        }
        serverMessage.appendInt(Integer.valueOf(getBaseItem().getSpriteId()));
        serverMessage.appendInt(Integer.valueOf(getId()));
        serverMessage.appendString(Emulator.PREVIEW);
        serverMessage.appendInt((Integer) 2);
        serverMessage.appendInt(Integer.valueOf(this.startRotation != null ? this.startRotation.getValue() : 0));
        serverMessage.appendInt(Integer.valueOf(this.blockedAction));
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(getType().code));
        serverMessage.appendInt(Integer.valueOf(getDelay()));
        serverMessage.appendInt((Integer) 0);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect
    public boolean saveData(WiredSettings wiredSettings, GameClient gameClient) throws WiredSaveException {
        if (wiredSettings.getIntParams().length < 2) {
            throw new WiredSaveException("Invalid data");
        }
        int i = wiredSettings.getIntParams()[0];
        if (i < 0 || i > 7 || i % 2 != 0) {
            throw new WiredSaveException("Start direction is invalid");
        }
        RoomUserRotation roomUserRotationFromValue = RoomUserRotation.fromValue(i);
        int i2 = wiredSettings.getIntParams()[1];
        if (i2 < 0 || i2 > 6) {
            throw new WiredSaveException("Blocked action is invalid");
        }
        int length = wiredSettings.getFurniIds().length;
        if (length > Emulator.getConfig().getInt("hotel.wired.furni.selection.count")) {
            throw new WiredSaveException("Too many furni selected");
        }
        THashMap tHashMap = new THashMap();
        for (int i3 = 0; i3 < length; i3++) {
            int i4 = wiredSettings.getFurniIds()[i3];
            HabboItem habboItem = Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId()).getHabboItem(i4);
            if (habboItem == null) {
                throw new WiredSaveException(String.format("Item %s not found", Integer.valueOf(i4)));
            }
            tHashMap.put(habboItem, new WiredChangeDirectionSetting(habboItem.getId(), habboItem.getRotation(), roomUserRotationFromValue));
        }
        int delay = wiredSettings.getDelay();
        if (delay > Emulator.getConfig().getInt("hotel.wired.max_delay", 20)) {
            throw new WiredSaveException("Delay too long");
        }
        this.items.clear();
        this.items.putAll(tHashMap);
        this.startRotation = roomUserRotationFromValue;
        this.blockedAction = i2;
        setDelay(delay);
        return true;
    }

    private RoomUserRotation nextRotation(RoomUserRotation roomUserRotation) {
        switch (this.blockedAction) {
            case 0:
            default:
                return roomUserRotation;
            case 1:
                return RoomUserRotation.clockwise(roomUserRotation);
            case 2:
                return RoomUserRotation.clockwise(RoomUserRotation.clockwise(roomUserRotation));
            case 3:
                return RoomUserRotation.counterClockwise(roomUserRotation);
            case 4:
                return RoomUserRotation.counterClockwise(RoomUserRotation.counterClockwise(roomUserRotation));
            case 5:
                return RoomUserRotation.fromValue(roomUserRotation.getValue()).getOpposite();
            case 6:
                return RoomUserRotation.fromValue(Emulator.getRandom().nextInt(8));
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    protected long requiredCooldown() {
        return 495L;
    }
}
