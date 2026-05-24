package com.eu.habbo.networking.camera.messages.incoming;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.outgoing.generic.alerts.GenericAlertComposer;
import com.eu.habbo.networking.camera.CameraIncomingMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/networking/camera/messages/incoming/CameraUpdateNotification.class */
public class CameraUpdateNotification extends CameraIncomingMessage {
    private static final Logger LOGGER = LoggerFactory.getLogger(CameraUpdateNotification.class);

    public CameraUpdateNotification(Short sh, ByteBuf byteBuf) {
        super(sh, byteBuf);
    }

    @Override // com.eu.habbo.networking.camera.CameraIncomingMessage
    public void handle(Channel channel) throws Exception {
        boolean z = readBoolean();
        String string = readString();
        int iIntValue = readInt().intValue();
        if (iIntValue == 0) {
            LOGGER.info("Camera update: {}", string);
        } else if (iIntValue == 1) {
            LOGGER.warn("Camera update: {}", string);
        } else if (iIntValue == 2) {
            LOGGER.error("Camera update: {}", string);
        }
        if (z) {
            Emulator.getGameServer().getGameClientManager().sendBroadcastResponse(new GenericAlertComposer(string).compose());
        }
    }
}
