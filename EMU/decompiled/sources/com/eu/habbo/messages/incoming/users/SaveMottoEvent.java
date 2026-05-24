package com.eu.habbo.messages.incoming.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserDataComposer;
import com.eu.habbo.plugin.events.users.UserSavedMottoEvent;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/users/SaveMottoEvent.class */
public class SaveMottoEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        UserSavedMottoEvent userSavedMottoEvent = new UserSavedMottoEvent(this.client.getHabbo(), this.client.getHabbo().getHabboInfo().getMotto(), this.packet.readString());
        Emulator.getPluginManager().fireEvent(userSavedMottoEvent);
        String str = userSavedMottoEvent.newMotto;
        if (str.length() <= Emulator.getConfig().getInt("motto.max_length", 38)) {
            this.client.getHabbo().getHabboInfo().setMotto(str);
            this.client.getHabbo().getHabboInfo().run();
        }
        if (this.client.getHabbo().getHabboInfo().getCurrentRoom() != null) {
            this.client.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RoomUserDataComposer(this.client.getHabbo()).compose());
        } else {
            this.client.sendResponse(new RoomUserDataComposer(this.client.getHabbo()));
        }
        AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("Motto"));
    }
}
