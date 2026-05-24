package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import com.eu.habbo.plugin.events.emulator.EmulatorConfigUpdatedEvent;
import gnu.trove.map.hash.THashMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/core/ConfigurationManager.class */
public class ConfigurationManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationManager.class);
    private final String configurationPath;
    public boolean loaded = false;
    public boolean isLoading = false;
    private final Properties properties = new Properties();

    public ConfigurationManager(String str) {
        this.configurationPath = str;
        reload();
    }

    public void reload() {
        this.isLoading = true;
        this.properties.clear();
        FileInputStream fileInputStream = null;
        String str = System.getenv("DB_HOSTNAME");
        try {
            if (str != null ? str.length() > 1 : false) {
                THashMap tHashMap = new THashMap();
                tHashMap.put("db.hostname", "DB_HOSTNAME");
                tHashMap.put("db.port", "DB_PORT");
                tHashMap.put("db.database", "DB_DATABASE");
                tHashMap.put("db.username", "DB_USERNAME");
                tHashMap.put("db.password", "DB_PASSWORD");
                tHashMap.put("db.params", "DB_PARAMS");
                tHashMap.put("game.host", "EMU_HOST");
                tHashMap.put("game.port", "EMU_PORT");
                tHashMap.put("rcon.host", "RCON_HOST");
                tHashMap.put("rcon.port", "RCON_PORT");
                tHashMap.put("rcon.allowed", "RCON_ALLOWED");
                tHashMap.put("runtime.threads", "RT_THREADS");
                tHashMap.put("logging.errors.runtime", "RT_LOG_ERRORS");
                for (Map.Entry entry : tHashMap.entrySet()) {
                    String str2 = System.getenv((String) entry.getValue());
                    if (str2 == null || str2.length() == 0) {
                        LOGGER.info("Cannot find environment-value for variable `" + ((String) entry.getValue()) + "`");
                    } else {
                        this.properties.setProperty((String) entry.getKey(), str2);
                    }
                }
            } else {
                try {
                    fileInputStream = new FileInputStream(new File(this.configurationPath));
                    this.properties.load(fileInputStream);
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e2) {
                    LOGGER.error("Failed to load config file.", e2);
                    e2.printStackTrace();
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e3) {
                            e3.printStackTrace();
                        }
                    }
                }
            }
            if (this.loaded) {
                loadFromDatabase();
            }
            this.isLoading = false;
            LOGGER.info("Configuration Manager -> Loaded!");
            if (Emulator.getPluginManager() != null) {
                Emulator.getPluginManager().fireEvent(new EmulatorConfigUpdatedEvent());
            }
        } catch (Throwable th) {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
            throw th;
        }
    }

    public void loadFromDatabase() {
        Connection connection;
        Statement statementCreateStatement;
        LOGGER.info("Loading configuration from database...");
        long jCurrentTimeMillis = System.currentTimeMillis();
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                statementCreateStatement = connection.createStatement();
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            if (statementCreateStatement.execute("SELECT * FROM emulator_settings")) {
                ResultSet resultSet = statementCreateStatement.getResultSet();
                while (resultSet.next()) {
                    try {
                        this.properties.put(resultSet.getString("key"), resultSet.getString("value"));
                    } catch (Throwable th) {
                        if (resultSet != null) {
                            try {
                                resultSet.close();
                            } catch (Throwable th2) {
                                th.addSuppressed(th2);
                            }
                        }
                        throw th;
                    }
                }
                if (resultSet != null) {
                    resultSet.close();
                }
            }
            if (statementCreateStatement != null) {
                statementCreateStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
            LOGGER.info("Configuration -> loaded! (" + (System.currentTimeMillis() - jCurrentTimeMillis) + " MS)");
        } catch (Throwable th3) {
            if (statementCreateStatement != null) {
                try {
                    statementCreateStatement.close();
                } catch (Throwable th4) {
                    th3.addSuppressed(th4);
                }
            }
            throw th3;
        }
    }

    public void saveToDatabase() {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE emulator_settings SET `value` = ? WHERE `key` = ? LIMIT 1");
                try {
                    for (Map.Entry entry : this.properties.entrySet()) {
                        preparedStatementPrepareStatement.setString(1, entry.getValue().toString());
                        preparedStatementPrepareStatement.setString(2, entry.getKey().toString());
                        preparedStatementPrepareStatement.executeUpdate();
                    }
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Throwable th) {
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                    }
                    throw th;
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }

    public String getValue(String str) {
        return getValue(str, Emulator.PREVIEW);
    }

    public String getValue(String str, String str2) {
        if (this.isLoading) {
            return str2;
        }
        if (!this.properties.containsKey(str)) {
            LOGGER.error("Config key not found {}", str);
        }
        return this.properties.getProperty(str, str2);
    }

    public boolean getBoolean(String str) {
        return getBoolean(str, false);
    }

    public boolean getBoolean(String str, boolean z) {
        if (this.isLoading) {
            return z;
        }
        try {
            if (!getValue(str, "0").equals("1")) {
                if (!getValue(str, "false").equals("true")) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to parse key {} with value '{}' to type boolean.", str, getValue(str));
            return z;
        }
    }

    public int getInt(String str) {
        return getInt(str, 0);
    }

    public int getInt(String str, Integer num) {
        if (this.isLoading) {
            return num.intValue();
        }
        try {
            return Integer.parseInt(getValue(str, num.toString()));
        } catch (Exception e) {
            LOGGER.error("Failed to parse key {} with value '{}' to type integer.", str, getValue(str));
            return num.intValue();
        }
    }

    public double getDouble(String str) {
        return getDouble(str, Double.valueOf(0.0d));
    }

    public double getDouble(String str, Double d) {
        if (this.isLoading) {
            return d.doubleValue();
        }
        try {
            return Double.parseDouble(getValue(str, d.toString()));
        } catch (Exception e) {
            LOGGER.error("Failed to parse key {} with value '{}' to type double.", str, getValue(str));
            return d.doubleValue();
        }
    }

    public void update(String str, String str2) {
        this.properties.setProperty(str, str2);
    }

    public void register(String str, String str2) {
        if (this.properties.getProperty(str, null) != null) {
            return;
        }
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO emulator_settings VALUES (?, ?)");
                try {
                    preparedStatementPrepareStatement.setString(1, str);
                    preparedStatementPrepareStatement.setString(2, str2);
                    preparedStatementPrepareStatement.execute();
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Throwable th) {
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                    }
                    throw th;
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        update(str, str2);
    }
}
