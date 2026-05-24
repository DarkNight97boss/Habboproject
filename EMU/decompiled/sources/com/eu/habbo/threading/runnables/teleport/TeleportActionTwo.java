package com.eu.habbo.threading.runnables.teleport;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.interactions.InteractionTeleport;
import com.eu.habbo.habbohotel.items.interactions.InteractionTeleportTile;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.Outgoing;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserStatusComposer;
import com.eu.habbo.threading.runnables.HabboItemNewState;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/teleport/TeleportActionTwo.class */
class TeleportActionTwo implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(TeleportActionTwo.class);
    private final HabboItem currentTeleport;
    private final Room room;
    private final GameClient client;

    public TeleportActionTwo(HabboItem habboItem, Room room, GameClient gameClient) {
        this.currentTeleport = habboItem;
        this.client = gameClient;
        this.room = room;
    }

    @Override // java.lang.Runnable
    public void run() {
        HabboItem habboItem;
        int i = 500;
        if (this.currentTeleport instanceof InteractionTeleportTile) {
            i = 0;
        }
        if (this.client.getHabbo().getHabboInfo().getCurrentRoom() != this.room) {
            return;
        }
        this.client.getHabbo().getRoomUnit().removeStatus(RoomUnitStatus.MOVE);
        this.room.sendComposer(new RoomUserStatusComposer(this.client.getHabbo().getRoomUnit()).compose());
        if (((InteractionTeleport) this.currentTeleport).getTargetRoomId() <= 0 || ((InteractionTeleport) this.currentTeleport).getTargetId() <= 0 || (habboItem = this.room.getHabboItem(((InteractionTeleport) this.currentTeleport).getTargetId())) == null) {
            ((InteractionTeleport) this.currentTeleport).setTargetRoomId(0);
            ((InteractionTeleport) this.currentTeleport).setTargetId(0);
        } else if (((InteractionTeleport) habboItem).getTargetRoomId() != ((InteractionTeleport) this.currentTeleport).getTargetRoomId()) {
            ((InteractionTeleport) this.currentTeleport).setTargetId(0);
            ((InteractionTeleport) this.currentTeleport).setTargetRoomId(0);
            ((InteractionTeleport) habboItem).setTargetId(0);
            ((InteractionTeleport) habboItem).setTargetRoomId(0);
        }
        if (((InteractionTeleport) this.currentTeleport).getTargetId() == 0) {
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT items_teleports.*, A.room_id as a_room_id, A.id as a_id, B.room_id as b_room_id, B.id as b_id FROM items_teleports INNER JOIN items AS A ON items_teleports.teleport_one_id = A.id INNER JOIN items AS B ON items_teleports.teleport_two_id = B.id  WHERE (teleport_one_id = ? OR teleport_two_id = ?)");
                    try {
                        preparedStatementPrepareStatement.setInt(1, this.currentTeleport.getId());
                        preparedStatementPrepareStatement.setInt(2, this.currentTeleport.getId());
                        ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                        try {
                            if (resultSetExecuteQuery.next()) {
                                if (resultSetExecuteQuery.getInt("a_id") != this.currentTeleport.getId()) {
                                    ((InteractionTeleport) this.currentTeleport).setTargetId(resultSetExecuteQuery.getInt("a_id"));
                                    ((InteractionTeleport) this.currentTeleport).setTargetRoomId(resultSetExecuteQuery.getInt("a_room_id"));
                                } else {
                                    ((InteractionTeleport) this.currentTeleport).setTargetId(resultSetExecuteQuery.getInt("b_id"));
                                    ((InteractionTeleport) this.currentTeleport).setTargetRoomId(resultSetExecuteQuery.getInt("b_room_id"));
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
                } finally {
                }
            } catch (SQLException e) {
                LOGGER.error("Caught SQL exception", e);
            }
        }
        this.currentTeleport.setExtradata("0");
        this.room.updateItem(this.currentTeleport);
        if (((InteractionTeleport) this.currentTeleport).getTargetRoomId() == 0) {
            Emulator.getThreading().run(new TeleportActionFive(this.currentTeleport, this.room, this.client), 0L);
            return;
        }
        Emulator.getThreading().run(new HabboItemNewState(this.currentTeleport, this.room, "2"), i);
        Emulator.getThreading().run(new HabboItemNewState(this.currentTeleport, this.room, "0"), i + Outgoing.CraftableProductsComposer);
        Emulator.getThreading().run(new TeleportActionThree(this.currentTeleport, this.room, this.client), i);
    }
}
