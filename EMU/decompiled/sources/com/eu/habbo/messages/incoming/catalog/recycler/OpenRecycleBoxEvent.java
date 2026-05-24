package com.eu.habbo.messages.incoming.catalog.recycler;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionGift;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessage;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.inventory.AddHabboItemComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import com.eu.habbo.messages.outgoing.rooms.UpdateStackHeightComposer;
import com.eu.habbo.messages.outgoing.rooms.items.PresentItemOpenedComposer;
import com.eu.habbo.messages.outgoing.rooms.items.RemoveFloorItemComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserWhisperComposer;
import com.eu.habbo.threading.runnables.OpenGift;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/catalog/recycler/OpenRecycleBoxEvent.class */
public class OpenRecycleBoxEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        HabboItem habboItem;
        RoomTile tile;
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null) {
            return;
        }
        if ((currentRoom.getOwnerId() == this.client.getHabbo().getHabboInfo().getId() || this.client.getHabbo().hasPermission(Permission.ACC_ANYROOMOWNER)) && (habboItem = currentRoom.getHabboItem(this.packet.readInt().intValue())) != null) {
            if (habboItem instanceof InteractionGift) {
                if (habboItem.getBaseItem().getName().contains("present_wrap")) {
                    ((InteractionGift) habboItem).explode = true;
                    currentRoom.updateItem(habboItem);
                }
                Emulator.getThreading().run(new OpenGift(habboItem, this.client.getHabbo(), currentRoom), habboItem.getBaseItem().getName().contains("present_wrap") ? 1000L : 0L);
            } else {
                if (habboItem.getExtradata().length() == 0) {
                    this.client.sendResponse(new RoomUserWhisperComposer(new RoomChatMessage(Emulator.getTexts().getValue("error.recycler.box.empty"), this.client.getHabbo(), this.client.getHabbo(), RoomChatMessageBubbles.BOT)));
                } else {
                    HabboItem habboItemHandleOpenRecycleBox = Emulator.getGameEnvironment().getItemManager().handleOpenRecycleBox(this.client.getHabbo(), habboItem);
                    if (habboItemHandleOpenRecycleBox != null) {
                        this.client.getHabbo().getInventory().getItemsComponent().addItem(habboItemHandleOpenRecycleBox);
                        this.client.sendResponse(new AddHabboItemComposer(habboItemHandleOpenRecycleBox));
                        this.client.sendResponse(new InventoryRefreshComposer());
                        this.client.sendResponse(new PresentItemOpenedComposer(habboItemHandleOpenRecycleBox, habboItem.getExtradata(), true));
                    }
                }
                currentRoom.sendComposer(new RemoveFloorItemComposer(habboItem).compose());
                currentRoom.removeHabboItem(habboItem);
            }
            if (habboItem.getRoomId() == 0) {
                currentRoom.updateTile(currentRoom.getLayout().getTile(habboItem.getX(), habboItem.getY()));
                RoomLayout layout = currentRoom.getLayout();
                short stackHeight = (short) currentRoom.getStackHeight(habboItem.getX(), habboItem.getY(), true);
                if (layout != null && (tile = layout.getTile(habboItem.getX(), habboItem.getY())) != null) {
                    stackHeight = tile.z;
                }
                currentRoom.sendComposer(new UpdateStackHeightComposer(habboItem.getX(), habboItem.getY(), stackHeight, currentRoom.getStackHeight(habboItem.getX(), habboItem.getY(), true)).compose());
            }
        }
    }
}
