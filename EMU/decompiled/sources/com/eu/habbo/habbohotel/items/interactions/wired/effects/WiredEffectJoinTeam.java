package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.games.GameTeamColors;
import com.eu.habbo.habbohotel.games.wired.WiredGame;
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

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectJoinTeam.class */
public class WiredEffectJoinTeam extends InteractionWiredEffect {
    public static final WiredEffectType type = WiredEffectType.JOIN_TEAM;
    private GameTeamColors teamColor;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectJoinTeam$JsonData.class */
    static class JsonData {
        GameTeamColors team;
        int delay;

        public JsonData(GameTeamColors gameTeamColors, int i) {
            this.team = gameTeamColors;
            this.delay = i;
        }
    }

    public WiredEffectJoinTeam(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.teamColor = GameTeamColors.RED;
    }

    public WiredEffectJoinTeam(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.teamColor = GameTeamColors.RED;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public boolean execute(RoomUnit roomUnit, Room room, Object[] objArr) {
        Habbo habbo = room.getHabbo(roomUnit);
        if (habbo == null) {
            return false;
        }
        WiredGame wiredGame = (WiredGame) room.getGameOrCreate(WiredGame.class);
        if (habbo.getHabboInfo().getGamePlayer() != null && habbo.getHabboInfo().getCurrentGame() != null && (habbo.getHabboInfo().getCurrentGame() != WiredGame.class || (habbo.getHabboInfo().getCurrentGame() == WiredGame.class && habbo.getHabboInfo().getGamePlayer().getTeamColor() != this.teamColor))) {
            room.getGame(habbo.getHabboInfo().getCurrentGame()).removeHabbo(habbo);
        }
        if (habbo.getHabboInfo().getGamePlayer() != null) {
            return true;
        }
        wiredGame.addHabbo(habbo, this.teamColor);
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.teamColor, getDelay()));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void loadWiredData(ResultSet resultSet, Room room) throws SQLException {
        String string = resultSet.getString("wired_data");
        if (string.startsWith("{")) {
            JsonData jsonData = (JsonData) WiredHandler.getGsonBuilder().create().fromJson(string, JsonData.class);
            setDelay(jsonData.delay);
            this.teamColor = jsonData.team;
        } else {
            String[] strArrSplit = resultSet.getString("wired_data").split("\t");
            if (strArrSplit.length >= 1) {
                setDelay(Integer.valueOf(strArrSplit[0]).intValue());
                if (strArrSplit.length >= 2) {
                    this.teamColor = GameTeamColors.values()[Integer.valueOf(strArrSplit[1]).intValue()];
                }
            }
            needsUpdate(true);
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void onPickUp() {
        this.teamColor = GameTeamColors.RED;
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
        serverMessage.appendInt((Integer) 1);
        serverMessage.appendInt(Integer.valueOf(this.teamColor.type));
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(getType().code));
        serverMessage.appendInt(Integer.valueOf(getDelay()));
        if (!requiresTriggeringUser()) {
            serverMessage.appendInt((Integer) 0);
            return;
        }
        final ArrayList arrayList = new ArrayList();
        room.getRoomSpecialTypes().getTriggers(getX(), getY()).forEach(new TObjectProcedure<InteractionWiredTrigger>() { // from class: com.eu.habbo.habbohotel.items.interactions.wired.effects.WiredEffectJoinTeam.1
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
            throw new WiredSaveException("invalid data");
        }
        int i = wiredSettings.getIntParams()[0];
        if (i < 1 || i > 4) {
            throw new WiredSaveException("Team is invalid");
        }
        int delay = wiredSettings.getDelay();
        if (delay > Emulator.getConfig().getInt("hotel.wired.max_delay", 20)) {
            throw new WiredSaveException("Delay too long");
        }
        this.teamColor = GameTeamColors.values()[i];
        setDelay(delay);
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect
    public boolean requiresTriggeringUser() {
        return true;
    }
}
