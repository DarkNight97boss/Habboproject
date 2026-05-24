package com.eu.habbo.messages.incoming.camera;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.camera.CameraRoomThumbnailSavedComposer;
import com.eu.habbo.networking.camera.CameraClient;
import com.eu.habbo.networking.camera.messages.outgoing.CameraRenderImageComposer;
import com.eu.habbo.util.crypto.ZIP;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/camera/CameraRoomThumbnailEvent.class */
public class CameraRoomThumbnailEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (!this.client.getHabbo().hasPermission("acc_camera")) {
            this.client.getHabbo().alert(Emulator.getTexts().getValue("camera.permission"));
            return;
        }
        if (this.client.getHabbo().getHabboInfo().getCurrentRoom().isOwner(this.client.getHabbo())) {
            if (!CameraClient.isLoggedIn) {
                this.client.sendResponse(new CameraRoomThumbnailSavedComposer());
                this.client.getHabbo().alert(Emulator.getTexts().getValue("camera.disabled"));
                return;
            }
            this.packet.getBuffer().readFloat();
            CameraRenderImageComposer cameraRenderImageComposer = new CameraRenderImageComposer(this.client.getHabbo().getHabboInfo().getId(), this.client.getHabbo().getHabboInfo().getCurrentRoom().getBackgroundTonerColor().getRGB(), 110, 110, new String(ZIP.inflate(this.packet.getBuffer().readBytes(this.packet.getBuffer().readableBytes()).array())));
            this.client.getHabbo().getHabboInfo().setPhotoJSON(Emulator.getConfig().getValue("camera.extradata").replace("%timestamp%", cameraRenderImageComposer.timestamp + Emulator.PREVIEW));
            this.client.getHabbo().getHabboInfo().setPhotoTimestamp(cameraRenderImageComposer.timestamp);
            Emulator.getCameraClient().sendMessage(cameraRenderImageComposer);
        }
    }
}
