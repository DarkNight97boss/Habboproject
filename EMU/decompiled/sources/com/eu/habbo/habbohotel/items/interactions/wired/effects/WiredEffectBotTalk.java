package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.wired.WiredSaveException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectBotTalk.class */
public class WiredEffectBotTalk extends InteractionWiredEffect {
    public static final WiredEffectType type = WiredEffectType.BOT_TALK;
    private int mode;
    private String botName;
    private String message;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectBotTalk$JsonData.class */
    static class JsonData {
        String bot_name;
        int mode;
        String message;
        int delay;

        public JsonData(String str, int i, String str2, int i2) {
            this.bot_name = str;
            this.mode = i;
            this.message = str2;
            this.delay = i2;
        }
    }

    public WiredEffectBotTalk(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.botName = Emulator.PREVIEW;
        this.message = Emulator.PREVIEW;
    }

    public WiredEffectBotTalk(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.botName = Emulator.PREVIEW;
        this.message = Emulator.PREVIEW;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void serializeWiredData(ServerMessage serverMessage, Room room) {
        serverMessage.appendBoolean(false);
        serverMessage.appendInt((Integer) 5);
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(getBaseItem().getSpriteId()));
        serverMessage.appendInt(Integer.valueOf(getId()));
        serverMessage.appendString(this.botName + Emulator.PREVIEW + '\t' + Emulator.PREVIEW + this.message);
        serverMessage.appendInt((Integer) 1);
        serverMessage.appendInt(Integer.valueOf(this.mode));
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(getType().code));
        serverMessage.appendInt(Integer.valueOf(getDelay()));
        serverMessage.appendInt((Integer) 0);
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
        String stringParam = wiredSettings.getStringParam();
        if (!stringParam.contains("\t")) {
            throw new WiredSaveException("Malformed data string");
        }
        String[] strArrSplit = stringParam.split(Pattern.quote("\t"));
        if (strArrSplit.length != 2) {
            throw new WiredSaveException("Malformed data string. Invalid data length");
        }
        int delay = wiredSettings.getDelay();
        if (delay > Emulator.getConfig().getInt("hotel.wired.max_delay", 20)) {
            throw new WiredSaveException("Delay too long");
        }
        setDelay(delay);
        this.botName = strArrSplit[0].substring(0, Math.min(strArrSplit[0].length(), Emulator.getConfig().getInt("hotel.wired.message.max_length", 100)));
        this.message = strArrSplit[1].substring(0, Math.min(strArrSplit[1].length(), Emulator.getConfig().getInt("hotel.wired.message.max_length", 100)));
        this.mode = i;
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect
    public WiredEffectType getType() {
        return type;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public boolean execute(RoomUnit roomUnit, Room room, Object[] objArr) {
        String strReplace = this.message;
        Habbo habbo = room.getHabbo(roomUnit);
        if (habbo != null) {
            strReplace = strReplace.replace(Emulator.getTexts().getValue("wired.variable.username", "%username%"), habbo.getHabboInfo().getUsername()).replace(Emulator.getTexts().getValue("wired.variable.credits", "%credits%"), habbo.getHabboInfo().getCredits() + Emulator.PREVIEW).replace(Emulator.getTexts().getValue("wired.variable.pixels", "%pixels%"), habbo.getHabboInfo().getPixels() + Emulator.PREVIEW).replace(Emulator.getTexts().getValue("wired.variable.points", "%points%"), habbo.getHabboInfo().getCurrencyAmount(Emulator.getConfig().getInt("seasonal.primary.type")) + Emulator.PREVIEW).replace(Emulator.getTexts().getValue("wired.variable.owner", "%owner%"), room.getOwnerName()).replace(Emulator.getTexts().getValue("wired.variable.item_count", "%item_count%"), room.itemCount() + Emulator.PREVIEW).replace(Emulator.getTexts().getValue("wired.variable.name", "%name%"), this.botName).replace(Emulator.getTexts().getValue("wired.variable.roomname", "%roomname%"), room.getName()).replace(Emulator.getTexts().getValue("wired.variable.user_count", "%user_count%"), room.getUserCount() + Emulator.PREVIEW);
        }
        List<Bot> bots = room.getBots(this.botName);
        if (bots.size() != 1) {
            return true;
        }
        Bot bot = bots.get(0);
        if (WiredHandler.handle(WiredTriggerType.SAY_SOMETHING, bot.getRoomUnit(), room, new Object[]{strReplace})) {
            return true;
        }
        if (this.mode == 1) {
            bot.shout(strReplace);
            return true;
        }
        bot.talk(strReplace);
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.botName, this.mode, this.message, getDelay()));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void loadWiredData(ResultSet resultSet, Room room) throws SQLException {
        String string = resultSet.getString("wired_data");
        if (string.startsWith("{")) {
            JsonData jsonData = (JsonData) WiredHandler.getGsonBuilder().create().fromJson(string, JsonData.class);
            setDelay(jsonData.delay);
            this.mode = jsonData.mode;
            this.botName = jsonData.bot_name;
            this.message = jsonData.message;
            return;
        }
        String[] strArrSplit = string.split("\t");
        if (strArrSplit.length == 4) {
            setDelay(Integer.valueOf(strArrSplit[0]).intValue());
            this.mode = strArrSplit[1].equalsIgnoreCase("1") ? 1 : 0;
            this.botName = strArrSplit[2];
            this.message = strArrSplit[3];
        }
        needsUpdate(true);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void onPickUp() {
        this.mode = 0;
        this.botName = Emulator.PREVIEW;
        this.message = Emulator.PREVIEW;
        setDelay(0);
    }

    public int getMode() {
        return this.mode;
    }

    public void setMode(int i) {
        this.mode = i;
    }

    public String getBotName() {
        return this.botName;
    }

    public void setBotName(String str) {
        this.botName = str;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String str) {
        this.message = str;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    protected long requiredCooldown() {
        return 500L;
    }
}
