package com.eu.habbo.messages.incoming.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.users.HabboGender;
import com.eu.habbo.habbohotel.users.clothingvalidation.ClothingValidationManager;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserDataComposer;
import com.eu.habbo.messages.outgoing.users.UpdateUserLookComposer;
import com.eu.habbo.plugin.events.users.UserSavedLookEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/users/UserSaveLookEvent.class */
public class UserSaveLookEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserSaveLookEvent.class);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        String string = this.packet.readString();
        try {
            UserSavedLookEvent userSavedLookEvent = new UserSavedLookEvent(this.client.getHabbo(), HabboGender.valueOf(string), this.packet.readString());
            Emulator.getPluginManager().fireEvent(userSavedLookEvent);
            if (userSavedLookEvent.isCancelled()) {
                return;
            }
            this.client.getHabbo().getHabboInfo().setLook(ClothingValidationManager.VALIDATE_ON_CHANGE_LOOKS ? ClothingValidationManager.validateLook(this.client.getHabbo(), userSavedLookEvent.newLook, userSavedLookEvent.gender.name()) : userSavedLookEvent.newLook);
            this.client.getHabbo().getHabboInfo().setGender(userSavedLookEvent.gender);
            Emulator.getThreading().run(this.client.getHabbo().getHabboInfo());
            this.client.sendResponse(new UpdateUserLookComposer(this.client.getHabbo()));
            if (this.client.getHabbo().getHabboInfo().getCurrentRoom() != null) {
                this.client.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RoomUserDataComposer(this.client.getHabbo()).compose());
            }
            AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("AvatarLooks"));
        } catch (IllegalArgumentException e) {
            String strReplace = Emulator.getTexts().getValue("scripter.warning.look.gender").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()).replace("%gender%", string);
            ScripterManager.scripterDetected(this.client, strReplace);
            LOGGER.info(strReplace);
        }
    }
}
