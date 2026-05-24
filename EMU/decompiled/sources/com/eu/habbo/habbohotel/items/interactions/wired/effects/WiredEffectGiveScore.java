package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.games.Game;
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
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectProcedure;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectGiveScore.class */
public class WiredEffectGiveScore extends InteractionWiredEffect {
    public static final WiredEffectType type = WiredEffectType.GIVE_SCORE;
    private int score;
    private int count;
    private TObjectIntMap<Map.Entry<Integer, Integer>> data;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectGiveScore$JsonData.class */
    static class JsonData {
        int score;
        int count;
        int delay;

        public JsonData(int i, int i2, int i3) {
            this.score = i;
            this.count = i2;
            this.delay = i3;
        }
    }

    public WiredEffectGiveScore(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.data = new TObjectIntHashMap();
    }

    public WiredEffectGiveScore(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.data = new TObjectIntHashMap();
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public boolean execute(RoomUnit roomUnit, Room room, Object[] objArr) {
        Game game;
        Habbo habbo = room.getHabbo(roomUnit);
        if (habbo == null || habbo.getHabboInfo().getCurrentGame() == null || (game = room.getGame(habbo.getHabboInfo().getCurrentGame())) == null) {
            return false;
        }
        int startTime = game.getStartTime();
        TObjectIntHashMap tObjectIntHashMap = new TObjectIntHashMap(this.data);
        TObjectIntIterator it = tObjectIntHashMap.iterator();
        int size = tObjectIntHashMap.size();
        while (true) {
            int i = size;
            size--;
            if (i <= 0) {
                try {
                    this.data.put(new AbstractMap.SimpleEntry(Integer.valueOf(startTime), Integer.valueOf(habbo.getHabboInfo().getId())), 1);
                } catch (IllegalArgumentException e) {
                }
                if (habbo.getHabboInfo().getGamePlayer() == null) {
                    return true;
                }
                habbo.getHabboInfo().getGamePlayer().addScore(this.score, true);
                return true;
            }
            it.advance();
            Map.Entry entry = (Map.Entry) it.key();
            if (((Integer) entry.getValue()).intValue() == habbo.getHabboInfo().getId()) {
                if (((Integer) entry.getKey()).intValue() != startTime) {
                    it.remove();
                } else if (it.value() < this.count) {
                    it.setValue(it.value() + 1);
                    habbo.getHabboInfo().getGamePlayer().addScore(this.score, true);
                    return true;
                }
            }
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.score, this.count, getDelay()));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void loadWiredData(ResultSet resultSet, Room room) throws SQLException {
        String string = resultSet.getString("wired_data");
        if (string.startsWith("{")) {
            JsonData jsonData = (JsonData) WiredHandler.getGsonBuilder().create().fromJson(string, JsonData.class);
            this.score = jsonData.score;
            this.count = jsonData.count;
            setDelay(jsonData.delay);
            return;
        }
        String[] strArrSplit = string.split(";");
        if (strArrSplit.length == 3) {
            this.score = Integer.valueOf(strArrSplit[0]).intValue();
            this.count = Integer.valueOf(strArrSplit[1]).intValue();
            setDelay(Integer.valueOf(strArrSplit[2]).intValue());
        }
        needsUpdate(true);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void onPickUp() {
        this.score = 0;
        this.count = 0;
        setDelay(0);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect
    public WiredEffectType getType() {
        return type;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void serializeWiredData(ServerMessage serverMessage, Room room) {
        serverMessage.appendBoolean(false);
        serverMessage.appendInt((Integer) 5);
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(getBaseItem().getSpriteId()));
        serverMessage.appendInt(Integer.valueOf(getId()));
        serverMessage.appendString(Emulator.PREVIEW);
        serverMessage.appendInt((Integer) 2);
        serverMessage.appendInt(Integer.valueOf(this.score));
        serverMessage.appendInt(Integer.valueOf(this.count));
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(getType().code));
        serverMessage.appendInt(Integer.valueOf(getDelay()));
        if (!requiresTriggeringUser()) {
            serverMessage.appendInt((Integer) 0);
            return;
        }
        final ArrayList arrayList = new ArrayList();
        room.getRoomSpecialTypes().getTriggers(getX(), getY()).forEach(new TObjectProcedure<InteractionWiredTrigger>() { // from class: com.eu.habbo.habbohotel.items.interactions.wired.effects.WiredEffectGiveScore.1
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
        if (wiredSettings.getIntParams().length < 2) {
            throw new WiredSaveException("Invalid data");
        }
        int i = wiredSettings.getIntParams()[0];
        if (i < 1 || i > 100) {
            throw new WiredSaveException("Score is invalid");
        }
        int i2 = wiredSettings.getIntParams()[1];
        if (i2 < 1 || i2 > 10) {
            throw new WiredSaveException("Times per game is invalid");
        }
        int delay = wiredSettings.getDelay();
        if (delay > Emulator.getConfig().getInt("hotel.wired.max_delay", 20)) {
            throw new WiredSaveException("Delay too long");
        }
        this.score = i;
        this.count = i2;
        setDelay(delay);
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect
    public boolean requiresTriggeringUser() {
        return true;
    }
}
