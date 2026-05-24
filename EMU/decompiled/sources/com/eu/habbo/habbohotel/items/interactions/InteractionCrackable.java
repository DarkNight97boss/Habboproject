package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.CrackableReward;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboGender;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.users.subscriptions.Subscription;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.threading.runnables.CrackableExplode;
import com.eu.habbo.util.pathfinding.Rotation;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionCrackable.class */
public class InteractionCrackable extends HabboItem {
    private final Object lock;
    public boolean cracked;
    protected int ticks;

    public InteractionCrackable(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.lock = new Object();
        this.cracked = false;
        this.ticks = 0;
    }

    public InteractionCrackable(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.lock = new Object();
        this.cracked = false;
        this.ticks = 0;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void serializeExtradata(ServerMessage serverMessage) {
        if (getExtradata().length() == 0) {
            setExtradata("0");
        }
        serverMessage.appendInt(Integer.valueOf(7 + (isLimited() ? 256 : 0)));
        serverMessage.appendString(Emulator.getGameEnvironment().getItemManager().calculateCrackState(Integer.valueOf(getExtradata()).intValue(), Emulator.getGameEnvironment().getItemManager().getCrackableCount(getBaseItem().getId()), getBaseItem()) + Emulator.PREVIEW);
        serverMessage.appendInt(Integer.valueOf(getExtradata()));
        serverMessage.appendInt(Integer.valueOf(Emulator.getGameEnvironment().getItemManager().getCrackableCount(getBaseItem().getId())));
        super.serializeExtradata(serverMessage);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return false;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        if (gameClient == null) {
            return;
        }
        super.onClick(gameClient, room, objArr);
        synchronized (this.lock) {
            if (getRoomId() == 0) {
                return;
            }
            if (this.cracked) {
                return;
            }
            if (userRequiredToBeAdjacent() && gameClient.getHabbo().getRoomUnit().getCurrentLocation().distance(room.getLayout().getTile(getX(), getY())) > 1.5d) {
                gameClient.getHabbo().getRoomUnit().setGoalLocation(room.getLayout().getTileInFront(room.getLayout().getTile(getX(), getY()), Rotation.Calculate(gameClient.getHabbo().getRoomUnit().getX(), gameClient.getHabbo().getRoomUnit().getY(), getX(), getY())));
                return;
            }
            if (getExtradata().length() == 0) {
                setExtradata("0");
            }
            if (getBaseItem().getEffectF() > 0 && gameClient.getHabbo().getHabboInfo().getGender().equals(HabboGender.F) && getBaseItem().getEffectF() == gameClient.getHabbo().getRoomUnit().getEffectId()) {
                return;
            }
            if (getBaseItem().getEffectM() > 0 && gameClient.getHabbo().getHabboInfo().getGender().equals(HabboGender.M) && getBaseItem().getEffectM() == gameClient.getHabbo().getRoomUnit().getEffectId()) {
                return;
            }
            onTick(gameClient.getHabbo(), room);
        }
    }

    public void onTick(Habbo habbo, Room room) {
        CrackableReward crackableData;
        if (this.cracked) {
        }
        if ((allowAnyone() || getUserId() == habbo.getHabboInfo().getId()) && (crackableData = Emulator.getGameEnvironment().getItemManager().getCrackableData(getBaseItem().getId())) != null) {
            if (crackableData.requiredEffect <= 0 || habbo.getRoomUnit().getEffectId() == crackableData.requiredEffect) {
                if (this.ticks < 1) {
                    this.ticks = Integer.parseInt(getExtradata());
                }
                this.ticks++;
                setExtradata(Emulator.PREVIEW + this.ticks);
                needsUpdate(true);
                room.updateItem(this);
                if (!crackableData.achievementTick.isEmpty()) {
                    AchievementManager.progressAchievement(habbo, Emulator.getGameEnvironment().getAchievementManager().getAchievement(crackableData.achievementTick));
                }
                if (this.cracked || this.ticks != Emulator.getGameEnvironment().getItemManager().getCrackableCount(getBaseItem().getId())) {
                    return;
                }
                this.cracked = true;
                Emulator.getThreading().run(new CrackableExplode(room, this, habbo, !placeInRoom(), getX(), getY()), 1500L);
                if (!crackableData.achievementCracked.isEmpty()) {
                    AchievementManager.progressAchievement(habbo, Emulator.getGameEnvironment().getAchievementManager().getAchievement(crackableData.achievementCracked));
                }
                if (crackableData.subscriptionType == null || crackableData.subscriptionDuration <= 0) {
                    return;
                }
                switch (crackableData.subscriptionType) {
                    case HABBO_CLUB:
                        habbo.getHabboStats().createSubscription(Subscription.HABBO_CLUB, crackableData.subscriptionDuration * 86400);
                        break;
                    case BUILDERS_CLUB:
                        habbo.getHabboStats().createSubscription("BUILDERS_CLUB", crackableData.subscriptionDuration * 86400);
                        break;
                }
            }
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOff(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }

    public boolean allowAnyone() {
        return false;
    }

    protected boolean placeInRoom() {
        return true;
    }

    public boolean resetable() {
        return false;
    }

    public boolean userRequiredToBeAdjacent() {
        return true;
    }

    public void reset(Room room) {
        this.cracked = false;
        this.ticks = 0;
        setExtradata("0");
        room.updateItem(this);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isUsable() {
        return false;
    }
}
