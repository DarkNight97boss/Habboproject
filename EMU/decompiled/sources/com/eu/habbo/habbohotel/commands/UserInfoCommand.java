package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.modtool.ModToolBan;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboManager;
import gnu.trove.iterator.TIntIntIterator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/UserInfoCommand.class */
public class UserInfoCommand extends Command {
    public UserInfoCommand() {
        super("cmd_userinfo", Emulator.getTexts().getValue("commands.keys.cmd_userinfo").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length < 2) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_userinfo.forgot_username"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(strArr[1]);
        HabboInfo habboInfo = habbo != null ? habbo.getHabboInfo() : null;
        if (habboInfo == null) {
            habboInfo = HabboManager.getOfflineHabboInfo(strArr[1]);
        }
        if (habboInfo == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_userinfo.not_found").replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
            return true;
        }
        StringBuilder sb = new StringBuilder(Emulator.getTexts().getValue("command.cmd_userinfo.userinfo") + ":  <b>" + habboInfo.getUsername() + "</b> (<b>" + habboInfo.getId() + "</b>)\r" + Emulator.getTexts().getValue("command.cmd_userinfo.user_id") + ": " + habboInfo.getId() + "\r" + Emulator.getTexts().getValue("command.cmd_userinfo.user_name") + ": " + habboInfo.getUsername() + "\r" + Emulator.getTexts().getValue("command.cmd_userinfo.motto") + ": " + habboInfo.getMotto().replace("<", "[").replace(">", "]") + "\r" + Emulator.getTexts().getValue("command.cmd_userinfo.rank") + ": " + habboInfo.getRank().getName() + " (" + habboInfo.getRank().getId() + ") \r" + Emulator.getTexts().getValue("command.cmd_userinfo.online") + ": " + (habbo == null ? Emulator.getTexts().getValue("generic.no") : Emulator.getTexts().getValue("generic.yes")) + "\r" + (habboInfo.getRank().hasPermission(Permission.ACC_HIDE_MAIL, true) ? Emulator.PREVIEW : Emulator.getTexts().getValue("command.cmd_userinfo.email") + ": " + habboInfo.getMail() + "\r") + (habboInfo.getRank().hasPermission(Permission.ACC_HIDE_IP, true) ? Emulator.PREVIEW : Emulator.getTexts().getValue("command.cmd_userinfo.ip_register") + ": " + habboInfo.getIpRegister() + "\r") + ((habboInfo.getRank().hasPermission(Permission.ACC_HIDE_IP, true) || habbo == null) ? Emulator.PREVIEW : Emulator.getTexts().getValue("command.cmd_userinfo.ip_current") + ": " + habbo.getHabboInfo().getIpLogin() + "\r") + (habbo != null ? Emulator.getTexts().getValue("command.cmd_userinfo.achievement_score") + ": " + habbo.getHabboStats().achievementScore + "\r" : Emulator.PREVIEW));
        ModToolBan modToolBanCheckForBan = Emulator.getGameEnvironment().getModToolManager().checkForBan(habboInfo.getId());
        sb.append(Emulator.getTexts().getValue("command.cmd_userinfo.total_bans")).append(": ").append(Emulator.getGameEnvironment().getModToolManager().totalBans(habboInfo.getId())).append("\r");
        sb.append(Emulator.getTexts().getValue("command.cmd_userinfo.banned")).append(": ").append(Emulator.getTexts().getValue(modToolBanCheckForBan != null ? "generic.yes" : "generic.no")).append("\r\r");
        if (modToolBanCheckForBan != null) {
            sb.append("<b>").append(Emulator.getTexts().getValue("command.cmd_userinfo.ban_info")).append("</b>\r");
            sb.append(modToolBanCheckForBan.listInfo()).append("\r");
        }
        sb.append("<b>").append(Emulator.getTexts().getValue("command.cmd_userinfo.currencies")).append("</b>\r");
        sb.append(Emulator.getTexts().getValue("command.cmd_userinfo.credits")).append(": ").append(habboInfo.getCredits()).append("\r");
        TIntIntIterator it = habboInfo.getCurrencies().iterator();
        int size = habboInfo.getCurrencies().size();
        while (true) {
            int i = size;
            size--;
            if (i <= 0) {
                break;
            }
            try {
                it.advance();
                sb.append(Emulator.getTexts().getValue("seasonal.name." + it.key())).append(": ").append(it.value()).append("\r");
            } catch (Exception e) {
            }
        }
        sb.append("\r").append(habbo != null ? "<b>" + Emulator.getTexts().getValue("command.cmd_userinfo.current_activity") + "</b>\r" : Emulator.PREVIEW).append(habbo != null ? Emulator.getTexts().getValue("command.cmd_userinfo.room") + ": " + (habbo.getHabboInfo().getCurrentRoom() != null ? habbo.getHabboInfo().getCurrentRoom().getName() + "(" + habbo.getHabboInfo().getCurrentRoom().getId() + ")\r" : "-") : Emulator.PREVIEW).append(habbo != null ? Emulator.getTexts().getValue("command.cmd_userinfo.respect_left") + ": " + habbo.getHabboStats().respectPointsToGive + "\r" : Emulator.PREVIEW).append(habbo != null ? Emulator.getTexts().getValue("command.cmd_userinfo.pet_respect_left") + ": " + habbo.getHabboStats().petRespectPointsToGive + "\r" : Emulator.PREVIEW).append(habbo != null ? Emulator.getTexts().getValue("command.cmd_userinfo.allow_trade") + ": " + (habbo.getHabboStats().allowTrade() ? Emulator.getTexts().getValue("generic.yes") : Emulator.getTexts().getValue("generic.no")) + "\r" : Emulator.PREVIEW).append(habbo != null ? Emulator.getTexts().getValue("command.cmd_userinfo.allow_follow") + ": " + (habbo.getHabboStats().blockFollowing ? Emulator.getTexts().getValue("generic.no") : Emulator.getTexts().getValue("generic.yes")) + "\r" : Emulator.PREVIEW).append(habbo != null ? Emulator.getTexts().getValue("command.cmd_userinfo.allow_friend_request") + ": " + (habbo.getHabboStats().blockFriendRequests ? Emulator.getTexts().getValue("generic.no") : Emulator.getTexts().getValue("generic.yes")) + "\r" : Emulator.PREVIEW);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Map.Entry<Integer, String>> nameChanges = Emulator.getGameEnvironment().getHabboManager().getNameChanges(habboInfo.getId(), 3);
        if (!nameChanges.isEmpty()) {
            sb.append("\r<b>Latest name changes:<b><br/>");
            for (Map.Entry<Integer, String> entry : nameChanges) {
                sb.append(simpleDateFormat.format(new Date(((long) entry.getKey().intValue()) * 1000))).append(" : ").append(entry.getValue()).append("<br/>");
            }
        }
        if (habbo != null) {
            sb.append("\r<b>Other accounts (");
            ArrayList<HabboInfo> cloneAccounts = Emulator.getGameEnvironment().getHabboManager().getCloneAccounts(habbo, 10);
            cloneAccounts.sort(new Comparator<HabboInfo>() { // from class: com.eu.habbo.habbohotel.commands.UserInfoCommand.1
                @Override // java.util.Comparator
                public int compare(HabboInfo habboInfo2, HabboInfo habboInfo3) {
                    return habboInfo2.getId() - habboInfo3.getId();
                }
            });
            sb.append(cloneAccounts.size()).append("):</b>\r");
            sb.append("<b>Username,\tID,\tDate register,\tDate last online</b>\r");
            for (HabboInfo habboInfo2 : cloneAccounts) {
                sb.append(habboInfo2.getUsername()).append(",\t").append(habboInfo2.getId()).append(",\t").append(simpleDateFormat.format(new Date(((long) habboInfo2.getAccountCreated()) * 1000))).append(",\t").append(simpleDateFormat.format(new Date(((long) habboInfo2.getLastOnline()) * 1000))).append("\r");
            }
        }
        gameClient.getHabbo().alert(sb.toString());
        return true;
    }
}
