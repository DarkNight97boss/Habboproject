package com.eu.habbo.threading.runnables.hopper;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/hopper/HopperActionTwo.class */
class HopperActionTwo implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(HopperActionTwo.class);
    private final HabboItem teleportOne;
    private final Room room;
    private final GameClient client;

    public HopperActionTwo(HabboItem habboItem, Room room, GameClient gameClient) {
        this.teleportOne = habboItem;
        this.room = room;
        this.client = gameClient;
    }

    @Override // java.lang.Runnable
    public void run() {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        this.teleportOne.setExtradata("2");
        int i = 0;
        int i2 = 0;
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("SELECT items.id, items.room_id FROM items_hoppers INNER JOIN items ON items_hoppers.item_id = items.id WHERE base_item = ? AND items.id != ? AND room_id > 0 ORDER BY RAND() LIMIT 1");
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setInt(1, this.teleportOne.getBaseItem().getId());
            preparedStatementPrepareStatement.setInt(2, this.teleportOne.getId());
            ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
            try {
                if (resultSetExecuteQuery.next()) {
                    i2 = resultSetExecuteQuery.getInt("id");
                    i = resultSetExecuteQuery.getInt("room_id");
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
                if (i == 0 || i2 == 0) {
                    this.teleportOne.setExtradata("0");
                    this.client.getHabbo().getRoomUnit().setCanWalk(true);
                    this.client.getHabbo().getRoomUnit().isTeleporting = false;
                    Emulator.getThreading().run(new HopperActionFour(this.teleportOne, this.room, this.client), 500L);
                } else {
                    Emulator.getThreading().run(new HopperActionThree(this.teleportOne, this.room, this.client, i, i2), 500L);
                }
                this.room.updateItem(this.teleportOne);
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
    }
}
