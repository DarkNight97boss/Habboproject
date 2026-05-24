package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.modtool.ModToolBan;
import com.eu.habbo.habbohotel.modtool.ModToolBanType;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboManager;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/BanCommand.class */
public class BanCommand extends Command {
    public BanCommand() {
        super("cmd_ban", Emulator.getTexts().getValue("commands.keys.cmd_ban").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length < 2) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_ban.forgot_user"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (strArr.length < 3) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_ban.forgot_time"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        try {
            int iIntValue = Integer.valueOf(strArr[2]).intValue();
            if (iIntValue < 600) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_ban.time_to_short"), RoomChatMessageBubbles.ALERT);
                return true;
            }
            if (strArr[1].toLowerCase().equals(gameClient.getHabbo().getHabboInfo().getUsername().toLowerCase())) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_ban.ban_self"), RoomChatMessageBubbles.ALERT);
                return true;
            }
            Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(strArr[1]);
            HabboInfo habboInfo = habbo != null ? habbo.getHabboInfo() : HabboManager.getOfflineHabboInfo(strArr[1]);
            if (habboInfo == null) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_ban.user_offline"), RoomChatMessageBubbles.ALERT);
                return true;
            }
            if (habboInfo.getRank().getId() >= gameClient.getHabbo().getHabboInfo().getRank().getId()) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_ban.target_rank_higher"), RoomChatMessageBubbles.ALERT);
                return true;
            }
            StringBuilder sb = new StringBuilder();
            if (strArr.length > 3) {
                for (int i = 3; i < strArr.length; i++) {
                    sb.append(strArr[i]).append(" ");
                }
            }
            ModToolBan modToolBan = Emulator.getGameEnvironment().getModToolManager().ban(habboInfo.getId(), gameClient.getHabbo(), sb.toString(), iIntValue, ModToolBanType.ACCOUNT, -1).get(0);
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_ban.ban_issued").replace("%user%", habboInfo.getUsername()).replace("%time%", (modToolBan.expireDate - Emulator.getIntUnixTimestamp()) + Emulator.PREVIEW).replace("%reason%", modToolBan.reason), RoomChatMessageBubbles.ALERT);
            return true;
        } catch (Exception e) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_ban.invalid_time"), RoomChatMessageBubbles.ALERT);
            return true;
        }
    }
}
