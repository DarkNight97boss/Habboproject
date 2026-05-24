package com.eu.habbo.networking.camera.messages.outgoing;

import com.eu.habbo.Emulator;
import com.eu.habbo.networking.camera.CameraOutgoingMessage;
import io.netty.channel.Channel;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/networking/camera/messages/outgoing/CameraRenderImageComposer.class */
public class CameraRenderImageComposer extends CameraOutgoingMessage {
    public final int timestamp;
    final int userId;
    final int backgroundColor;
    final int width;
    final int height;
    final String JSON;

    public CameraRenderImageComposer(int i, int i2, int i3, int i4, String str) {
        super((short) 2);
        this.userId = i;
        this.timestamp = Emulator.getIntUnixTimestamp();
        this.backgroundColor = i2;
        this.width = i3;
        this.height = i4;
        this.JSON = str;
    }

    @Override // com.eu.habbo.networking.camera.CameraOutgoingMessage
    public void compose(Channel channel) {
        appendInt32(Integer.valueOf(this.userId));
        appendInt32(Integer.valueOf(this.timestamp));
        appendInt32(Integer.valueOf(this.backgroundColor));
        appendInt32(Integer.valueOf(this.width));
        appendInt32(Integer.valueOf(this.height));
        appendString(this.JSON);
    }
}
