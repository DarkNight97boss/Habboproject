package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.generic.alerts.StaffAlertWithLinkComposer;

import java.util.Map;

public class HotelAlertCommand extends Command {

    // Per-actor cooldown — a compromised (or playful) staff account can otherwise spam
    // hotel-wide alerts continuously, each one iterating every online user.
    private static final java.util.concurrent.ConcurrentHashMap<Integer, Long> LAST_FIRED = new java.util.concurrent.ConcurrentHashMap<>();

    public HotelAlertCommand() {
        super("cmd_ha", Emulator.getTexts().getValue("commands.keys.cmd_ha").split(";"));
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) {
        if (params.length > 1) {
            int cooldownMs = Math.max(0, Emulator.getConfig().getInt("commands.hotelalert.cooldown_ms", 5000));
            int actorId = gameClient.getHabbo().getHabboInfo().getId();
            long now = System.currentTimeMillis();
            Long last = LAST_FIRED.get(actorId);
            if (last != null && now - last < cooldownMs) {
                gameClient.getHabbo().whisper("Cooldown avviso hotel — aspetta " + ((cooldownMs - (now - last)) / 1000 + 1) + "s.", RoomChatMessageBubbles.ALERT);
                return true;
            }
            LAST_FIRED.put(actorId, now);

            StringBuilder message = new StringBuilder();
            for (int i = 1; i < params.length; i++) {
                message.append(params[i]).append(" ");
            }

            ServerMessage msg = new StaffAlertWithLinkComposer(message + "\r\n-" + gameClient.getHabbo().getHabboInfo().getUsername(), "").compose();

            for (Map.Entry<Integer, Habbo> set : Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().entrySet()) {
                Habbo habbo = set.getValue();
                if (habbo.getHabboStats().blockStaffAlerts)
                    continue;

                habbo.getClient().sendResponse(msg);
            }
        } else {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_ha.forgot_message"), RoomChatMessageBubbles.ALERT);
        }
        return true;
    }
}
