package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/StaffOnlineCommand.class */
public class StaffOnlineCommand extends Command {
    public StaffOnlineCommand() {
        super("cmd_staffonline", Emulator.getTexts().getValue("commands.keys.cmd_staffonline").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        int i = Emulator.getConfig().getInt("commands.cmd_staffonline.min_rank");
        if (strArr.length >= 2) {
            try {
                int iIntValue = Integer.valueOf(strArr[1]).intValue();
                if (iIntValue < 1) {
                    gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_staffonline.positive_only"), RoomChatMessageBubbles.ALERT);
                    return true;
                }
                i = iIntValue;
            } catch (Exception e) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_staffonline.numbers_only"), RoomChatMessageBubbles.ALERT);
                return true;
            }
        }
        synchronized (Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos()) {
            ArrayList<Habbo> arrayList = new ArrayList();
            for (Map.Entry<Integer, Habbo> entry : Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().entrySet()) {
                if (entry.getValue().getHabboInfo().getRank().getId() >= i) {
                    arrayList.add(entry.getValue());
                }
            }
            arrayList.sort(new Comparator<Habbo>() { // from class: com.eu.habbo.habbohotel.commands.StaffOnlineCommand.1
                @Override // java.util.Comparator
                public int compare(Habbo habbo, Habbo habbo2) {
                    return habbo.getHabboInfo().getId() - habbo2.getHabboInfo().getId();
                }
            });
            StringBuilder sb = new StringBuilder(Emulator.getTexts().getValue("commands.generic.cmd_staffonline.staffs"));
            sb.append("\r\n");
            for (Habbo habbo : arrayList) {
                sb.append(habbo.getHabboInfo().getUsername());
                sb.append(": ");
                sb.append(habbo.getHabboInfo().getRank().getName());
                sb.append("\r");
            }
            gameClient.getHabbo().alert(sb.toString());
        }
        return true;
    }
}
