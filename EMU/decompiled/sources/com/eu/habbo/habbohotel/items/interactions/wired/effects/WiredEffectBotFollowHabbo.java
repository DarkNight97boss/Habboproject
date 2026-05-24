package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.wired.WiredSaveException;
import gnu.trove.procedure.TObjectProcedure;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectBotFollowHabbo.class */
public class WiredEffectBotFollowHabbo extends InteractionWiredEffect {
    public static final WiredEffectType type = WiredEffectType.BOT_FOLLOW_AVATAR;
    private String botName;
    private int mode;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectBotFollowHabbo$JsonData.class */
    static class JsonData {
        String bot_name;
        int mode;
        int delay;

        public JsonData(String str, int i, int i2) {
            this.bot_name = str;
            this.mode = i;
            this.delay = i2;
        }
    }

    public WiredEffectBotFollowHabbo(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.botName = Emulator.PREVIEW;
        this.mode = 0;
    }

    public WiredEffectBotFollowHabbo(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.botName = Emulator.PREVIEW;
        this.mode = 0;
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
        serverMessage.appendInt(Integer.valueOf(this.mode));
        serverMessage.appendInt((Integer) 1);
        serverMessage.appendInt(Integer.valueOf(getType().code));
        serverMessage.appendInt(Integer.valueOf(getDelay()));
        if (!requiresTriggeringUser()) {
            serverMessage.appendInt((Integer) 0);
            return;
        }
        final ArrayList arrayList = new ArrayList();
        room.getRoomSpecialTypes().getTriggers(getX(), getY()).forEach(new TObjectProcedure<InteractionWiredTrigger>() { // from class: com.eu.habbo.habbohotel.items.interactions.wired.effects.WiredEffectBotFollowHabbo.1
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
            throw new WiredSaveException("Mode is invalid");
        }
        int i = wiredSettings.getIntParams()[0];
        if (i != 0 && i != 1) {
            throw new WiredSaveException("Mode is invalid");
        }
        String strReplace = wiredSettings.getStringParam().replace("\t", Emulator.PREVIEW);
        String strSubstring = strReplace.substring(0, Math.min(strReplace.length(), Emulator.getConfig().getInt("hotel.wired.message.max_length", 100)));
        int delay = wiredSettings.getDelay();
        if (delay > Emulator.getConfig().getInt("hotel.wired.max_delay", 20)) {
            throw new WiredSaveException("Delay too long");
        }
        this.botName = strSubstring;
        this.mode = i;
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
        if (this.mode == 1) {
            bot.startFollowingHabbo(habbo);
            return true;
        }
        bot.stopFollowingHabbo();
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.botName, this.mode, getDelay()));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void loadWiredData(ResultSet resultSet, Room room) throws SQLException {
        String string = resultSet.getString("wired_data");
        if (string.startsWith("{")) {
            JsonData jsonData = (JsonData) WiredHandler.getGsonBuilder().create().fromJson(string, JsonData.class);
            setDelay(jsonData.delay);
            this.mode = jsonData.mode;
            this.botName = jsonData.bot_name;
            return;
        }
        String[] strArrSplit = string.split("\t");
        if (strArrSplit.length == 3) {
            setDelay(Integer.valueOf(strArrSplit[0]).intValue());
            this.mode = strArrSplit[1].equalsIgnoreCase("1") ? 1 : 0;
            this.botName = strArrSplit[2];
        }
        needsUpdate(true);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void onPickUp() {
        this.botName = Emulator.PREVIEW;
        this.mode = 0;
        setDelay(0);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect
    public boolean requiresTriggeringUser() {
        return true;
    }
}
