package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboManager;
import com.eu.habbo.habbohotel.users.HabboStats;
import com.eu.habbo.habbohotel.users.subscriptions.Subscription;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/SubscriptionCommand.class */
public class SubscriptionCommand extends Command {
    public SubscriptionCommand() {
        super("cmd_subscription", Emulator.getTexts().getValue("commands.keys.cmd_subscription").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length < 4) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_subscription.invalid_params", "Invalid command format"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        HabboInfo offlineHabboInfo = HabboManager.getOfflineHabboInfo(strArr[1]);
        if (offlineHabboInfo == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_subscription.user_not_found", "%user% was not found").replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
            return true;
        }
        HabboStats habboStats = offlineHabboInfo.getHabboStats();
        String upperCase = strArr[2].toUpperCase();
        String str = strArr[3];
        StringBuilder sb = new StringBuilder();
        if (strArr.length > 4) {
            for (int i = 4; i < strArr.length; i++) {
                sb.append(strArr[i]).append(" ");
            }
        }
        if (!Emulator.getGameEnvironment().getSubscriptionManager().types.containsKey(upperCase)) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_subscription.type_not_found", "%subscription% is not a valid subscription type").replace("%subscription%", upperCase), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (str.equalsIgnoreCase("add") || str.equalsIgnoreCase("+") || str.equalsIgnoreCase("a")) {
            int iTimeStringToSeconds = Emulator.timeStringToSeconds(sb.toString());
            if (iTimeStringToSeconds < 1) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_subscription.invalid_params_time", "Invalid time span, try: x minutes/days/weeks/months"), RoomChatMessageBubbles.ALERT);
                return true;
            }
            habboStats.createSubscription(upperCase, iTimeStringToSeconds);
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_subscription.success_add_time", "Successfully added %time% seconds to %subscription% on %user%").replace("%time%", iTimeStringToSeconds + Emulator.PREVIEW).replace("%user%", strArr[1]).replace("%subscription%", upperCase), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (!str.equalsIgnoreCase("remove") && !str.equalsIgnoreCase("-") && !str.equalsIgnoreCase("r")) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_subscription.invalid_action", "Invalid action specified. Must be add, +, remove or -"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        Subscription subscription = habboStats.getSubscription(upperCase);
        if (subscription == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_subscription.user_not_have", "%user% does not have the %subscription% subscription").replace("%user%", strArr[1]).replace("%subscription%", upperCase), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (sb.length() == 0) {
            subscription.addDuration(-subscription.getRemaining());
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_subscription.success_remove_sub", "Successfully removed %subscription% sub from %user%").replace("%user%", strArr[1]).replace("%subscription%", upperCase), RoomChatMessageBubbles.ALERT);
            return true;
        }
        int iTimeStringToSeconds2 = Emulator.timeStringToSeconds(sb.toString());
        if (iTimeStringToSeconds2 < 1) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_subscription.invalid_params_time", "Invalid time span, try: x minutes/days/weeks/months"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        subscription.addDuration(-iTimeStringToSeconds2);
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_subscription.success_remove_time", "Successfully removed %time% seconds from %subscription% on %user%").replace("%time%", iTimeStringToSeconds2 + Emulator.PREVIEW).replace("%user%", strArr[1]).replace("%subscription%", upperCase), RoomChatMessageBubbles.ALERT);
        return true;
    }
}
