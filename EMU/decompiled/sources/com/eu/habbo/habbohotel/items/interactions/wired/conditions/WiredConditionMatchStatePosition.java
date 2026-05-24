package com.eu.habbo.habbohotel.items.interactions.wired.conditions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.items.interactions.wired.interfaces.InteractionWiredMatchFurniSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredConditionType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredMatchFurniSetting;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/conditions/WiredConditionMatchStatePosition.class */
public class WiredConditionMatchStatePosition extends InteractionWiredCondition implements InteractionWiredMatchFurniSettings {
    public static final WiredConditionType type = WiredConditionType.MATCH_SSHOT;
    private THashSet<WiredMatchFurniSetting> settings;
    private boolean state;
    private boolean position;
    private boolean direction;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/conditions/WiredConditionMatchStatePosition$JsonData.class */
    static class JsonData {
        boolean state;
        boolean position;
        boolean direction;
        List<WiredMatchFurniSetting> settings;

        public JsonData(boolean z, boolean z2, boolean z3, List<WiredMatchFurniSetting> list) {
            this.state = z;
            this.position = z2;
            this.direction = z3;
            this.settings = list;
        }
    }

    public WiredConditionMatchStatePosition(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.settings = new THashSet<>();
    }

    public WiredConditionMatchStatePosition(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.settings = new THashSet<>();
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition
    public WiredConditionType getType() {
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
        serverMessage.appendInt((Integer) 4);
        serverMessage.appendInt(Integer.valueOf(this.state ? 1 : 0));
        serverMessage.appendInt(Integer.valueOf(this.direction ? 1 : 0));
        serverMessage.appendInt(Integer.valueOf(this.position ? 1 : 0));
        serverMessage.appendInt((Integer) 10);
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(getType().code));
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt((Integer) 0);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition
    public boolean saveData(WiredSettings wiredSettings) {
        if (wiredSettings.getIntParams().length < 3) {
            return false;
        }
        this.state = wiredSettings.getIntParams()[0] == 1;
        this.direction = wiredSettings.getIntParams()[1] == 1;
        this.position = wiredSettings.getIntParams()[2] == 1;
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId());
        if (room == null) {
            return true;
        }
        int length = wiredSettings.getFurniIds().length;
        if (length > Emulator.getConfig().getInt("hotel.wired.furni.selection.count")) {
            return false;
        }
        this.settings.clear();
        for (int i = 0; i < length; i++) {
            HabboItem habboItem = room.getHabboItem(wiredSettings.getFurniIds()[i]);
            if (habboItem != null) {
                this.settings.add(new WiredMatchFurniSetting(habboItem.getId(), habboItem.getExtradata(), habboItem.getRotation(), habboItem.getX(), habboItem.getY()));
            }
        }
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public boolean execute(RoomUnit roomUnit, Room room, Object[] objArr) {
        if (this.settings.isEmpty()) {
            return true;
        }
        THashSet tHashSet = new THashSet();
        TObjectHashIterator it = this.settings.iterator();
        while (it.hasNext()) {
            WiredMatchFurniSetting wiredMatchFurniSetting = (WiredMatchFurniSetting) it.next();
            HabboItem habboItem = room.getHabboItem(wiredMatchFurniSetting.item_id);
            if (habboItem == null) {
                tHashSet.add(wiredMatchFurniSetting);
            } else {
                if (this.state && !habboItem.getExtradata().equals(wiredMatchFurniSetting.state)) {
                    return false;
                }
                if (this.position && (wiredMatchFurniSetting.x != habboItem.getX() || wiredMatchFurniSetting.y != habboItem.getY())) {
                    return false;
                }
                if (this.direction && wiredMatchFurniSetting.rotation != habboItem.getRotation()) {
                    return false;
                }
            }
        }
        if (tHashSet.isEmpty()) {
            return true;
        }
        TObjectHashIterator it2 = tHashSet.iterator();
        while (it2.hasNext()) {
            this.settings.remove((WiredMatchFurniSetting) it2.next());
        }
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.state, this.position, this.direction, new ArrayList((Collection) this.settings)));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void loadWiredData(ResultSet resultSet, Room room) throws SQLException {
        String string = resultSet.getString("wired_data");
        if (string.startsWith("{")) {
            JsonData jsonData = (JsonData) WiredHandler.getGsonBuilder().create().fromJson(string, JsonData.class);
            this.state = jsonData.state;
            this.position = jsonData.position;
            this.direction = jsonData.direction;
            this.settings.addAll(jsonData.settings);
            return;
        }
        String[] strArrSplit = string.split(":");
        int i = Integer.parseInt(strArrSplit[0]);
        String[] strArrSplit2 = strArrSplit[1].split(";");
        for (int i2 = 0; i2 < i; i2++) {
            String[] strArrSplit3 = strArrSplit2[i2].split("-");
            if (strArrSplit3.length >= 5) {
                this.settings.add(new WiredMatchFurniSetting(Integer.parseInt(strArrSplit3[0]), strArrSplit3[1], Integer.parseInt(strArrSplit3[2]), Integer.parseInt(strArrSplit3[3]), Integer.parseInt(strArrSplit3[4])));
            }
        }
        this.state = strArrSplit[2].equals("1");
        this.direction = strArrSplit[3].equals("1");
        this.position = strArrSplit[4].equals("1");
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void onPickUp() {
        this.settings.clear();
        this.direction = false;
        this.position = false;
        this.state = false;
    }

    private void refresh() {
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId());
        if (room != null) {
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
