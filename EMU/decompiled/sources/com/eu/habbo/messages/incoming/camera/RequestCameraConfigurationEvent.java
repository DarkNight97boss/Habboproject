package com.eu.habbo.messages.incoming.camera;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.camera.CameraPriceComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/camera/RequestCameraConfigurationEvent.class */
public class RequestCameraConfigurationEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        this.client.sendResponse(new CameraPriceComposer(Emulator.getConfig().getInt("camera.price.credits"), Emulator.getConfig().getInt("camera.price.points"), Emulator.getConfig().getInt("camera.price.points.publish")));
    }
}
