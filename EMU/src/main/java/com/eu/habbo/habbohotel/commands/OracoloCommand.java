package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.core.OracoloManager;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;

import java.util.List;

/**
 * Comando :oracolo per gestire la bacheca delle feature proposte.
 *
 *   :oracolo lista              — top 10 proposte ordinate per voti
 *   :oracolo vota <id>          — upvote (-1 con :oracolo down <id>)
 *   :oracolo down <id>          — downvote
 *   :oracolo accetta <id>       — staff only — sposta in ACCEPTED
 *   :oracolo rifiuta <id>       — staff only — sposta in REJECTED
 *   :oracolo inreview <id>      — staff only — sposta in IN_REVIEW
 *   :oracolo done <id>          — staff only — sposta in IMPLEMENTED
 */
public class OracoloCommand extends Command
{
    public OracoloCommand()
    {
        super(null, new String[] { "oracolo", "idea", "proposta" });
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params)
    {
        Habbo habbo = gameClient.getHabbo();
        if(habbo == null) return true;

        if(params.length < 2)
        {
            whisper(habbo, "commands.error.cmd_oracolo.no_subcommand",
                "Usa: :oracolo lista — oppure scrivi in chat nella stanza Oracolo per proporre una feature.");
            return true;
        }

        String sub = params[1].toLowerCase();

        switch(sub)
        {
            case "lista":
            case "list":
                return handleList(habbo);
            case "vota":
            case "vote":
            case "up":
                return handleVote(habbo, params, +1);
            case "down":
            case "dislike":
                return handleVote(habbo, params, -1);
            case "accetta":
            case "accept":
                return handleStatusChange(habbo, params, OracoloManager.Status.ACCEPTED);
            case "rifiuta":
            case "reject":
                return handleStatusChange(habbo, params, OracoloManager.Status.REJECTED);
            case "inreview":
            case "review":
                return handleStatusChange(habbo, params, OracoloManager.Status.IN_REVIEW);
            case "done":
            case "fatto":
            case "implementato":
                return handleStatusChange(habbo, params, OracoloManager.Status.IMPLEMENTED);
            default:
                whisper(habbo, "commands.error.cmd_oracolo.no_subcommand",
                    "Sub-comando sconosciuto. Usa: :oracolo lista|vota|down|accetta|rifiuta|inreview|done");
                return true;
        }
    }

    private boolean handleList(Habbo habbo)
    {
        List<OracoloManager.FeatureRequest> list = OracoloManager.listTop(10, null);
        if(list.isEmpty())
        {
            whisper(habbo, "commands.info.cmd_oracolo.list_empty",
                "Nessuna richiesta in bacheca. Sii il primo a proporre qualcosa nella stanza Oracolo!");
            return true;
        }

        whisper(habbo, "commands.info.cmd_oracolo.list_header",
            "🔮 Bacheca Idee Oracolo — Top richieste:");

        String tpl = Emulator.getTexts().getValue(
            "commands.info.cmd_oracolo.list_entry",
            "#%id% [%status%] +%up%/-%down% — %text% (di %user%)"
        );

        for(OracoloManager.FeatureRequest r : list)
        {
            String line = tpl
                .replace("%id%", String.valueOf(r.id))
                .replace("%status%", r.status.name())
                .replace("%up%", String.valueOf(r.votesUp))
                .replace("%down%", String.valueOf(r.votesDown))
                .replace("%text%", r.text.length() > 80 ? r.text.substring(0, 77) + "..." : r.text)
                .replace("%user%", r.userName == null ? "?" : r.userName);
            try { habbo.whisper(line, RoomChatMessageBubbles.BOT); } catch(Exception ignored) {}
        }
        return true;
    }

    private boolean handleVote(Habbo habbo, String[] params, int direction)
    {
        Integer id = parseId(habbo, params);
        if(id == null) return true;

        int result = OracoloManager.castVote(id, habbo, direction);
        switch(result)
        {
            case 1:
                whisperReplace(habbo, "commands.error.cmd_oracolo.not_found",
                    "Richiesta #%id% non trovata.", "%id%", id.toString());
                break;
            case 2:
                whisper(habbo, "commands.error.cmd_oracolo.own_request",
                    "Non puoi votare la tua stessa proposta.");
                break;
            case 3:
                whisper(habbo, "commands.error.cmd_oracolo.already_voted",
                    "Hai già votato questa richiesta.");
                break;
            case 0:
            default:
                OracoloManager.FeatureRequest r = OracoloManager.findById(id);
                if(r != null)
                {
                    String msg = Emulator.getTexts().getValue(
                        "commands.success.cmd_oracolo.voted",
                        "Hai votato la richiesta #%id%. Punteggio attuale: +%up% / -%down%."
                    )
                    .replace("%id%", String.valueOf(r.id))
                    .replace("%up%", String.valueOf(r.votesUp))
                    .replace("%down%", String.valueOf(r.votesDown));
                    try { habbo.whisper(msg, RoomChatMessageBubbles.BOT); } catch(Exception ignored) {}
                }
                break;
        }
        return true;
    }

    private boolean handleStatusChange(Habbo habbo, String[] params, OracoloManager.Status newStatus)
    {
        try
        {
            if(!habbo.hasPermission(Permission.ACC_SUPPORTTOOL))
            {
                whisper(habbo, "commands.error.cmd_oracolo.staff_only",
                    "Solo lo staff può cambiare lo stato delle richieste.");
                return true;
            }
        }
        catch(Exception ignored) {}

        Integer id = parseId(habbo, params);
        if(id == null) return true;

        boolean ok = OracoloManager.changeStatus(id, newStatus);
        if(!ok)
        {
            whisperReplace(habbo, "commands.error.cmd_oracolo.not_found",
                "Richiesta #%id% non trovata.", "%id%", id.toString());
        }
        else
        {
            String msg = Emulator.getTexts().getValue(
                "commands.success.cmd_oracolo.status_changed",
                "Stato richiesta #%id% aggiornato a: %status%."
            )
            .replace("%id%", id.toString())
            .replace("%status%", newStatus.name());
            try { habbo.whisper(msg, RoomChatMessageBubbles.BOT); } catch(Exception ignored) {}
        }
        return true;
    }

    private Integer parseId(Habbo habbo, String[] params)
    {
        if(params.length < 3)
        {
            whisper(habbo, "commands.error.cmd_oracolo.invalid_id",
                "ID richiesta non valido. Usa :oracolo lista per vedere gli ID.");
            return null;
        }
        try
        {
            return Integer.parseInt(params[2]);
        }
        catch(NumberFormatException ex)
        {
            whisper(habbo, "commands.error.cmd_oracolo.invalid_id",
                "ID richiesta non valido. Usa :oracolo lista per vedere gli ID.");
            return null;
        }
    }

    private void whisper(Habbo habbo, String key, String fallback)
    {
        try
        {
            habbo.whisper(Emulator.getTexts().getValue(key, fallback), RoomChatMessageBubbles.BOT);
        }
        catch(Exception ignored) {}
    }

    private void whisperReplace(Habbo habbo, String key, String fallback, String token, String value)
    {
        try
        {
            String msg = Emulator.getTexts().getValue(key, fallback).replace(token, value);
            habbo.whisper(msg, RoomChatMessageBubbles.ALERT);
        }
        catch(Exception ignored) {}
    }
}
