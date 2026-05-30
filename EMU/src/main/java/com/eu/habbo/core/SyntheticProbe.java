package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Self-probe periodico per il game socket.
 *
 * Apre una connessione TCP verso la game port dell'emulatore stesso ogni N
 * secondi e misura il tempo di connect. Serve a:
 *   - rilevare freeze del boss event-loop (`accept()` non risponde) prima
 *     che la coda di SYN si saturi e i client veri inizino a vedere errori;
 *   - alimentare {@link HealthEndpoint#SYNTHETIC_CONNECT_SECONDS} per
 *     dashboarding/alert ("connect time anomalo" => problema reale);
 *   - dare un canary "zero-cost" che non richiede un client esterno.
 *
 * NON parla il wire protocol: apre TCP, misura il connect, chiude. Per un
 * canary applicativo completo (login + chat) servira' un client Habbo
 * minimale — lavoro di Fase E.
 *
 * Config:
 *   synthetic.probe.enabled  (default 1)
 *   synthetic.probe.interval_sec (default 30)
 *   synthetic.probe.timeout_ms   (default 2000)
 *   synthetic.probe.host         (default 127.0.0.1)
 *
 * Si AUTO-DISABLE se il bind dell'EMU e' su 0.0.0.0 e probe.host=auto:
 * proviamo 127.0.0.1, e se fallisce 3 volte di fila smettiamo (operatore
 * probabilmente ha messo l'EMU dietro a una rete che non risponde su loopback).
 */
public final class SyntheticProbe {

    private static final Logger LOGGER = LoggerFactory.getLogger(SyntheticProbe.class);

    private static ScheduledExecutorService scheduler;
    private static volatile int consecutiveFailures = 0;
    private static final int MAX_CONSECUTIVE_FAILURES = 10;

    private SyntheticProbe() {
    }

    public static synchronized void start() {
        if (scheduler != null) return;
        boolean enabled = Emulator.getConfig().getBoolean("synthetic.probe.enabled", true);
        if (!enabled) {
            LOGGER.info("SyntheticProbe -> disabilitato");
            return;
        }
        int interval = Math.max(5, Emulator.getConfig().getInt("synthetic.probe.interval_sec", 30));

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SyntheticProbe");
            t.setDaemon(true);
            return t;
        });
        // Initial delay 30s: lascia all'EMU il tempo di completare il bootstrap.
        scheduler.scheduleAtFixedRate(SyntheticProbe::tick, 30, interval, TimeUnit.SECONDS);
        LOGGER.info("SyntheticProbe -> probing ogni {}s", interval);
    }

    public static synchronized void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    private static void tick() {
        if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
            // Stop auto-loud: bind probabilmente unreachable, niente da fare.
            // L'operatore lo capisce dalla mancanza di sample nel histogram.
            return;
        }
        try {
            String host = Emulator.getConfig().getValue("synthetic.probe.host", "127.0.0.1");
            int port = Emulator.getConfig().getInt("game.port", 30000);
            int timeoutMs = Emulator.getConfig().getInt("synthetic.probe.timeout_ms", 2000);

            long t0 = System.nanoTime();
            try (Socket s = new Socket()) {
                s.setSoTimeout(timeoutMs);
                s.connect(new InetSocketAddress(host, port), timeoutMs);
                long dt = System.nanoTime() - t0;
                HealthEndpoint.SYNTHETIC_CONNECT_SECONDS.record(dt);
                if (consecutiveFailures > 0) {
                    LOGGER.info("SyntheticProbe -> ripristinato dopo {} tentativi falliti", consecutiveFailures);
                }
                consecutiveFailures = 0;
            }
        } catch (IOException e) {
            consecutiveFailures++;
            // Solo i primi 3 fallimenti vengono loggati per non spammare; dopo
            // ci pensa l'alert PromQL (assenza di sample nel bucket).
            if (consecutiveFailures <= 3) {
                LOGGER.warn("SyntheticProbe -> connect fallito ({}): {}",
                        consecutiveFailures, e.toString());
            }
        } catch (Throwable t) {
            LOGGER.warn("SyntheticProbe -> errore inatteso", t);
        }
    }
}
