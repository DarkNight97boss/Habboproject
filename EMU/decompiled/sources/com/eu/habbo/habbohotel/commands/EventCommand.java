package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import gnu.trove.map.hash.THashMap;
import java.util.Iterator;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/EventCommand.class */
public class EventCommand extends Command {
    public EventCommand() {
        super("cmd_event", Emulator.getTexts().getValue("commands.keys.cmd_event").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (gameClient.getHabbo().getHabboInfo().getCurrentRoom() == null || strArr.length < 2) {
            return false;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < strArr.length; i++) {
            sb.append(strArr[i]);
            sb.append(" ");
        }
        THashMap tHashMap = new THashMap();
        tHashMap.put("ROOMNAME", gameClient.getHabbo().getHabboInfo().getCurrentRoom().getName());
        tHashMap.put("ROOMID", gameClient.getHabbo().getHabboInfo().getCurrentRoom().getId() + Emulator.PREVIEW);
        tHashMap.put("USERNAME", gameClient.getHabbo().getHabboInfo().getUsername());
        tHashMap.put("LOOK", gameClient.getHabbo().getHabboInfo().getLook());
        tHashMap.put("TIME", Emulator.getDate().toString());
        tHashMap.put("MESSAGE", sb.toString());
        ServerMessage serverMessageCompose = new BubbleAlertComposer("hotel.event", (THashMap<String, String>) tHashMap).compose();
        Iterator<Map.Entry<Integer, Habbo>> it = Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().entrySet().iterator();
        while (it.hasNext()) {
            Habbo value = it.next().getValue();
            if (!value.getHabboStats().blockStaffAlerts) {
                value.getClient().sendResponse(serverMessageCompose);
            }
        }
        return true;
    }
}
