package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/UpdatePetDataCommand.class */
public class UpdatePetDataCommand extends Command {
    public UpdatePetDataCommand() {
        super("cmd_update_pet_data", Emulator.getTexts().getValue("commands.keys.cmd_update_pet_data").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        Emulator.getGameEnvironment().getPetManager().reloadPetData();
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_update_pet_data"), RoomChatMessageBubbles.ALERT);
        return true;
    }
}
