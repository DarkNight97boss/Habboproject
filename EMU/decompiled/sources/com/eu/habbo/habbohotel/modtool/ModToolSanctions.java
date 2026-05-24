package com.eu.habbo.habbohotel.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.plugin.events.sanctions.SanctionEvent;
import gnu.trove.map.hash.THashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/modtool/ModToolSanctions.class */
public class ModToolSanctions {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModToolSanctions.class);
    private final THashMap<Integer, ArrayList<ModToolSanctionItem>> sanctionHashmap;
    private final THashMap<Integer, ModToolSanctionLevelItem> sanctionLevelsHashmap;

    public ModToolSanctions() {
        long jCurrentTimeMillis = System.currentTimeMillis();
        this.sanctionHashmap = new THashMap<>();
        this.sanctionLevelsHashmap = new THashMap<>();
        loadModSanctions();
        LOGGER.info("Sanctions Manager -> Loaded! (" + (System.currentTimeMillis() - jCurrentTimeMillis) + " MS)");
    }

    public synchronized void loadModSanctions() {
        this.sanctionHashmap.clear();
        this.sanctionLevelsHashmap.clear();
        loadSanctionLevels();
    }

    private void loadSanctionLevels() {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM sanction_levels");
                try {
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            this.sanctionLevelsHashmap.put(Integer.valueOf(resultSetExecuteQuery.getInt("level")), new ModToolSanctionLevelItem(resultSetExecuteQuery.getInt("level"), resultSetExecuteQuery.getString("type"), resultSetExecuteQuery.getInt("hour_length"), resultSetExecuteQuery.getInt("probation_days")));
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
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Throwable th3) {
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
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

    public ModToolSanctionLevelItem getSanctionLevelItem(int i) {
        return (ModToolSanctionLevelItem) this.sanctionLevelsHashmap.get(Integer.valueOf(i));
    }

    public THashMap<Integer, ArrayList<ModToolSanctionItem>> getSanctions(int i) {
        THashMap<Integer, ArrayList<ModToolSanctionItem>> tHashMap;
        Connection connection;
        synchronized (this.sanctionHashmap) {
            this.sanctionHashmap.clear();
            try {
                connection = Emulator.getDatabase().getDataSource().getConnection();
            } catch (SQLException e) {
                LOGGER.error("Caught SQL exception", e);
            }
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM sanctions WHERE habbo_id = ? ORDER BY id ASC");
                try {
                    preparedStatementPrepareStatement.setInt(1, i);
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            if (this.sanctionHashmap.get(Integer.valueOf(resultSetExecuteQuery.getInt("habbo_id"))) == null) {
                                this.sanctionHashmap.put(Integer.valueOf(resultSetExecuteQuery.getInt("habbo_id")), new ArrayList());
                            }
                            ModToolSanctionItem modToolSanctionItem = new ModToolSanctionItem(resultSetExecuteQuery.getInt("id"), resultSetExecuteQuery.getInt("habbo_id"), resultSetExecuteQuery.getInt("sanction_level"), resultSetExecuteQuery.getInt("probation_timestamp"), resultSetExecuteQuery.getBoolean("is_muted"), resultSetExecuteQuery.getInt("mute_duration"), resultSetExecuteQuery.getInt("trade_locked_until"), resultSetExecuteQuery.getString("reason"));
                            if (!((ArrayList) this.sanctionHashmap.get(Integer.valueOf(resultSetExecuteQuery.getInt("habbo_id")))).contains(modToolSanctionItem)) {
                                ((ArrayList) this.sanctionHashmap.get(Integer.valueOf(resultSetExecuteQuery.getInt("habbo_id")))).add(modToolSanctionItem);
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
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                    tHashMap = this.sanctionHashmap;
                } catch (Throwable th3) {
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th4) {
                            th3.addSuppressed(th4);
                        }
                    }
                    throw th3;
                }
            } catch (Throwable th5) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Throwable th6) {
                        th5.addSuppressed(th6);
                    }
                }
                throw th5;
            }
        }
        return tHashMap;
    }

    private void insertSanction(int i, int i2, int i3, String str, int i4, boolean z, int i5) {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO sanctions (habbo_id, sanction_level, probation_timestamp, reason, trade_locked_until, is_muted, mute_duration) VALUES (?, ?, ?, ?, ?, ?, ?)");
                try {
                    preparedStatementPrepareStatement.setInt(1, i);
                    preparedStatementPrepareStatement.setInt(2, i2);
                    preparedStatementPrepareStatement.setInt(3, i3);
                    preparedStatementPrepareStatement.setString(4, str);
                    preparedStatementPrepareStatement.setInt(5, i4);
                    preparedStatementPrepareStatement.setBoolean(6, z);
                    preparedStatementPrepareStatement.setInt(7, i5);
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
    }

    public void updateSanction(int i, int i2) {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE sanctions SET probation_timestamp = ? WHERE id = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, i2);
                    preparedStatementPrepareStatement.setInt(2, i);
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
    }

    public void updateTradeLockedUntil(int i, int i2) {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE sanctions SET trade_locked_until = ? WHERE id = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, i2);
                    preparedStatementPrepareStatement.setInt(2, i);
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
    }

    public void updateMuteDuration(int i, int i2) {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE sanctions SET mute_duration = ? WHERE id = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, i2);
                    preparedStatementPrepareStatement.setInt(2, i);
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
    }

    public void run(int i, Habbo habbo, int i2, int i3, String str, int i4, boolean z, int i5) {
        int i6 = i2 + 1;
        ModToolSanctionLevelItem sanctionLevelItem = getSanctionLevelItem(i6);
        insertSanction(i, i6, buildProbationTimestamp(sanctionLevelItem), str, i4, z, i5);
        runSanctionBasedOnLevel(sanctionLevelItem, i, str, i3, habbo, i5);
        Emulator.getPluginManager().fireEvent(new SanctionEvent(habbo, Emulator.getGameEnvironment().getHabboManager().getHabbo(i), i6));
    }

    private int buildProbationTimestamp(ModToolSanctionLevelItem modToolSanctionLevelItem) {
        return Emulator.getIntUnixTimestamp() + (modToolSanctionLevelItem.sanctionProbationDays * 24 * 60 * 60);
    }

    public int getProbationDays(ModToolSanctionLevelItem modToolSanctionLevelItem) {
        return modToolSanctionLevelItem.sanctionProbationDays;
    }

    private void runSanctionBasedOnLevel(ModToolSanctionLevelItem modToolSanctionLevelItem, int i, String str, int i2, Habbo habbo, int i3) {
        Habbo habbo2;
        int intExact;
        habbo2 = Emulator.getGameEnvironment().getHabboManager().getHabbo(i);
        intExact = 0;
        if (i3 > 0) {
            intExact = Math.toIntExact((new Date(((long) i3) * 1000).getTime() - Emulator.getDate().getTime()) / 1000);
        }
        switch (modToolSanctionLevelItem.sanctionType) {
            case "ALERT":
                habbo2.alert(str);
                break;
            case "BAN":
                Emulator.getGameEnvironment().getModToolManager().ban(i, habbo, str, modToolSanctionLevelItem.sanctionHourLength, ModToolBanType.ACCOUNT, i2);
                break;
            case "MUTE":
                habbo2.mute(intExact == 0 ? 3600 : intExact, false);
                break;
        }
    }

    public String getSanctionType(ModToolSanctionLevelItem modToolSanctionLevelItem) {
        return modToolSanctionLevelItem.sanctionType;
    }

    public int getTimeOfSanction(ModToolSanctionLevelItem modToolSanctionLevelItem) {
        return modToolSanctionLevelItem.sanctionHourLength;
    }
}
