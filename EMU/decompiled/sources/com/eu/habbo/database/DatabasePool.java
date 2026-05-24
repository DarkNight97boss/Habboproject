package com.eu.habbo.database;

import com.eu.habbo.core.ConfigurationManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/database/DatabasePool.class */
class DatabasePool {
    private final Logger log = LoggerFactory.getLogger(DatabasePool.class);
    private HikariDataSource database;

    DatabasePool() {
    }

    public boolean getStoragePooling(ConfigurationManager configurationManager) {
        try {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setMaximumPoolSize(configurationManager.getInt("db.pool.maxsize", 50));
            hikariConfig.setMinimumIdle(configurationManager.getInt("db.pool.minsize", 10));
            hikariConfig.setJdbcUrl("jdbc:mysql://" + configurationManager.getValue("db.hostname", "localhost") + ":" + configurationManager.getValue("db.port", "3306") + "/" + configurationManager.getValue("db.database", "habbo") + configurationManager.getValue("db.params"));
            hikariConfig.addDataSourceProperty("serverName", configurationManager.getValue("db.hostname", "localhost"));
            hikariConfig.addDataSourceProperty("port", configurationManager.getValue("db.port", "3306"));
            hikariConfig.addDataSourceProperty("databaseName", configurationManager.getValue("db.database", "habbo"));
            hikariConfig.addDataSourceProperty("user", configurationManager.getValue("db.username"));
            hikariConfig.addDataSourceProperty("password", configurationManager.getValue("db.password"));
            hikariConfig.addDataSourceProperty("dataSource.logger", "com.mysql.jdbc.log.StandardLogger");
            hikariConfig.addDataSourceProperty("dataSource.logSlowQueries", "true");
            hikariConfig.addDataSourceProperty("dataSource.dumpQueriesOnException", "true");
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", "500");
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
            hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
            hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
            hikariConfig.addDataSourceProperty("useUnicode", "true");
            hikariConfig.setAutoCommit(true);
            hikariConfig.setConnectionTimeout(300000L);
            hikariConfig.setValidationTimeout(5000L);
            hikariConfig.setLeakDetectionThreshold(20000L);
            hikariConfig.setMaxLifetime(1800000L);
            hikariConfig.setIdleTimeout(600000L);
            this.database = new HikariDataSource(hikariConfig);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public HikariDataSource getDatabase() {
        return this.database;
    }
}
