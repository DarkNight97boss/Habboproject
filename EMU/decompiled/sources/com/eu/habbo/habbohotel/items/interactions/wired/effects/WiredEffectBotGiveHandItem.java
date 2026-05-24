package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.wired.WiredSaveException;
import com.eu.habbo.threading.runnables.RoomUnitGiveHanditem;
import com.eu.habbo.threading.runnables.RoomUnitWalkToLocation;
import gnu.trove.procedure.TObjectProcedure;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectBotGiveHandItem.class */
public class WiredEffectBotGiveHandItem extends InteractionWiredEffect {
    public static final WiredEffectType type = WiredEffectType.BOT_GIVE_HANDITEM;
    private String botName;
    private int itemId;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectBotGiveHandItem$JsonData.class */
    static class JsonData {
        String bot_name;
        int item_id;
        int delay;

        public JsonData(String str, int i, int i2) {
            this.bot_name = str;
            this.item_id = i;
            this.delay = i2;
        }
    }

    public WiredEffectBotGiveHandItem(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.botName = Emulator.PREVIEW;
    }

    public WiredEffectBotGiveHandItem(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.botName = Emulator.PREVIEW;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void serializeWiredData(ServerMessage serverMessage, Room room) {
        serverMessage.appendBoolean(false);
        serverMessage.appendInt((Integer) 5);
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(getBaseItem().getSpriteId()));
        serverMessage.appendInt(Integer.valueOf(getId()));
        serverMessage.appendString(this.botName);
        serverMessage.appendInt((Integer) 1);
        serverMessage.appendInt(Integer.valueOf(this.itemId));
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(getType().code));
        serverMessage.appendInt(Integer.valueOf(getDelay()));
        if (!requiresTriggeringUser()) {
            serverMessage.appendInt((Integer) 0);
            return;
        }
        final ArrayList arrayList = new ArrayList();
        room.getRoomSpecialTypes().getTriggers(getX(), getY()).forEach(new TObjectProcedure<InteractionWiredTrigger>() { // from class: com.eu.habbo.habbohotel.items.interactions.wired.effects.WiredEffectBotGiveHandItem.1
            public boolean execute(InteractionWiredTrigger interactionWiredTrigger) {
                if (interactionWiredTrigger.isTriggeredByRoomUnit()) {
                    return true;
                }
                arrayList.add(Integer.valueOf(interactionWiredTrigger.getBaseItem().getSpriteId()));
                return true;
            }
        });
        serverMessage.appendInt(Integer.valueOf(arrayList.size()));
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            serverMessage.appendInt((Integer) it.next());
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect
    public boolean saveData(WiredSettings wiredSettings, GameClient gameClient) throws WiredSaveException {
        if (wiredSettings.getIntParams().length < 1) {
            throw new WiredSaveException("Missing item id");
        }
        int i = wiredSettings.getIntParams()[0];
        if (i < 0) {
            i = 0;
        }
        String stringParam = wiredSettings.getStringParam();
        int delay = wiredSettings.getDelay();
        if (delay > Emulator.getConfig().getInt("hotel.wired.max_delay", 20)) {
            throw new WiredSaveException("Delay too long");
        }
        this.itemId = i;
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
        Habbo habbo = room.getHabbo(roomUnit);
        List<Bot> bots = room.getBots(this.botName);
        if (habbo == null || bots.size() != 1) {
            return false;
        }
        Bot bot = bots.get(0);
        ArrayList arrayList = new ArrayList();
        arrayList.add(new RoomUnitGiveHanditem(roomUnit, room, this.itemId));
        arrayList.add(new RoomUnitGiveHanditem(bot.getRoomUnit(), room, 0));
        arrayList.add(() -> {
            if (roomUnit.getRoom() == null || roomUnit.getRoom().getId() != room.getId() || roomUnit.getCurrentLocation().distance(bot.getRoomUnit().getCurrentLocation()) >= 2.0d) {
                return;
            }
            WiredHandler.handle(WiredTriggerType.BOT_REACHED_AVTR, bot.getRoomUnit(), room, new Object[0]);
        });
        RoomTile closestAdjacentTile = bot.getRoomUnit().getClosestAdjacentTile(roomUnit.getX(), roomUnit.getY(), true);
        if (closestAdjacentTile != null) {
            bot.getRoomUnit().setGoalLocation(closestAdjacentTile);
        }
        Emulator.getThreading().run(new RoomUnitGiveHanditem(bot.getRoomUnit(), room, this.itemId));
        Emulator.getThreading().run(new RoomUnitWalkToLocation(bot.getRoomUnit(), closestAdjacentTile, room, arrayList, arrayList));
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.botName, this.itemId, getDelay()));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void loadWiredData(ResultSet resultSet, Room room) throws SQLException {
        String string = resultSet.getString("wired_data");
        if (string.startsWith("{")) {
            JsonData jsonData = (JsonData) WiredHandler.getGsonBuilder().create().fromJson(string, JsonData.class);
            setDelay(jsonData.delay);
            this.itemId = jsonData.item_id;
            this.botName = jsonData.bot_name;
            return;
        }
        String[] strArrSplit = string.split("\t");
        if (strArrSplit.length == 3) {
            setDelay(Integer.valueOf(strArrSplit[0]).intValue());
            this.itemId = Integer.valueOf(strArrSplit[1]).intValue();
            this.botName = strArrSplit[2];
        }
        needsUpdate(true);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void onPickUp() {
        this.botName = Emulator.PREVIEW;
        this.itemId = 0;
        setDelay(0);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect
    public boolean requiresTriggeringUser() {
        return true;
    }
}
