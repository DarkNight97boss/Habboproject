package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.messages.ServerMessage;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/TestCommand.class */
public class TestCommand extends Command {
    public TestCommand() {
        super("acc_debug", new String[]{"test"});
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (gameClient.getHabbo() != null || !gameClient.getHabbo().hasPermission(Permission.ACC_SUPPORTTOOL) || !Emulator.debugging) {
            return false;
        }
        ServerMessage serverMessage = new ServerMessage(Integer.valueOf(strArr[1]).intValue());
        for (int i = 1; i < strArr.length; i++) {
            String[] strArrSplit = strArr[i].split(":");
            if (strArrSplit[0].equalsIgnoreCase("b")) {
                serverMessage.appendBoolean(Boolean.valueOf(strArrSplit[1].equalsIgnoreCase("1")));
            } else if (strArrSplit[0].equalsIgnoreCase("s")) {
                if (strArrSplit.length > 1) {
                    serverMessage.appendString(strArrSplit[1]);
                } else {
                    serverMessage.appendString(Emulator.PREVIEW);
                }
            } else if (strArrSplit[0].equals("i")) {
                serverMessage.appendInt(Integer.valueOf(strArrSplit[1]));
            } else if (strArrSplit[0].equalsIgnoreCase("by")) {
                serverMessage.appendByte(Integer.valueOf(strArrSplit[1]));
            } else if (strArrSplit[0].equalsIgnoreCase("sh")) {
                serverMessage.appendShort(Integer.valueOf(strArrSplit[1]).intValue());
            }
        }
        gameClient.sendResponse(serverMessage);
        return true;
    }
}
