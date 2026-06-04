package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.rooms.ForwardToRoomComposer;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code :bringroom <id stanza>} — Teleport di gruppo (#14): porta TUTTI gli
 * occupanti online della stanza indicata nella stanza corrente dello staff
 * (utile per radunare la gente da un'altra stanza durante eventi/tour guidati).
 *
 * Net-new rispetto a {@code :summon} (per utente) e {@code :summonrank} (per
 * rank): qui il criterio e' la STANZA di provenienza. La destinazione e' sempre
 * la stanza corrente dello staff (sempre valida) → nessun rischio di lasciare
 * utenti "senza stanza". Permesso {@code cmd_bringroom}. Best-effort per utente:
 * un errore su un singolo non ferma il gruppo. Riusa lo stesso pattern di
 * teleport di {@code SummonCommand}.
 */
public class BringRoomCommand extends Command {
    public BringRoomCommand() {
        super("cmd_bringroom", Emulator.getTexts().getValue("commands.keys.cmd_bringroom", "bringroom;portastanza").split(";"));
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) throws Exception {
        Habbo staff = gameClient.getHabbo();
        Room dest = staff.getHabboInfo().getCurrentRoom();
        if (dest == null) return true;

        if (params.length < 2) {
            staff.whisper(Emulator.getTexts().getValue("commands.error.cmd_bringroom.usage", "Uso: :bringroom <id stanza>"), RoomChatMessageBubbles.ALERT);
            return true;
        }

        int sourceId;
        try {
            sourceId = Integer.parseInt(params[1]);
        } catch (NumberFormatException e) {
            staff.whisper(Emulator.getTexts().getValue("commands.error.cmd_bringroom.usage", "Uso: :bringroom <id stanza>"), RoomChatMessageBubbles.ALERT);
            return true;
        }

        if (sourceId == dest.getId()) {
            staff.whisper(Emulator.getTexts().getValue("commands.error.cmd_bringroom.same", "Sono gia' in questa stanza."), RoomChatMessageBubbles.ALERT);
            return true;
        }

        // Snapshot degli occupanti online della stanza sorgente (poi li spostiamo).
        List<Habbo> toMove = new ArrayList<>();
        for (Habbo h : Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().values()) {
            if (h == null) continue;
            Room r = h.getHabboInfo().getCurrentRoom();
            if (r != null && r.getId() == sourceId) toMove.add(h);
        }

        int moved = 0;
        for (Habbo h : toMove) {
            try {
                Room r = h.getHabboInfo().getCurrentRoom();
                if (r != null) {
                    Emulator.getGameEnvironment().getRoomManager().logExit(h);
                    r.removeHabbo(h, true);
                    h.getHabboInfo().setCurrentRoom(null);
                }
                Emulator.getGameEnvironment().getRoomManager().enterRoom(h, dest.getId(), "", true);
                h.getClient().sendResponse(new ForwardToRoomComposer(dest.getId()));
                moved++;
            } catch (Throwable ignored) {
                // un singolo utente non deve fermare il gruppo
            }
        }

        staff.whisper(Emulator.getTexts().getValue("commands.succes.cmd_bringroom.done", "Spostati %count% utenti in questa stanza.").replace("%count%", String.valueOf(moved)), RoomChatMessageBubbles.ALERT);
        return true;
    }
}
