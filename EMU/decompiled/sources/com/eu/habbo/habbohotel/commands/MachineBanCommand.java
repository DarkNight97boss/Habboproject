package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.modtool.ModToolBanType;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboManager;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/MachineBanCommand.class */
public class MachineBanCommand extends Command {
    public MachineBanCommand() {
        super("cmd_machine_ban", Emulator.getTexts().getValue("commands.keys.cmd_machine_ban").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        HabboInfo habboInfo = null;
        StringBuilder sb = new StringBuilder();
        if (strArr.length >= 2) {
            Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(strArr[1]);
            habboInfo = habbo != null ? habbo.getHabboInfo() : HabboManager.getOfflineHabboInfo(strArr[1]);
        }
        if (strArr.length > 2) {
            for (int i = 2; i < strArr.length; i++) {
                sb.append(strArr[i]);
                sb.append(" ");
            }
        }
        if (habboInfo == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_ban.user_offline"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (habboInfo == gameClient.getHabbo().getHabboInfo()) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_machine_ban.ban_self"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (habboInfo.getRank().getId() >= gameClient.getHabbo().getHabboInfo().getRank().getId()) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_ban.target_rank_higher"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_machine_ban").replace("%count%", Emulator.getGameEnvironment().getModToolManager().ban(habboInfo.getId(), gameClient.getHabbo(), sb.toString(), IPBanCommand.TEN_YEARS, ModToolBanType.MACHINE, -1).size() + Emulator.PREVIEW), RoomChatMessageBubbles.ALERT);
        return true;
    }
}
