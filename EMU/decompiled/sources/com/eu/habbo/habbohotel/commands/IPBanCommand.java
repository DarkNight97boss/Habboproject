package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.modtool.ModToolBanType;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboManager;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/IPBanCommand.class */
public class IPBanCommand extends Command {
    public static final int TEN_YEARS = 315569260;

    public IPBanCommand() {
        super("cmd_ip_ban", Emulator.getTexts().getValue("commands.keys.cmd_ip_ban").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        StringBuilder sb = new StringBuilder();
        if (strArr.length < 2) {
            return true;
        }
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(strArr[1]);
        HabboInfo habboInfo = habbo != null ? habbo.getHabboInfo() : HabboManager.getOfflineHabboInfo(strArr[1]);
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
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_ip_ban.ban_self"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (habboInfo.getRank().getId() >= gameClient.getHabbo().getHabboInfo().getRank().getId()) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_ban.target_rank_higher"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        Emulator.getGameEnvironment().getModToolManager().ban(habboInfo.getId(), gameClient.getHabbo(), sb.toString(), TEN_YEARS, ModToolBanType.IP, -1);
        int i2 = 0 + 1;
        for (Habbo habbo2 : Emulator.getGameServer().getGameClientManager().getHabbosWithIP(habboInfo.getIpLogin())) {
            if (habbo2 != null) {
                i2++;
                Emulator.getGameEnvironment().getModToolManager().ban(habbo2.getHabboInfo().getId(), gameClient.getHabbo(), sb.toString(), TEN_YEARS, ModToolBanType.IP, -1);
            }
        }
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_ip_ban").replace("%count%", i2 + Emulator.PREVIEW), RoomChatMessageBubbles.ALERT);
        return true;
    }
}
