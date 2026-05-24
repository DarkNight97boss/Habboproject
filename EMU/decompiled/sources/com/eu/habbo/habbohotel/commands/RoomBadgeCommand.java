package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.inventory.BadgesComponent;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;
import com.eu.habbo.messages.outgoing.users.AddUserBadgeComposer;
import gnu.trove.map.hash.THashMap;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/RoomBadgeCommand.class */
public class RoomBadgeCommand extends Command {
    public RoomBadgeCommand() {
        super("cmd_roombadge", Emulator.getTexts().getValue("commands.keys.cmd_roombadge").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (gameClient == null) {
            return true;
        }
        if (strArr.length != 2) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_roombadge.no_badge"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        String str = strArr[1];
        if (str.isEmpty()) {
            return true;
        }
        THashMap tHashMap = new THashMap();
        tHashMap.put("display", "BUBBLE");
        tHashMap.put("image", "${image.library.url}album1584/" + str + ".gif");
        tHashMap.put("message", Emulator.getTexts().getValue("commands.generic.cmd_badge.received"));
        ServerMessage serverMessageCompose = new BubbleAlertComposer(BubbleAlertKeys.RECEIVED_BADGE.key, (THashMap<String, String>) tHashMap).compose();
        for (Habbo habbo : gameClient.getHabbo().getRoomUnit().getRoom().getHabbos()) {
            if (habbo.isOnline() && habbo.getInventory() != null && habbo.getInventory().getBadgesComponent() != null && !habbo.getInventory().getBadgesComponent().hasBadge(str)) {
                habbo.getClient().sendResponse(new AddUserBadgeComposer(BadgesComponent.createBadge(str, habbo)));
                habbo.getClient().sendResponse(serverMessageCompose);
            }
        }
        return true;
    }
}
