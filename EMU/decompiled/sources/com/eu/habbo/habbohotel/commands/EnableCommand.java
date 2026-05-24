package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/EnableCommand.class */
public class EnableCommand extends Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnableCommand.class);

    public EnableCommand() {
        super("cmd_enable", Emulator.getTexts().getValue("commands.keys.cmd_enable").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length < 2) {
            return true;
        }
        try {
            int i = Integer.parseInt(strArr[1]);
            Habbo habbo = gameClient.getHabbo();
            if (strArr.length == 3) {
                habbo = gameClient.getHabbo().getHabboInfo().getCurrentRoom().getHabbo(strArr[2]);
            }
            if (habbo == null) {
                return true;
            }
            if (habbo != gameClient.getHabbo() && !gameClient.getHabbo().hasPermission(Permission.ACC_ENABLE_OTHERS)) {
                return true;
            }
            try {
                if (habbo.getHabboInfo().getCurrentRoom() != null && habbo.getHabboInfo().getRiding() == null) {
                    if (Emulator.getGameEnvironment().getPermissionsManager().isEffectBlocked(i, habbo.getHabboInfo().getRank().getId())) {
                        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_enable.not_allowed"), RoomChatMessageBubbles.ALERT);
                        return true;
                    }
                    habbo.getHabboInfo().getCurrentRoom().giveEffect(habbo, i, -1);
                }
                return true;
            } catch (Exception e) {
                LOGGER.error("Caught exception", e);
                return true;
            }
        } catch (Exception e2) {
            return false;
        }
    }
}
