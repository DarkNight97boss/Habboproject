package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboManager;
import com.eu.habbo.habbohotel.users.inventory.BadgesComponent;
import com.eu.habbo.messages.outgoing.inventory.InventoryBadgesComposer;
import com.eu.habbo.messages.outgoing.users.UserBadgesComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/TakeBadgeCommand.class */
public class TakeBadgeCommand extends Command {
    public TakeBadgeCommand() {
        super("cmd_take_badge", Emulator.getTexts().getValue("commands.keys.cmd_take_badge").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length == 2) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_take_badge.forgot_badge"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (strArr.length == 1) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_take_badge.forgot_username"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (strArr.length != 3) {
            return true;
        }
        String str = strArr[1];
        String str2 = strArr[2];
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(str);
        if (habbo != null) {
            if (habbo.getInventory().getBadgesComponent().removeBadge(str2) == null) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_take_badge.no_badge").replace("%username%", str).replace("%badge%", str2), RoomChatMessageBubbles.ALERT);
                return true;
            }
            habbo.getClient().sendResponse(new InventoryBadgesComposer(habbo));
            if (habbo.getHabboInfo().getCurrentRoom() != null) {
                habbo.getHabboInfo().getCurrentRoom().sendComposer(new UserBadgesComposer(habbo.getInventory().getBadgesComponent().getWearingBadges(), habbo.getHabboInfo().getId()).compose());
            }
        }
        int id = 0;
        if (habbo != null) {
            id = habbo.getHabboInfo().getId();
        } else {
            HabboInfo offlineHabboInfo = HabboManager.getOfflineHabboInfo(str);
            if (offlineHabboInfo != null) {
                id = offlineHabboInfo.getId();
            }
        }
        if (id <= 0) {
            return true;
        }
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_take_badge"), RoomChatMessageBubbles.ALERT);
        BadgesComponent.deleteBadge(id, str2);
        return true;
    }
}
