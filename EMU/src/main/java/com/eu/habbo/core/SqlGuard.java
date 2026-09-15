package com.eu.habbo.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Guardia per le poche query che arrivano da DATI (tabella navigator_filter,
 * config.ini) e non da costanti del codice. Non sono raggiungibili dal client
 * di gioco, ma chi scrive nel DB o nella config non deve poter trasformare una
 * SELECT in qualcosa d'altro: accettiamo solo una singola SELECT senza
 * statement multipli, commenti o parole chiave di scrittura/file.
 */
public final class SqlGuard {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlGuard.class);
    private static final Pattern FORBIDDEN = Pattern.compile(
            "(?i)(;|--|/\\*|\\b(INSERT|UPDATE|DELETE|DROP|ALTER|CREATE|GRANT|REVOKE|TRUNCATE|REPLACE|LOAD_FILE|OUTFILE|DUMPFILE|BENCHMARK|SLEEP)\\b)");

    private SqlGuard() {
    }

    public static boolean isReadOnlySelect(String query) {
        if (query == null) return false;
        String q = query.trim();
        return q.regionMatches(true, 0, "SELECT", 0, 6) && !FORBIDDEN.matcher(q).find();
    }

    /** Ritorna la query se sicura, altrimenti logga e ritorna una SELECT vuota. */
    public static String readOnlySelectOrEmpty(String query, String origin) {
        if (isReadOnlySelect(query)) return query;
        LOGGER.error("Query rifiutata da {} (non e' una singola SELECT read-only): {}", origin, query);
        return "SELECT NULL LIMIT 0";
    }
}
