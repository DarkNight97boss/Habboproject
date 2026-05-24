package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/SetSpeedCommand.class */
public class SetSpeedCommand extends Command {
    public SetSpeedCommand() {
        super("cmd_setspeed", Emulator.getTexts().getValue("commands.keys.cmd_setspeed").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (gameClient.getHabbo().getHabboInfo().getCurrentRoom() == null || !gameClient.getHabbo().getHabboInfo().getCurrentRoom().hasRights(gameClient.getHabbo())) {
            return false;
        }
        Room currentRoom = gameClient.getHabbo().getHabboInfo().getCurrentRoom();
        int rollerSpeed = currentRoom.getRollerSpeed();
        try {
            int iIntValue = Integer.valueOf(strArr[1]).intValue();
            if (iIntValue < -1 || iIntValue > Emulator.getConfig().getInt("hotel.rollers.speed.maximum")) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_setspeed.bounds"), RoomChatMessageBubbles.ALERT);
                return true;
            }
            currentRoom.setRollerSpeed(iIntValue);
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_setspeed").replace("%oldspeed%", rollerSpeed + Emulator.PREVIEW).replace("%newspeed%", iIntValue + Emulator.PREVIEW), RoomChatMessageBubbles.ALERT);
            return true;
        } catch (Exception e) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_setspeed.invalid_amount"), RoomChatMessageBubbles.ALERT);
            return true;
        }
    }
}
