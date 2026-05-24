package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/ConnectCameraCommand.class */
public class ConnectCameraCommand extends Command {
    public ConnectCameraCommand() {
        super("cmd_connect_camera", Emulator.getTexts().getValue("commands.keys.cmd_connect_camera").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        return false;
    }
}
