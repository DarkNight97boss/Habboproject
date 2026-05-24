package com.eu.habbo.messages.incoming.rooms.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessage;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserWhisperComposer;
import com.eu.habbo.plugin.events.users.UserKickEvent;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/users/RoomUserKickEvent.class */
public class RoomUserKickEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Habbo habbo;
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null || (habbo = currentRoom.getHabbo(this.packet.readInt().intValue())) == null) {
            return;
        }
        if (habbo.hasPermission(Permission.ACC_UNKICKABLE)) {
            this.client.sendResponse(new RoomUserWhisperComposer(new RoomChatMessage(Emulator.getTexts().getValue("commands.error.cmd_kick.unkickable").replace("%username%", habbo.getHabboInfo().getUsername()), this.client.getHabbo(), this.client.getHabbo(), RoomChatMessageBubbles.ALERT)));
            return;
        }
        if (currentRoom.isOwner(habbo)) {
            return;
        }
        UserKickEvent userKickEvent = new UserKickEvent(this.client.getHabbo(), habbo);
        Emulator.getPluginManager().fireEvent(userKickEvent);
        if (userKickEvent.isCancelled()) {
            return;
        }
        if ((currentRoom.hasRights(this.client.getHabbo()) || this.client.getHabbo().hasPermission(Permission.ACC_ANYROOMOWNER) || this.client.getHabbo().hasPermission(Permission.ACC_AMBASSADOR)) && !habbo.hasPermission(Permission.ACC_UNKICKABLE)) {
            currentRoom.kickHabbo(habbo, true);
            AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("SelfModKickSeen"));
        }
    }
}
