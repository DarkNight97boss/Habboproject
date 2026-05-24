package com.eu.habbo.networking.camera.messages.incoming;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.camera.CameraURLComposer;
import com.eu.habbo.networking.camera.CameraIncomingMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/networking/camera/messages/incoming/CameraResultURLEvent.class */
public class CameraResultURLEvent extends CameraIncomingMessage {
    public static final int STATUS_OK = 0;
    public static final int STATUS_ERROR = 1;

    public CameraResultURLEvent(Short sh, ByteBuf byteBuf) {
        super(sh, byteBuf);
    }

    @Override // com.eu.habbo.networking.camera.CameraIncomingMessage
    public void handle(Channel channel) throws Exception {
        int iIntValue = readInt().intValue();
        int iIntValue2 = readInt().intValue();
        String string = readString();
        if (!Emulator.getConfig().getBoolean("camera.use.https", true)) {
            string = string.replace("https://", "http://");
        }
        int iIntValue3 = readInt().intValue();
        int iIntValue4 = readInt().intValue();
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(iIntValue);
        if (iIntValue2 == 1 && habbo != null) {
            habbo.getHabboInfo().setPhotoTimestamp(0);
            habbo.getHabboInfo().setPhotoJSON(Emulator.PREVIEW);
            habbo.getHabboInfo().setPhotoURL(Emulator.PREVIEW);
            habbo.alert(Emulator.getTexts().getValue("camera.error.creation"));
            return;
        }
        if (iIntValue2 == 0 && habbo != null && iIntValue4 == habbo.getHabboInfo().getPhotoTimestamp()) {
            AchievementManager.progressAchievement(habbo, Emulator.getGameEnvironment().getAchievementManager().getAchievement("CameraPhotoCount"), 1);
            habbo.getClient().sendResponse(new CameraURLComposer(string));
            habbo.getHabboInfo().setPhotoJSON(habbo.getHabboInfo().getPhotoJSON().replace("%room_id%", iIntValue3 + Emulator.PREVIEW).replace("%url%", string));
            habbo.getHabboInfo().setPhotoURL(string);
        }
    }
}
