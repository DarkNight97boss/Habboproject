package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.items.interactions.wired.interfaces.InteractionWiredMatchFurniSettings;
import com.eu.habbo.habbohotel.rooms.FurnitureMovementError;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomTileState;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredMatchFurniSetting;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.wired.WiredSaveException;
import com.eu.habbo.messages.outgoing.rooms.items.FloorItemOnRollerComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectMatchFurni.class */
public class WiredEffectMatchFurni extends InteractionWiredEffect implements InteractionWiredMatchFurniSettings {
    private static final Logger LOGGER = LoggerFactory.getLogger(WiredEffectMatchFurni.class);
    private static final WiredEffectType type = WiredEffectType.MATCH_SSHOT;
    public boolean checkForWiredResetPermission;
    private THashSet<WiredMatchFurniSetting> settings;
    private boolean state;
    private boolean direction;
    private boolean position;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectMatchFurni$JsonData.class */
    static class JsonData {
        boolean state;
        boolean direction;
        boolean position;
        List<WiredMatchFurniSetting> items;
        int delay;

        public JsonData(boolean z, boolean z2, boolean z3, List<WiredMatchFurniSetting> list, int i) {
            this.state = z;
            this.direction = z2;
            this.position = z3;
            this.items = list;
            this.delay = i;
        }
    }

    public WiredEffectMatchFurni(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.checkForWiredResetPermission = true;
        this.state = false;
        this.direction = false;
        this.position = false;
        this.settings = new THashSet<>(0);
    }

    public WiredEffectMatchFurni(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.checkForWiredResetPermission = true;
        this.state = false;
        this.direction = false;
        this.position = false;
        this.settings = new THashSet<>(0);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public boolean execute(RoomUnit roomUnit, Room room, Object[] objArr) {
        if (this.settings.isEmpty()) {
            return true;
        }
        TObjectHashIterator it = this.settings.iterator();
        while (it.hasNext()) {
            WiredMatchFurniSetting wiredMatchFurniSetting = (WiredMatchFurniSetting) it.next();
            HabboItem habboItem = room.getHabboItem(wiredMatchFurniSetting.item_id);
            if (habboItem != null) {
                if (this.state && this.checkForWiredResetPermission && habboItem.allowWiredResetState() && !wiredMatchFurniSetting.state.equals(" ") && !habboItem.getExtradata().equals(wiredMatchFurniSetting.state)) {
                    habboItem.setExtradata(wiredMatchFurniSetting.state);
                    room.updateItemState(habboItem);
                }
                RoomTile tile = room.getLayout().getTile(habboItem.getX(), habboItem.getY());
                double z = habboItem.getZ();
                if (!this.direction || this.position) {
                    if (this.position) {
                        boolean z2 = !this.direction || habboItem.getRotation() == wiredMatchFurniSetting.rotation;
                        RoomTile tile2 = room.getLayout().getTile((short) wiredMatchFurniSetting.x, (short) wiredMatchFurniSetting.y);
                        int rotation = this.direction ? wiredMatchFurniSetting.rotation : habboItem.getRotation();
                        if (tile2 != null && tile2.state != RoomTileState.INVALID && (tile2 != tile || rotation != habboItem.getRotation())) {
                            if (room.furnitureFitsAt(tile2, habboItem, rotation, true) == FurnitureMovementError.NONE) {
                                if (room.moveFurniTo(habboItem, tile2, rotation, null, !z2) == FurnitureMovementError.NONE && z2) {
                                    room.sendComposer(new FloorItemOnRollerComposer(habboItem, null, tile, z, tile2, habboItem.getZ(), 0.0d, room).compose());
                                }
                            }
                        }
                    }
                } else if (habboItem.getRotation() != wiredMatchFurniSetting.rotation && room.furnitureFitsAt(tile, habboItem, wiredMatchFurniSetting.rotation, false) == FurnitureMovementError.NONE) {
                    room.moveFurniTo(habboItem, tile, wiredMatchFurniSetting.rotation, null, true);
                }
            }
        }
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public String getWiredData() {
        refresh();
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.state, this.direction, this.position, new ArrayList((Collection) this.settings), getDelay()));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void loadWiredData(ResultSet resultSet, Room room) throws SQLException {
        String string = resultSet.getString("wired_data");
        if (string.startsWith("{")) {
            JsonData jsonData = (JsonData) WiredHandler.getGsonBuilder().create().fromJson(string, JsonData.class);
            setDelay(jsonData.delay);
            this.state = jsonData.state;
            this.direction = jsonData.direction;
            this.position = jsonData.position;
            this.settings.clear();
            this.settings.addAll(jsonData.items);
            return;
        }
        String[] strArrSplit = resultSet.getString("wired_data").split(":");
        Integer.parseInt(strArrSplit[0]);
        for (String str : strArrSplit[1].split(Pattern.quote(";"))) {
            try {
                String[] strArrSplit2 = str.split(Pattern.quote("-"));
                if (strArrSplit2.length >= 5) {
                    this.settings.add(new WiredMatchFurniSetting(Integer.parseInt(strArrSplit2[0]), strArrSplit2[1], Integer.parseInt(strArrSplit2[2]), Integer.parseInt(strArrSplit2[3]), Integer.parseInt(strArrSplit2[4])));
                }
            } catch (Exception e) {
                LOGGER.error("Caught exception", e);
            }
        }
        this.state = strArrSplit[2].equals("1");
        this.direction = strArrSplit[3].equals("1");
        this.position = strArrSplit[4].equals("1");
        setDelay(Integer.parseInt(strArrSplit[5]));
        needsUpdate(true);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void onPickUp() {
        this.settings.clear();
        this.state = false;
        this.direction = false;
        this.position = false;
        setDelay(0);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect
    public WiredEffectType getType() {
        return type;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void serializeWiredData(ServerMessage serverMessage, Room room) {
        refresh();
        serverMessage.appendBoolean(false);
        serverMessage.appendInt(Integer.valueOf(WiredHandler.MAXIMUM_FURNI_SELECTION));
        serverMessage.appendInt(Integer.valueOf(this.settings.size()));
        TObjectHashIterator it = this.settings.iterator();
        while (it.hasNext()) {
            serverMessage.appendInt(Integer.valueOf(((WiredMatchFurniSetting) it.next()).item_id));
        }
        serverMessage.appendInt(Integer.valueOf(getBaseItem().getSpriteId()));
        serverMessage.appendInt(Integer.valueOf(getId()));
        serverMessage.appendString(Emulator.PREVIEW);
        serverMessage.appendInt((Integer) 3);
        serverMessage.appendInt(Integer.valueOf(this.state ? 1 : 0));
        serverMessage.appendInt(Integer.valueOf(this.direction ? 1 : 0));
        serverMessage.appendInt(Integer.valueOf(this.position ? 1 : 0));
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(getType().code));
        serverMessage.appendInt(Integer.valueOf(getDelay()));
        serverMessage.appendInt((Integer) 0);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect
    public boolean saveData(WiredSettings wiredSettings, GameClient gameClient) throws WiredSaveException {
        if (wiredSettings.getIntParams().length < 3) {
            throw new WiredSaveException("Invalid data");
        }
        boolean z = wiredSettings.getIntParams()[0] == 1;
        boolean z2 = wiredSettings.getIntParams()[1] == 1;
        boolean z3 = wiredSettings.getIntParams()[2] == 1;
        if (Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId()) == null) {
            throw new WiredSaveException("Trying to save wired in unloaded room");
        }
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
            arrayList.add(new WiredMatchFurniSetting(habboItem.getId(), (this.checkForWiredResetPermission && habboItem.allowWiredResetState()) ? habboItem.getExtradata() : " ", habboItem.getRotation(), habboItem.getX(), habboItem.getY()));
        }
        int delay = wiredSettings.getDelay();
        if (delay > Emulator.getConfig().getInt("hotel.wired.max_delay", 20)) {
            throw new WiredSaveException("Delay too long");
        }
        this.state = z;
        this.direction = z2;
        this.position = z3;
        this.settings.clear();
        this.settings.addAll(arrayList);
        setDelay(delay);
        return true;
    }

    private void refresh() {
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId());
        if (room == null || !room.isLoaded()) {
            return;
        }
        THashSet tHashSet = new THashSet();
        TObjectHashIterator it = this.settings.iterator();
        while (it.hasNext()) {
            WiredMatchFurniSetting wiredMatchFurniSetting = (WiredMatchFurniSetting) it.next();
            if (room.getHabboItem(wiredMatchFurniSetting.item_id) == null) {
                tHashSet.add(wiredMatchFurniSetting);
            }
        }
        TObjectHashIterator it2 = tHashSet.iterator();
        while (it2.hasNext()) {
            this.settings.remove((WiredMatchFurniSetting) it2.next());
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.wired.interfaces.InteractionWiredMatchFurniSettings
    public THashSet<WiredMatchFurniSetting> getMatchFurniSettings() {
        return this.settings;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.wired.interfaces.InteractionWiredMatchFurniSettings
    public boolean shouldMatchState() {
        return this.state;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.wired.interfaces.InteractionWiredMatchFurniSettings
    public boolean shouldMatchRotation() {
        return this.direction;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.wired.interfaces.InteractionWiredMatchFurniSettings
    public boolean shouldMatchPosition() {
        return this.position;
    }
}
