package com.eu.habbo.core;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * Histogram fisso-bucket per latenze (nanosecondi -> Prometheus-style seconds).
 *
 * Senza dipendenze esterne: ~70 righe, AtomicLong[]. Trade-off accettato:
 * niente quantili adattivi (HdrHistogram lo farebbe meglio) ma per il nostro
 * caso d'uso (packet processing time) la copertura con 12 bucket fissi e'
 * comparable a quella che Prometheus stesso usa per i suoi exporter
 * ufficiali.
 *
 * Bucket: 1ms, 5ms, 10ms, 25ms, 50ms, 100ms, 250ms, 500ms, 1s, 2.5s, 5s, +Inf.
 * Coprono dal "tutto in-memory veloce" al "DB roundtrip lento" senza spreco.
 *
 * Output format = Prometheus histogram canonico:
 *
 *   # HELP <name> <help>
 *   # TYPE <name> histogram
 *   <name>_bucket{le="0.001"} N1
 *   <name>_bucket{le="0.005"} N2
 *   ...
 *   <name>_bucket{le="+Inf"}  Ntot
 *   <name>_sum  <total seconds>
 *   <name>_count <Ntot>
 *
 * I bucket sono CUMULATIVI come da spec: bucket[i] = count(samples <= upper[i]).
 *
 * Thread-safety: tutti gli increment sono AtomicLong, niente lock. La snapshot
 * per /metrics e' relaxed-consistent (puo' contare un sample tra _sum e _count
 * in window infinitesimo): accettabile per scopi di monitoring.
 *
 * Costo per record(): ~80ns (1 ricerca lineare + 2 add atomici).
 */
public final class LatencyHistogram {

    /** Limiti superiori dei bucket, in secondi. Ordinati ascending. */
    private static final double[] UPPER_BOUNDS_SEC = {
            0.001, 0.005, 0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 1.0, 2.5, 5.0
    };
    /** Upper bound in nanosecondi (precomputed per non fare la moltiplicazione in hot path). */
    private static final long[] UPPER_BOUNDS_NS;

    static {
        UPPER_BOUNDS_NS = new long[UPPER_BOUNDS_SEC.length];
        for (int i = 0; i < UPPER_BOUNDS_SEC.length; i++) {
            UPPER_BOUNDS_NS[i] = (long) (UPPER_BOUNDS_SEC[i] * 1_000_000_000.0);
        }
    }

    private final String name;
    private final String help;
    /** Counter per bucket; ultimo elemento e' +Inf (overflow > 5s). */
    private final AtomicLongArray buckets;
    /** Somma totale in nanosecondi (compatta a 64 bit -> overflow a ~292 anni). */
    private final AtomicLong sumNanos = new AtomicLong(0);
    private final AtomicLong totalCount = new AtomicLong(0);

    public LatencyHistogram(String name, String help) {
        this.name = name;
        this.help = help;
        this.buckets = new AtomicLongArray(UPPER_BOUNDS_NS.length + 1); // + +Inf
    }

    /** Registra una latenza in ns. Hot path: tieni minimo. */
    public void record(long durationNanos) {
        if (durationNanos < 0) durationNanos = 0;
        // Ricerca lineare: 12 bucket, branch predictor fa il suo. Tentammo
        // binary search a 12 elementi -> stessa velocita', codice piu' fragile.
        int idx = UPPER_BOUNDS_NS.length; // default = +Inf
        for (int i = 0; i < UPPER_BOUNDS_NS.length; i++) {
            if (durationNanos <= UPPER_BOUNDS_NS[i]) {
                idx = i;
                break;
            }
        }
        // Bucket cumulativi: incrementa SOLO il bucket trovato. La snapshot
        // poi sommera' ascending (vedi appendTo). Alternativa: incrementare
        // tutti i bucket idx..N -> piu' write su hot path, no thanks.
        buckets.incrementAndGet(idx);
        sumNanos.addAndGet(durationNanos);
        totalCount.incrementAndGet();
    }

    /** Emette il blocco Prometheus-text-format dell'istogramma. */
    public void appendTo(StringBuilder sb) {
        sb.append("# HELP ").append(name).append(' ').append(help).append('\n');
        sb.append("# TYPE ").append(name).append(" histogram\n");
        long cumulative = 0;
        for (int i = 0; i < UPPER_BOUNDS_SEC.length; i++) {
            cumulative += buckets.get(i);
            sb.append(name).append("_bucket{le=\"").append(UPPER_BOUNDS_SEC[i]).append("\"} ").append(cumulative).append('\n');
        }
        cumulative += buckets.get(buckets.length() - 1);
        sb.append(name).append("_bucket{le=\"+Inf\"} ").append(cumulative).append('\n');
        // _sum in secondi (non in ns) come da convention Prometheus.
        double sumSec = sumNanos.get() / 1_000_000_000.0;
        sb.append(name).append("_sum ").append(sumSec).append('\n');
        sb.append(name).append("_count ").append(totalCount.get()).append('\n');
    }

    /** Conta totale di sample registrati. Utile per test/diagnostica. */
    public long count() {
        return totalCount.get();
    }
}
