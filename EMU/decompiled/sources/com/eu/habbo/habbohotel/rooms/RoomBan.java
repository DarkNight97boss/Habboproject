package com.eu.habbo.habbohotel.rooms;

import com.eu.habbo.Emulator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/rooms/RoomBan.class */
public class RoomBan {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoomBan.class);
    public final int roomId;
    public final int userId;
    public final String username;
    public final int endTimestamp;

    public RoomBan(int i, int i2, String str, int i3) {
        this.roomId = i;
        this.userId = i2;
        this.username = str;
        this.endTimestamp = i3;
    }

    public RoomBan(ResultSet resultSet) throws SQLException {
        this.roomId = resultSet.getInt("room_id");
        this.userId = resultSet.getInt("user_id");
        this.username = resultSet.getString("username");
        this.endTimestamp = resultSet.getInt("ends");
    }

    public void insert() {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO room_bans (room_id, user_id, ends) VALUES (?, ?, ?)");
                try {
                    preparedStatementPrepareStatement.setInt(1, this.roomId);
                    preparedStatementPrepareStatement.setInt(2, this.userId);
                    preparedStatementPrepareStatement.setInt(3, this.endTimestamp);
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

    public void delete() {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("DELETE FROM room_bans WHERE room_id = ? AND user_id = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, this.roomId);
                    preparedStatementPrepareStatement.setInt(2, this.userId);
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
}
