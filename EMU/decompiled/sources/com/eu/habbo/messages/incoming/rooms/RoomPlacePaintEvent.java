package com.eu.habbo.messages.incoming.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.inventory.RemoveHabboItemComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomPaintComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/RoomPlacePaintEvent.class */
public class RoomPlacePaintEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (this.client.getHabbo().getHabboInfo().getCurrentRoom() == null) {
            return;
        }
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom.getOwnerId() == this.client.getHabbo().getHabboInfo().getId() || currentRoom.hasRights(this.client.getHabbo()) || this.client.getHabbo().hasPermission(Permission.ACC_PLACEFURNI)) {
            int iIntValue = this.packet.readInt().intValue();
            HabboItem habboItem = this.client.getHabbo().getInventory().getItemsComponent().getHabboItem(iIntValue);
            if (habboItem == null) {
                this.client.sendResponse(new RemoveHabboItemComposer(iIntValue));
                return;
            }
            if (habboItem.getBaseItem().getName().equals("floor")) {
                currentRoom.setFloorPaint(habboItem.getExtradata());
                AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("RoomDecoFloor"));
            } else if (habboItem.getBaseItem().getName().equals("wallpaper")) {
                currentRoom.setWallPaint(habboItem.getExtradata());
                AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("RoomDecoWallpaper"));
            } else {
                if (!habboItem.getBaseItem().getName().equals("landscape")) {
                    return;
                }
                currentRoom.setBackgroundPaint(habboItem.getExtradata());
                AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("RoomDecoLandscape"));
            }
            this.client.getHabbo().getInventory().getItemsComponent().removeHabboItem(habboItem);
            currentRoom.setNeedsUpdate(true);
            currentRoom.sendComposer(new RoomPaintComposer(habboItem.getBaseItem().getName(), habboItem.getExtradata()).compose());
            habboItem.needsDelete(true);
            Emulator.getThreading().run(habboItem);
            this.client.sendResponse(new RemoveHabboItemComposer(iIntValue));
        }
    }
}
