package com.eu.habbo.networking.camera.messages.outgoing;

import com.eu.habbo.Emulator;
import com.eu.habbo.networking.camera.CameraOutgoingMessage;
import io.netty.channel.Channel;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/networking/camera/messages/outgoing/CameraLoginComposer.class */
public class CameraLoginComposer extends CameraOutgoingMessage {
    public CameraLoginComposer() {
        super((short) 1);
    }

    @Override // com.eu.habbo.networking.camera.CameraOutgoingMessage
    public void compose(Channel channel) {
        appendString(Emulator.getConfig().getValue("username").trim());
        appendString(Emulator.getConfig().getValue("password").trim());
        appendString(Emulator.version);
    }
}
