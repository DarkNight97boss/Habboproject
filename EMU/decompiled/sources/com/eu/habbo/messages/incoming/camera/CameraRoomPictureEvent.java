package com.eu.habbo.messages.incoming.camera;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.networking.camera.CameraClient;
import com.eu.habbo.networking.camera.messages.outgoing.CameraRenderImageComposer;
import com.eu.habbo.util.crypto.ZIP;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/camera/CameraRoomPictureEvent.class */
public class CameraRoomPictureEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (!this.client.getHabbo().hasPermission("acc_camera")) {
            this.client.getHabbo().alert(Emulator.getTexts().getValue("camera.permission"));
            return;
        }
        if (!CameraClient.isLoggedIn) {
            this.client.getHabbo().alert(Emulator.getTexts().getValue("camera.disabled"));
            return;
        }
        this.packet.getBuffer().readFloat();
        CameraRenderImageComposer cameraRenderImageComposer = new CameraRenderImageComposer(this.client.getHabbo().getHabboInfo().getId(), this.client.getHabbo().getHabboInfo().getCurrentRoom().getBackgroundTonerColor().getRGB(), 320, 320, new String(ZIP.inflate(this.packet.getBuffer().readBytes(this.packet.getBuffer().readableBytes()).array())));
        this.client.getHabbo().getHabboInfo().setPhotoJSON(Emulator.getConfig().getValue("camera.extradata").replace("%timestamp%", cameraRenderImageComposer.timestamp + Emulator.PREVIEW));
        this.client.getHabbo().getHabboInfo().setPhotoTimestamp(cameraRenderImageComposer.timestamp);
        if (this.client.getHabbo().getHabboInfo().getCurrentRoom() != null) {
            this.client.getHabbo().getHabboInfo().setPhotoRoomId(this.client.getHabbo().getHabboInfo().getCurrentRoom().getId());
        }
        Emulator.getCameraClient().sendMessage(cameraRenderImageComposer);
    }
}
