package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;

import java.util.regex.Pattern;

/**
 * Filtro server-side anti-link per utenti NON staff.
 *
 * Lo staff (acc_supporttool oppure security level >= soglia config) puo'
 * mandare URL liberamente: il client li renderizza come <a> cliccabili
 * (vedi formatStaffLinks nel client).
 *
 * Gli utenti normali vengono fermati anche se provano a oscurare l'URL:
 *   - https://example.com
 *   - example.com
 *   - www.example.com
 *   - e x a m p l e . c o m
 *   - e.x.a.m.p.l.e.com
 *   - example (dot) com  /  example[.]com  /  example DOT com
 *   - example punto com (italiano)
 *
 * Algoritmo:
 *   1. Lowercase
 *   2. Sostituzione testuale dei dot-evasions: (dot), [dot], {dot},
 *      <dot>, " dot ", " punto ", DOT, PUNTO → "."
 *   3. Test diretto regex URL/dominio
 *   4. "deep strip": rimuovi tutti i caratteri non [a-z0-9.] e ricerca
 *      domini in forma <nome>.<tld>
 *   5. "naked strip": rimuovi anche i punti, cerca finali con TLD nota
 *      (cattura "googlecom" da "g.o o.g.l.e.c.o.m")
 *
 * Config keys (con default sicuri):
 *   - chat.link.filter.enabled (default true)
 *   - chat.link.filter.bypass_security_level (default 5)
 *
 * Notifica i giocatori bloccati con un whisper di sistema localizzato
 * (chiave testo: "chat.link.blocked").
 */
public class LinkFilterGuard
{
    // Pattern URL diretto: http(s):// o www.X.tld
    private static final Pattern URL_DIRECT = Pattern.compile(
        "https?://[a-z0-9][a-z0-9.\\-/?=&%#_:+~@]*|www\\.[a-z0-9][a-z0-9.\\-]*\\.[a-z]{2,}",
        Pattern.CASE_INSENSITIVE
    );

    // Dominio in forma "nome.tld" — TLD comuni (incluse phishy: ml/tk/gq/cf/ga)
    private static final Pattern DOMAIN_PATTERN = Pattern.compile(
        "\\b[a-z0-9][a-z0-9\\-]{1,63}\\." +
        "(com|net|org|io|me|tv|gg|biz|info|xyz|app|dev|tech|" +
        "online|site|store|shop|club|live|stream|fun|" +
        "it|de|fr|es|pt|nl|be|ch|at|pl|cz|ru|ua|gr|tr|" +
        "uk|us|ca|au|nz|br|mx|ar|cl|co|pe|in|jp|kr|cn|tw|hk|sg|" +
        "ml|tk|gq|cf|ga|ws|cc|to|tm|fi|se|no|dk|ie|ro|hu|" +
        "edu|gov|mil|int|pro|cat|asia|name|tel|mobi|aero|coop|jobs|museum|travel)" +
        "\\b"
    );

    // TLD nuda alla fine di una stringa alfanumerica (per "googlecom" deep-strip)
    private static final Pattern NAKED_DOMAIN = Pattern.compile(
        "[a-z0-9]{3,}" +
        "(com|net|org|io|me|tv|gg|biz|info|xyz|app|dev|tech|" +
        "online|site|store|shop|club|live|stream|fun|" +
        "it|de|fr|es|pt|nl|be|ch|at|pl|cz|ru|ua|gr|tr|" +
        "uk|us|ca|au|nz|br|mx|ar|cl|co|pe|in|jp|kr|cn|tw|hk|sg|" +
        "ml|tk|gq|cf|ga|ws|cc|to|tm|fi|se|no|dk|ie|ro|hu)" +
        "(?:[^a-z0-9]|$)"
    );

    private LinkFilterGuard() {}

    /**
     * Ritorna true se il messaggio deve essere bloccato per via di un link.
     * Notifica il giocatore con whisper localizzato.
     */
    public static boolean shouldBlock(Habbo habbo, String message)
    {
        if(habbo == null || message == null || message.isEmpty()) return false;

        // Toggle globale.
        if(!Emulator.getConfig().getBoolean("chat.link.filter.enabled", true)) return false;

        // Bypass staff: chi ha il supporttool oppure il rank superiore alla soglia.
        if(isStaff(habbo)) return false;

        if(!containsLink(message)) return false;

        // Bloccato: notifica al mittente.
        try
        {
            String warn = Emulator.getTexts().getValue(
                "chat.link.blocked",
                "I link nella chat sono consentiti solo allo staff."
            );
            habbo.whisper(warn, RoomChatMessageBubbles.ALERT);
        }
        catch(Exception ignored) {}

        return true;
    }

    private static boolean isStaff(Habbo habbo)
    {
        try
        {
            if(habbo.hasPermission(Permission.ACC_SUPPORTTOOL)) return true;
        }
        catch(Exception ignored) {}

        try
        {
            int minLvl = Emulator.getConfig().getInt("chat.link.filter.bypass_security_level", 5);
            int rank = habbo.getHabboInfo().getRank() != null
                ? habbo.getHabboInfo().getRank().getId()
                : 0;
            return rank >= minLvl;
        }
        catch(Exception ignored) {}
        return false;
    }

    /**
     * Esegue le 3 fasi di detection. Ritorna true al primo match.
     */
    static boolean containsLink(String raw)
    {
        if(raw == null || raw.isEmpty()) return false;

        String normalized = normalize(raw);

        // 1. Match URL diretto su testo normalizzato.
        if(URL_DIRECT.matcher(normalized).find()) return true;
        if(DOMAIN_PATTERN.matcher(normalized).find()) return true;

        // 2. Deep strip: rimuovi tutto tranne [a-z0-9.]
        String deepStripped = normalized.replaceAll("[^a-z0-9.]", "");
        if(URL_DIRECT.matcher(deepStripped).find()) return true;
        if(DOMAIN_PATTERN.matcher(deepStripped).find()) return true;

        // 3. Naked strip: rimuovi anche i punti, cerca TLD finali.
        // Si attiva SOLO se il testo normalizzato contiene almeno un '.'
        // — questa è la firma di un tentativo di evasion. Senza punti il
        // detector matcherebbe parole italiane finite in -io/-it/-co
        // (es. "pomeriggio" → "io").
        if(normalized.indexOf('.') >= 0)
        {
            String naked = normalized.replaceAll("[^a-z0-9]", "") + " ";
            if(NAKED_DOMAIN.matcher(naked).find()) return true;
        }

        return false;
    }

    /**
     * Normalizzazione: lowercase + sostituzione dot-evasions comuni.
     */
    static String normalize(String raw)
    {
        String s = raw.toLowerCase();

        // dot-evasions tra parentesi/quadre/graffe
        s = s.replace("(dot)", ".");
        s = s.replace("[dot]", ".");
        s = s.replace("{dot}", ".");
        s = s.replace("<dot>", ".");
        s = s.replace("(punto)", ".");
        s = s.replace("[punto]", ".");
        s = s.replace("(punkt)", ".");
        s = s.replace("(.)", ".");
        s = s.replace("[.]", ".");
        s = s.replace("{.}", ".");

        // dot-evasions con parole separate da spazi
        s = s.replaceAll("\\s+dot\\s+", ".");
        s = s.replaceAll("\\s+punto\\s+", ".");
        s = s.replaceAll("\\s+punkt\\s+", ".");
        s = s.replaceAll("\\s+point\\s+", ".");

        // slash-evasions
        s = s.replace("(slash)", "/");
        s = s.replace("[slash]", "/");
        s = s.replaceAll("\\s+slash\\s+", "/");

        return s;
    }
}
