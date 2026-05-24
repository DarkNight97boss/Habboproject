package com.eu.habbo.database;

import com.eu.habbo.Emulator;
import com.eu.habbo.core.ConfigurationManager;
import com.zaxxer.hikari.HikariDataSource;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/database/Database.class */
public class Database {
    private static final Logger LOGGER = LoggerFactory.getLogger(Database.class);
    private HikariDataSource dataSource;
    private DatabasePool databasePool;

    public Database(ConfigurationManager configurationManager) {
        long jCurrentTimeMillis = System.currentTimeMillis();
        try {
            try {
                this.databasePool = new DatabasePool();
            } catch (Exception e) {
                LOGGER.error("Failed to connect to your database.", e);
                if (1 != 0) {
                    Emulator.prepareShutdown();
                }
            }
            if (this.databasePool.getStoragePooling(configurationManager)) {
                this.dataSource = this.databasePool.getDatabase();
                if (0 != 0) {
                    Emulator.prepareShutdown();
                }
                LOGGER.info("Database -> Connected! ({} MS)", Long.valueOf(System.currentTimeMillis() - jCurrentTimeMillis));
                return;
            }
            LOGGER.info("Failed to connect to the database. Please check config.ini and make sure the MySQL process is running. Shutting down...");
            if (1 != 0) {
                Emulator.prepareShutdown();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                Emulator.prepareShutdown();
            }
            throw th;
        }
    }

    public void dispose() {
        if (this.databasePool != null) {
            this.databasePool.getDatabase().close();
        }
        this.dataSource.close();
    }

    public HikariDataSource getDataSource() {
        return this.dataSource;
    }

    public DatabasePool getDatabasePool() {
        return this.databasePool;
    }

    public static PreparedStatement preparedStatementWithParams(Connection connection, String str, THashMap<String, Object> tHashMap) throws SQLException {
        THashMap tHashMap2 = new THashMap();
        THashSet tHashSet = new THashSet();
        Iterator it = tHashMap.keySet().iterator();
        while (it.hasNext()) {
            tHashSet.add(Pattern.quote((String) it.next()));
        }
        String str2 = "(" + String.join("|", (Iterable<? extends CharSequence>) tHashSet) + ")";
        Matcher matcher = Pattern.compile(str2).matcher(str);
        int i = 1;
        while (matcher.find()) {
            try {
                tHashMap2.put(Integer.valueOf(i), tHashMap.get(matcher.group(1)));
                i++;
            } catch (Exception e) {
            }
        }
        PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement(str.replaceAll(str2, "?"));
        for (Map.Entry entry : tHashMap2.entrySet()) {
            if (entry.getValue().getClass() == String.class) {
                preparedStatementPrepareStatement.setString(((Integer) entry.getKey()).intValue(), (String) entry.getValue());
            } else if (entry.getValue().getClass() == Integer.class) {
                preparedStatementPrepareStatement.setInt(((Integer) entry.getKey()).intValue(), ((Integer) entry.getValue()).intValue());
            } else if (entry.getValue().getClass() == Double.class) {
                preparedStatementPrepareStatement.setDouble(((Integer) entry.getKey()).intValue(), ((Double) entry.getValue()).doubleValue());
            } else if (entry.getValue().getClass() == Float.class) {
                preparedStatementPrepareStatement.setFloat(((Integer) entry.getKey()).intValue(), ((Float) entry.getValue()).floatValue());
            } else if (entry.getValue().getClass() == Long.class) {
                preparedStatementPrepareStatement.setLong(((Integer) entry.getKey()).intValue(), ((Long) entry.getValue()).longValue());
            } else {
                preparedStatementPrepareStatement.setObject(((Integer) entry.getKey()).intValue(), entry.getValue());
            }
        }
        return preparedStatementPrepareStatement;
    }
}
