package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessage;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboGender;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserTalkComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/PullCommand.class */
public class PullCommand extends Command {
    public PullCommand() {
        super("cmd_pull", Emulator.getTexts().getValue("commands.keys.cmd_pull").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length != 2) {
            return true;
        }
        Habbo habbo = gameClient.getHabbo().getHabboInfo().getCurrentRoom().getHabbo(strArr[1]);
        if (habbo == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_pull.not_found").replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (habbo == gameClient.getHabbo()) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_pull.pull_self"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        int x = habbo.getRoomUnit().getX() - gameClient.getHabbo().getRoomUnit().getX();
        int y = habbo.getRoomUnit().getY() - gameClient.getHabbo().getRoomUnit().getY();
        if (x < -2 || x > 2 || y < -2 || y > 2) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_pull.cant_reach").replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
            return true;
        }
        RoomTile tileInFront = gameClient.getHabbo().getHabboInfo().getCurrentRoom().getLayout().getTileInFront(gameClient.getHabbo().getRoomUnit().getCurrentLocation(), gameClient.getHabbo().getRoomUnit().getBodyRotation().getValue());
        if (tileInFront == null || !tileInFront.isWalkable()) {
            return true;
        }
        if (gameClient.getHabbo().getHabboInfo().getCurrentRoom().getLayout().getDoorTile() == tileInFront) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_pull.invalid").replace("%username%", strArr[1]));
            return true;
        }
        habbo.getRoomUnit().setGoalLocation(tileInFront);
        gameClient.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RoomUserTalkComposer(new RoomChatMessage(Emulator.getTexts().getValue("commands.succes.cmd_pull.pull").replace("%user%", strArr[1]).replace("%gender_name%", gameClient.getHabbo().getHabboInfo().getGender().equals(HabboGender.M) ? Emulator.getTexts().getValue("gender.him") : Emulator.getTexts().getValue("gender.her")), gameClient.getHabbo(), gameClient.getHabbo(), RoomChatMessageBubbles.NORMAL)).compose());
        return true;
    }
}
