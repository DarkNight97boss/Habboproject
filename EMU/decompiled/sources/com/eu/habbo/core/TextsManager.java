package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/core/TextsManager.class */
public class TextsManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(TextsManager.class);
    private final Properties texts;

    public TextsManager() {
        long jCurrentTimeMillis = System.currentTimeMillis();
        this.texts = new Properties();
        try {
            reload();
            LOGGER.info("Texts Manager -> Loaded! (" + (System.currentTimeMillis() - jCurrentTimeMillis) + " MS)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reload() throws Exception {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                Statement statementCreateStatement = connection.createStatement();
                try {
                    ResultSet resultSetExecuteQuery = statementCreateStatement.executeQuery("SELECT * FROM emulator_texts");
                    while (resultSetExecuteQuery.next()) {
                        try {
                            if (this.texts.containsKey(resultSetExecuteQuery.getString("key"))) {
                                this.texts.setProperty(resultSetExecuteQuery.getString("key"), resultSetExecuteQuery.getString("value"));
                            } else {
                                this.texts.put(resultSetExecuteQuery.getString("key"), resultSetExecuteQuery.getString("value"));
                            }
                        } catch (Throwable th) {
                            if (resultSetExecuteQuery != null) {
                                try {
                                    resultSetExecuteQuery.close();
                                } catch (Throwable th2) {
                                    th.addSuppressed(th2);
                                }
                            }
                            throw th;
                        }
                    }
                    if (resultSetExecuteQuery != null) {
                        resultSetExecuteQuery.close();
                    }
                    if (statementCreateStatement != null) {
                        statementCreateStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
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
        if (!this.texts.containsKey(str)) {
            LOGGER.error("Text key not found: {}", str);
        }
        return this.texts.getProperty(str, str2);
    }

    public boolean getBoolean(String str) {
        return getBoolean(str, false);
    }

    public boolean getBoolean(String str, Boolean bool) {
        try {
            if (!getValue(str, "0").equals("1")) {
                if (!getValue(str, "false").equals("true")) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
            return bool.booleanValue();
        }
    }

    public int getInt(String str) {
        return getInt(str, 0);
    }

    public int getInt(String str, Integer num) {
        try {
            return Integer.parseInt(getValue(str, num.toString()));
        } catch (NumberFormatException e) {
            LOGGER.error("Caught exception", e);
            return num.intValue();
        }
    }

    public void update(String str, String str2) {
        this.texts.setProperty(str, str2);
    }

    public void register(String str, String str2) {
        if (this.texts.getProperty(str, null) != null) {
            return;
        }
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO emulator_texts VALUES (?, ?)");
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
