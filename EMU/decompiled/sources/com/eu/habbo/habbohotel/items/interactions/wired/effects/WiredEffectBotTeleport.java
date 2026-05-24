package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomTileState;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.wired.WiredSaveException;
import com.eu.habbo.messages.outgoing.Outgoing;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserEffectComposer;
import com.eu.habbo.threading.runnables.RoomUnitTeleport;
import com.eu.habbo.threading.runnables.SendRoomUnitEffectComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectBotTeleport.class */
public class WiredEffectBotTeleport extends InteractionWiredEffect {
    public static final WiredEffectType type = WiredEffectType.BOT_TELEPORT;
    private THashSet<HabboItem> items;
    private String botName;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectBotTeleport$JsonData.class */
    static class JsonData {
        String bot_name;
        List<Integer> items;
        int delay;

        public JsonData(String str, List<Integer> list, int i) {
            this.bot_name = str;
            this.items = list;
            this.delay = i;
        }
    }

    public WiredEffectBotTeleport(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.botName = Emulator.PREVIEW;
        this.items = new THashSet<>();
    }

    public WiredEffectBotTeleport(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.botName = Emulator.PREVIEW;
        this.items = new THashSet<>();
    }

    public static void teleportUnitToTile(RoomUnit roomUnit, RoomTile roomTile) {
        Room room;
        if (roomUnit == null || roomTile == null || roomUnit.isWiredTeleporting || (room = roomUnit.getRoom()) == null) {
            return;
        }
        roomUnit.getRoom().unIdle(roomUnit.getRoom().getHabbo(roomUnit));
        room.sendComposer(new RoomUserEffectComposer(roomUnit, 4).compose());
        Emulator.getThreading().run(new SendRoomUnitEffectComposer(room, roomUnit), WiredHandler.TELEPORT_DELAY + Outgoing.CraftableProductsComposer);
        if (roomTile == roomUnit.getCurrentLocation()) {
            return;
        }
        if (roomTile.state == RoomTileState.INVALID || roomTile.state == RoomTileState.BLOCKED) {
            RoomTile roomTile2 = null;
            List<RoomTile> tilesAround = room.getLayout().getTilesAround(roomTile);
            Collections.reverse(tilesAround);
            Iterator<RoomTile> it = tilesAround.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                RoomTile next = it.next();
                if (next.state != RoomTileState.INVALID && next.state != RoomTileState.BLOCKED) {
                    roomTile2 = next;
                    break;
                }
            }
            if (roomTile2 != null) {
                roomTile = roomTile2;
            }
        }
        Emulator.getThreading().run(() -> {
            roomUnit.isWiredTeleporting = true;
        }, Math.max(0, WiredHandler.TELEPORT_DELAY - 500));
        Emulator.getThreading().run(new RoomUnitTeleport(roomUnit, room, roomTile.x, roomTile.y, roomTile.getStackHeight() + (roomTile.state == RoomTileState.SIT ? -0.5d : 0.0d), roomUnit.getEffectId()), WiredHandler.TELEPORT_DELAY);
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
        serverMessage.appendString(this.botName);
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(getType().code));
        serverMessage.appendInt(Integer.valueOf(getDelay()));
        serverMessage.appendInt((Integer) 0);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect
    public boolean saveData(WiredSettings wiredSettings, GameClient gameClient) throws WiredSaveException {
        String stringParam = wiredSettings.getStringParam();
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
        this.botName = stringParam.substring(0, Math.min(stringParam.length(), Emulator.getConfig().getInt("hotel.wired.message.max_length", 100)));
        setDelay(delay);
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect
    public WiredEffectType getType() {
        return type;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public boolean execute(RoomUnit roomUnit, Room room, Object[] objArr) {
        if (this.items.isEmpty()) {
            return false;
        }
        List<Bot> bots = room.getBots(this.botName);
        if (bots.size() != 1) {
            return false;
        }
        Bot bot = bots.get(0);
        int iNextInt = Emulator.getRandom().nextInt(this.items.size()) + 1;
        int i = 1;
        TObjectHashIterator it = this.items.iterator();
        while (it.hasNext()) {
            HabboItem habboItem = (HabboItem) it.next();
            if (habboItem.getRoomId() != 0 && habboItem.getRoomId() == bot.getRoom().getId()) {
                if (iNextInt == i) {
                    teleportUnitToTile(bot.getRoomUnit(), room.getLayout().getTile(habboItem.getX(), habboItem.getY()));
                    return true;
                }
                i++;
            }
        }
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public String getWiredData() {
        ArrayList arrayList = new ArrayList();
        if (this.items != null) {
            TObjectHashIterator it = this.items.iterator();
            while (it.hasNext()) {
                HabboItem habboItem = (HabboItem) it.next();
                if (habboItem.getRoomId() != 0) {
                    arrayList.add(Integer.valueOf(habboItem.getId()));
                }
            }
        }
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.botName, arrayList, getDelay()));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void loadWiredData(ResultSet resultSet, Room room) throws SQLException {
        this.items = new THashSet<>();
        String string = resultSet.getString("wired_data");
        if (string.startsWith("{")) {
            JsonData jsonData = (JsonData) WiredHandler.getGsonBuilder().create().fromJson(string, JsonData.class);
            setDelay(jsonData.delay);
            this.botName = jsonData.bot_name;
            Iterator<Integer> it = jsonData.items.iterator();
            while (it.hasNext()) {
                HabboItem habboItem = room.getHabboItem(it.next().intValue());
                if (habboItem != null) {
                    this.items.add(habboItem);
                }
            }
            return;
        }
        String[] strArrSplit = resultSet.getString("wired_data").split("\t");
        if (strArrSplit.length >= 2) {
            setDelay(Integer.valueOf(strArrSplit[0]).intValue());
            String[] strArrSplit2 = strArrSplit[1].split(";");
            if (strArrSplit2.length > 1) {
                this.botName = strArrSplit2[0];
                for (int i = 1; i < strArrSplit2.length; i++) {
                    HabboItem habboItem2 = room.getHabboItem(Integer.valueOf(strArrSplit2[i]).intValue());
                    if (habboItem2 != null) {
                        this.items.add(habboItem2);
                    }
                }
            }
        }
        needsUpdate(true);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void onPickUp() {
        this.botName = Emulator.PREVIEW;
        this.items.clear();
        setDelay(0);
    }
}
