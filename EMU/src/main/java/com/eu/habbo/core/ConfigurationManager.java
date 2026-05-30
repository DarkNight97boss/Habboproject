package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import com.eu.habbo.plugin.events.emulator.EmulatorConfigUpdatedEvent;
import gnu.trove.map.hash.THashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Map;
import java.util.Properties;

public class ConfigurationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationManager.class);

    private final Properties properties;
    private final String configurationPath;
    public boolean loaded = false;
    public boolean isLoading = false;

    public ConfigurationManager(String configurationPath) {
        this.properties = new Properties();
        this.configurationPath = configurationPath;
        this.reload();
    }

    public void reload() {
        this.isLoading = true;
        this.properties.clear();

        InputStream input = null;

        String envDbHostname = System.getenv("DB_HOSTNAME");

        boolean useEnvVarsForDbConnection = false;

        if(envDbHostname != null)
        {
            useEnvVarsForDbConnection = envDbHostname.length() > 1;
        }

        if (!useEnvVarsForDbConnection) {
            try {
                File f = new File(this.configurationPath);
                input = new FileInputStream(f);
                this.properties.load(input);

            } catch (IOException ex) {
                LOGGER.error("Caricamento del file di configurazione fallito.", ex);
                ex.printStackTrace();
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        } else {

            Map<String, String> envMapping = new THashMap<>();

            // Database section
            envMapping.put("db.hostname", "DB_HOSTNAME");
            envMapping.put("db.port", "DB_PORT");
            envMapping.put("db.database", "DB_DATABASE");
            envMapping.put("db.username", "DB_USERNAME");
            envMapping.put("db.password", "DB_PASSWORD");
            envMapping.put("db.params", "DB_PARAMS");

            // Game Configuration
            envMapping.put("game.host", "EMU_HOST");
            envMapping.put("game.port", "EMU_PORT");

            // RCON
            envMapping.put("rcon.host", "RCON_HOST");
            envMapping.put("rcon.port", "RCON_PORT");
            envMapping.put("rcon.allowed", "RCON_ALLOWED");

            // Runtime
            envMapping.put("runtime.threads", "RT_THREADS");
            envMapping.put("logging.errors.runtime", "RT_LOG_ERRORS");

            for (Map.Entry<String, String> entry : envMapping.entrySet()) {
                String envValue = System.getenv(entry.getValue());

                if (envValue == null || envValue.length() == 0) {
                    LOGGER.info("Impossibile trovare il valore d'ambiente per la variabile `{}`", entry.getValue());
                } else {
                    this.properties.setProperty(entry.getKey(), envValue);
                }
            }
        }

        if (this.loaded) {
            this.loadFromDatabase();
        }

        this.isLoading = false;
        LOGGER.info("Configuration Manager -> Caricato!");

        if (Emulator.getPluginManager() != null) {
            Emulator.getPluginManager().fireEvent(new EmulatorConfigUpdatedEvent());
        }
    }

    /**
     * Security-relevant keys whose value changes are audited so any silent toggle
     * from `:reload_config` (or a CMS-side SQLi residual) is forensically visible.
     */
    private static final String[] SECURITY_AUDITED_KEYS = new String[] {
            "mfa.staff.enabled", "mfa.staff.min_rank",
            "fingerprint.enabled", "watchdog.enabled",
            "rcon.allowed", "rcon.token", "rcon.allow.public",
            "websockets.whitelist",
            "io.proxy.protocol.enabled", "io.proxy.protocol.trusted",
            "sso.ticket.ttl.seconds",
            "subscriptions.hc.payday.query",
            "audit.signing.key",
            "plugins.enforce_allowlist", "plugins.allowed"
    };

    public void loadFromDatabase() {
        LOGGER.info("Caricamento configurazione dal database...");

        long millis = System.currentTimeMillis();

        // Snapshot before reload — we audit any DELTA on security-relevant keys.
        java.util.Map<String, String> before = new java.util.HashMap<>();
        for (String key : SECURITY_AUDITED_KEYS) {
            before.put(key, this.properties.getProperty(key, ""));
        }

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); Statement statement = connection.createStatement()) {
            if (statement.execute("SELECT * FROM emulator_settings")) {
                try (ResultSet set = statement.getResultSet()) {
                    while (set.next()) {
                        this.properties.put(set.getString("key"), set.getString("value"));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Eccezione SQL intercettata", e);
        }

        // Audit security deltas. Skip on cold start (when `before` is full of empty
        // strings AND we now have non-empty values) — that's just initial load, not a change.
        for (String key : SECURITY_AUDITED_KEYS) {
            String old = before.get(key);
            String now = this.properties.getProperty(key, "");
            boolean coldStart = (old == null || old.isEmpty());
            if (!coldStart && !old.equals(now)) {
                try {
                    String redactedOld = key.contains("token") || key.contains("signing.key") ? "***" : old;
                    String redactedNow = key.contains("token") || key.contains("signing.key") ? "***" : now;
                    com.eu.habbo.core.AuditLog.record(0, "system", "CONFIG_SECURITY_DELTA",
                            key, "old=" + redactedOld + " new=" + redactedNow);
                    LOGGER.warn("Security-relevant config changed: {} (audited)", key);
                } catch (Throwable ignored) {}
            }
        }

        LOGGER.info("Configurazione -> caricata! ({} MS)", System.currentTimeMillis() - millis);
    }

    public void saveToDatabase() {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("UPDATE emulator_settings SET `value` = ? WHERE `key` = ? LIMIT 1")) {
            for (Map.Entry<Object, Object> entry : this.properties.entrySet()) {
                statement.setString(1, entry.getValue().toString());
                statement.setString(2, entry.getKey().toString());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            LOGGER.error("Eccezione SQL intercettata", e);
        }
    }


    public String getValue(String key) {
        return this.getValue(key, "");
    }


    public String getValue(String key, String defaultValue) {
        if (this.isLoading)
            return defaultValue;

        if (!this.properties.containsKey(key)) {
            LOGGER.error("Chiave di configurazione non trovata {}", key);
        }
        return this.properties.getProperty(key, defaultValue);
    }

    public boolean getBoolean(String key) {
        return this.getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        if (this.isLoading)
            return defaultValue;

        try {
            return (this.getValue(key, "0").equals("1")) || (this.getValue(key, "false").equals("true"));
        } catch (Exception e) {
            LOGGER.error("Parsing della chiave {} con valore '{}' al tipo boolean fallito.", key, this.getValue(key));
        }
        return defaultValue;
    }

    public int getInt(String key) {
        return this.getInt(key, 0);
    }

    public int getInt(String key, Integer defaultValue) {
        if (this.isLoading)
            return defaultValue;

        try {
            return Integer.parseInt(this.getValue(key, defaultValue.toString()));
        } catch (Exception e) {
            LOGGER.error("Parsing della chiave {} con valore '{}' al tipo integer fallito.", key, this.getValue(key));
        }
        return defaultValue;
    }

    public double getDouble(String key) {
        return this.getDouble(key, 0.0);
    }

    public double getDouble(String key, Double defaultValue) {
        if (this.isLoading)
            return defaultValue;

        try {
            return Double.parseDouble(this.getValue(key, defaultValue.toString()));
        } catch (Exception e) {
            LOGGER.error("Parsing della chiave {} con valore '{}' al tipo double fallito.", key, this.getValue(key));
        }

        return defaultValue;
    }

    public void update(String key, String value) {
        this.properties.setProperty(key, value);
    }

    public void register(String key, String value) {
        if (this.properties.getProperty(key, null) != null)
            return;

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("INSERT INTO emulator_settings VALUES (?, ?)")) {
            statement.setString(1, key);
            statement.setString(2, value);
            statement.execute();
        } catch (SQLException e) {
            LOGGER.error("Eccezione SQL intercettata", e);
        }

        this.update(key, value);
    }
}
