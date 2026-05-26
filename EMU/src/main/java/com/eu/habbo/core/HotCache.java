package com.eu.habbo.core;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Central registry of in-process Caffeine caches for hot read paths.
 *
 * Why a registry instead of per-class caches:
 *   - One place to tune eviction limits when operators tail metrics.
 *   - One place to invalidate everything on a hot-reload event.
 *   - Each domain stays narrow: cache-by-name is the *only* concession to
 *     "performance"; the source of truth still lives in CatalogManager/etc.
 *
 * Naming convention: lower-snake_case keys, scoped by domain. We deliberately
 * keep the API tiny — callers go through {@link #catalogPageByName} (etc.)
 * rather than fetching a raw Cache, so misuse can't leak invariants like
 * "case-insensitive lookup" or "trim before query" outside this class.
 */
public final class HotCache {

    private HotCache() {}

    /**
     * Catalog pages addressed by their human name (e.g. "frontpage").
     *
     * Hit path in CatalogManager scans every page in a stream filter — O(n)
     * for what's effectively a hash-table lookup. Pages don't change at
     * runtime outside a hot-reload, so a 30-minute TTL is generous.
     */
    private static final Cache<String, Object> CATALOG_PAGE_BY_NAME =
            Caffeine.newBuilder()
                    .maximumSize(512)
                    .expireAfterWrite(30, TimeUnit.MINUTES)
                    .recordStats()
                    .build();

    public static Object catalogPageByName(String name) {
        if (name == null) return null;
        return CATALOG_PAGE_BY_NAME.getIfPresent(name.toLowerCase(Locale.ROOT));
    }

    public static void putCatalogPageByName(String name, Object page) {
        if (name == null || page == null) return;
        CATALOG_PAGE_BY_NAME.put(name.toLowerCase(Locale.ROOT), page);
    }

    public static void invalidateCatalogPages() {
        CATALOG_PAGE_BY_NAME.invalidateAll();
    }

    /**
     * Total number of entries across all caches. Exposed for the SecurityMonitor
     * tick so operators can spot a cache that's been disabled by misconfiguration.
     */
    public static long approximateSize() {
        return CATALOG_PAGE_BY_NAME.estimatedSize();
    }
}
