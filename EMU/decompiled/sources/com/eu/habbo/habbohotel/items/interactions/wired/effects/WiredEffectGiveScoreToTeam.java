package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.games.Game;
import com.eu.habbo.habbohotel.games.GameState;
import com.eu.habbo.habbohotel.games.GameTeam;
import com.eu.habbo.habbohotel.games.GameTeamColors;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.wired.WiredSaveException;
import gnu.trove.map.hash.TIntIntHashMap;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectGiveScoreToTeam.class */
public class WiredEffectGiveScoreToTeam extends InteractionWiredEffect {
    public static final WiredEffectType type = WiredEffectType.GIVE_SCORE_TEAM;
    private int points;
    private int count;
    private GameTeamColors teamColor;
    private TIntIntHashMap startTimes;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/effects/WiredEffectGiveScoreToTeam$JsonData.class */
    static class JsonData {
        int score;
        int count;
        GameTeamColors team;
        int delay;

        public JsonData(int i, int i2, GameTeamColors gameTeamColors, int i3) {
            this.score = i;
            this.count = i2;
            this.team = gameTeamColors;
            this.delay = i3;
        }
    }

    public WiredEffectGiveScoreToTeam(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.teamColor = GameTeamColors.RED;
        this.startTimes = new TIntIntHashMap();
    }

    public WiredEffectGiveScoreToTeam(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.teamColor = GameTeamColors.RED;
        this.startTimes = new TIntIntHashMap();
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public boolean execute(RoomUnit roomUnit, Room room, Object[] objArr) {
        int i;
        GameTeam team;
        for (Game game : room.getGames()) {
            if (game != null && game.state.equals(GameState.RUNNING) && (i = this.startTimes.get(game.getStartTime())) < this.count && (team = game.getTeam(this.teamColor)) != null) {
                team.addTeamScore(this.points);
                this.startTimes.put(game.getStartTime(), i + 1);
            }
        }
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.points, this.count, this.teamColor, getDelay()));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void loadWiredData(ResultSet resultSet, Room room) throws SQLException {
        String string = resultSet.getString("wired_data");
        if (string.startsWith("{")) {
            JsonData jsonData = (JsonData) WiredHandler.getGsonBuilder().create().fromJson(string, JsonData.class);
            this.points = jsonData.score;
            this.count = jsonData.count;
            this.teamColor = jsonData.team;
            setDelay(jsonData.delay);
            return;
        }
        String[] strArrSplit = resultSet.getString("wired_data").split(";");
        if (strArrSplit.length == 4) {
            this.points = Integer.valueOf(strArrSplit[0]).intValue();
            this.count = Integer.valueOf(strArrSplit[1]).intValue();
            this.teamColor = GameTeamColors.values()[Integer.valueOf(strArrSplit[2]).intValue()];
            setDelay(Integer.valueOf(strArrSplit[3]).intValue());
        }
        needsUpdate(true);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWired
    public void onPickUp() {
        this.startTimes.clear();
        this.points = 0;
        this.count = 0;
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
        serverMessage.appendInt((Integer) 3);
        serverMessage.appendInt(Integer.valueOf(this.points));
        serverMessage.appendInt(Integer.valueOf(this.count));
        serverMessage.appendInt(Integer.valueOf(this.teamColor.type));
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(getType().code));
        serverMessage.appendInt(Integer.valueOf(getDelay()));
        serverMessage.appendInt((Integer) 0);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect
    public boolean saveData(WiredSettings wiredSettings, GameClient gameClient) throws WiredSaveException {
        if (wiredSettings.getIntParams().length < 3) {
            throw new WiredSaveException("Invalid data");
        }
        int i = wiredSettings.getIntParams()[0];
        if (i < 1 || i > 100) {
            throw new WiredSaveException("Points is invalid");
        }
        int i2 = wiredSettings.getIntParams()[1];
        if (i2 < 1 || i2 > 10) {
            throw new WiredSaveException("Times per game is invalid");
        }
        int i3 = wiredSettings.getIntParams()[2];
        if (i3 < 1 || i3 > 4) {
            throw new WiredSaveException("Team is invalid");
        }
        int delay = wiredSettings.getDelay();
        if (delay > Emulator.getConfig().getInt("hotel.wired.max_delay", 20)) {
            throw new WiredSaveException("Delay too long");
        }
        this.points = i;
        this.count = i2;
        this.teamColor = GameTeamColors.values()[i3];
        setDelay(delay);
        return true;
    }
}
