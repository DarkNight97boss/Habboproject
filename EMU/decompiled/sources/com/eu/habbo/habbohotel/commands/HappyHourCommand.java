package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.generic.alerts.GenericAlertComposer;
import java.util.Iterator;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/HappyHourCommand.class */
public class HappyHourCommand extends Command {
    public HappyHourCommand() {
        super("cmd_happyhour", Emulator.getTexts().getValue("commands.keys.cmd_happyhour").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        Emulator.getGameServer().getGameClientManager().sendBroadcastResponse(new GenericAlertComposer("Happy Hour!"));
        Iterator<Map.Entry<Integer, Habbo>> it = Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().entrySet().iterator();
        while (it.hasNext()) {
            AchievementManager.progressAchievement(it.next().getValue(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("HappyHour"));
        }
        return true;
    }
}
