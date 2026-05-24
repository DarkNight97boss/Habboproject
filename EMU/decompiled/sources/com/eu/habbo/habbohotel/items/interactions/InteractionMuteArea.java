package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomTileState;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.messages.outgoing.rooms.items.ItemExtraDataComposer;
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

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionMuteArea.class */
public class InteractionMuteArea extends InteractionCustomValues {
    public static THashMap<String, String> defaultValues = new THashMap<String, String>() { // from class: com.eu.habbo.habbohotel.items.interactions.InteractionMuteArea.1
        {
            put("tilesLeft", "0");
            put("tilesRight", "0");
            put("tilesFront", "0");
            put("tilesBack", "0");
            put("state", "0");
        }
    };
    private THashSet<RoomTile> tiles;

    public InteractionMuteArea(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item, defaultValues);
        this.tiles = new THashSet<>();
    }

    public InteractionMuteArea(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4, defaultValues);
        this.tiles = new THashSet<>();
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        super.onClick(gameClient, room, objArr);
        if ((objArr.length < 2 || !(objArr[1] instanceof WiredEffectType)) && (gameClient == null || !room.hasRights(gameClient.getHabbo()))) {
            return;
        }
        this.values.put("state", ((String) this.values.get("state")).equals("0") ? "1" : "0");
        room.sendComposer(new ItemExtraDataComposer(this).compose());
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPlace(Room room) {
        super.onPlace(room);
        regenAffectedTiles(room);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPickUp(Room room) {
        super.onPickUp(room);
        this.tiles.clear();
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onMove(Room room, RoomTile roomTile, RoomTile roomTile2) {
        super.onMove(room, roomTile, roomTile2);
        regenAffectedTiles(room);
    }

    public boolean inSquare(RoomTile roomTile) {
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId());
        if (!((String) this.values.get("state")).equals("1")) {
            return false;
        }
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
        super.onCustomValuesSaved(room, gameClient, tHashMap);
        regenAffectedTiles(room);
        Item item = Emulator.getGameEnvironment().getItemManager().getItem("mutearea_sign2");
        if (item != null) {
            TIntObjectMap tIntObjectMapSynchronizedMap = TCollections.synchronizedMap(new TIntObjectHashMap(0));
            tIntObjectMapSynchronizedMap.put(-1, "System");
            THashSet tHashSet = new THashSet();
            int i = 0;
            TObjectHashIterator it = this.tiles.iterator();
            while (it.hasNext()) {
                RoomTile roomTile = (RoomTile) it.next();
                i--;
                InteractionDefault interactionDefault = new InteractionDefault(i, -1, item, "1", 0, 0);
                interactionDefault.setX(roomTile.x);
                interactionDefault.setY(roomTile.y);
                interactionDefault.setZ(roomTile.relativeHeight());
                tHashSet.add(interactionDefault);
            }
            gameClient.sendResponse(new RoomFloorItemsComposer(tIntObjectMapSynchronizedMap, tHashSet));
            Emulator.getThreading().run(() -> {
                TObjectHashIterator it2 = tHashSet.iterator();
                while (it2.hasNext()) {
                    gameClient.sendResponse(new RemoveFloorItemComposer((HabboItem) it2.next(), true));
                }
            }, 3000L);
        }
    }
}
