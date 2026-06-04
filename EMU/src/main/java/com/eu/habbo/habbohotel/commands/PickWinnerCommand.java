package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessage;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserShoutComposer;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code :pickwinner} — estrae un partecipante a caso tra i presenti nella
 * stanza (escluso chi lancia) e ne annuncia il nome a tutti. Per raffle e
 * giveaway durante gli eventi.
 *
 * Permesso {@code cmd_pickwinner} impostato a ROOM_OWNER ('2') → usabile dal
 * proprietario/host nella propria stanza (oltre allo staff). E' solo un
 * messaggio di chat: nessun cambio di stato, nessuna ricompensa → rischio zero.
 * Broadcast con lo stesso pattern di {@code ShoutCommand}/{@code RollCommand}.
 */
public class PickWinnerCommand extends Command {
    private static final SecureRandom RANDOM = new SecureRandom();

    public PickWinnerCommand() {
        super("cmd_pickwinner", Emulator.getTexts().getValue("commands.keys.cmd_pickwinner", "pickwinner;vincitore;raffle").split(";"));
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) throws Exception {
        Habbo host = gameClient.getHabbo();
        Room room = host.getHabboInfo().getCurrentRoom();
        if (room == null) return true;

        // Candidati = presenti nella stanza, escluso chi lancia il comando.
        List<Habbo> candidates = new ArrayList<>();
        for (Habbo h : room.getHabbos()) {
            if (h == null || h == host) continue;
            candidates.add(h);
        }

        if (candidates.isEmpty()) {
            host.whisper(Emulator.getTexts().getValue("commands.error.cmd_pickwinner.empty", "Nessun partecipante in stanza."), RoomChatMessageBubbles.ALERT);
            return true;
        }

        Habbo winner = candidates.get(RANDOM.nextInt(candidates.size()));
        String text = Emulator.getTexts().getValue("commands.succes.cmd_pickwinner", "🎉 Il vincitore e': %user%!").replace("%user%", winner.getHabboInfo().getUsername());
        room.sendComposer(new RoomUserShoutComposer(new RoomChatMessage(text, host, RoomChatMessageBubbles.NORMAL)).compose());
        return true;
    }
}
