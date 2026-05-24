package com.eu.habbo.messages.incoming.rooms.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.rooms.RoomChatMessage;
import com.eu.habbo.habbohotel.rooms.RoomChatType;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.plugin.events.users.UserTalkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/users/RoomUserWhisperEvent.class */
public class RoomUserWhisperEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoomUserWhisperEvent.class);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (this.client.getHabbo().getHabboInfo().getCurrentRoom() == null) {
            return;
        }
        RoomChatMessage roomChatMessage = new RoomChatMessage(this);
        if (roomChatMessage.getMessage().length() > RoomChatMessage.MAXIMUM_LENGTH) {
            String strReplace = Emulator.getTexts().getValue("scripter.warning.chat.length").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()).replace("%length%", roomChatMessage.getMessage().length() + Emulator.PREVIEW);
            ScripterManager.scripterDetected(this.client, strReplace);
            LOGGER.info(strReplace);
        } else {
            if (!this.client.getHabbo().getHabboStats().allowTalk() || roomChatMessage.getTargetHabbo() == null || ((UserTalkEvent) Emulator.getPluginManager().fireEvent(new UserTalkEvent(this.client.getHabbo(), roomChatMessage, RoomChatType.WHISPER))).isCancelled()) {
                return;
            }
            this.client.getHabbo().getHabboInfo().getCurrentRoom().talk(this.client.getHabbo(), roomChatMessage, RoomChatType.WHISPER, true);
            if (RoomChatMessage.SAVE_ROOM_CHATS) {
                Emulator.getThreading().run(roomChatMessage);
            }
        }
    }
}
