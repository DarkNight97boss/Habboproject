package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.generic.alerts.StaffAlertWithLinkComposer;
import java.util.Iterator;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/HotelAlertCommand.class */
public class HotelAlertCommand extends Command {
    public HotelAlertCommand() {
        super("cmd_ha", Emulator.getTexts().getValue("commands.keys.cmd_ha").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) {
        if (strArr.length <= 1) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_ha.forgot_message"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < strArr.length; i++) {
            sb.append(strArr[i]).append(" ");
        }
        ServerMessage serverMessageCompose = new StaffAlertWithLinkComposer(((Object) sb) + "\r\n-" + gameClient.getHabbo().getHabboInfo().getUsername(), Emulator.PREVIEW).compose();
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
