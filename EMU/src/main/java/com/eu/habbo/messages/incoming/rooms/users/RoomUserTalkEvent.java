package com.eu.habbo.messages.incoming.rooms.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessage;
import com.eu.habbo.habbohotel.rooms.RoomChatType;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.plugin.events.users.UserTalkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoomUserTalkEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoomUserTalkEvent.class);


    @Override
    public void handle() throws Exception {
        Room room = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (room == null)
            return;

        if (!this.client.getHabbo().getHabboStats().allowTalk())
            return;

        RoomChatMessage message = new RoomChatMessage(this);

        if (message.getMessage().length() <= RoomChatMessage.MAXIMUM_LENGTH) {
            // Anti-flood: drop the message (and auto-mute) when the user is spamming.
            if (!message.isCommand && com.eu.habbo.core.ChatSpamGuard.observe(this.client.getHabbo(), message.getMessage())) {
                return;
            }

            // Anti-link: blocca i link per utenti non staff (anche con escamotage tipo
            // "g o o g l e . c o m", "(dot)", "punto" ecc.). Staff bypassa.
            if (!message.isCommand && com.eu.habbo.core.LinkFilterGuard.shouldBlock(this.client.getHabbo(), message.getMessage())) {
                return;
            }

            // Bot Oracolo: se siamo nella stanza Oracolo, registra il messaggio
            // come proposta di feature (chat continua normalmente).
            if (!message.isCommand) {
                com.eu.habbo.core.OracoloManager.tryRecordRequest(this.client.getHabbo(), message.getMessage());
            }

            // Anti-macro (#17): osserva la regolarita' temporale della chat per
            // segnalare possibili bot. SOLO osservazione (nessun blocco/mute),
            // gated dal flag anti_macro (no-op se OFF). Vedi core/MacroGuard.
            if (!message.isCommand) {
                com.eu.habbo.core.MacroGuard.observe(this.client.getHabbo(), message.getMessage());
            }

            if (Emulator.getPluginManager().fireEvent(new UserTalkEvent(this.client.getHabbo(), message, RoomChatType.TALK)).isCancelled()) {
                return;
            }

            room.talk(this.client.getHabbo(), message, RoomChatType.TALK);

            if (!message.isCommand) {
                if (RoomChatMessage.SAVE_ROOM_CHATS) {
                    Emulator.getThreading().run(message);
                }
            }
        } else {
            String reportMessage = Emulator.getTexts().getValue("scripter.warning.chat.length").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()).replace("%length%", message.getMessage().length() + "");
            ScripterManager.scripterDetected(this.client, reportMessage);
            LOGGER.info(reportMessage);
        }
    }
}
