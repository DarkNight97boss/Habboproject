package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.messages.outgoing.rooms.RoomMutedComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomSettingsUpdatedComposer;

public class RoomMuteCommand extends Command {
   public RoomMuteCommand() {
      super("cmd_roommute", Emulator.getTexts().getValue("commands.keys.cmd_roommute").split(";"));
   }

   @Override
   public boolean handle(GameClient gameClient, String[] params) throws Exception {
      Room room = gameClient.getHabbo().getHabboInfo().getCurrentRoom();
      if (room != null) {
         if (room.isMuted()) {
            room.setMuted(false);
            gameClient.sendResponse(new RoomMutedComposer(room));
            room.sendComposer(new RoomSettingsUpdatedComposer(room).compose());
            room.sendWhisper("The room has been unmuted!", RoomChatMessageBubbles.ALERT);
         } else {
            room.setMuted(true);
            gameClient.sendResponse(new RoomMutedComposer(room));
            room.sendComposer(new RoomSettingsUpdatedComposer(room).compose());
            room.sendWhisper("The room has been muted!", RoomChatMessageBubbles.ALERT);
         }
      }

      return true;
   }
}
