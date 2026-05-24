package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.interactions.InteractionCannon;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.hash.THashMap;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/CannonKickAction.class */
public class CannonKickAction implements Runnable {
    private final InteractionCannon cannon;
    private final Room room;
    private final GameClient client;

    public CannonKickAction(InteractionCannon interactionCannon, Room room, GameClient gameClient) {
        this.cannon = interactionCannon;
        this.room = room;
        this.client = gameClient;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.client != null) {
            this.client.getHabbo().getRoomUnit().setCanWalk(true);
        }
        THashMap tHashMap = new THashMap();
        tHashMap.put("title", "${notification.room.kick.cannonball.title}");
        tHashMap.put("message", "${notification.room.kick.cannonball.message}");
        List<RoomTile> tilesInFront = this.room.getLayout().getTilesInFront(this.room.getLayout().getTile(this.cannon.getX(), this.cannon.getY()), this.cannon.getRotation() + 6, 3);
        ServerMessage serverMessageCompose = new BubbleAlertComposer("cannon.png", (THashMap<String, String>) tHashMap).compose();
        for (RoomTile roomTile : tilesInFront) {
            TObjectHashIterator it = this.room.getHabbosAt(roomTile.x, roomTile.y).iterator();
            while (it.hasNext()) {
                Habbo habbo = (Habbo) it.next();
                if (!habbo.hasPermission(Permission.ACC_UNKICKABLE) && !this.room.isOwner(habbo)) {
                    Emulator.getGameEnvironment().getRoomManager().leaveRoom(habbo, this.room);
                    habbo.getClient().sendResponse(serverMessageCompose);
                }
            }
        }
    }
}
