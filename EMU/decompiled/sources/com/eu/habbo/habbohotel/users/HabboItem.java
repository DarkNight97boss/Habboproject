package com.eu.habbo.habbohotel.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.Achievement;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.FurnitureType;
import com.eu.habbo.habbohotel.items.IEventTriggers;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionCrackable;
import com.eu.habbo.habbohotel.items.interactions.InteractionDice;
import com.eu.habbo.habbohotel.items.interactions.InteractionGuildGate;
import com.eu.habbo.habbohotel.items.interactions.InteractionMultiHeight;
import com.eu.habbo.habbohotel.items.interactions.InteractionPostIt;
import com.eu.habbo.habbohotel.items.interactions.InteractionTrophy;
import com.eu.habbo.habbohotel.items.interactions.InteractionWired;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredHighscore;
import com.eu.habbo.habbohotel.items.interactions.games.InteractionGameTimer;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomTileState;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserDanceComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserDataComposer;
import com.eu.habbo.messages.outgoing.users.UpdateUserLookComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.awt.Rectangle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/users/HabboItem.class */
public abstract class HabboItem implements Runnable, IEventTriggers {
    private static final Logger LOGGER = LoggerFactory.getLogger(HabboItem.class);
    private static Class[] TOGGLING_INTERACTIONS = {InteractionGameTimer.class, InteractionWired.class, InteractionWiredHighscore.class, InteractionMultiHeight.class};
    private int id;
    private int userId;
    private int roomId;
    private Item baseItem;
    private String wallPosition;
    private short x;
    private short y;
    private double z;
    private int rotation;
    private String extradata;
    private int limitedStack;
    private int limitedSells;
    private boolean needsUpdate;
    private boolean needsDelete;
    private boolean isFromGift;

    public HabboItem(ResultSet resultSet, Item item) throws SQLException {
        this.needsUpdate = false;
        this.needsDelete = false;
        this.isFromGift = false;
        this.id = resultSet.getInt("id");
        this.userId = resultSet.getInt("user_id");
        this.roomId = resultSet.getInt("room_id");
        this.baseItem = item;
        this.wallPosition = resultSet.getString("wall_pos");
        this.x = resultSet.getShort("x");
        this.y = resultSet.getShort("y");
        this.z = resultSet.getDouble("z");
        this.rotation = resultSet.getInt("rot");
        this.extradata = resultSet.getString("extra_data").isEmpty() ? "0" : resultSet.getString("extra_data");
        if (resultSet.getString("limited_data").isEmpty()) {
            return;
        }
        this.limitedStack = Integer.parseInt(resultSet.getString("limited_data").split(":")[0]);
        this.limitedSells = Integer.parseInt(resultSet.getString("limited_data").split(":")[1]);
    }

    public HabboItem(int i, int i2, Item item, String str, int i3, int i4) {
        this.needsUpdate = false;
        this.needsDelete = false;
        this.isFromGift = false;
        this.id = i;
        this.userId = i2;
        this.roomId = 0;
        this.baseItem = item;
        this.wallPosition = Emulator.PREVIEW;
        this.x = (short) 0;
        this.y = (short) 0;
        this.z = 0.0d;
        this.rotation = 0;
        this.extradata = str.isEmpty() ? "0" : str;
        this.limitedSells = i4;
        this.limitedStack = i3;
    }

    public static RoomTile getSquareInFront(RoomLayout roomLayout, HabboItem habboItem) {
        return roomLayout.getTileInFront(roomLayout.getTile(habboItem.getX(), habboItem.getY()), habboItem.getRotation());
    }

    public void serializeFloorData(ServerMessage serverMessage) {
        try {
            serverMessage.appendInt(Integer.valueOf(getId()));
            serverMessage.appendInt(Integer.valueOf(this.baseItem.getSpriteId()));
            serverMessage.appendInt(Short.valueOf(this.x));
            serverMessage.appendInt(Short.valueOf(this.y));
            serverMessage.appendInt(Integer.valueOf(getRotation()));
            serverMessage.appendString(Double.toString(this.z));
            serverMessage.appendString((getBaseItem().getInteractionType().getType() == InteractionTrophy.class || getBaseItem().getInteractionType().getType() == InteractionCrackable.class || getBaseItem().getName().toLowerCase().equals("gnome_box")) ? "1.0" : (getBaseItem().allowWalk() || (getBaseItem().allowSit() && this.roomId != 0)) ? Item.getCurrentHeight(this) + Emulator.PREVIEW : Emulator.PREVIEW);
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
        }
    }

    public void serializeExtradata(ServerMessage serverMessage) {
        if (isLimited()) {
            serverMessage.appendInt(Integer.valueOf(getLimitedSells()));
            serverMessage.appendInt(Integer.valueOf(getLimitedStack()));
        }
    }

    public void serializeWallData(ServerMessage serverMessage) {
        serverMessage.appendString(getId() + Emulator.PREVIEW);
        serverMessage.appendInt(Integer.valueOf(this.baseItem.getSpriteId()));
        serverMessage.appendString(this.wallPosition);
        if (this instanceof InteractionPostIt) {
            serverMessage.appendString(this.extradata.split(" ")[0]);
        } else {
            serverMessage.appendString(this.extradata);
        }
        serverMessage.appendInt((Integer) (-1));
        serverMessage.appendInt(Boolean.valueOf(isUsable()));
        serverMessage.appendInt(Integer.valueOf(getUserId()));
    }

    public int getId() {
        return this.id;
    }

    public int getGiftAdjustedId() {
        return this.isFromGift ? -this.id : this.id;
    }

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int i) {
        this.userId = i;
    }

    public int getRoomId() {
        return this.roomId;
    }

    public void setRoomId(int i) {
        this.roomId = i;
    }

    public Item getBaseItem() {
        return this.baseItem;
    }

    public String getWallPosition() {
        return this.wallPosition;
    }

    public void setWallPosition(String str) {
        this.wallPosition = str;
    }

    public short getX() {
        return this.x;
    }

    public void setX(short s) {
        this.x = s;
    }

    public short getY() {
        return this.y;
    }

    public void setY(short s) {
        this.y = s;
    }

    public double getZ() {
        return this.z;
    }

    public void setZ(double d) {
        if (d > 9999.0d || d < -9999.0d) {
            return;
        }
        this.z = d;
    }

    public int getRotation() {
        return this.rotation;
    }

    public void setRotation(int i) {
        this.rotation = (byte) (i % 8);
    }

    public String getExtradata() {
        return this.extradata;
    }

    public void setExtradata(String str) {
        this.extradata = str;
    }

    public boolean needsUpdate() {
        return this.needsUpdate;
    }

    public boolean needsDelete() {
        return this.needsDelete;
    }

    public void needsUpdate(boolean z) {
        this.needsUpdate = z;
    }

    public void needsDelete(boolean z) {
        this.needsDelete = z;
    }

    public boolean isLimited() {
        return this.limitedStack > 0;
    }

    public int getLimitedStack() {
        return this.limitedStack;
    }

    public int getLimitedSells() {
        return this.limitedSells;
    }

    public int getMaximumRotations() {
        return this.baseItem.getRotations();
    }

    @Override // java.lang.Runnable
    public void run() {
        PreparedStatement preparedStatementPrepareStatement;
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                if (this.needsDelete) {
                    this.needsUpdate = false;
                    this.needsDelete = false;
                    preparedStatementPrepareStatement = connection.prepareStatement("DELETE FROM items WHERE id = ?");
                    try {
                        preparedStatementPrepareStatement.setInt(1, getId());
                        preparedStatementPrepareStatement.execute();
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
                        }
                    } finally {
                    }
                } else if (this.needsUpdate) {
                    try {
                        preparedStatementPrepareStatement = connection.prepareStatement("UPDATE items SET user_id = ?, room_id = ?, wall_pos = ?, x = ?, y = ?, z = ?, rot = ?, extra_data = ?, limited_data = ? WHERE id = ?");
                    } catch (SQLException e) {
                        LOGGER.error("Caught SQL exception", e);
                        LOGGER.error("SQLException trying to save HabboItem: " + toString());
                    }
                    try {
                        preparedStatementPrepareStatement.setInt(1, this.userId);
                        preparedStatementPrepareStatement.setInt(2, this.roomId);
                        preparedStatementPrepareStatement.setString(3, this.wallPosition);
                        preparedStatementPrepareStatement.setInt(4, this.x);
                        preparedStatementPrepareStatement.setInt(5, this.y);
                        preparedStatementPrepareStatement.setDouble(6, Math.max(-9999.0d, Math.min(9999.0d, Math.round(this.z * Math.pow(10.0d, 6.0d)) / Math.pow(10.0d, 6.0d))));
                        preparedStatementPrepareStatement.setInt(7, this.rotation);
                        preparedStatementPrepareStatement.setString(8, this instanceof InteractionGuildGate ? Emulator.PREVIEW : getDatabaseExtraData());
                        preparedStatementPrepareStatement.setString(9, this.limitedStack + ":" + this.limitedSells);
                        preparedStatementPrepareStatement.setInt(10, this.id);
                        preparedStatementPrepareStatement.execute();
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
                        }
                        this.needsUpdate = false;
                    } finally {
                    }
                }
                if (connection != null) {
                    connection.close();
                }
            } finally {
            }
        } catch (SQLException e2) {
            LOGGER.error("Caught SQL exception", e2);
        }
    }

    public abstract boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr);

    public abstract boolean isWalkable();

    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        if (gameClient == null || getBaseItem().getType() != FurnitureType.FLOOR) {
            return;
        }
        if (objArr == null || objArr.length < 2 || !(objArr[1] instanceof WiredEffectType)) {
            if ((getBaseItem().getStateCount() > 1 && !(this instanceof InteractionDice)) || Arrays.asList(TOGGLING_INTERACTIONS).contains(getClass()) || (objArr != null && objArr.length == 1 && objArr[0].equals("TOGGLE_OVERRIDE"))) {
                WiredHandler.handle(WiredTriggerType.STATE_CHANGED, gameClient.getHabbo().getRoomUnit(), room, new Object[]{this});
            }
        }
    }

    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        Habbo habbo;
        WiredHandler.handle(WiredTriggerType.WALKS_ON_FURNI, roomUnit, room, new Object[]{this});
        if ((getBaseItem().allowSit() || getBaseItem().allowLay()) && !roomUnit.getDanceType().equals(DanceType.NONE)) {
            roomUnit.setDanceType(DanceType.NONE);
            room.sendComposer(new RoomUserDanceComposer(roomUnit).compose());
        }
        if (getBaseItem().getClothingOnWalk().isEmpty() || roomUnit.getPreviousLocation() == roomUnit.getGoal() || roomUnit.getGoal() != room.getLayout().getTile(this.x, this.y) || (habbo = room.getHabbo(roomUnit)) == null || habbo.getClient() == null) {
            return;
        }
        String[] strArr = (String[]) Arrays.stream(getBaseItem().getClothingOnWalk().split("\\.")).map(str -> {
            return str.split("-")[0];
        }).toArray(i -> {
            return new String[i];
        });
        habbo.getHabboInfo().setLook(String.join(".", (CharSequence[]) Arrays.stream(habbo.getHabboInfo().getLook().split("\\.")).filter(str2 -> {
            return !ArrayUtils.contains(strArr, str2.split("-")[0]);
        }).toArray(i2 -> {
            return new String[i2];
        })) + "." + getBaseItem().getClothingOnWalk());
        habbo.getClient().sendResponse(new UpdateUserLookComposer(habbo));
        if (habbo.getHabboInfo().getCurrentRoom() != null) {
            habbo.getHabboInfo().getCurrentRoom().sendComposer(new RoomUserDataComposer(habbo).compose());
        }
    }

    public void onWalkOff(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        if (objArr == null || objArr.length <= 0) {
            return;
        }
        WiredHandler.handle(WiredTriggerType.WALKS_OFF_FURNI, roomUnit, room, new Object[]{this});
    }

    public abstract void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception;

    public void onPlace(Room room) {
        Achievement achievement = Emulator.getGameEnvironment().getAchievementManager().getAchievement("RoomDecoFurniCount");
        Habbo habbo = room.getHabbo(getUserId());
        int userFurniCount = room.getUserFurniCount(getUserId()) - (habbo == null ? AchievementManager.getAchievementProgressForHabbo(getUserId(), achievement) : habbo.getHabboStats().getAchievementProgress(achievement));
        if (userFurniCount > 0) {
            if (habbo != null) {
                AchievementManager.progressAchievement(habbo, achievement, userFurniCount);
            } else {
                AchievementManager.progressAchievement(getUserId(), achievement, userFurniCount);
            }
        }
        Achievement achievement2 = Emulator.getGameEnvironment().getAchievementManager().getAchievement("RoomDecoFurniTypeCount");
        int userUniqueFurniCount = room.getUserUniqueFurniCount(getUserId()) - (habbo == null ? AchievementManager.getAchievementProgressForHabbo(getUserId(), achievement2) : habbo.getHabboStats().getAchievementProgress(achievement2));
        if (userUniqueFurniCount > 0) {
            if (habbo != null) {
                AchievementManager.progressAchievement(habbo, achievement2, userUniqueFurniCount);
            } else {
                AchievementManager.progressAchievement(getUserId(), achievement2, userUniqueFurniCount);
            }
        }
    }

    public void onPickUp(Room room) {
        if (getBaseItem().getEffectF() > 0 || getBaseItem().getEffectM() > 0) {
            HabboItem topItemAt = room.getTopItemAt(getX(), getY(), this);
            int effectM = 0;
            int effectF = 0;
            if (topItemAt != null) {
                effectM = topItemAt.getBaseItem().getEffectM();
                effectF = topItemAt.getBaseItem().getEffectF();
            }
            TObjectHashIterator it = room.getHabbosOnItem(this).iterator();
            while (it.hasNext()) {
                Habbo habbo = (Habbo) it.next();
                if (getBaseItem().getEffectM() > 0 && habbo.getHabboInfo().getGender().equals(HabboGender.M) && habbo.getRoomUnit().getEffectId() == getBaseItem().getEffectM()) {
                    room.giveEffect(habbo, effectM, -1);
                }
                if (getBaseItem().getEffectF() > 0 && habbo.getHabboInfo().getGender().equals(HabboGender.F) && habbo.getRoomUnit().getEffectId() == getBaseItem().getEffectF()) {
                    room.giveEffect(habbo, effectF, -1);
                }
            }
            TObjectHashIterator it2 = room.getBotsAt(room.getLayout().getTile(getX(), getY())).iterator();
            while (it2.hasNext()) {
                Bot bot = (Bot) it2.next();
                if (getBaseItem().getEffectM() > 0 && bot.getGender().equals(HabboGender.M) && bot.getRoomUnit().getEffectId() == getBaseItem().getEffectM()) {
                    room.giveEffect(bot.getRoomUnit(), effectM, -1);
                }
                if (getBaseItem().getEffectF() > 0 && bot.getGender().equals(HabboGender.F) && bot.getRoomUnit().getEffectId() == getBaseItem().getEffectF()) {
                    room.giveEffect(bot.getRoomUnit(), effectF, -1);
                }
            }
        }
    }

    public void onMove(Room room, RoomTile roomTile, RoomTile roomTile2) {
        if (getBaseItem().getEffectF() > 0 || getBaseItem().getEffectM() > 0) {
            HabboItem topItemAt = room.getTopItemAt(roomTile.x, roomTile.y, this);
            int effectM = 0;
            int effectF = 0;
            if (topItemAt != null) {
                effectM = topItemAt.getBaseItem().getEffectM();
                effectF = topItemAt.getBaseItem().getEffectF();
            }
            ArrayList<Habbo> arrayList = new ArrayList();
            ArrayList<Habbo> arrayList2 = new ArrayList();
            ArrayList<Bot> arrayList3 = new ArrayList();
            ArrayList<Bot> arrayList4 = new ArrayList();
            TObjectHashIterator it = room.getLayout().getTilesAt(roomTile, getBaseItem().getWidth(), getBaseItem().getLength(), getRotation()).iterator();
            while (it.hasNext()) {
                RoomTile roomTile3 = (RoomTile) it.next();
                arrayList.addAll(room.getHabbosAt(roomTile3));
                arrayList3.addAll(room.getBotsAt(roomTile3));
            }
            TObjectHashIterator it2 = room.getLayout().getTilesAt(roomTile, getBaseItem().getWidth(), getBaseItem().getLength(), getRotation()).iterator();
            while (it2.hasNext()) {
                RoomTile roomTile4 = (RoomTile) it2.next();
                arrayList2.addAll(room.getHabbosAt(roomTile4));
                arrayList4.addAll(room.getBotsAt(roomTile4));
            }
            arrayList.removeAll(arrayList2);
            arrayList3.removeAll(arrayList4);
            for (Habbo habbo : arrayList) {
                if (getBaseItem().getEffectM() > 0 && habbo.getHabboInfo().getGender().equals(HabboGender.M) && habbo.getRoomUnit().getEffectId() == getBaseItem().getEffectM()) {
                    room.giveEffect(habbo, effectM, -1);
                }
                if (getBaseItem().getEffectF() > 0 && habbo.getHabboInfo().getGender().equals(HabboGender.F) && habbo.getRoomUnit().getEffectId() == getBaseItem().getEffectF()) {
                    room.giveEffect(habbo, effectF, -1);
                }
            }
            for (Habbo habbo2 : arrayList2) {
                if (getBaseItem().getEffectM() > 0 && habbo2.getHabboInfo().getGender().equals(HabboGender.M) && habbo2.getRoomUnit().getEffectId() != getBaseItem().getEffectM()) {
                    room.giveEffect(habbo2, getBaseItem().getEffectM(), -1);
                }
                if (getBaseItem().getEffectF() > 0 && habbo2.getHabboInfo().getGender().equals(HabboGender.F) && habbo2.getRoomUnit().getEffectId() != getBaseItem().getEffectF()) {
                    room.giveEffect(habbo2, getBaseItem().getEffectF(), -1);
                }
            }
            for (Bot bot : arrayList3) {
                if (getBaseItem().getEffectM() > 0 && bot.getGender().equals(HabboGender.M) && bot.getRoomUnit().getEffectId() == getBaseItem().getEffectM()) {
                    room.giveEffect(bot.getRoomUnit(), effectM, -1);
                }
                if (getBaseItem().getEffectF() > 0 && bot.getGender().equals(HabboGender.F) && bot.getRoomUnit().getEffectId() == getBaseItem().getEffectF()) {
                    room.giveEffect(bot.getRoomUnit(), effectF, -1);
                }
            }
            for (Bot bot2 : arrayList4) {
                if (getBaseItem().getEffectM() > 0 && bot2.getGender().equals(HabboGender.M) && bot2.getRoomUnit().getEffectId() != getBaseItem().getEffectM()) {
                    room.giveEffect(bot2.getRoomUnit(), getBaseItem().getEffectM(), -1);
                }
                if (getBaseItem().getEffectF() > 0 && bot2.getGender().equals(HabboGender.F) && bot2.getRoomUnit().getEffectId() != getBaseItem().getEffectF()) {
                    room.giveEffect(bot2.getRoomUnit(), getBaseItem().getEffectF(), -1);
                }
            }
        }
    }

    public String getDatabaseExtraData() {
        return getExtradata();
    }

    public String toString() {
        return "ID: " + this.id + ", BaseID: " + getBaseItem().getId() + ", X: " + ((int) this.x) + ", Y: " + ((int) this.y) + ", Z: " + this.z + ", Extradata: " + this.extradata;
    }

    public boolean allowWiredResetState() {
        return false;
    }

    public boolean isUsable() {
        return this.baseItem.getStateCount() > 1;
    }

    public boolean canStackAt(Room room, List<Pair<RoomTile, THashSet<HabboItem>>> list) {
        return true;
    }

    public boolean isFromGift() {
        return this.isFromGift;
    }

    public void setFromGift(boolean z) {
        this.isFromGift = z;
    }

    public boolean invalidatesToRoomKick() {
        return false;
    }

    public List<RoomTile> getOccupyingTiles(RoomLayout roomLayout) {
        ArrayList arrayList = new ArrayList();
        Rectangle rectangle = RoomLayout.getRectangle(getX(), getY(), getBaseItem().getWidth(), getBaseItem().getLength(), getRotation());
        for (int i = rectangle.x; i < ((double) rectangle.x) + rectangle.getWidth(); i++) {
            for (int i2 = rectangle.y; i2 < ((double) rectangle.y) + rectangle.getHeight(); i2++) {
                arrayList.add(roomLayout.getTile((short) i, (short) i2));
            }
        }
        return arrayList;
    }

    public RoomTile getOverrideGoalTile(RoomUnit roomUnit, Room room, RoomTile roomTile) {
        return roomTile;
    }

    public RoomTileState getOverrideTileState(RoomTile roomTile, Room room) {
        return null;
    }

    public boolean canOverrideTile(RoomUnit roomUnit, Room room, RoomTile roomTile) {
        return false;
    }

    public Rectangle getRectangle() {
        return RoomLayout.getRectangle(getX(), getY(), getBaseItem().getWidth(), getBaseItem().getLength(), getRotation());
    }

    public Rectangle getRectangle(int i, int i2) {
        return RoomLayout.getRectangle(getX() - i, getY() - i2, getBaseItem().getWidth() + (i * 2), getBaseItem().getLength() + (i2 * 2), getRotation());
    }
}
