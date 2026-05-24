package com.eu.habbo.habbohotel.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWater;
import com.eu.habbo.habbohotel.items.interactions.InteractionWaterItem;
import com.eu.habbo.habbohotel.items.interactions.interfaces.ConditionalGate;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.RideablePet;
import com.eu.habbo.habbohotel.users.DanceType;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserStatusComposer;
import com.eu.habbo.plugin.events.roomunit.RoomUnitLookAtPointEvent;
import com.eu.habbo.plugin.events.roomunit.RoomUnitSetGoalEvent;
import com.eu.habbo.plugin.events.users.UserIdleEvent;
import com.eu.habbo.plugin.events.users.UserTakeStepEvent;
import com.eu.habbo.threading.runnables.RoomUnitKick;
import com.eu.habbo.util.pathfinding.Rotation;
import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/rooms/RoomUnit.class */
public class RoomUnit {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoomUnit.class);
    private RoomTile startLocation;
    private RoomTile previousLocation;
    private double previousLocationZ;
    private RoomTile currentLocation;
    private RoomTile goalLocation;
    private double z;
    private int tilesWalked;
    private int effectEndTimestamp;
    private ScheduledFuture moveBlockingTask;
    private int idleTimer;
    private Room room;
    public boolean isWiredTeleporting = false;
    public boolean isLeavingTeleporter = false;
    public boolean canRotate = true;
    public boolean animateWalk = false;
    public boolean cmdTeleport = false;
    public boolean cmdSit = false;
    public boolean cmdStand = false;
    public boolean cmdLay = false;
    public boolean sitUpdate = false;
    public boolean isTeleporting = false;
    public int kickCount = 0;
    private boolean fastWalk = false;
    private boolean statusUpdate = false;
    private boolean invisible = false;
    private boolean lastCycleStatus = false;
    private boolean canLeaveRoomByDoor = true;
    private RoomUserRotation bodyRotation = RoomUserRotation.NORTH;
    private RoomUserRotation headRotation = RoomUserRotation.NORTH;
    private Deque<RoomTile> path = new LinkedList();
    private RoomRightLevels rightsLevel = RoomRightLevels.NONE;
    private int id = 0;
    private boolean inRoom = false;
    private boolean canWalk = true;
    private final ConcurrentHashMap<RoomUnitStatus, String> status = new ConcurrentHashMap<>();
    private final THashMap<String, Object> cacheable = new THashMap<>();
    private RoomUnitType roomUnitType = RoomUnitType.UNKNOWN;
    private DanceType danceType = DanceType.NONE;
    private int handItem = 0;
    private long handItemTimestamp = 0;
    private int walkTimeOut = Emulator.getIntUnixTimestamp();
    private int effectId = 0;
    public boolean isKicked = false;
    private THashSet<Integer> overridableTiles = new THashSet<>();
    private int timeInRoom = 0;

    public void clearWalking() {
        this.goalLocation = null;
        this.startLocation = this.currentLocation;
        this.inRoom = false;
        this.status.clear();
        this.cacheable.clear();
    }

    public void stopWalking() {
        synchronized (this.status) {
            this.status.remove(RoomUnitStatus.MOVE);
            setGoalLocation(this.currentLocation);
        }
    }

    public boolean cycle(Room room) {
        double heightAtSquare;
        RoomUnit roomUnit;
        HabboItem tallestChair;
        try {
            Habbo rider = null;
            if (getRoomUnitType() == RoomUnitType.PET) {
                Pet pet = room.getPet(this);
                if (pet instanceof RideablePet) {
                    rider = ((RideablePet) pet).getRider();
                }
            }
            if (rider != null) {
                if (this.status.containsKey(RoomUnitStatus.MOVE) && !rider.getRoomUnit().getStatusMap().containsKey(RoomUnitStatus.MOVE)) {
                    this.status.remove(RoomUnitStatus.MOVE);
                }
                if (rider.getRoomUnit().getCurrentLocation().x != getX() || rider.getRoomUnit().getCurrentLocation().y != getY()) {
                    this.status.put(RoomUnitStatus.MOVE, ((int) rider.getRoomUnit().getCurrentLocation().x) + "," + ((int) rider.getRoomUnit().getCurrentLocation().y) + "," + rider.getRoomUnit().getCurrentLocation().getStackHeight());
                    setPreviousLocation(rider.getRoomUnit().getPreviousLocation());
                    setPreviousLocationZ(rider.getRoomUnit().getPreviousLocation().getStackHeight());
                    setCurrentLocation(rider.getRoomUnit().getCurrentLocation());
                    setZ(rider.getRoomUnit().getCurrentLocation().getStackHeight());
                }
                return this.statusUpdate;
            }
            if (!isWalking() && !this.isKicked && this.status.remove(RoomUnitStatus.MOVE) == null) {
                Habbo habbo = room.getHabbo(this);
                if (habbo == null) {
                    return true;
                }
                habbo.getHabboInfo().getRiding().getRoomUnit().status.remove(RoomUnitStatus.MOVE);
                return true;
            }
            if (this.status.remove(RoomUnitStatus.SIT) != null) {
                this.statusUpdate = true;
            }
            if (this.status.remove(RoomUnitStatus.MOVE) != null) {
                this.statusUpdate = true;
            }
            if (this.status.remove(RoomUnitStatus.LAY) != null) {
                this.statusUpdate = true;
            }
            for (Map.Entry<RoomUnitStatus, String> entry : this.status.entrySet()) {
                if (entry.getKey().removeWhenWalking) {
                    this.status.remove(entry.getKey());
                }
            }
            if (this.path == null || this.path.isEmpty()) {
                return true;
            }
            boolean z = true;
            Habbo habbo2 = room.getHabbo(this);
            if (habbo2 != null && habbo2.getHabboInfo().getRiding() != null) {
                z = false;
            }
            RoomTile roomTilePoll = this.path.poll();
            boolean z2 = roomTilePoll != null && canOverrideTile(roomTilePoll);
            if (this.path.isEmpty()) {
                this.sitUpdate = true;
                if (roomTilePoll != null && roomTilePoll.hasUnits() && !z2) {
                    return false;
                }
            }
            Deque<RoomTile> dequeFindPath = room.getLayout().findPath(this.currentLocation, this.path.peek(), this.goalLocation, this);
            if (dequeFindPath == null) {
                dequeFindPath = new LinkedList();
            }
            if (dequeFindPath.size() >= 3) {
                if (this.path.isEmpty()) {
                    return true;
                }
                this.path.pop();
                dequeFindPath.removeLast();
                if (dequeFindPath.peek() != roomTilePoll) {
                    roomTilePoll = dequeFindPath.poll();
                    for (int i = 0; i < dequeFindPath.size(); i++) {
                        this.path.addFirst(dequeFindPath.removeLast());
                    }
                }
            }
            if (z && this.fastWalk && this.path.size() > 1) {
                roomTilePoll = this.path.poll();
            }
            if (roomTilePoll == null) {
                return true;
            }
            Habbo habbo3 = room.getHabbo(this);
            this.status.remove(RoomUnitStatus.DEAD);
            if (habbo3 != null) {
                if (isIdle()) {
                    UserIdleEvent userIdleEvent = new UserIdleEvent(habbo3, UserIdleEvent.IdleReason.WALKED, false);
                    Emulator.getPluginManager().fireEvent(userIdleEvent);
                    if (!userIdleEvent.isCancelled() && !userIdleEvent.idle) {
                        room.unIdle(habbo3);
                        this.idleTimer = 0;
                    }
                }
                if (Emulator.getPluginManager().isRegistered(UserTakeStepEvent.class, false)) {
                    UserTakeStepEvent userTakeStepEvent = new UserTakeStepEvent(habbo3, room.getLayout().getTile(getX(), getY()), roomTilePoll);
                    Emulator.getPluginManager().fireEvent(userTakeStepEvent);
                    if (userTakeStepEvent.isCancelled()) {
                        return true;
                    }
                }
            }
            HabboItem topItemAt = room.getTopItemAt(roomTilePoll.x, roomTilePoll.y);
            double stackHeight = roomTilePoll.getStackHeight() - this.currentLocation.getStackHeight();
            if (!room.tileWalkable(roomTilePoll) || ((!RoomLayout.ALLOW_FALLING && stackHeight < (-RoomLayout.MAXIMUM_STEP_HEIGHT)) || (roomTilePoll.state == RoomTileState.OPEN && stackHeight > RoomLayout.MAXIMUM_STEP_HEIGHT))) {
                this.room = room;
                this.path.clear();
                findPath();
                if (this.path.isEmpty()) {
                    this.status.remove(RoomUnitStatus.MOVE);
                    return false;
                }
                roomTilePoll = this.path.pop();
            }
            if (room.canSitAt(roomTilePoll.x, roomTilePoll.y) && (tallestChair = room.getTallestChair(roomTilePoll)) != null) {
                topItemAt = tallestChair;
            }
            if (roomTilePoll.equals(this.goalLocation) && roomTilePoll.state == RoomTileState.SIT && !z2 && (topItemAt == null || topItemAt.getZ() - getZ() > RoomLayout.MAXIMUM_STEP_HEIGHT)) {
                this.status.remove(RoomUnitStatus.MOVE);
                return false;
            }
            double d = 0.0d;
            if (habbo3 != null && habbo3.getHabboInfo().getRiding() != null) {
                d = 0.0d + 1.0d;
            }
            HabboItem topItemAt2 = room.getTopItemAt(getX(), getY());
            if (topItemAt2 != null && (topItemAt2 != topItemAt || !RoomLayout.pointInSquare(topItemAt2.getX(), topItemAt2.getY(), (topItemAt2.getX() + topItemAt2.getBaseItem().getWidth()) - 1, (topItemAt2.getY() + topItemAt2.getBaseItem().getLength()) - 1, roomTilePoll.x, roomTilePoll.y))) {
                topItemAt2.onWalkOff(this, room, new Object[]{getCurrentLocation(), roomTilePoll});
            }
            this.tilesWalked++;
            RoomUserRotation bodyRotation = getBodyRotation();
            setRotation(RoomUserRotation.values()[Rotation.Calculate(getX(), getY(), roomTilePoll.x, roomTilePoll.y)]);
            if (topItemAt != null) {
                if (topItemAt == topItemAt2 && RoomLayout.pointInSquare(topItemAt.getX(), topItemAt.getY(), (topItemAt.getX() + topItemAt.getBaseItem().getWidth()) - 1, (topItemAt.getY() + topItemAt.getBaseItem().getLength()) - 1, getX(), getY())) {
                    topItemAt.onWalk(this, room, new Object[]{getCurrentLocation(), roomTilePoll});
                } else if (topItemAt.canWalkOn(this, room, null)) {
                    topItemAt.onWalkOn(this, room, new Object[]{getCurrentLocation(), roomTilePoll});
                } else if (topItemAt instanceof ConditionalGate) {
                    setRotation(bodyRotation);
                    this.tilesWalked--;
                    setGoalLocation(this.currentLocation);
                    this.status.remove(RoomUnitStatus.MOVE);
                    room.sendComposer(new RoomUserStatusComposer(this).compose());
                    if (habbo3 == null) {
                        return false;
                    }
                    ((ConditionalGate) topItemAt).onRejected(this, getRoom(), new Object[0]);
                    return false;
                }
                heightAtSquare = d + topItemAt.getZ();
                if (!topItemAt.getBaseItem().allowSit() && !topItemAt.getBaseItem().allowLay()) {
                    heightAtSquare += Item.getCurrentHeight(topItemAt);
                }
            } else {
                heightAtSquare = d + ((double) room.getLayout().getHeightAtSquare(roomTilePoll.x, roomTilePoll.y));
            }
            setPreviousLocation(getCurrentLocation());
            setStatus(RoomUnitStatus.MOVE, ((int) roomTilePoll.x) + "," + ((int) roomTilePoll.y) + "," + heightAtSquare);
            if (habbo3 != null && habbo3.getHabboInfo().getRiding() != null && (roomUnit = habbo3.getHabboInfo().getRiding().getRoomUnit()) != null) {
                roomUnit.setPreviousLocationZ(getZ());
                setZ(heightAtSquare - 1.0d);
                roomUnit.setRotation(RoomUserRotation.values()[Rotation.Calculate(getX(), getY(), roomTilePoll.x, roomTilePoll.y)]);
                roomUnit.setPreviousLocation(getCurrentLocation());
                roomUnit.setGoalLocation(getGoal());
                roomUnit.setStatus(RoomUnitStatus.MOVE, ((int) roomTilePoll.x) + "," + ((int) roomTilePoll.y) + "," + (heightAtSquare - 1.0d));
                room.sendComposer(new RoomUserStatusComposer(roomUnit).compose());
            }
            setZ(heightAtSquare);
            setCurrentLocation(room.getLayout().getTile(roomTilePoll.x, roomTilePoll.y));
            resetIdleTimer();
            if (habbo3 == null) {
                return false;
            }
            HabboItem topItemAt3 = room.getTopItemAt(roomTilePoll.x, roomTilePoll.y);
            boolean z3 = roomTilePoll.x == room.getLayout().getDoorX() && roomTilePoll.y == room.getLayout().getDoorY();
            boolean z4 = !room.isPublicRoom() || Emulator.getConfig().getBoolean("hotel.room.public.doortile.kick");
            boolean z5 = topItemAt3 != null && topItemAt3.invalidatesToRoomKick();
            if (!this.canLeaveRoomByDoor || !z3 || !z4 || z5) {
                return false;
            }
            Emulator.getThreading().run(new RoomUnitKick(habbo3, room, false), 500L);
            return false;
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
            return false;
        }
    }

    public int getId() {
        return this.id;
    }

    public void setId(int i) {
        this.id = i;
    }

    public RoomTile getCurrentLocation() {
        return this.currentLocation;
    }

    public void setCurrentLocation(RoomTile roomTile) {
        if (roomTile != null) {
            if (this.currentLocation != null) {
                this.currentLocation.removeUnit(this);
            }
            this.currentLocation = roomTile;
            roomTile.addUnit(this);
        }
    }

    public short getX() {
        return this.currentLocation.x;
    }

    public short getY() {
        return this.currentLocation.y;
    }

    public double getZ() {
        return this.z;
    }

    public void setZ(double d) {
        Bot bot;
        this.z = d;
        if (this.room == null || (bot = this.room.getBot(this)) == null) {
            return;
        }
        bot.needsUpdate(true);
    }

    public boolean isInRoom() {
        return this.inRoom;
    }

    public synchronized void setInRoom(boolean z) {
        this.inRoom = z;
    }

    public RoomUnitType getRoomUnitType() {
        return this.roomUnitType;
    }

    public synchronized void setRoomUnitType(RoomUnitType roomUnitType) {
        this.roomUnitType = roomUnitType;
    }

    public void setRotation(RoomUserRotation roomUserRotation) {
        this.bodyRotation = roomUserRotation;
        this.headRotation = roomUserRotation;
    }

    public RoomUserRotation getBodyRotation() {
        return this.bodyRotation;
    }

    public void setBodyRotation(RoomUserRotation roomUserRotation) {
        this.bodyRotation = roomUserRotation;
    }

    public RoomUserRotation getHeadRotation() {
        return this.headRotation;
    }

    public void setHeadRotation(RoomUserRotation roomUserRotation) {
        this.headRotation = roomUserRotation;
    }

    public DanceType getDanceType() {
        return this.danceType;
    }

    public synchronized void setDanceType(DanceType danceType) {
        this.danceType = danceType;
    }

    public void setCanWalk(boolean z) {
        this.canWalk = z;
    }

    public boolean canWalk() {
        return this.canWalk;
    }

    public boolean isFastWalk() {
        return this.fastWalk;
    }

    public void setFastWalk(boolean z) {
        this.fastWalk = z;
    }

    public RoomTile getStartLocation() {
        return this.startLocation;
    }

    public int tilesWalked() {
        return this.tilesWalked;
    }

    public RoomTile getGoal() {
        return this.goalLocation;
    }

    public void setGoalLocation(RoomTile roomTile) {
        if (roomTile != null) {
            setGoalLocation(roomTile, false);
        }
    }

    public void setGoalLocation(RoomTile roomTile, boolean z) {
        if (Emulator.getPluginManager().isRegistered(RoomUnitSetGoalEvent.class, false)) {
            RoomUnitSetGoalEvent roomUnitSetGoalEvent = new RoomUnitSetGoalEvent(this.room, this, roomTile);
            Emulator.getPluginManager().fireEvent(roomUnitSetGoalEvent);
            if (roomUnitSetGoalEvent.isCancelled()) {
                return;
            }
        }
        this.startLocation = this.currentLocation;
        if (roomTile == null || z) {
            return;
        }
        boolean zHasStatus = hasStatus(RoomUnitStatus.MOVE);
        this.goalLocation = roomTile;
        findPath();
        if (this.path.isEmpty()) {
            this.goalLocation = this.currentLocation;
        } else {
            this.tilesWalked = zHasStatus ? this.tilesWalked : 0;
            this.cmdSit = false;
        }
    }

    public void setLocation(RoomTile roomTile) {
        if (roomTile != null) {
            this.startLocation = roomTile;
            setPreviousLocation(roomTile);
            setCurrentLocation(roomTile);
            this.goalLocation = roomTile;
        }
    }

    public RoomTile getPreviousLocation() {
        return this.previousLocation;
    }

    public void setPreviousLocation(RoomTile roomTile) {
        this.previousLocation = roomTile;
        this.previousLocationZ = this.z;
    }

    public double getPreviousLocationZ() {
        return this.previousLocationZ;
    }

    public void setPreviousLocationZ(double d) {
        this.previousLocationZ = d;
    }

    public void setPathFinderRoom(Room room) {
        this.room = room;
    }

    public void findPath() {
        Deque<RoomTile> dequeFindPath;
        if (this.room == null || this.room.getLayout() == null || this.goalLocation == null) {
            return;
        }
        if ((this.goalLocation.isWalkable() || this.room.canSitOrLayAt(this.goalLocation.x, this.goalLocation.y) || canOverrideTile(this.goalLocation)) && (dequeFindPath = this.room.getLayout().findPath(this.currentLocation, this.goalLocation, this.goalLocation, this)) != null) {
            this.path = dequeFindPath;
        }
    }

    public boolean isAtGoal() {
        return this.currentLocation.equals(this.goalLocation);
    }

    public boolean isWalking() {
        return !isAtGoal() && this.canWalk;
    }

    public String getStatus(RoomUnitStatus roomUnitStatus) {
        return this.status.get(roomUnitStatus);
    }

    public ConcurrentHashMap<RoomUnitStatus, String> getStatusMap() {
        return this.status;
    }

    public void removeStatus(RoomUnitStatus roomUnitStatus) {
        this.status.remove(roomUnitStatus);
    }

    public void setStatus(RoomUnitStatus roomUnitStatus, String str) {
        if (roomUnitStatus == null || str == null) {
            return;
        }
        this.status.put(roomUnitStatus, str);
    }

    public boolean hasStatus(RoomUnitStatus roomUnitStatus) {
        return this.status.containsKey(roomUnitStatus);
    }

    public void clearStatus() {
        this.status.clear();
    }

    public void statusUpdate(boolean z) {
        this.statusUpdate = z;
    }

    public boolean needsStatusUpdate() {
        return this.statusUpdate;
    }

    public TMap<String, Object> getCacheable() {
        return this.cacheable;
    }

    public int getHandItem() {
        return this.handItem;
    }

    public void setHandItem(int i) {
        this.handItem = i;
        this.handItemTimestamp = System.currentTimeMillis();
    }

    public long getHandItemTimestamp() {
        return this.handItemTimestamp;
    }

    public int getEffectId() {
        return this.effectId;
    }

    public void setEffectId(int i, int i2) {
        this.effectId = i;
        this.effectEndTimestamp = i2;
    }

    public int getEffectEndTimestamp() {
        return this.effectEndTimestamp;
    }

    public int getWalkTimeOut() {
        return this.walkTimeOut;
    }

    public void setWalkTimeOut(int i) {
        this.walkTimeOut = i;
    }

    public void increaseTimeInRoom() {
        this.timeInRoom++;
    }

    public int getTimeInRoom() {
        return this.timeInRoom;
    }

    public void resetTimeInRoom() {
        this.timeInRoom = 0;
    }

    public void increaseIdleTimer() {
        this.idleTimer++;
    }

    public boolean isIdle() {
        return this.idleTimer > Room.IDLE_CYCLES;
    }

    public int getIdleTimer() {
        return this.idleTimer;
    }

    public void resetIdleTimer() {
        this.idleTimer = 0;
    }

    public void setIdle() {
        this.idleTimer = Room.IDLE_CYCLES + 1;
    }

    public void lookAtPoint(RoomTile roomTile) {
        if (this.canRotate) {
            if (Emulator.getPluginManager().isRegistered(RoomUnitLookAtPointEvent.class, false)) {
                RoomUnitLookAtPointEvent roomUnitLookAtPointEvent = new RoomUnitLookAtPointEvent(this.room, this, roomTile);
                Emulator.getPluginManager().fireEvent(roomUnitLookAtPointEvent);
                if (roomUnitLookAtPointEvent.isCancelled()) {
                    return;
                }
            }
            if (this.status.containsKey(RoomUnitStatus.LAY)) {
                return;
            }
            if (!this.status.containsKey(RoomUnitStatus.SIT)) {
                this.bodyRotation = RoomUserRotation.values()[Rotation.Calculate(getX(), getY(), roomTile.x, roomTile.y)];
            }
            RoomUserRotation roomUserRotation = RoomUserRotation.values()[Rotation.Calculate(getX(), getY(), roomTile.x, roomTile.y)];
            if (Math.abs(roomUserRotation.getValue() - this.bodyRotation.getValue()) <= 1) {
                this.headRotation = roomUserRotation;
            }
        }
    }

    public Deque<RoomTile> getPath() {
        return this.path;
    }

    public void setPath(Deque<RoomTile> deque) {
        this.path = deque;
    }

    public RoomRightLevels getRightsLevel() {
        return this.rightsLevel;
    }

    public void setRightsLevel(RoomRightLevels roomRightLevels) {
        this.rightsLevel = roomRightLevels;
    }

    public boolean isInvisible() {
        return this.invisible;
    }

    public void setInvisible(boolean z) {
        this.invisible = z;
    }

    public Room getRoom() {
        return this.room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public boolean canOverrideTile(RoomTile roomTile) {
        if (roomTile == null || this.room == null || this.room.getLayout() == null) {
            return false;
        }
        if (this.room.getItemsAt(roomTile).stream().anyMatch(habboItem -> {
            return habboItem.canOverrideTile(this, this.room, roomTile);
        })) {
            return true;
        }
        return this.overridableTiles.contains(Integer.valueOf((roomTile.x & 255) | (roomTile.y << 12)));
    }

    public void addOverrideTile(RoomTile roomTile) {
        int i = (roomTile.x & 255) | (roomTile.y << 12);
        if (this.overridableTiles.contains(Integer.valueOf(i))) {
            return;
        }
        this.overridableTiles.add(Integer.valueOf(i));
    }

    public void removeOverrideTile(RoomTile roomTile) {
        if (this.room == null || this.room.getLayout() == null) {
            return;
        }
        this.overridableTiles.remove(Integer.valueOf((roomTile.x & 255) | (roomTile.y << 12)));
    }

    public void clearOverrideTiles() {
        this.overridableTiles.clear();
    }

    public boolean canLeaveRoomByDoor() {
        return this.canLeaveRoomByDoor;
    }

    public void setCanLeaveRoomByDoor(boolean z) {
        this.canLeaveRoomByDoor = z;
    }

    public boolean canForcePosture() {
        if (this.room == null) {
            return false;
        }
        HabboItem topItemAt = this.room.getTopItemAt(getX(), getY());
        return topItemAt == null || !((topItemAt instanceof InteractionWater) || (topItemAt instanceof InteractionWaterItem));
    }

    public RoomTile getClosestTile(List<RoomTile> list) {
        return list.stream().min(Comparator.comparingDouble(roomTile -> {
            return roomTile.distance(getCurrentLocation());
        })).orElse(null);
    }

    public RoomTile getClosestAdjacentTile(short s, short s2, boolean z) {
        RoomTile tile;
        if (this.room == null || (tile = this.room.getLayout().getTile(s, s2)) == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        arrayList.add(Integer.valueOf(RoomUserRotation.SOUTH.getValue()));
        arrayList.add(Integer.valueOf(RoomUserRotation.NORTH.getValue()));
        arrayList.add(Integer.valueOf(RoomUserRotation.EAST.getValue()));
        arrayList.add(Integer.valueOf(RoomUserRotation.WEST.getValue()));
        if (z) {
            arrayList.add(Integer.valueOf(RoomUserRotation.NORTH_EAST.getValue()));
            arrayList.add(Integer.valueOf(RoomUserRotation.NORTH_WEST.getValue()));
            arrayList.add(Integer.valueOf(RoomUserRotation.SOUTH_EAST.getValue()));
            arrayList.add(Integer.valueOf(RoomUserRotation.SOUTH_WEST.getValue()));
        }
        return getClosestTile((List) arrayList.stream().map(num -> {
            return this.room.getLayout().getTileInFront(tile, num.intValue());
        }).filter(roomTile -> {
            return roomTile != null && roomTile.isWalkable() && (getCurrentLocation().equals(roomTile) || !this.room.hasHabbosAt(roomTile.x, roomTile.y));
        }).collect(Collectors.toList()));
    }

    public ScheduledFuture getMoveBlockingTask() {
        return this.moveBlockingTask;
    }

    public void setMoveBlockingTask(ScheduledFuture scheduledFuture) {
        this.moveBlockingTask = scheduledFuture;
    }
}
