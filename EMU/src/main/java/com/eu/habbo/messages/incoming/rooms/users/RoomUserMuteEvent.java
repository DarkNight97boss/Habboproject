package com.eu.habbo.messages.incoming.rooms.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.users.MutedWhisperComposer;

public class RoomUserMuteEvent extends MessageHandler {
    @Override
    public void handle() throws Exception {
        int userId = this.packet.readInt();
        int roomId = this.packet.readInt();
        int minutes = this.packet.readInt();

        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(roomId);

        if (room != null) {
            if (room.hasRights(this.client.getHabbo()) || this.client.getHabbo().hasPermission("cmd_mute") || this.client.getHabbo().hasPermission(Permission.ACC_AMBASSADOR)) {
                Habbo habbo = room.getHabbo(userId);

                if (habbo != null) {
                    // Same rank-target guard as ModToolSanctionMuteEvent / MuteCommand:
                    // a junior staff member must not be able to mute a higher-rank user
                    // via the per-user mute button.
                    if (habbo.getHabboInfo().getRank().getId() >= this.client.getHabbo().getHabboInfo().getRank().getId()) {
                        return;
                    }
                    room.muteHabbo(habbo, minutes);
                    habbo.getClient().sendResponse(new MutedWhisperComposer(minutes * 60));
                    AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("SelfModMuteSeen"));
                }
            }
        }
    }
}
