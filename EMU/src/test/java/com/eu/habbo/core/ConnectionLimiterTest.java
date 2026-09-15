package com.eu.habbo.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Path critico: il cap per-IP non deve MAI bloccare il traffico legittimo
 * dietro nginx (tutti 127.0.0.1) e deve fermare un singolo IP che apre
 * troppe connessioni.
 */
class ConnectionLimiterTest {

    @Test
    void oltreIlLimiteRifiutaSenzaContare() {
        ConnectionLimiter l = new ConnectionLimiter(2);
        assertTrue(l.acquire("203.0.113.5"));
        assertTrue(l.acquire("203.0.113.5"));
        assertFalse(l.acquire("203.0.113.5"), "la terza connessione va rifiutata");
        assertEquals(2, l.current("203.0.113.5"), "la rifiutata NON deve essere contata");
        l.release("203.0.113.5");
        assertTrue(l.acquire("203.0.113.5"), "dopo un release si riapre uno slot");
    }

    @Test
    void ipDiversiHannoContatoriIndipendenti() {
        ConnectionLimiter l = new ConnectionLimiter(1);
        assertTrue(l.acquire("203.0.113.1"));
        assertTrue(l.acquire("203.0.113.2"));
        assertFalse(l.acquire("203.0.113.1"));
    }

    @Test
    void loopbackEIpSconosciutiSonoSempreEsenti() {
        ConnectionLimiter l = new ConnectionLimiter(1);
        for (int i = 0; i < 100; i++) {
            assertTrue(l.acquire("127.0.0.1"), "dietro nginx tutti i client sono 127.0.0.1: mai limitare");
            assertTrue(l.acquire("::1"));
            assertTrue(l.acquire("::ffff:127.0.0.1"));
            assertTrue(l.acquire(null));
            assertTrue(l.acquire(""));
        }
        assertEquals(0, l.current("127.0.0.1"));
    }

    @Test
    void limiteZeroDisattivaIlCap() {
        ConnectionLimiter l = new ConnectionLimiter(0);
        for (int i = 0; i < 1000; i++) assertTrue(l.acquire("198.51.100.9"));
    }

    @Test
    void releaseInEccessoNonVaSottoZero() {
        ConnectionLimiter l = new ConnectionLimiter(3);
        l.release("198.51.100.1");
        l.release("198.51.100.1");
        assertEquals(0, l.current("198.51.100.1"));
        assertTrue(l.acquire("198.51.100.1"));
    }
}
