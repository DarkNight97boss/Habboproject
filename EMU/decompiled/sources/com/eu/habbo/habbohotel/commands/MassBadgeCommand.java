package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboBadge;
import com.eu.habbo.habbohotel.users.inventory.BadgesComponent;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;
import com.eu.habbo.messages.outgoing.users.AddUserBadgeComposer;
import gnu.trove.map.hash.THashMap;
import java.util.Iterator;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/MassBadgeCommand.class */
public class MassBadgeCommand extends Command {
    public MassBadgeCommand() {
        super("cmd_massbadge", Emulator.getTexts().getValue("commands.keys.cmd_massbadge").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        HabboBadge habboBadgeCreateBadge;
        if (strArr.length != 2) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_massbadge.no_badge"), RoomChatMessageBubbles.ALERT);
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
        Iterator<Map.Entry<Integer, Habbo>> it = Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().entrySet().iterator();
        while (it.hasNext()) {
            Habbo value = it.next().getValue();
            if (value.isOnline() && value.getInventory() != null && value.getInventory().getBadgesComponent() != null && !value.getInventory().getBadgesComponent().hasBadge(str) && (habboBadgeCreateBadge = BadgesComponent.createBadge(str, value)) != null) {
                value.getClient().sendResponse(new AddUserBadgeComposer(habboBadgeCreateBadge));
                value.getClient().sendResponse(serverMessageCompose);
            }
        }
        return true;
    }
}
