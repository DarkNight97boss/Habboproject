package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.modtool.ModToolBanType;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboManager;

public class IPBanCommand extends Command {
   public static final int TEN_YEARS = 315569260;

   public IPBanCommand() {
      super("cmd_ip_ban", Emulator.getTexts().getValue("commands.keys.cmd_ip_ban").split(";"));
   }

   @Override
   public boolean handle(GameClient gameClient, String[] params) throws Exception {
      StringBuilder reason = new StringBuilder();
      if (params.length >= 2) {
         Habbo h = Emulator.getGameEnvironment().getHabboManager().getHabbo(params[1]);
         HabboInfo habbo;
         if (h != null) {
            habbo = h.getHabboInfo();
         } else {
            habbo = HabboManager.getOfflineHabboInfo(params[1]);
         }

         if (params.length > 2) {
            for (int i = 2; i < params.length; i++) {
               reason.append(params[i]);
               reason.append(" ");
            }
         }

         int count = 0;
         if (habbo != null) {
            if (habbo == gameClient.getHabbo().getHabboInfo()) {
               gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_ip_ban.ban_self"), RoomChatMessageBubbles.ALERT);
               return true;
            }

            if (habbo.getRank().getId() >= gameClient.getHabbo().getHabboInfo().getRank().getId()) {
               gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_ban.target_rank_higher"), RoomChatMessageBubbles.ALERT);
               return true;
            }

            Emulator.getGameEnvironment().getModToolManager().ban(habbo.getId(), gameClient.getHabbo(), reason.toString(), 315569260, ModToolBanType.IP, -1);
            count++;

            for (Habbo hx : Emulator.getGameServer().getGameClientManager().getHabbosWithIP(habbo.getIpLogin())) {
               if (hx != null) {
                  count++;
                  Emulator.getGameEnvironment()
                     .getModToolManager()
                     .ban(hx.getHabboInfo().getId(), gameClient.getHabbo(), reason.toString(), 315569260, ModToolBanType.IP, -1);
               }
            }

            gameClient.getHabbo()
               .whisper(Emulator.getTexts().getValue("commands.succes.cmd_ip_ban").replace("%count%", count + ""), RoomChatMessageBubbles.ALERT);
            return true;
         } else {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_ban.user_offline"), RoomChatMessageBubbles.ALERT);
            return true;
         }
      } else {
         return true;
      }
   }
}
