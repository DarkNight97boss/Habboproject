package com.eu.habbo.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Path critico di osservabilita': l'istogramma alimenta lo SLI di latenza
 * su /metrics (Prometheus). Deve contare correttamente ed emettere il nome
 * della metrica nel formato text.
 */
class LatencyHistogramTest {

    @Test
    void contaLeOsservazioniEdEsponeLaMetrica() {
        LatencyHistogram h = new LatencyHistogram("test_op_seconds", "test help");
        assertEquals(0, h.count());
        h.record(1_000_000L);   // 1 ms
        h.record(250_000_000L); // 250 ms
        assertEquals(2, h.count());

        StringBuilder sb = new StringBuilder();
        h.appendTo(sb);
        String out = sb.toString();
        assertTrue(out.contains("test_op_seconds"), "il nome metrica deve comparire nell'output Prometheus");
        assertTrue(out.contains("test help"), "l'HELP deve comparire nell'output Prometheus");
    }
}
