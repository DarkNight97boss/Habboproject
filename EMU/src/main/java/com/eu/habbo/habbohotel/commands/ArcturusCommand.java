package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;

/**
 * Comando ":arcturus" / ":emulator".
 *
 * CLASS NAME conservato come "ArcturusCommand" perche':
 *   - e' registrato per nome in CommandHandler;
 *   - il DB permissions.commands referenzia la chiave;
 *   - rinominare richiederebbe migration + coordinamento ranks.
 *
 * Le KEYS dei comandi ("arcturus", "emulator") restano per backward-compat;
 * aggiunte "asteria" e "core" come alias futuri user-facing.
 *
 * Il messaggio whisper e' tradotto in italiano + cita upstream Arcturus
 * come da obbligo NOTICE della licenza GPL-3.0.
 */
public class ArcturusCommand extends Command {
    public ArcturusCommand() {
        super(null, new String[]{"arcturus", "emulator", "asteria", "core"});
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) throws Exception {
        if (gameClient.getHabbo().getHabboInfo().getCurrentRoom() != null) {
            gameClient.getHabbo().whisper(
                    "Questo ambiente virtuale e' alimentato da Asteria Core. \r" +
                            "Motore: Arcturus Morningstar (GPL-3.0) — credit upstream a TheGeneral.",
                    RoomChatMessageBubbles.ALERT);
        }

        return true;
    }
}
