package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.items.interactions.InteractionPostIt;
import com.eu.habbo.habbohotel.items.interactions.InteractionStickyPole;
import com.eu.habbo.habbohotel.rooms.FurnitureMovementError;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;
import com.eu.habbo.messages.outgoing.inventory.RemoveHabboItemComposer;
import com.eu.habbo.messages.outgoing.rooms.items.AddWallItemComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/PostItPlaceEvent.class */
public class PostItPlaceEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        String string = this.packet.readString();
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom != null) {
            if (currentRoom.hasRights(this.client.getHabbo()) || !currentRoom.getRoomSpecialTypes().getItemsOfType(InteractionStickyPole.class).isEmpty()) {
                HabboItem habboItem = this.client.getHabbo().getInventory().getItemsComponent().getHabboItem(iIntValue);
                if (habboItem instanceof InteractionPostIt) {
                    if (currentRoom.getPostItNotes().size() >= Room.MAXIMUM_POSTITNOTES) {
                        this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, FurnitureMovementError.MAX_STICKIES.errorCode));
                        return;
                    }
                    currentRoom.addHabboItem(habboItem);
                    habboItem.setExtradata("FFFF33");
                    habboItem.setRoomId(this.client.getHabbo().getHabboInfo().getCurrentRoom().getId());
                    habboItem.setWallPosition(string);
                    habboItem.setUserId(this.client.getHabbo().getHabboInfo().getId());
                    habboItem.needsUpdate(true);
                    currentRoom.sendComposer(new AddWallItemComposer(habboItem, this.client.getHabbo().getHabboInfo().getUsername()).compose());
                    this.client.getHabbo().getInventory().getItemsComponent().removeHabboItem(habboItem);
                    this.client.sendResponse(new RemoveHabboItemComposer(habboItem.getGiftAdjustedId()));
                    habboItem.setFromGift(false);
                    Emulator.getThreading().run(habboItem);
                    if (currentRoom.getOwnerId() != this.client.getHabbo().getHabboInfo().getId()) {
                        AchievementManager.progressAchievement(currentRoom.getOwnerId(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("NotesReceived"));
                        AchievementManager.progressAchievement(this.client.getHabbo().getHabboInfo().getId(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("NotesLeft"));
                    }
                }
            }
        }
    }
}
