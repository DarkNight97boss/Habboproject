package com.eu.habbo.habbohotel.rooms;

import com.eu.habbo.Emulator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/rooms/RoomPromotion.class */
public class RoomPromotion {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoomPromotion.class);
    private final Room room;
    public boolean needsUpdate;
    private String title;
    private String description;
    private int endTimestamp;
    private int startTimestamp;
    private int category;

    public RoomPromotion(Room room, String str, String str2, int i, int i2, int i3) {
        this.room = room;
        this.title = str;
        this.description = str2;
        this.endTimestamp = i;
        this.startTimestamp = i2;
        this.category = i3;
    }

    public RoomPromotion(Room room, ResultSet resultSet) throws SQLException {
        this.room = room;
        this.title = resultSet.getString("title");
        this.description = resultSet.getString("description");
        this.endTimestamp = resultSet.getInt("end_timestamp");
        this.startTimestamp = resultSet.getInt("start_timestamp");
        this.category = resultSet.getInt("category");
    }

    public void save() {
        Connection connection;
        if (this.needsUpdate) {
            try {
                connection = Emulator.getDatabase().getDataSource().getConnection();
            } catch (SQLException e) {
                LOGGER.error("Caught SQL exception", e);
            }
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE room_promotions SET title = ?, description = ?, category = ? WHERE room_id = ?");
                try {
                    preparedStatementPrepareStatement.setString(1, this.title);
                    preparedStatementPrepareStatement.setString(2, this.description);
                    preparedStatementPrepareStatement.setInt(3, this.category);
                    preparedStatementPrepareStatement.setInt(4, this.room.getId());
                    preparedStatementPrepareStatement.executeUpdate();
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                    this.needsUpdate = false;
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
        }
    }

    public Room getRoom() {
        return this.room;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String str) {
        this.title = str;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String str) {
        this.description = str;
    }

    public int getEndTimestamp() {
        return this.endTimestamp;
    }

    public void setEndTimestamp(int i) {
        this.endTimestamp = i;
    }

    public void addEndTimestamp(int i) {
        this.endTimestamp += i;
    }

    public int getStartTimestamp() {
        return this.startTimestamp;
    }

    public void setStartTimestamp(int i) {
        this.startTimestamp = i;
    }

    public int getCategory() {
        return this.category;
    }

    public void setCategory(int i) {
        this.category = i;
    }
}
