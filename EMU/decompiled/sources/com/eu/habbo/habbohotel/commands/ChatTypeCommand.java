package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.messages.outgoing.users.MeMenuSettingsComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/ChatTypeCommand.class */
public class ChatTypeCommand extends Command {
    public ChatTypeCommand() {
        super("cmd_chatcolor", Emulator.getTexts().getValue("commands.keys.cmd_chatcolor").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length < 2) {
            gameClient.getHabbo().getHabboStats().chatColor = RoomChatMessageBubbles.NORMAL;
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_chatcolor.reset"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        try {
            int iIntValue = Integer.valueOf(strArr[1]).intValue();
            if (RoomChatMessageBubbles.values().length < iIntValue) {
                iIntValue = 0;
            }
            if (iIntValue < 0) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_chatcolor.numbers"), RoomChatMessageBubbles.ALERT);
                return true;
            }
            if (!gameClient.getHabbo().hasPermission(Permission.ACC_ANYCHATCOLOR)) {
                for (String str : Emulator.getConfig().getValue("commands.cmd_chatcolor.banned_numbers").split(";")) {
                    if (Integer.valueOf(str).intValue() == iIntValue) {
                        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_chatcolor.banned"), RoomChatMessageBubbles.ALERT);
                        return true;
                    }
                }
            }
            gameClient.getHabbo().getHabboStats().chatColor = RoomChatMessageBubbles.getBubble(iIntValue);
            gameClient.sendResponse(new MeMenuSettingsComposer(gameClient.getHabbo()));
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_chatcolor.set").replace("%chat%", RoomChatMessageBubbles.values()[iIntValue].name().replace("_", " ").toLowerCase()), RoomChatMessageBubbles.ALERT);
            return true;
        } catch (Exception e) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_chatcolor.numbers"), RoomChatMessageBubbles.ALERT);
            return true;
        }
    }
}
