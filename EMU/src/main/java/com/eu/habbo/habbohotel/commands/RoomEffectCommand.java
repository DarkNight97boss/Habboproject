package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;

public class RoomEffectCommand extends Command {
    public RoomEffectCommand() {
        super("cmd_roomeffect", Emulator.getTexts().getValue("commands.keys.cmd_roomeffect").split(";"));
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) throws Exception {
        if (params.length < 2) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_roomeffect.no_effect"), RoomChatMessageBubbles.ALERT);
            return true;
        }

        try {
            int effectId = Integer.parseInt(params[1]);

            if (effectId >= 0) {
                Room room = gameClient.getHabbo().getHabboInfo().getCurrentRoom();
                if (room == null) return true;
                for (Habbo habbo : room.getHabbos()) {
                    if (Emulator.getGameEnvironment().getPermissionsManager().isEffectBlocked(effectId, habbo.getHabboInfo().getRank().getId())) {
                        continue; // don't push rank-restricted effects onto users
                    }
                    room.giveEffect(habbo, effectId, -1);
                }

                return true;
            } else {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_roomeffect.positive"), RoomChatMessageBubbles.ALERT);
                return true;
            }
        } catch (Exception e) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_roomeffect.numbers_only"), RoomChatMessageBubbles.ALERT);
            return true;
        }
    }
}
