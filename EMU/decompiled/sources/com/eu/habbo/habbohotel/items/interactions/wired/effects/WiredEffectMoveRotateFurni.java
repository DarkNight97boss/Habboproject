package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.ICycleable;
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
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.wired.WiredSaveException;
import com.eu.habbo.messages.outgoing.rooms.items.FloorItemOnRollerComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectMoveRotateFurni.class */
public class WiredEffectMoveRotateFurni extends InteractionWiredEffect implements ICycleable {
    private static final Logger LOGGER = LoggerFactory.getLogger(WiredEffectMoveRotateFurni.class);
    public static final WiredEffectType type = WiredEffectType.MOVE_ROTATE;
    private final THashSet<HabboItem> items;
    private int direction;
    private int rotation;
    private THashSet<HabboItem> itemCooldowns;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectMoveRotateFurni$JsonData.class */
    static class JsonData {
        int direction;
        int rotation;
        int delay;
        List<Integer> itemIds;

        public JsonData(int i, int i2, int i3, List<Integer> list) {
            this.direction = i;
            this.rotation = i2;
            this.delay = i3;
            this.itemIds = list;
        }
    }

    public WiredEffectMoveRotateFurni(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.items = new THashSet<>(WiredHandler.MAXIMUM_FURNI_SELECTION / 2);
        this.itemCooldowns = new THashSet<>();
    }

    public WiredEffectMoveRotateFurni(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.items = new THashSet<>(WiredHandler.MAXIMUM_FURNI_SELECTION / 2);
        this.itemCooldowns = new THashSet<>();
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public boolean execute(RoomUnit roomUnit, Room room, Object[] objArr) {
        this.items.removeIf(habboItem -> {
            return Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId()).getHabboItem(habboItem.getId()) == null;
        });
        TObjectHashIterator it = this.items.iterator();
        while (it.hasNext()) {
            HabboItem habboItem2 = (HabboItem) it.next();
            if (!this.itemCooldowns.contains(habboItem2)) {
                int newRotation = this.rotation > 0 ? getNewRotation(habboItem2) : habboItem2.getRotation();
                RoomTile tile = room.getLayout().getTile(habboItem2.getX(), habboItem2.getY());
                RoomTile tile2 = room.getLayout().getTile(habboItem2.getX(), habboItem2.getY());
                double z = habboItem2.getZ();
                if (this.direction > 0) {
                    RoomUserRotation movementDirection = getMovementDirection();
                    tile = room.getLayout().getTile((short) (habboItem2.getX() + ((movementDirection == RoomUserRotation.WEST || movementDirection == RoomUserRotation.NORTH_WEST || movementDirection == RoomUserRotation.SOUTH_WEST) ? (short) -1 : (movementDirection == RoomUserRotation.EAST || movementDirection == RoomUserRotation.SOUTH_EAST || movementDirection == RoomUserRotation.NORTH_EAST) ? (short) 1 : (short) 0)), (short) (habboItem2.getY() + ((movementDirection == RoomUserRotation.NORTH || movementDirection == RoomUserRotation.NORTH_EAST || movementDirection == RoomUserRotation.NORTH_WEST) ? (short) 1 : (movementDirection == RoomUserRotation.SOUTH || movementDirection == RoomUserRotation.SOUTH_EAST || movementDirection == RoomUserRotation.SOUTH_WEST) ? (short) -1 : (short) 0)));
                }
                boolean z2 = habboItem2.getRotation() == newRotation;
                FurnitureMovementError furnitureMovementErrorFurnitureFitsAt = room.furnitureFitsAt(tile, habboItem2, newRotation, true);
                if (tile != null && tile.state != RoomTileState.INVALID && (tile != tile2 || newRotation != habboItem2.getRotation())) {
                    if (furnitureMovementErrorFurnitureFitsAt != FurnitureMovementError.NONE) {
                        if (furnitureMovementErrorFurnitureFitsAt == FurnitureMovementError.TILE_HAS_BOTS || furnitureMovementErrorFurnitureFitsAt == FurnitureMovementError.TILE_HAS_HABBOS || furnitureMovementErrorFurnitureFitsAt == FurnitureMovementError.TILE_HAS_PETS) {
                            if (tile == tile2) {
                            }
                        }
                    }
                    if (room.furnitureFitsAt(tile, habboItem2, newRotation, false) == FurnitureMovementError.NONE) {
                        if (room.moveFurniTo(habboItem2, tile, newRotation, null, !z2) == FurnitureMovementError.NONE) {
                            this.itemCooldowns.add(habboItem2);
                            if (z2) {
                                room.sendComposer(new FloorItemOnRollerComposer(habboItem2, null, tile2, z, tile, habboItem2.getZ(), 0.0d, room).compose());
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public String getWiredData() {
        THashSet tHashSet = new THashSet(this.items.size() / 2);
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId());
        TObjectHashIterator it = this.items.iterator();
        while (it.hasNext()) {
            HabboItem habboItem = (HabboItem) it.next();
            if (habboItem.getRoomId() != getRoomId() || (room != null && room.getHabboItem(habboItem.getId()) == null)) {
                tHashSet.add(habboItem);
            }
        }
        TObjectHashIterator it2 = tHashSet.iterator();
        while (it2.hasNext()) {
            this.items.remove((HabboItem) it2.next());
        }
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.direction, this.rotation, getDelay(), (List) this.items.stream().map((v0) -> {
            return v0.getId();
        }).collect(Collectors.toList())));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void loadWiredData(ResultSet resultSet, Room room) throws SQLException {
        this.items.clear();
        String string = resultSet.getString("wired_data");
        if (string.startsWith("{")) {
            JsonData jsonData = (JsonData) WiredHandler.getGsonBuilder().create().fromJson(string, JsonData.class);
            setDelay(jsonData.delay);
            this.direction = jsonData.direction;
            this.rotation = jsonData.rotation;
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
        if (strArrSplit.length == 4) {
            try {
                this.direction = Integer.parseInt(strArrSplit[0]);
                this.rotation = Integer.parseInt(strArrSplit[1]);
                setDelay(Integer.parseInt(strArrSplit[2]));
            } catch (Exception e) {
                System.out.println(e);
            }
            for (String str : strArrSplit[3].split("\r")) {
                HabboItem habboItem2 = room.getHabboItem(Integer.parseInt(str));
                if (habboItem2 != null) {
                    this.items.add(habboItem2);
                }
            }
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void onPickUp() {
        this.direction = 0;
        this.rotation = 0;
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
        serverMessage.appendInt((Integer) 2);
        serverMessage.appendInt(Integer.valueOf(this.direction));
        serverMessage.appendInt(Integer.valueOf(this.rotation));
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(getType().code));
        serverMessage.appendInt(Integer.valueOf(getDelay()));
        serverMessage.appendInt((Integer) 0);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect
    public boolean saveData(WiredSettings wiredSettings, GameClient gameClient) throws WiredSaveException {
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId());
        if (room == null) {
            return false;
        }
        if (wiredSettings.getIntParams().length < 2) {
            throw new WiredSaveException("invalid data");
        }
        this.direction = wiredSettings.getIntParams()[0];
        this.rotation = wiredSettings.getIntParams()[1];
        int length = wiredSettings.getFurniIds().length;
        if (length > Emulator.getConfig().getInt("hotel.wired.furni.selection.count", 5)) {
            return false;
        }
        this.items.clear();
        for (int i = 0; i < length; i++) {
            this.items.add(room.getHabboItem(wiredSettings.getFurniIds()[i]));
        }
        setDelay(wiredSettings.getDelay());
        return true;
    }

    private int getNewRotation(HabboItem habboItem) {
        if (habboItem.getMaximumRotations() == 2) {
            return habboItem.getRotation() == 0 ? 4 : 0;
        }
        if (habboItem.getMaximumRotations() == 1) {
            return habboItem.getRotation();
        }
        if (habboItem.getMaximumRotations() > 4) {
            if (this.rotation == 1) {
                if (habboItem.getRotation() == habboItem.getMaximumRotations() - 1) {
                    return 0;
                }
                return habboItem.getRotation() + 1;
            }
            if (this.rotation == 2) {
                return habboItem.getRotation() > 0 ? habboItem.getRotation() - 1 : habboItem.getMaximumRotations() - 1;
            }
            if (this.rotation == 3) {
                THashSet tHashSet = new THashSet();
                for (int i = 0; i < habboItem.getMaximumRotations(); i++) {
                    tHashSet.add(Integer.valueOf(i));
                }
                tHashSet.remove(Integer.valueOf(habboItem.getRotation()));
                if (tHashSet.size() > 0) {
                    int iNextInt = Emulator.getRandom().nextInt(tHashSet.size());
                    TObjectHashIterator it = tHashSet.iterator();
                    for (int i2 = 0; i2 < iNextInt; i2++) {
                        it.next();
                    }
                    return ((Integer) it.next()).intValue();
                }
            }
        } else {
            if (this.rotation == 1) {
                return (habboItem.getRotation() + 2) % 8;
            }
            if (this.rotation == 2) {
                int rotation = (habboItem.getRotation() - 2) % 8;
                if (rotation < 0) {
                    rotation += 8;
                }
                return rotation;
            }
            if (this.rotation == 3) {
                THashSet tHashSet2 = new THashSet();
                for (int i3 = 0; i3 < habboItem.getMaximumRotations(); i3++) {
                    tHashSet2.add(Integer.valueOf(i3 * 2));
                }
                tHashSet2.remove(Integer.valueOf(habboItem.getRotation()));
                if (tHashSet2.size() > 0) {
                    int iNextInt2 = Emulator.getRandom().nextInt(tHashSet2.size());
                    TObjectHashIterator it2 = tHashSet2.iterator();
                    for (int i4 = 0; i4 < iNextInt2; i4++) {
                        it2.next();
                    }
                    return ((Integer) it2.next()).intValue();
                }
            }
        }
        return habboItem.getRotation();
    }

    private RoomUserRotation getMovementDirection() {
        RoomUserRotation roomUserRotation = RoomUserRotation.NORTH;
        if (this.direction == 1) {
            roomUserRotation = RoomUserRotation.values()[Emulator.getRandom().nextInt(RoomUserRotation.values().length / 2) * 2];
        } else if (this.direction == 2) {
            roomUserRotation = Emulator.getRandom().nextInt(2) == 1 ? RoomUserRotation.EAST : RoomUserRotation.WEST;
        } else if (this.direction == 3) {
            if (Emulator.getRandom().nextInt(2) != 1) {
                roomUserRotation = RoomUserRotation.SOUTH;
            }
        } else if (this.direction == 4) {
            roomUserRotation = RoomUserRotation.SOUTH;
        } else if (this.direction == 5) {
            roomUserRotation = RoomUserRotation.EAST;
        } else if (this.direction == 7) {
            roomUserRotation = RoomUserRotation.WEST;
        }
        return roomUserRotation;
    }

    @Override // com.eu.habbo.habbohotel.items.ICycleable
    public void cycle(Room room) {
        this.itemCooldowns.clear();
    }
}
