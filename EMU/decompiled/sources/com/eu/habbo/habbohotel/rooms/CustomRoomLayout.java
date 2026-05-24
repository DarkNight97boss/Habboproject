package com.eu.habbo.habbohotel.rooms;

import com.eu.habbo.Emulator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/rooms/CustomRoomLayout.class */
public class CustomRoomLayout extends RoomLayout implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomRoomLayout.class);
    private final int roomId;
    private boolean needsUpdate;

    public CustomRoomLayout(ResultSet resultSet, Room room) throws SQLException {
        super(resultSet, room);
        this.roomId = room.getId();
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.needsUpdate) {
            this.needsUpdate = false;
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE room_models_custom SET door_x = ?, door_y = ?, door_dir = ?, heightmap = ? WHERE id = ? LIMIT 1");
                    try {
                        preparedStatementPrepareStatement.setInt(1, getDoorX());
                        preparedStatementPrepareStatement.setInt(2, getDoorY());
                        preparedStatementPrepareStatement.setInt(3, getDoorDirection());
                        preparedStatementPrepareStatement.setString(4, getHeightmap());
                        preparedStatementPrepareStatement.setInt(5, this.roomId);
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

    public boolean needsUpdate() {
        return this.needsUpdate;
    }

    public void needsUpdate(boolean z) {
        this.needsUpdate = z;
    }
}
