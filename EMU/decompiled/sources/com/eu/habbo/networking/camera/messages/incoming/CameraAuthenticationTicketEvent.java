package com.eu.habbo.networking.camera.messages.incoming;

import com.eu.habbo.messages.outgoing.gamecenter.basejump.BaseJumpLoadGameComposer;
import com.eu.habbo.networking.camera.CameraIncomingMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/networking/camera/messages/incoming/CameraAuthenticationTicketEvent.class */
public class CameraAuthenticationTicketEvent extends CameraIncomingMessage {
    public CameraAuthenticationTicketEvent(Short sh, ByteBuf byteBuf) {
        super(sh, byteBuf);
    }

    @Override // com.eu.habbo.networking.camera.CameraIncomingMessage
    public void handle(Channel channel) throws Exception {
        String string = readString();
        if (string.startsWith("FASTFOOD")) {
            BaseJumpLoadGameComposer.FASTFOOD_KEY = string;
        }
    }
}
