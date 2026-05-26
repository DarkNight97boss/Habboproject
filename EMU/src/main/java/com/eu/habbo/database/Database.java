package com.eu.habbo.database;

import com.eu.habbo.Emulator;
import com.eu.habbo.core.ConfigurationManager;
import com.zaxxer.hikari.HikariDataSource;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Database {

    private static final Logger LOGGER = LoggerFactory.getLogger(Database.class);

    private HikariDataSource dataSource;
    private DatabasePool databasePool;

    /**
     * Pool secondario verso una read-replica MariaDB (opzionale).
     * Quando configurato, le query read-only "pesanti" (audit_log scan,
     * leaderboard, statistiche) possono passare via {@link #getReadDataSource()}
     * invece di consumare connection del pool primary. Senza replica
     * configurata, getReadDataSource() ritorna il primary -> zero impatto
     * sui caller esistenti.
     */
    private HikariDataSource readDataSource;

    public Database(ConfigurationManager config) {
        long millis = System.currentTimeMillis();

        boolean SQLException = false;

        try {
            this.databasePool = new DatabasePool();
            if (!this.databasePool.getStoragePooling(config)) {
                LOGGER.info("Failed to connect to the database. Please check config.ini and make sure the MySQL process is running. Shutting down...");
                SQLException = true;
                return;
            }
            this.dataSource = this.databasePool.getDatabase();

            // Read replica opzionale. Se db.replica.host e' vuoto, restiamo
            // single-DB (getReadDataSource() = primary). Se settato, monta
            // un secondo pool isolato verso quel host, in read-only.
            String replicaHost = config.getValue("db.replica.host", "");
            if (replicaHost != null && !replicaHost.trim().isEmpty()) {
                try {
                    this.readDataSource = DatabasePool.buildReadReplica(config);
                    LOGGER.info("Database -> read replica configured at {}", replicaHost);
                } catch (Exception e) {
                    LOGGER.warn("Database -> failed to init read replica '{}', falling back to primary: {}",
                            replicaHost, e.toString());
                    this.readDataSource = null;
                }
            }
        } catch (Exception e) {
            SQLException = true;
            LOGGER.error("Failed to connect to your database.", e);
        } finally {
            if (SQLException) {
                Emulator.prepareShutdown();
            }
        }

        LOGGER.info("Database -> Connected! ({} MS)", System.currentTimeMillis() - millis);
    }

    public void dispose() {
        if (this.databasePool != null) {
            this.databasePool.getDatabase().close();
        }

        this.dataSource.close();
        if (this.readDataSource != null) {
            try { this.readDataSource.close(); } catch (Exception ignored) {}
        }
    }

    public HikariDataSource getDataSource() {
        return this.dataSource;
    }

    /**
     * Ritorna il pool da usare per query READ-ONLY potenzialmente pesanti
     * (scan, leaderboard, stats). Se non e' configurata una replica,
     * ritorna il primary -> chiamanti possono usarlo opportunisticamente
     * senza preoccuparsi della topologia.
     */
    public HikariDataSource getReadDataSource() {
        return this.readDataSource != null ? this.readDataSource : this.dataSource;
    }

    public DatabasePool getDatabasePool() {
        return this.databasePool;
    }

    public static PreparedStatement preparedStatementWithParams(Connection connection, String query, THashMap<String, Object> queryParams) throws SQLException {
        THashMap<Integer, Object> params = new THashMap<Integer, Object>();
        THashSet<String> quotedParams = new THashSet<>();

        for(String key : queryParams.keySet()) {
            quotedParams.add(Pattern.quote(key));
        }

        String regex = "(" + String.join("|", quotedParams) + ")";

        Matcher m = Pattern.compile(regex).matcher(query);

        int i = 1;

        while (m.find()) {
            try {
                params.put(i, queryParams.get(m.group(1)));
                i++;
            }
            catch (Exception ignored) { }
        }

        PreparedStatement statement = connection.prepareStatement(query.replaceAll(regex, "?"));

        for(Map.Entry<Integer, Object> set : params.entrySet()) {
            if(set.getValue().getClass() == String.class) {
                statement.setString(set.getKey(), (String)set.getValue());
            }
            else if(set.getValue().getClass() == Integer.class) {
                statement.setInt(set.getKey(), (Integer)set.getValue());
            }
            else if(set.getValue().getClass() == Double.class) {
                statement.setDouble(set.getKey(), (Double)set.getValue());
            }
            else if(set.getValue().getClass() == Float.class) {
                statement.setFloat(set.getKey(), (Float)set.getValue());
            }
            else if(set.getValue().getClass() == Long.class) {
                statement.setLong(set.getKey(), (Long)set.getValue());
            }
            else {
                statement.setObject(set.getKey(), set.getValue());
            }
        }

        return statement;
    }
}
