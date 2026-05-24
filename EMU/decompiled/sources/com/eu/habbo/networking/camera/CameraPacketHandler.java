package com.eu.habbo.networking.camera;

import com.eu.habbo.networking.camera.messages.incoming.CameraAuthenticationTicketEvent;
import com.eu.habbo.networking.camera.messages.incoming.CameraLoginStatusEvent;
import com.eu.habbo.networking.camera.messages.incoming.CameraResultURLEvent;
import com.eu.habbo.networking.camera.messages.incoming.CameraRoomThumbnailGeneratedEvent;
import com.eu.habbo.networking.camera.messages.incoming.CameraUpdateNotification;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/networking/camera/CameraPacketHandler.class */
public class CameraPacketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CameraPacketHandler.class);
    private static CameraPacketHandler INSTANCE;
    private final HashMap<Short, Class<? extends CameraIncomingMessage>> packetDefinitions = new HashMap<>();

    public CameraPacketHandler() {
        this.packetDefinitions.put((short) 1, CameraLoginStatusEvent.class);
        this.packetDefinitions.put((short) 2, CameraResultURLEvent.class);
        this.packetDefinitions.put((short) 3, CameraRoomThumbnailGeneratedEvent.class);
        this.packetDefinitions.put((short) 4, CameraUpdateNotification.class);
        this.packetDefinitions.put((short) 5, CameraAuthenticationTicketEvent.class);
    }

    public static CameraPacketHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new CameraPacketHandler();
        }
        return INSTANCE;
    }

    public void handle(Channel channel, short s, ByteBuf byteBuf) {
        Class<? extends CameraIncomingMessage> cls = this.packetDefinitions.get(Short.valueOf(s));
        if (cls != null) {
            try {
                CameraIncomingMessage cameraIncomingMessageNewInstance = cls.getDeclaredConstructor(Short.class, ByteBuf.class).newInstance(Short.valueOf(s), byteBuf);
                cameraIncomingMessageNewInstance.handle(channel);
                cameraIncomingMessageNewInstance.buffer.release();
            } catch (Exception e) {
                LOGGER.error("Caught exception", e);
            }
        }
    }
}
