package com.eu.habbo.database;

import com.eu.habbo.core.ConfigurationManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DatabasePool {
    private final Logger log = LoggerFactory.getLogger(DatabasePool.class);
    private HikariDataSource database;

    public boolean getStoragePooling(ConfigurationManager config) {
        try {
            HikariConfig databaseConfiguration = new HikariConfig();

            // Auto-size the pool to the hardware. The legacy default of 50 was set
            // for a 2-core dev box; on a 16-core production node it caps throughput
            // long before the DB or network. 4×CPU is the rule-of-thumb HikariCP
            // recommends for mixed I/O workloads. Operators can still override via
            // db.pool.maxsize / db.pool.minsize in config.ini.
            int cores = Math.max(1, Runtime.getRuntime().availableProcessors());
            int defaultMax = Math.max(50, cores * 4);
            int defaultMin = Math.max(10, Math.min(defaultMax, cores * 2));
            databaseConfiguration.setMaximumPoolSize(config.getInt("db.pool.maxsize", defaultMax));
            databaseConfiguration.setMinimumIdle(config.getInt("db.pool.minsize", defaultMin));
            databaseConfiguration.setJdbcUrl("jdbc:mysql://" + config.getValue("db.hostname", "localhost") + ":" + config.getValue("db.port", "3306") + "/" + config.getValue("db.database", "habbo") + config.getValue("db.params"));
            databaseConfiguration.addDataSourceProperty("serverName", config.getValue("db.hostname", "localhost"));
            databaseConfiguration.addDataSourceProperty("port", config.getValue("db.port", "3306"));
            databaseConfiguration.addDataSourceProperty("databaseName", config.getValue("db.database", "habbo"));
            databaseConfiguration.addDataSourceProperty("user", config.getValue("db.username"));
            databaseConfiguration.addDataSourceProperty("password", config.getValue("db.password"));
            databaseConfiguration.addDataSourceProperty("dataSource.logger", "com.mysql.jdbc.log.StandardLogger");
            databaseConfiguration.addDataSourceProperty("dataSource.logSlowQueries", "true");
            databaseConfiguration.addDataSourceProperty("dataSource.dumpQueriesOnException", "true");
            databaseConfiguration.addDataSourceProperty("prepStmtCacheSize", "500");
            databaseConfiguration.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            // databaseConfiguration.addDataSourceProperty("dataSource.logWriter", Logging.getErrorsSQLWriter());
            databaseConfiguration.addDataSourceProperty("cachePrepStmts", "true");
            databaseConfiguration.addDataSourceProperty("useServerPrepStmts", "true");
            databaseConfiguration.addDataSourceProperty("rewriteBatchedStatements", "true");
            databaseConfiguration.addDataSourceProperty("useUnicode", "true");
            databaseConfiguration.setAutoCommit(true);
            // Connection timeout reduced from 5 min to 30s — if we can't get a
            // connection in 30s the pool is exhausted and we want the caller to
            // fail loud (and trigger an upstream alert) instead of accumulating
            // threads blocked on getConnection().
            databaseConfiguration.setConnectionTimeout(config.getInt("db.pool.connection_timeout_ms", 30_000));
            databaseConfiguration.setValidationTimeout(5000L);
            databaseConfiguration.setLeakDetectionThreshold(config.getInt("db.pool.leak_detection_ms", 20000));
            databaseConfiguration.setMaxLifetime(1800000L);
            databaseConfiguration.setIdleTimeout(600000L);
            // NOTE: setKeepaliveTime() would defeat stale-connection drops from
            // MySQL `wait_timeout` / NAT idle reapers, but it requires HikariCP
            // >=4.0 (Java 11). We're on 3.4.3 — keep maxLifetime conservative
            // (30 min, below MySQL's default 8h wait_timeout) until the upgrade.
            //databaseConfiguration.setDriverClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
            this.database = new HikariDataSource(databaseConfiguration);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public HikariDataSource getDatabase() {
        return this.database;
    }
}