package com.habbo.loadtest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TCP connect-storm load tester. Misura accept-rate e latenza di handshake
 * (SYN, SYN-ACK, ACK) verso un endpoint Netty.
 *
 * Non manda payload applicativo — quello richiede l'implementazione del
 * wire protocol Habbo, fuori scope. Per i fini di capacity planning
 * (boss event-loop, kernel backlog, FD cap) il connect-storm e' la
 * misura piu' utile e neutrale.
 *
 * Usage (vedi README):
 *   java -jar loadtest.jar --host H --port P --connections N --duration 30s
 */
public final class LoadTest {

    public static void main(String[] argv) throws Exception {
        Args a = Args.parse(argv);
        System.out.printf("Target=%s:%d connections=%d duration=%ds connectTimeoutMs=%d%n",
                a.host, a.port, a.connections, a.durationSec, a.connectTimeoutMs);

        AtomicInteger connected = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);
        AtomicInteger inflight = new AtomicInteger(0);
        AtomicLong nanoSum = new AtomicLong(0);
        long[] samples = new long[a.connections]; // p50/p95/p99 storage
        Arrays.fill(samples, -1L);

        ExecutorService exec = Executors.newFixedThreadPool(
                Math.min(64, Math.max(8, Runtime.getRuntime().availableProcessors() * 4)));
        ScheduledExecutorService statusTicker = Executors.newSingleThreadScheduledExecutor();
        long start = System.nanoTime();

        // Status ticker ogni secondo
        statusTicker.scheduleAtFixedRate(() -> {
            long elapsedSec = (System.nanoTime() - start) / 1_000_000_000L;
            System.out.printf("[+%ds] connected=%d failed=%d inflight=%d%n",
                    elapsedSec, connected.get(), failed.get(), inflight.get());
        }, 1, 1, TimeUnit.SECONDS);

        CountDownLatch latch = new CountDownLatch(a.connections);
        SocketAddress addr = new InetSocketAddress(a.host, a.port);

        for (int i = 0; i < a.connections; i++) {
            final int idx = i;
            exec.submit(() -> {
                long t0 = System.nanoTime();
                inflight.incrementAndGet();
                try (Socket s = new Socket()) {
                    s.connect(addr, a.connectTimeoutMs);
                    long dt = System.nanoTime() - t0;
                    samples[idx] = dt;
                    nanoSum.addAndGet(dt);
                    connected.incrementAndGet();
                    // Mantieni la connessione aperta per "duration" totale,
                    // cosi' il server vede contemporaneamente N socket.
                    long holdUntil = start + a.durationSec * 1_000_000_000L;
                    while (System.nanoTime() < holdUntil) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                } catch (IOException e) {
                    failed.incrementAndGet();
                } finally {
                    inflight.decrementAndGet();
                    latch.countDown();
                }
            });
        }

        // Aspetta che la "duration" piu' un piccolo grace finiscano i task.
        boolean done = latch.await(a.durationSec + 30, TimeUnit.SECONDS);
        statusTicker.shutdownNow();
        exec.shutdownNow();

        long elapsedNs = System.nanoTime() - start;
        printSummary(connected.get(), failed.get(), samples, nanoSum.get(), elapsedNs);
        if (!done) {
            System.out.println("WARN: timeout aspettando i task; alcuni socket potrebbero non aver chiuso");
        }
        // Exit code != 0 se troppe failure (>= 5%)
        if (a.connections > 0 && failed.get() * 100 / a.connections >= 5) {
            System.exit(2);
        }
    }

    private static void printSummary(int connected, int failed, long[] samples, long nanoSum, long elapsedNs) {
        int total = connected + failed;
        System.out.println();
        System.out.println("=== Summary ===");
        System.out.printf(Locale.ROOT, "Total attempts:    %d%n", total);
        if (total > 0) {
            System.out.printf(Locale.ROOT, "Connected:         %d (%.1f%%)%n",
                    connected, 100.0 * connected / total);
            System.out.printf(Locale.ROOT, "Failed:            %d (%.1f%%)%n",
                    failed, 100.0 * failed / total);
        }
        // Estrai solo i sample validi
        long[] valid = Arrays.stream(samples).filter(x -> x >= 0).sorted().toArray();
        if (valid.length > 0) {
            System.out.printf(Locale.ROOT, "Connect avg:       %.1f ms%n",
                    (nanoSum / 1e6) / connected);
            System.out.printf(Locale.ROOT, "Connect p50:       %.1f ms%n", percentile(valid, 50) / 1e6);
            System.out.printf(Locale.ROOT, "Connect p95:       %.1f ms%n", percentile(valid, 95) / 1e6);
            System.out.printf(Locale.ROOT, "Connect p99:       %.1f ms%n", percentile(valid, 99) / 1e6);
            System.out.printf(Locale.ROOT, "Connect max:       %.1f ms%n", valid[valid.length - 1] / 1e6);
        }
        System.out.printf(Locale.ROOT, "Wall clock:        %.1fs%n", elapsedNs / 1e9);
    }

    private static long percentile(long[] sorted, int p) {
        if (sorted.length == 0) return 0;
        int idx = Math.min(sorted.length - 1, (int) Math.ceil(sorted.length * p / 100.0) - 1);
        return sorted[Math.max(0, idx)];
    }

    static final class Args {
        String host = "127.0.0.1";
        int port = 3000;
        int connections = 100;
        int durationSec = 10;
        int connectTimeoutMs = 5000;

        static Args parse(String[] argv) {
            Args a = new Args();
            for (int i = 0; i < argv.length; i++) {
                switch (argv[i]) {
                    case "--host": a.host = argv[++i]; break;
                    case "--port": a.port = Integer.parseInt(argv[++i]); break;
                    case "--connections": a.connections = Integer.parseInt(argv[++i]); break;
                    case "--duration":
                        a.durationSec = parseDuration(argv[++i]); break;
                    case "--connect-timeout":
                        a.connectTimeoutMs = Integer.parseInt(argv[++i]); break;
                    case "-h":
                    case "--help":
                        System.out.println(
                                "Usage: java -jar loadtest.jar [options]\n" +
                                "  --host HOST                target host (default 127.0.0.1)\n" +
                                "  --port PORT                target port (default 3000)\n" +
                                "  --connections N            concurrent connections to open (default 100)\n" +
                                "  --duration  SEC[s|m]       hold connections open for SEC (default 10s)\n" +
                                "  --connect-timeout MS       per-connect timeout in ms (default 5000)\n");
                        System.exit(0);
                        break;
                    default:
                        System.err.println("Unknown arg: " + argv[i]);
                        System.exit(1);
                }
            }
            return a;
        }

        private static int parseDuration(String s) {
            s = s.trim().toLowerCase(Locale.ROOT);
            if (s.endsWith("ms")) return Math.max(1, Integer.parseInt(s.substring(0, s.length() - 2)) / 1000);
            if (s.endsWith("s")) return Integer.parseInt(s.substring(0, s.length() - 1));
            if (s.endsWith("m")) return Integer.parseInt(s.substring(0, s.length() - 1)) * 60;
            return Integer.parseInt(s);
        }
    }
}
