package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.wired.WiredSaveException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectBotClothes.class */
public class WiredEffectBotClothes extends InteractionWiredEffect {
    public static final WiredEffectType type = WiredEffectType.BOT_CLOTHES;
    private String botName;
    private String botLook;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectBotClothes$JsonData.class */
    static class JsonData {
        String bot_name;
        String look;
        int delay;

        public JsonData(String str, String str2, int i) {
            this.bot_name = str;
            this.look = str2;
            this.delay = i;
        }
    }

    public WiredEffectBotClothes(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.botName = Emulator.PREVIEW;
        this.botLook = Emulator.PREVIEW;
    }

    public WiredEffectBotClothes(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.botName = Emulator.PREVIEW;
        this.botLook = Emulator.PREVIEW;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void serializeWiredData(ServerMessage serverMessage, Room room) {
        serverMessage.appendBoolean(false);
        serverMessage.appendInt((Integer) 5);
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(getBaseItem().getSpriteId()));
        serverMessage.appendInt(Integer.valueOf(getId()));
        serverMessage.appendString(this.botName + '\t' + Emulator.PREVIEW + this.botLook);
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(getType().code));
        serverMessage.appendInt(Integer.valueOf(getDelay()));
        serverMessage.appendInt((Integer) 0);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect
    public boolean saveData(WiredSettings wiredSettings, GameClient gameClient) throws WiredSaveException {
        String stringParam = wiredSettings.getStringParam();
        int delay = wiredSettings.getDelay();
        if (delay > Emulator.getConfig().getInt("hotel.wired.max_delay", 20)) {
            throw new WiredSaveException("Delay too long");
        }
        if (!stringParam.contains("\t")) {
            throw new WiredSaveException("Malformed data string");
        }
        String[] strArrSplit = stringParam.split(Pattern.quote("\t"));
        if (strArrSplit.length != 2) {
            throw new WiredSaveException("Malformed data string. Invalid data length");
        }
        this.botName = strArrSplit[0].substring(0, Math.min(strArrSplit[0].length(), Emulator.getConfig().getInt("hotel.wired.message.max_length", 100)));
        this.botLook = strArrSplit[1];
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
        if (bots.size() != 1) {
            return true;
        }
        bots.get(0).setFigure(this.botLook);
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.botName, this.botLook, getDelay()));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void loadWiredData(ResultSet resultSet, Room room) throws SQLException {
        String string = resultSet.getString("wired_data");
        if (string.startsWith("{")) {
            JsonData jsonData = (JsonData) WiredHandler.getGsonBuilder().create().fromJson(string, JsonData.class);
            setDelay(jsonData.delay);
            this.botName = jsonData.bot_name;
            this.botLook = jsonData.look;
            return;
        }
        String[] strArrSplit = string.split("\t");
        if (strArrSplit.length >= 3) {
            setDelay(Integer.valueOf(strArrSplit[0]).intValue());
            this.botName = strArrSplit[1];
            this.botLook = strArrSplit[2];
        }
        needsUpdate(true);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void onPickUp() {
        this.botLook = Emulator.PREVIEW;
        this.botName = Emulator.PREVIEW;
        setDelay(0);
    }

    public String getBotName() {
        return this.botName;
    }

    public void setBotName(String str) {
        this.botName = str;
    }

    public String getBotLook() {
        return this.botLook;
    }

    public void setBotLook(String str) {
        this.botLook = str;
    }
}
