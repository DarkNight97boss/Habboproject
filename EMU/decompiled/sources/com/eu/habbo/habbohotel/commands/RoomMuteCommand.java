package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.messages.outgoing.rooms.RoomMutedComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomSettingsUpdatedComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/RoomMuteCommand.class */
public class RoomMuteCommand extends Command {
    public RoomMuteCommand() {
        super("cmd_roommute", Emulator.getTexts().getValue("commands.keys.cmd_roommute").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        Room currentRoom = gameClient.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null) {
            return true;
        }
        if (currentRoom.isMuted()) {
            currentRoom.setMuted(false);
            gameClient.sendResponse(new RoomMutedComposer(currentRoom));
            currentRoom.sendComposer(new RoomSettingsUpdatedComposer(currentRoom).compose());
            currentRoom.sendWhisper("The room has been unmuted!", RoomChatMessageBubbles.ALERT);
            return true;
        }
        currentRoom.setMuted(true);
        gameClient.sendResponse(new RoomMutedComposer(currentRoom));
        currentRoom.sendComposer(new RoomSettingsUpdatedComposer(currentRoom).compose());
        currentRoom.sendWhisper("The room has been muted!", RoomChatMessageBubbles.ALERT);
        return true;
    }
}
