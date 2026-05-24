package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredGiveRewardItem;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.wired.WiredSaveException;
import com.eu.habbo.messages.outgoing.generic.alerts.UpdateFailedComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectGiveReward.class */
public class WiredEffectGiveReward extends InteractionWiredEffect {
    public static final int LIMIT_ONCE = 0;
    public static final int LIMIT_N_DAY = 1;
    public static final int LIMIT_N_HOURS = 2;
    public static final int LIMIT_N_MINUTES = 3;
    public static final WiredEffectType type = WiredEffectType.GIVE_REWARD;
    public int limit;
    public int limitationInterval;
    public int given;
    public int rewardTime;
    public boolean uniqueRewards;
    public THashSet<WiredGiveRewardItem> rewardItems;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectGiveReward$JsonData.class */
    static class JsonData {
        int limit;
        int given;
        int reward_time;
        boolean unique_rewards;
        int limit_interval;
        List<WiredGiveRewardItem> rewards;
        int delay;

        public JsonData(int i, int i2, int i3, boolean z, int i4, List<WiredGiveRewardItem> list, int i5) {
            this.limit = i;
            this.given = i2;
            this.reward_time = i3;
            this.unique_rewards = z;
            this.limit_interval = i4;
            this.rewards = list;
            this.delay = i5;
        }
    }

    public WiredEffectGiveReward(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.rewardItems = new THashSet<>();
    }

    public WiredEffectGiveReward(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.rewardItems = new THashSet<>();
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public boolean execute(RoomUnit roomUnit, Room room, Object[] objArr) {
        Habbo habbo = room.getHabbo(roomUnit);
        return habbo != null && WiredHandler.getReward(habbo, this);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.limit, this.given, this.rewardTime, this.uniqueRewards, this.limitationInterval, new ArrayList((Collection) this.rewardItems), getDelay()));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void loadWiredData(ResultSet resultSet, Room room) throws SQLException {
        String string = resultSet.getString("wired_data");
        if (string.startsWith("{")) {
            JsonData jsonData = (JsonData) WiredHandler.getGsonBuilder().create().fromJson(string, JsonData.class);
            setDelay(jsonData.delay);
            this.limit = jsonData.limit;
            this.given = jsonData.given;
            this.rewardTime = jsonData.reward_time;
            this.uniqueRewards = jsonData.unique_rewards;
            this.limitationInterval = jsonData.limit_interval;
            this.rewardItems.clear();
            this.rewardItems.addAll(jsonData.rewards);
            return;
        }
        String[] strArrSplit = string.split(":");
        if (strArrSplit.length > 0) {
            this.limit = Integer.valueOf(strArrSplit[0]).intValue();
            this.given = Integer.valueOf(strArrSplit[1]).intValue();
            this.rewardTime = Integer.valueOf(strArrSplit[2]).intValue();
            this.uniqueRewards = strArrSplit[3].equals("1");
            this.limitationInterval = Integer.valueOf(strArrSplit[4]).intValue();
            setDelay(Integer.valueOf(strArrSplit[5]).intValue());
            if (strArrSplit.length > 6 && !strArrSplit[6].equalsIgnoreCase("\t")) {
                String[] strArrSplit2 = strArrSplit[6].split(";");
                this.rewardItems.clear();
                for (String str : strArrSplit2) {
                    try {
                        this.rewardItems.add(new WiredGiveRewardItem(str));
                    } catch (Exception e) {
                    }
                }
            }
            needsUpdate(true);
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void onPickUp() {
        this.limit = 0;
        this.limitationInterval = 0;
        this.given = 0;
        this.rewardTime = 0;
        this.uniqueRewards = false;
        this.rewardItems.clear();
        setDelay(0);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect
    public WiredEffectType getType() {
        return type;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect, com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        super.onClick(gameClient, room, objArr);
        if (gameClient.getHabbo().hasPermission(Permission.ACC_SUPERWIRED)) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("hotel.wired.superwired.info"), RoomChatMessageBubbles.BOT);
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void serializeWiredData(ServerMessage serverMessage, Room room) {
        serverMessage.appendBoolean(false);
        serverMessage.appendInt(Integer.valueOf(this.rewardItems.size()));
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(getBaseItem().getSpriteId()));
        serverMessage.appendInt(Integer.valueOf(getId()));
        StringBuilder sb = new StringBuilder();
        TObjectHashIterator it = this.rewardItems.iterator();
        while (it.hasNext()) {
            sb.append(((WiredGiveRewardItem) it.next()).wiredString()).append(";");
        }
        serverMessage.appendString(sb.toString());
        serverMessage.appendInt((Integer) 4);
        serverMessage.appendInt(Integer.valueOf(this.rewardTime));
        serverMessage.appendInt(Boolean.valueOf(this.uniqueRewards));
        serverMessage.appendInt(Integer.valueOf(this.limit));
        serverMessage.appendInt(Integer.valueOf(this.limitationInterval));
        serverMessage.appendInt(Boolean.valueOf(this.limit > 0));
        serverMessage.appendInt(Integer.valueOf(getType().code));
        serverMessage.appendInt(Integer.valueOf(getDelay()));
        if (!requiresTriggeringUser()) {
            serverMessage.appendInt((Integer) 0);
            return;
        }
        final ArrayList arrayList = new ArrayList();
        room.getRoomSpecialTypes().getTriggers(getX(), getY()).forEach(new TObjectProcedure<InteractionWiredTrigger>() { // from class: com.eu.habbo.habbohotel.items.interactions.wired.effects.WiredEffectGiveReward.1
            public boolean execute(InteractionWiredTrigger interactionWiredTrigger) {
                if (interactionWiredTrigger.isTriggeredByRoomUnit()) {
                    return true;
                }
                arrayList.add(Integer.valueOf(interactionWiredTrigger.getBaseItem().getSpriteId()));
                return true;
            }
        });
        serverMessage.appendInt(Integer.valueOf(arrayList.size()));
        Iterator it2 = arrayList.iterator();
        while (it2.hasNext()) {
            serverMessage.appendInt((Integer) it2.next());
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect
    public boolean saveData(WiredSettings wiredSettings, GameClient gameClient) throws WiredSaveException {
        if (!gameClient.getHabbo().hasPermission(Permission.ACC_SUPERWIRED)) {
            gameClient.getHabbo().whisper("U cannot do this.", RoomChatMessageBubbles.ALERT);
            return false;
        }
        if (wiredSettings.getIntParams().length < 4) {
            throw new WiredSaveException("Invalid data");
        }
        this.rewardTime = wiredSettings.getIntParams()[0];
        this.uniqueRewards = wiredSettings.getIntParams()[1] == 1;
        this.limit = wiredSettings.getIntParams()[2];
        this.limitationInterval = wiredSettings.getIntParams()[3];
        this.given = 0;
        String[] strArrSplit = wiredSettings.getStringParam().split(";");
        this.rewardItems.clear();
        for (String str : strArrSplit) {
            String[] strArrSplit2 = str.split(",");
            if (strArrSplit2.length != 3 || strArrSplit2[1].contains(":") || strArrSplit2[1].contains(";")) {
                gameClient.sendResponse(new UpdateFailedComposer(Emulator.getTexts().getValue("alert.superwired.invalid")));
                return false;
            }
            this.rewardItems.add(new WiredGiveRewardItem(1, strArrSplit2[0].equalsIgnoreCase("0"), strArrSplit2[1], Integer.valueOf(strArrSplit2[2]).intValue()));
        }
        setDelay(wiredSettings.getDelay());
        WiredHandler.dropRewards(getId());
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect
    public boolean requiresTriggeringUser() {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    protected long requiredCooldown() {
        return 0L;
    }
}
