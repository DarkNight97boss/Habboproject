package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessage;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserRemoveComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserWhisperComposer;
import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.events.users.UserSavedMottoEvent;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/core/Easter.class */
public class Easter {
    @EventHandler
    public static void onUserChangeMotto(UserSavedMottoEvent userSavedMottoEvent) {
        if (Emulator.getConfig().getBoolean("easter_eggs.enabled") && userSavedMottoEvent.newMotto.equalsIgnoreCase("crickey!")) {
            userSavedMottoEvent.habbo.getClient().sendResponse(new RoomUserWhisperComposer(new RoomChatMessage(userSavedMottoEvent.newMotto, userSavedMottoEvent.habbo, userSavedMottoEvent.habbo, RoomChatMessageBubbles.ALERT)));
            Room currentRoom = userSavedMottoEvent.habbo.getHabboInfo().getCurrentRoom();
            currentRoom.sendComposer(new RoomUserRemoveComposer(userSavedMottoEvent.habbo.getRoomUnit()).compose());
            currentRoom.sendComposer(new RoomUserPetComposer(2, 1, "FFFFFF", userSavedMottoEvent.habbo).compose());
        }
    }
}
