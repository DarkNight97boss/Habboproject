package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomTrade;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.Outgoing;
import com.eu.habbo.messages.outgoing.generic.alerts.GenericAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.HotelWillCloseInMinutesComposer;
import com.eu.habbo.threading.runnables.ShutdownEmulator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/ShutdownCommand.class */
public class ShutdownCommand extends Command {
    public ShutdownCommand() {
        super("cmd_shutdown", Emulator.getTexts().getValue("commands.keys.cmd_shutdown").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        StringBuilder sb = new StringBuilder("-");
        int iIntValue = 0;
        if (strArr.length > 2) {
            sb = new StringBuilder();
            for (int i = 1; i < strArr.length; i++) {
                sb.append(strArr[i]).append(" ");
            }
        } else if (strArr.length == 2) {
            try {
                iIntValue = Integer.valueOf(strArr[1]).intValue();
            } catch (Exception e) {
                sb = new StringBuilder(strArr[1]);
            }
        }
        ServerMessage serverMessageCompose = !sb.toString().equals("-") ? new GenericAlertComposer("<b>" + Emulator.getTexts().getValue("generic.warning") + "</b> \r\n" + Emulator.getTexts().getValue("generic.shutdown").replace("%minutes%", iIntValue + Emulator.PREVIEW) + "\r\n" + Emulator.getTexts().getValue("generic.reason.specified") + ": <b>" + ((Object) sb) + "</b>\r\r- " + gameClient.getHabbo().getHabboInfo().getUsername()).compose() : new HotelWillCloseInMinutesComposer(iIntValue).compose();
        RoomTrade.TRADING_ENABLED = false;
        ShutdownEmulator.timestamp = Emulator.getIntUnixTimestamp() + (60 * iIntValue);
        Emulator.getThreading().run(new ShutdownEmulator(serverMessageCompose), iIntValue * 60 * Outgoing.CraftableProductsComposer);
        return true;
    }
}
