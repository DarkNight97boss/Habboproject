package com.eu.habbo.habbohotel.users;

import com.eu.habbo.Emulator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/users/HabboBadge.class */
public class HabboBadge implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(HabboBadge.class);
    private int id;
    private String code;
    private int slot;
    private Habbo habbo;
    private boolean needsUpdate = false;
    private boolean needsInsert = false;

    public HabboBadge(ResultSet resultSet, Habbo habbo) throws SQLException {
        this.id = resultSet.getInt("id");
        this.code = resultSet.getString("badge_code");
        this.slot = resultSet.getInt("slot_id");
        this.habbo = habbo;
    }

    public HabboBadge(int i, String str, int i2, Habbo habbo) {
        this.id = i;
        this.code = str;
        this.slot = i2;
        this.habbo = habbo;
    }

    public int getId() {
        return this.id;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String str) {
        this.code = str;
    }

    public int getSlot() {
        return this.slot;
    }

    public void setSlot(int i) {
        this.slot = i;
    }

    @Override // java.lang.Runnable
    public void run() {
        Connection connection;
        try {
            if (this.needsInsert) {
                connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO users_badges (user_id, slot_id, badge_code) VALUES (?, ?, ?)", 1);
                    try {
                        preparedStatementPrepareStatement.setInt(1, this.habbo.getHabboInfo().getId());
                        preparedStatementPrepareStatement.setInt(2, this.slot);
                        preparedStatementPrepareStatement.setString(3, this.code);
                        preparedStatementPrepareStatement.execute();
                        ResultSet generatedKeys = preparedStatementPrepareStatement.getGeneratedKeys();
                        try {
                            if (generatedKeys.next()) {
                                this.id = generatedKeys.getInt(1);
                            }
                            if (generatedKeys != null) {
                                generatedKeys.close();
                            }
                            if (preparedStatementPrepareStatement != null) {
                                preparedStatementPrepareStatement.close();
                            }
                            if (connection != null) {
                                connection.close();
                            }
                            this.needsInsert = false;
                        } catch (Throwable th) {
                            if (generatedKeys != null) {
                                try {
                                    generatedKeys.close();
                                } catch (Throwable th2) {
                                    th.addSuppressed(th2);
                                }
                            }
                            throw th;
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
            } else if (this.needsUpdate) {
                connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement2 = connection.prepareStatement("UPDATE users_badges SET slot_id = ?, badge_code = ? WHERE id = ? AND user_id = ?");
                    try {
                        preparedStatementPrepareStatement2.setInt(1, this.slot);
                        preparedStatementPrepareStatement2.setString(2, this.code);
                        preparedStatementPrepareStatement2.setInt(3, this.id);
                        preparedStatementPrepareStatement2.setInt(4, this.habbo.getHabboInfo().getId());
                        preparedStatementPrepareStatement2.execute();
                        if (preparedStatementPrepareStatement2 != null) {
                            preparedStatementPrepareStatement2.close();
                        }
                        if (connection != null) {
                            connection.close();
                        }
                        this.needsUpdate = false;
                    } catch (Throwable th5) {
                        if (preparedStatementPrepareStatement2 != null) {
                            try {
                                preparedStatementPrepareStatement2.close();
                            } catch (Throwable th6) {
                                th5.addSuppressed(th6);
                            }
                        }
                        throw th5;
                    }
                } finally {
                    if (connection != null) {
                        try {
                            connection.close();
                        } catch (Throwable th7) {
                            th.addSuppressed(th7);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }

    public void needsUpdate(boolean z) {
        this.needsUpdate = z;
    }

    public void needsInsert(boolean z) {
        this.needsInsert = z;
    }
}
