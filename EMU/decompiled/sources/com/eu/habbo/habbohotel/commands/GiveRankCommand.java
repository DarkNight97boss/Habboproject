package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.permissions.Rank;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboManager;
import org.apache.commons.lang3.StringUtils;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/GiveRankCommand.class */
public class GiveRankCommand extends Command {
    public GiveRankCommand() {
        super("cmd_give_rank", Emulator.getTexts().getValue("commands.keys.cmd_give_rank").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        Rank rankByName = null;
        if (strArr.length == 1) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_give_rank.missing_username") + Emulator.getTexts().getValue("commands.description.cmd_give_rank"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (strArr.length == 2) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_give_rank.missing_rank") + Emulator.getTexts().getValue("commands.description.cmd_give_rank"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (strArr.length == 3) {
            if (StringUtils.isNumeric(strArr[2])) {
                int iIntValue = Integer.valueOf(strArr[2]).intValue();
                if (Emulator.getGameEnvironment().getPermissionsManager().rankExists(iIntValue)) {
                    rankByName = Emulator.getGameEnvironment().getPermissionsManager().getRank(iIntValue);
                }
            } else {
                rankByName = Emulator.getGameEnvironment().getPermissionsManager().getRankByName(strArr[2]);
            }
            if (rankByName != null) {
                if (rankByName.getId() > gameClient.getHabbo().getHabboInfo().getRank().getId()) {
                    gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_give_rank.higher").replace("%username%", strArr[1]).replace("%id%", rankByName.getName()), RoomChatMessageBubbles.ALERT);
                    return true;
                }
                HabboInfo offlineHabboInfo = HabboManager.getOfflineHabboInfo(strArr[1]);
                if (offlineHabboInfo == null) {
                    gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_give_rank.user_offline").replace("%id%", rankByName.getName()).replace("%username%", strArr[1]), RoomChatMessageBubbles.ALERT);
                    return true;
                }
                if (offlineHabboInfo.getRank().getId() > gameClient.getHabbo().getHabboInfo().getRank().getId()) {
                    gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_give_rank.higher.other").replace("%username%", strArr[1]).replace("%id%", rankByName.getName()), RoomChatMessageBubbles.ALERT);
                    return true;
                }
                Emulator.getGameEnvironment().getHabboManager().setRank(offlineHabboInfo.getId(), rankByName.getId());
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_give_rank.updated").replace("%id%", rankByName.getName()).replace("%username%", strArr[1]), RoomChatMessageBubbles.ALERT);
                return true;
            }
        }
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.errors.cmd_give_rank.not_found").replace("%id%", strArr[2]).replace("%username%", strArr[1]), RoomChatMessageBubbles.ALERT);
        return true;
    }
}
