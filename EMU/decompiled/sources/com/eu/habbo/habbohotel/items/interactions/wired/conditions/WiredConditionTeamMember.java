package com.eu.habbo.habbohotel.items.interactions.wired.conditions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.games.GameTeamColors;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.wired.WiredConditionType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ServerMessage;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/conditions/WiredConditionTeamMember.class */
public class WiredConditionTeamMember extends InteractionWiredCondition {
    public static final WiredConditionType type = WiredConditionType.ACTOR_IN_TEAM;
    private GameTeamColors teamColor;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/conditions/WiredConditionTeamMember$JsonData.class */
    static class JsonData {
        GameTeamColors teamColor;

        public JsonData(GameTeamColors gameTeamColors) {
            this.teamColor = gameTeamColors;
        }
    }

    public WiredConditionTeamMember(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.teamColor = GameTeamColors.RED;
    }

    public WiredConditionTeamMember(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.teamColor = GameTeamColors.RED;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public boolean execute(RoomUnit roomUnit, Room room, Object[] objArr) {
        Habbo habbo = room.getHabbo(roomUnit);
        if (habbo == null || habbo.getHabboInfo().getGamePlayer() == null) {
            return false;
        }
        return habbo.getHabboInfo().getGamePlayer().getTeamColor().equals(this.teamColor);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.teamColor));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void loadWiredData(ResultSet resultSet, Room room) throws SQLException {
        try {
            String string = resultSet.getString("wired_data");
            if (string.startsWith("{")) {
                this.teamColor = ((JsonData) WiredHandler.getGsonBuilder().create().fromJson(string, JsonData.class)).teamColor;
            } else if (!string.equals(Emulator.PREVIEW)) {
                this.teamColor = GameTeamColors.values()[Integer.parseInt(string)];
            }
        } catch (Exception e) {
            this.teamColor = GameTeamColors.RED;
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void onPickUp() {
        this.teamColor = GameTeamColors.RED;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition
    public WiredConditionType getType() {
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
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt((Integer) 0);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition
    public boolean saveData(WiredSettings wiredSettings) {
        if (wiredSettings.getIntParams().length < 1) {
            return false;
        }
        this.teamColor = GameTeamColors.values()[wiredSettings.getIntParams()[0]];
        return true;
    }
}
