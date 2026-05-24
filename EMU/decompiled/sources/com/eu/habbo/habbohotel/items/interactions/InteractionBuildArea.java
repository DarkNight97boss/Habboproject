package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomTileState;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.users.HabboManager;
import com.eu.habbo.messages.outgoing.rooms.items.RemoveFloorItemComposer;
import com.eu.habbo.messages.outgoing.rooms.items.RoomFloorItemsComposer;
import gnu.trove.TCollections;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionBuildArea.class */
public class InteractionBuildArea extends InteractionCustomValues {
    public static THashMap<String, String> defaultValues = new THashMap<String, String>() { // from class: com.eu.habbo.habbohotel.items.interactions.InteractionBuildArea.1
        {
            put("tilesLeft", "0");
            put("tilesRight", "0");
            put("tilesFront", "0");
            put("tilesBack", "0");
            put("builders", Emulator.PREVIEW);
        }
    };
    private THashSet<RoomTile> tiles;

    public InteractionBuildArea(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item, defaultValues);
        this.tiles = new THashSet<>();
    }

    public InteractionBuildArea(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4, defaultValues);
        this.tiles = new THashSet<>();
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPlace(Room room) {
        super.onPlace(room);
        regenAffectedTiles(room);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPickUp(Room room) {
        super.onPickUp(room);
        ArrayList<String> arrayList = new ArrayList(Arrays.asList(((String) this.values.get("builders")).split(";")));
        THashSet tHashSet = new THashSet();
        for (String str : arrayList) {
            Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(str);
            HabboInfo habboInfo = habbo != null ? habbo.getHabboInfo() : HabboManager.getOfflineHabboInfo(str);
            if (habboInfo != null) {
                tHashSet.add(Integer.valueOf(habboInfo.getId()));
            }
        }
        if (!tHashSet.isEmpty()) {
            TObjectHashIterator it = this.tiles.iterator();
            while (it.hasNext()) {
                TObjectHashIterator it2 = room.getItemsAt((RoomTile) it.next()).iterator();
                while (it2.hasNext()) {
                    HabboItem habboItem = (HabboItem) it2.next();
                    if (tHashSet.contains(Integer.valueOf(habboItem.getUserId())) && habboItem != this) {
                        room.pickUpItem(habboItem, null);
                    }
                }
            }
        }
        this.tiles.clear();
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onMove(Room room, RoomTile roomTile, RoomTile roomTile2) {
        super.onMove(room, roomTile, roomTile2);
        ArrayList<String> arrayList = new ArrayList(Arrays.asList(((String) this.values.get("builders")).split(";")));
        THashSet tHashSet = new THashSet();
        for (String str : arrayList) {
            Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(str);
            HabboInfo habboInfo = habbo != null ? habbo.getHabboInfo() : HabboManager.getOfflineHabboInfo(str);
            if (habboInfo != null) {
                tHashSet.add(Integer.valueOf(habboInfo.getId()));
            }
        }
        THashSet<RoomTile> tHashSet2 = this.tiles;
        THashSet tHashSet3 = new THashSet();
        int iMax = Math.max(0, roomTile2.x - Integer.parseInt((String) this.values.get("tilesBack")));
        int iMax2 = Math.max(0, roomTile2.y - Integer.parseInt((String) this.values.get("tilesRight")));
        int iMin = Math.min(room.getLayout().getMapSizeX(), roomTile2.x + Integer.parseInt((String) this.values.get("tilesFront")));
        int iMin2 = Math.min(room.getLayout().getMapSizeY(), roomTile2.y + Integer.parseInt((String) this.values.get("tilesLeft")));
        for (int i = iMax; i <= iMin; i++) {
            for (int i2 = iMax2; i2 <= iMin2; i2++) {
                RoomTile tile = room.getLayout().getTile((short) i, (short) i2);
                if (tile != null && tile.state != RoomTileState.INVALID) {
                    tHashSet3.add(tile);
                }
            }
        }
        if (!tHashSet.isEmpty()) {
            TObjectHashIterator it = tHashSet2.iterator();
            while (it.hasNext()) {
                RoomTile roomTile3 = (RoomTile) it.next();
                THashSet<HabboItem> itemsAt = room.getItemsAt(roomTile3);
                if (!tHashSet3.contains(roomTile3)) {
                    TObjectHashIterator it2 = itemsAt.iterator();
                    while (it2.hasNext()) {
                        HabboItem habboItem = (HabboItem) it2.next();
                        if (tHashSet.contains(Integer.valueOf(habboItem.getUserId())) && habboItem != this) {
                            room.pickUpItem(habboItem, null);
                        }
                    }
                }
            }
        }
        regenAffectedTiles(room);
    }

    public boolean inSquare(RoomTile roomTile) {
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId());
        if (room != null && this.tiles.size() == 0) {
            regenAffectedTiles(room);
        }
        return this.tiles.contains(roomTile);
    }

    private void regenAffectedTiles(Room room) {
        int iMax = Math.max(0, getX() - Integer.parseInt((String) this.values.get("tilesBack")));
        int iMax2 = Math.max(0, getY() - Integer.parseInt((String) this.values.get("tilesRight")));
        int iMin = Math.min(room.getLayout().getMapSizeX(), getX() + Integer.parseInt((String) this.values.get("tilesFront")));
        int iMin2 = Math.min(room.getLayout().getMapSizeY(), getY() + Integer.parseInt((String) this.values.get("tilesLeft")));
        this.tiles.clear();
        for (int i = iMax; i <= iMin; i++) {
            for (int i2 = iMax2; i2 <= iMin2; i2++) {
                RoomTile tile = room.getLayout().getTile((short) i, (short) i2);
                if (tile != null && tile.state != RoomTileState.INVALID) {
                    this.tiles.add(tile);
                }
            }
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionCustomValues
    public void onCustomValuesSaved(Room room, GameClient gameClient, THashMap<String, String> tHashMap) {
        regenAffectedTiles(room);
        ArrayList<String> arrayList = new ArrayList(Arrays.asList(((String) this.values.get("builders")).split(";")));
        THashSet tHashSet = new THashSet();
        for (String str : arrayList) {
            Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(str);
            HabboInfo habboInfo = habbo != null ? habbo.getHabboInfo() : HabboManager.getOfflineHabboInfo(str);
            if (habboInfo != null) {
                tHashSet.add(Integer.valueOf(habboInfo.getId()));
            }
        }
        THashSet tHashSet2 = new THashSet();
        int iMax = Math.max(0, getX() - Integer.parseInt((String) tHashMap.get("tilesBack")));
        int iMax2 = Math.max(0, getY() - Integer.parseInt((String) tHashMap.get("tilesRight")));
        int iMin = Math.min(room.getLayout().getMapSizeX(), getX() + Integer.parseInt((String) tHashMap.get("tilesFront")));
        int iMin2 = Math.min(room.getLayout().getMapSizeY(), getY() + Integer.parseInt((String) tHashMap.get("tilesLeft")));
        for (int i = iMax; i <= iMin; i++) {
            for (int i2 = iMax2; i2 <= iMin2; i2++) {
                RoomTile tile = room.getLayout().getTile((short) i, (short) i2);
                if (tile != null && tile.state != RoomTileState.INVALID && !this.tiles.contains(tile)) {
                    tHashSet2.add(tile);
                }
            }
        }
        if (!tHashSet.isEmpty()) {
            TObjectHashIterator it = tHashSet2.iterator();
            while (it.hasNext()) {
                TObjectHashIterator it2 = room.getItemsAt((RoomTile) it.next()).iterator();
                while (it2.hasNext()) {
                    HabboItem habboItem = (HabboItem) it2.next();
                    if (tHashSet.contains(Integer.valueOf(habboItem.getUserId())) && habboItem != this) {
                        room.pickUpItem(habboItem, null);
                    }
                }
            }
        }
        Item item = Emulator.getGameEnvironment().getItemManager().getItem("mutearea_sign2");
        if (item != null) {
            TIntObjectMap tIntObjectMapSynchronizedMap = TCollections.synchronizedMap(new TIntObjectHashMap(0));
            tIntObjectMapSynchronizedMap.put(-1, "System");
            THashSet tHashSet3 = new THashSet();
            int i3 = 0;
            TObjectHashIterator it3 = this.tiles.iterator();
            while (it3.hasNext()) {
                RoomTile roomTile = (RoomTile) it3.next();
                i3--;
                InteractionDefault interactionDefault = new InteractionDefault(i3, -1, item, "1", 0, 0);
                interactionDefault.setX(roomTile.x);
                interactionDefault.setY(roomTile.y);
                interactionDefault.setZ(roomTile.relativeHeight());
                tHashSet3.add(interactionDefault);
            }
            gameClient.sendResponse(new RoomFloorItemsComposer(tIntObjectMapSynchronizedMap, tHashSet3));
            Emulator.getThreading().run(() -> {
                TObjectHashIterator it4 = tHashSet3.iterator();
                while (it4.hasNext()) {
                    gameClient.sendResponse(new RemoveFloorItemComposer((HabboItem) it4.next(), true));
                }
            }, 3000L);
        }
    }

    public boolean isBuilder(String str) {
        return Arrays.asList(((String) this.values.get("builders")).split(";")).contains(str);
    }
}
