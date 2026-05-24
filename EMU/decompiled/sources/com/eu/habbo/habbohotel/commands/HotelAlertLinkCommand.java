package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.messages.outgoing.generic.alerts.StaffAlertWithLinkComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/HotelAlertLinkCommand.class */
public class HotelAlertLinkCommand extends Command {
    public HotelAlertLinkCommand() {
        super("cmd_hal", Emulator.getTexts().getValue("commands.keys.cmd_hal").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length < 3) {
            return true;
        }
        String str = strArr[1];
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < strArr.length; i++) {
            sb.append(strArr[i]);
            sb.append(" ");
        }
        sb.append("\r\r-<b>").append(gameClient.getHabbo().getHabboInfo().getUsername()).append("</b>");
        Emulator.getGameServer().getGameClientManager().sendBroadcastResponse(new StaffAlertWithLinkComposer(sb.toString(), str).compose());
        return true;
    }
}
