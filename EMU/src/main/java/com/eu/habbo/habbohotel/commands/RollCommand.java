package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessage;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserShoutComposer;

import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@code :roll [max]} — tira un numero casuale 1..max (default 100) e lo "grida"
 * nella stanza (visibile a tutti, sopra l'avatar di chi tira). Utile per giochi,
 * raffle e "tira piu' alto vince".
 *
 * Usabile da TUTTI i giocatori (permesso {@code cmd_roll} concesso a ogni rank),
 * con cooldown per utente anti-spam. E' solo un messaggio di chat: nessun cambio
 * di stato, nessuna ricompensa → a rischio zero. Broadcast con lo stesso pattern
 * di {@code ShoutCommand}.
 */
public class RollCommand extends Command {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final ConcurrentHashMap<Integer, Long> LAST_ROLL = new ConcurrentHashMap<>();
    private static final long COOLDOWN_MS = 3000L;

    public RollCommand() {
        super("cmd_roll", Emulator.getTexts().getValue("commands.keys.cmd_roll", "roll;dado;dice").split(";"));
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) throws Exception {
        Habbo habbo = gameClient.getHabbo();
        Room room = habbo.getHabboInfo().getCurrentRoom();
        if (room == null) return true;

        // Cooldown anti-spam per utente (i comandi non passano dall'anti-flood chat).
        int userId = habbo.getHabboInfo().getId();
        long now = System.currentTimeMillis();
        Long last = LAST_ROLL.get(userId);
        if (last != null && (now - last) < COOLDOWN_MS) {
            return true;
        }
        LAST_ROLL.put(userId, now);

        int max = 100;
        if (params.length >= 2) {
            try {
                max = Integer.parseInt(params[1]);
            } catch (NumberFormatException e) {
                max = 100;
            }
        }
        if (max < 2) max = 2;
        if (max > 1000000) max = 1000000;

        int result = RANDOM.nextInt(max) + 1;
        // 🎲 = 🎲 (escape ASCII per sicurezza di encoding in build).
        String text = "🎲 " + result + " (1-" + max + ")";
        room.sendComposer(new RoomUserShoutComposer(new RoomChatMessage(text, habbo, RoomChatMessageBubbles.NORMAL)).compose());
        return true;
    }
}
