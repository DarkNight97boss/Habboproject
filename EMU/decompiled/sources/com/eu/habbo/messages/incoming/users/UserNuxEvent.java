package com.eu.habbo.messages.incoming.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboStats;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.habboway.nux.NuxAlertComposer;
import java.util.HashMap;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/users/UserNuxEvent.class */
public class UserNuxEvent extends MessageHandler {
    public static Map<Integer, String> keys = new HashMap<Integer, String>() { // from class: com.eu.habbo.messages.incoming.users.UserNuxEvent.1
        {
            put(1, "BOTTOM_BAR_RECEPTION");
            put(2, "BOTTOM_BAR_NAVIGATOR");
            put(3, "CHAT_INPUT");
            put(4, "CHAT_HISTORY_BUTTON");
            put(5, "MEMENU_CLOTHES");
            put(6, "BOTTOM_BAR_CATALOGUE");
            put(7, "CREDITS_BUTTON");
            put(8, "DUCKETS_BUTTON");
            put(9, "DIAMONDS_BUTTON");
            put(10, "FRIENDS_BAR_ALL_FRIENDS");
            put(11, "BOTTOM_BAR_NAVIGATOR");
        }
    };

    public static void handle(Habbo habbo) {
        habbo.getHabboStats().nux = true;
        HabboStats habboStats = habbo.getHabboStats();
        int i = habboStats.nuxStep;
        habboStats.nuxStep = i + 1;
        if (keys.containsKey(Integer.valueOf(i))) {
            habbo.getClient().sendResponse(new NuxAlertComposer("helpBubble/add/" + keys.get(Integer.valueOf(i)) + "/" + Emulator.getTexts().getValue("nux.step." + i)));
        } else if (habbo.getHabboStats().nuxReward) {
            habbo.getClient().sendResponse(new NuxAlertComposer("nux/lobbyoffer/show"));
        }
    }

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        handle(this.client.getHabbo());
    }
}
