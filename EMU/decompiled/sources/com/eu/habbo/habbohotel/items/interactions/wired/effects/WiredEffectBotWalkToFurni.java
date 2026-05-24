package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.wired.WiredSaveException;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectBotWalkToFurni.class */
public class WiredEffectBotWalkToFurni extends InteractionWiredEffect {
    public static final WiredEffectType type = WiredEffectType.BOT_MOVE;
    private List<HabboItem> items;
    private String botName;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectBotWalkToFurni$JsonData.class */
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

    public WiredEffectBotWalkToFurni(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.botName = Emulator.PREVIEW;
        this.items = new ArrayList();
    }

    public WiredEffectBotWalkToFurni(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.botName = Emulator.PREVIEW;
        this.items = new ArrayList();
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
        List<Bot> bots = room.getBots(this.botName);
        if (this.items.isEmpty() || bots.size() != 1) {
            return true;
        }
        Bot bot = bots.get(0);
        this.items.removeIf(habboItem -> {
            return habboItem == null || habboItem.getRoomId() != getRoomId() || Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId()).getHabboItem(habboItem.getId()) == null;
        });
        List list = (List) this.items.stream().filter(habboItem2 -> {
            return !room.getBotsOnItem(habboItem2).contains(bot);
        }).collect(Collectors.toList());
        if (list.size() <= 0) {
            return true;
        }
        HabboItem habboItem3 = (HabboItem) list.get(Emulator.getRandom().nextInt(list.size()));
        if (habboItem3.getRoomId() == 0 || habboItem3.getRoomId() != bot.getRoom().getId()) {
            return true;
        }
        bot.getRoomUnit().setGoalLocation(room.getLayout().getTile(habboItem3.getX(), habboItem3.getY()));
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public String getWiredData() {
        ArrayList arrayList = new ArrayList();
        if (this.items != null) {
            for (HabboItem habboItem : this.items) {
                if (habboItem.getRoomId() != 0) {
                    arrayList.add(Integer.valueOf(habboItem.getId()));
                }
            }
        }
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.botName, arrayList, getDelay()));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void loadWiredData(ResultSet resultSet, Room room) throws SQLException {
        this.items = new ArrayList();
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
        this.items.clear();
        this.botName = Emulator.PREVIEW;
        setDelay(0);
    }
}
