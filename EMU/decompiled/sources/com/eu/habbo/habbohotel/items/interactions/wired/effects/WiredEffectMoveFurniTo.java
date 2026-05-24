package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectMoveFurniTo.class */
public class WiredEffectMoveFurniTo extends InteractionWiredEffect {
    public static final WiredEffectType type = WiredEffectType.MOVE_FURNI_TO;
    private final List<HabboItem> items;
    private int direction;
    private int spacing;
    private Map<Integer, Integer> indexOffset;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectMoveFurniTo$JsonData.class */
    static class JsonData {
        int direction;
        int spacing;
        int delay;
        List<Integer> itemIds;

        public JsonData(int i, int i2, int i3, List<Integer> list) {
            this.direction = i;
            this.spacing = i2;
            this.delay = i3;
            this.itemIds = list;
        }
    }

    public WiredEffectMoveFurniTo(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.items = new ArrayList();
        this.spacing = 1;
        this.indexOffset = new LinkedHashMap();
    }

    public WiredEffectMoveFurniTo(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.items = new ArrayList();
        this.spacing = 1;
        this.indexOffset = new LinkedHashMap();
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect
    public boolean saveData(WiredSettings wiredSettings, GameClient gameClient) throws WiredSaveException {
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId());
        if (room == null) {
            return false;
        }
        this.items.clear();
        this.indexOffset.clear();
        if (wiredSettings.getIntParams().length < 2) {
            throw new WiredSaveException("invalid data");
        }
        this.direction = wiredSettings.getIntParams()[0];
        this.spacing = wiredSettings.getIntParams()[1];
        int length = wiredSettings.getFurniIds().length;
        for (int i = 0; i < length; i++) {
            this.items.add(room.getHabboItem(wiredSettings.getFurniIds()[i]));
        }
        setDelay(wiredSettings.getDelay());
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect
    public WiredEffectType getType() {
        return type;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public boolean execute(RoomUnit roomUnit, Room room, Object[] objArr) {
        HabboItem habboItem;
        ArrayList arrayList = new ArrayList();
        for (HabboItem habboItem2 : this.items) {
            if (Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId()).getHabboItem(habboItem2.getId()) == null) {
                arrayList.add(habboItem2);
            }
        }
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            this.items.remove((HabboItem) it.next());
        }
        if (this.items.isEmpty()) {
            return false;
        }
        if (objArr == null || objArr.length <= 0) {
            return true;
        }
        for (Object obj : objArr) {
            if ((obj instanceof HabboItem) && (habboItem = this.items.get(Emulator.getRandom().nextInt(this.items.size()))) != null) {
                int iIntValue = 0;
                if (this.indexOffset.containsKey(Integer.valueOf(habboItem.getId()))) {
                    iIntValue = this.indexOffset.get(Integer.valueOf(habboItem.getId())).intValue() + this.spacing;
                } else {
                    this.indexOffset.put(Integer.valueOf(habboItem.getId()), 0);
                }
                RoomTile tile = room.getLayout().getTile(habboItem.getX(), habboItem.getY());
                if (tile != null) {
                    THashSet<RoomTile> tilesAt = room.getLayout().getTilesAt(room.getLayout().getTile(((HabboItem) obj).getX(), ((HabboItem) obj).getY()), ((HabboItem) obj).getBaseItem().getWidth(), ((HabboItem) obj).getBaseItem().getLength(), ((HabboItem) obj).getRotation());
                    RoomTile tileInFront = room.getLayout().getTileInFront(tile, this.direction, iIntValue);
                    if (tileInFront == null || !tileInFront.getAllowStack()) {
                        iIntValue = 0;
                        tileInFront = room.getLayout().getTileInFront(tile, this.direction, 0);
                    }
                    room.sendComposer(new FloorItemOnRollerComposer((HabboItem) obj, null, tileInFront, tileInFront.getStackHeight() - ((HabboItem) obj).getZ(), room).compose());
                    tilesAt.addAll(room.getLayout().getTilesAt(room.getLayout().getTile(((HabboItem) obj).getX(), ((HabboItem) obj).getY()), ((HabboItem) obj).getBaseItem().getWidth(), ((HabboItem) obj).getBaseItem().getLength(), ((HabboItem) obj).getRotation()));
                    room.updateTiles(tilesAt);
                    this.indexOffset.put(Integer.valueOf(habboItem.getId()), Integer.valueOf(iIntValue));
                }
            }
        }
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public String getWiredData() {
        THashSet tHashSet = new THashSet();
        for (HabboItem habboItem : this.items) {
            if (habboItem.getRoomId() != getRoomId() || Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId()).getHabboItem(habboItem.getId()) == null) {
                tHashSet.add(habboItem);
            }
        }
        TObjectHashIterator it = tHashSet.iterator();
        while (it.hasNext()) {
            this.items.remove((HabboItem) it.next());
        }
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.direction, this.spacing, getDelay(), (List) this.items.stream().map((v0) -> {
            return v0.getId();
        }).collect(Collectors.toList())));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void serializeWiredData(ServerMessage serverMessage, Room room) {
        THashSet tHashSet = new THashSet();
        for (HabboItem habboItem : this.items) {
            if (habboItem.getRoomId() != getRoomId() || Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId()).getHabboItem(habboItem.getId()) == null) {
                tHashSet.add(habboItem);
            }
        }
        TObjectHashIterator it = tHashSet.iterator();
        while (it.hasNext()) {
            this.items.remove((HabboItem) it.next());
        }
        serverMessage.appendBoolean(false);
        serverMessage.appendInt(Integer.valueOf(WiredHandler.MAXIMUM_FURNI_SELECTION));
        serverMessage.appendInt(Integer.valueOf(this.items.size()));
        Iterator<HabboItem> it2 = this.items.iterator();
        while (it2.hasNext()) {
            serverMessage.appendInt(Integer.valueOf(it2.next().getId()));
        }
        serverMessage.appendInt(Integer.valueOf(getBaseItem().getSpriteId()));
        serverMessage.appendInt(Integer.valueOf(getId()));
        serverMessage.appendString(Emulator.PREVIEW);
        serverMessage.appendInt((Integer) 2);
        serverMessage.appendInt(Integer.valueOf(this.direction));
        serverMessage.appendInt(Integer.valueOf(this.spacing));
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(getType().code));
        serverMessage.appendInt(Integer.valueOf(getDelay()));
        serverMessage.appendInt((Integer) 0);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void loadWiredData(ResultSet resultSet, Room room) throws SQLException {
        this.items.clear();
        String string = resultSet.getString("wired_data");
        if (string.startsWith("{")) {
            JsonData jsonData = (JsonData) WiredHandler.getGsonBuilder().create().fromJson(string, JsonData.class);
            this.direction = jsonData.direction;
            this.spacing = jsonData.spacing;
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
        if (strArrSplit.length == 4) {
            try {
                this.direction = Integer.parseInt(strArrSplit[0]);
                this.spacing = Integer.parseInt(strArrSplit[1]);
                setDelay(Integer.parseInt(strArrSplit[2]));
            } catch (Exception e) {
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
        setDelay(0);
        this.items.clear();
        this.direction = 0;
        this.spacing = 0;
        this.indexOffset.clear();
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    protected long requiredCooldown() {
        return 495L;
    }
}
