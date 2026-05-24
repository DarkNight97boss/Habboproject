package com.eu.habbo.networking.camera.messages.incoming;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.camera.CameraRoomThumbnailSavedComposer;
import com.eu.habbo.networking.camera.CameraIncomingMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/networking/camera/messages/incoming/CameraRoomThumbnailGeneratedEvent.class */
public class CameraRoomThumbnailGeneratedEvent extends CameraIncomingMessage {
    public CameraRoomThumbnailGeneratedEvent(Short sh, ByteBuf byteBuf) {
        super(sh, byteBuf);
    }

    @Override // com.eu.habbo.networking.camera.CameraIncomingMessage
    public void handle(Channel channel) throws Exception {
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(readInt().intValue());
        if (habbo != null) {
            habbo.getClient().sendResponse(new CameraRoomThumbnailSavedComposer());
        }
    }
}
