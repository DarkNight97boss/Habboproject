package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.users.Habbo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Shadow-mute (#19) — "mute ombra" gestita dal CMS, gated dal feature flag
 * {@code shadow_mute} (via {@link CmsFlags}).
 *
 * Un utente in shadow-mute continua a vedere i propri messaggi (e li vede lo
 * staff con {@code acc_see_whispers}), ma per tutti gli altri occupanti della
 * stanza i suoi messaggi TALK/SHOUT semplicemente non arrivano: non sa di
 * essere mutato. Strumento anti-troll a basso attrito.
 *
 * Architettura: il CMS possiede la tabella {@code cms_v3_shadow_mutes} (gestita
 * dagli endpoint staff /api/v2/moderation/shadow-mutes). L'EMU ne legge il set
 * con un poller periodico (stesso DB condiviso), SOLO quando il flag e' ON.
 * Niente comandi in-game, niente config aggiuntiva: lo staff accende il flag dal
 * CMS e gestisce la lista da li'.
 *
 * <b>Dark-launch</b>: flag OFF di default -> il poller non interroga nemmeno il
 * DB, il set resta vuoto e {@link #hidesFrom} ritorna sempre false -> la chat e'
 * identica a prima. A rischio zero anche non collaudato.
 *
 * Tutto best-effort + try/catch: un errore non puo' mai influire sulla chat.
 */
public final class ShadowMute {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShadowMute.class);
    private static volatile boolean started = false;
    private static volatile Set<Integer> MUTED = Collections.emptySet();

    private ShadowMute() {}

    /** Avvia il poller. Chiamato dal boot dell'EMU. Idempotente. */
    public static synchronized void start() {
        if (started) return;
        started = true;
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "shadow-mute-poll");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleWithFixedDelay(ShadowMute::refresh, 10, 30, TimeUnit.SECONDS);
        LOGGER.info("ShadowMute: poller avviato (refresh 30s, gated dal flag shadow_mute)");
    }

    private static void refresh() {
        try {
            // Flag OFF -> non tocchiamo nemmeno il DB: dark-launch a costo zero.
            if (!CmsFlags.isEnabled("shadow_mute")) {
                if (!MUTED.isEmpty()) MUTED = Collections.emptySet();
                return;
            }

            Set<Integer> next = new HashSet<>();
            try (Connection conn = Emulator.getDatabase().getDataSource().getConnection();
                 PreparedStatement st = conn.prepareStatement(
                     "SELECT user_id FROM cms_v3_shadow_mutes WHERE until_ts = 0 OR until_ts > UNIX_TIMESTAMP()")) {
                try (ResultSet rs = st.executeQuery()) {
                    while (rs.next()) next.add(rs.getInt(1));
                }
            }
            MUTED = next;
        } catch (Throwable t) {
            // Tabella assente o errore transitorio: mantieni lo stato precedente (fail-safe).
            LOGGER.debug("ShadowMute: refresh saltato ({})", t.toString());
        }
    }

    /**
     * true se i messaggi pubblici (TALK/SHOUT) di {@code speaker} devono essere
     * NASCOSTI a {@code recipient}. Ritorna false in ogni caso dubbio, cosi' che
     * la chat resti invariata se la feature e' spenta o non applicabile.
     */
    public static boolean hidesFrom(Habbo speaker, Habbo recipient) {
        try {
            if (speaker == null || recipient == null) return false;
            if (!CmsFlags.isEnabled("shadow_mute")) return false;

            Set<Integer> set = MUTED;
            if (set.isEmpty()) return false;

            int speakerId = speaker.getHabboInfo().getId();
            if (!set.contains(speakerId)) return false;

            // Il mittente continua a vedere i propri messaggi (non deve accorgersene).
            if (recipient.getHabboInfo().getId() == speakerId) return false;

            // Lo staff (chi vede i whisper) continua a vedere la chat shadow-mutata.
            if (recipient.hasPermission(Permission.ACC_SEE_WHISPERS)) return false;

            return true;
        } catch (Throwable t) {
            return false; // qualsiasi errore -> comportamento invariato
        }
    }
}
