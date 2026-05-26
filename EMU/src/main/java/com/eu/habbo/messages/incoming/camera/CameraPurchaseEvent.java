package com.eu.habbo.messages.incoming.camera;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.camera.CameraPurchaseSuccesfullComposer;
import com.eu.habbo.messages.outgoing.catalog.NotEnoughPointsTypeComposer;
import com.eu.habbo.messages.outgoing.inventory.AddHabboItemComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import com.eu.habbo.plugin.events.users.UserPurchasePictureEvent;

public class CameraPurchaseEvent extends MessageHandler {
    public static int CAMERA_PURCHASE_CREDITS = 5;
    public static int CAMERA_PURCHASE_POINTS = 5;
    public static int CAMERA_PURCHASE_POINTS_TYPE = 0;

    @Override
    public void handle() throws Exception {
        // Hardened: short-circuit if the client is mid-disconnect or the Habbo
        // session was reaped between dispatch and handle. Avoids NPE-spam on
        // every camera purchase under churn.
        com.eu.habbo.habbohotel.users.Habbo me = this.requireHabbo();
        if (me == null) return;
        com.eu.habbo.habbohotel.users.HabboInfo info = me.getHabboInfo();
        com.eu.habbo.habbohotel.rooms.Room currentRoom = info.getCurrentRoom();
        if (currentRoom == null) return;

        if (info.getCredits() < CameraPurchaseEvent.CAMERA_PURCHASE_CREDITS) {
            this.client.sendResponse(new NotEnoughPointsTypeComposer(true, false, 0));
            return;
        }

        if (info.getCurrencyAmount(CameraPurchaseEvent.CAMERA_PURCHASE_POINTS_TYPE) < CameraPurchaseEvent.CAMERA_PURCHASE_POINTS) {
            this.client.sendResponse(new NotEnoughPointsTypeComposer(false, true, CameraPurchaseEvent.CAMERA_PURCHASE_POINTS_TYPE));
            return;
        }

        if (info.getPhotoTimestamp() == 0) return;
        if (info.getPhotoJSON() == null || info.getPhotoJSON().isEmpty()) return;
        if (!info.getPhotoJSON().contains(info.getPhotoTimestamp() + ""))
            return;

        if (Emulator.getPluginManager().fireEvent(new UserPurchasePictureEvent(me, info.getPhotoURL(), currentRoom.getId(), info.getPhotoTimestamp())).isCancelled()) {
            return;
        }

        HabboItem photoItem = Emulator.getGameEnvironment().getItemManager().createItem(this.client.getHabbo().getHabboInfo().getId(), Emulator.getGameEnvironment().getItemManager().getItem(Emulator.getConfig().getInt("camera.item_id")), 0, 0, this.client.getHabbo().getHabboInfo().getPhotoJSON());

        if (photoItem != null) {
            photoItem.setExtradata(photoItem.getExtradata().replace("%id%", photoItem.getId() + ""));
            photoItem.needsUpdate(true);

            this.client.getHabbo().getInventory().getItemsComponent().addItem(photoItem);

            this.client.sendResponse(new CameraPurchaseSuccesfullComposer());
            this.client.sendResponse(new AddHabboItemComposer(photoItem));
            this.client.sendResponse(new InventoryRefreshComposer());

            this.client.getHabbo().giveCredits(-CameraPurchaseEvent.CAMERA_PURCHASE_CREDITS);
            this.client.getHabbo().givePoints(CameraPurchaseEvent.CAMERA_PURCHASE_POINTS_TYPE, -CameraPurchaseEvent.CAMERA_PURCHASE_POINTS);

            AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("CameraPhotoCount"));
        }
    }
}