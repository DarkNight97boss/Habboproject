package com.eu.habbo.messages.incoming.rooms.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.pets.PetTasks;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUnitOnRollerComposer;
import com.eu.habbo.plugin.events.users.UserIdleEvent;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/users/RoomUserWalkEvent.class */
public class RoomUserWalkEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoomUserWalkEvent.class);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public int getRatelimit() {
        return 500;
    }

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        HabboItem topItemAt;
        if (this.client.getHabbo().getHabboInfo().getCurrentRoom() != null) {
            int iIntValue = this.packet.readInt().intValue();
            int iIntValue2 = this.packet.readInt().intValue();
            Habbo habbo = this.client.getHabbo();
            RoomUnit roomUnit = this.client.getHabbo().getRoomUnit();
            if (roomUnit.isTeleporting || roomUnit.isKicked) {
                return;
            }
            if (roomUnit.getCacheable().get("control") != null) {
                habbo = (Habbo) roomUnit.getCacheable().get("control");
                if (habbo.getHabboInfo().getCurrentRoom() != this.client.getHabbo().getHabboInfo().getCurrentRoom()) {
                    habbo.getRoomUnit().getCacheable().remove("controller");
                    this.client.getHabbo().getRoomUnit().getCacheable().remove("control");
                    habbo = this.client.getHabbo();
                }
            }
            RoomUnit roomUnit2 = habbo.getRoomUnit();
            Room currentRoom = habbo.getHabboInfo().getCurrentRoom();
            if (roomUnit2 != null) {
                try {
                    if (roomUnit2.isInRoom() && roomUnit2.canWalk()) {
                        if (roomUnit2.cmdTeleport) {
                            RoomTile tile = currentRoom.getLayout().getTile((short) iIntValue, (short) iIntValue2);
                            currentRoom.sendComposer(new RoomUnitOnRollerComposer(roomUnit2, tile, currentRoom).compose());
                            if (habbo.getHabboInfo().getRiding() != null) {
                                currentRoom.sendComposer(new RoomUnitOnRollerComposer(habbo.getHabboInfo().getRiding().getRoomUnit(), tile, currentRoom).compose());
                            }
                        } else {
                            if (habbo.getHabboInfo().getRiding() != null && habbo.getHabboInfo().getRiding().getTask() != null && habbo.getHabboInfo().getRiding().getTask().equals(PetTasks.JUMP)) {
                                return;
                            }
                            if ((iIntValue == roomUnit2.getX() && iIntValue2 == roomUnit2.getY()) || currentRoom == null || currentRoom.getLayout() == null) {
                                return;
                            }
                            if (roomUnit2.isIdle()) {
                                UserIdleEvent userIdleEvent = new UserIdleEvent(habbo, UserIdleEvent.IdleReason.WALKED, false);
                                Emulator.getPluginManager().fireEvent(userIdleEvent);
                                if (!userIdleEvent.isCancelled() && !userIdleEvent.idle) {
                                    if (roomUnit2.getRoom() != null) {
                                        roomUnit2.getRoom().unIdle(habbo);
                                    }
                                    roomUnit2.resetIdleTimer();
                                }
                            }
                            RoomTile tile2 = currentRoom.getLayout().getTile((short) iIntValue, (short) iIntValue2);
                            if (tile2 == null) {
                                return;
                            }
                            if (habbo.getRoomUnit().hasStatus(RoomUnitStatus.LAY) && currentRoom.getLayout().getTilesInFront(habbo.getRoomUnit().getCurrentLocation(), habbo.getRoomUnit().getBodyRotation().getValue(), 2).contains(tile2)) {
                                return;
                            }
                            if (currentRoom.canLayAt(tile2.x, tile2.y) && (topItemAt = currentRoom.getTopItemAt(tile2.x, tile2.y)) != null && topItemAt.getBaseItem().allowLay()) {
                                RoomTile tile3 = currentRoom.getLayout().getTile(topItemAt.getX(), topItemAt.getY());
                                switch (topItemAt.getRotation()) {
                                    case 0:
                                    case 4:
                                        tile3 = currentRoom.getLayout().getTile((short) iIntValue, topItemAt.getY());
                                        break;
                                    case 2:
                                    case 8:
                                        tile3 = currentRoom.getLayout().getTile(topItemAt.getX(), (short) iIntValue2);
                                        break;
                                }
                                if (tile3 != null && currentRoom.canLayAt(tile3.x, tile3.y)) {
                                    roomUnit2.setGoalLocation(tile3);
                                    return;
                                }
                            }
                            THashSet<HabboItem> itemsAt = currentRoom.getItemsAt(tile2);
                            if (itemsAt.size() > 0) {
                                TObjectHashIterator it = itemsAt.iterator();
                                while (true) {
                                    if (it.hasNext()) {
                                        RoomTile overrideGoalTile = ((HabboItem) it.next()).getOverrideGoalTile(roomUnit2, currentRoom, tile2);
                                        if (overrideGoalTile == null) {
                                            return;
                                        }
                                        if (!overrideGoalTile.equals(tile2) && overrideGoalTile.isWalkable()) {
                                            tile2 = overrideGoalTile;
                                        }
                                    }
                                }
                            }
                            if (tile2.isWalkable() || currentRoom.canSitOrLayAt(tile2.x, tile2.y)) {
                                if (roomUnit2.getMoveBlockingTask() != null) {
                                    roomUnit2.getMoveBlockingTask().get();
                                }
                                roomUnit2.setGoalLocation(tile2);
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Caught exception", e);
                }
            }
        }
    }
}
