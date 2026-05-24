package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.rooms.items.rentablespaces.RentableSpaceInfoComposer;
import com.eu.habbo.threading.runnables.ClearRentedSpace;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.awt.Rectangle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionRentableSpace.class */
public class InteractionRentableSpace extends HabboItem {
    private static final Logger LOGGER = LoggerFactory.getLogger(InteractionRentableSpace.class);
    private int renterId;
    private String renterName;
    private int endTimestamp;

    public InteractionRentableSpace(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        String[] strArrSplit = resultSet.getString("extra_data").split(":");
        this.renterName = "Unknown";
        if (strArrSplit.length == 2) {
            this.renterId = Integer.valueOf(strArrSplit[0]).intValue();
            this.endTimestamp = Integer.valueOf(strArrSplit[1]).intValue();
            if (this.renterId > 0) {
                if (!isRented()) {
                    if (getRoomId() > 0) {
                        Emulator.getThreading().run(new ClearRentedSpace(this, Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId())));
                        this.renterId = 0;
                        return;
                    }
                    return;
                }
                Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(this.renterId);
                if (habbo != null) {
                    this.renterName = habbo.getHabboInfo().getUsername();
                    return;
                }
                try {
                    Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                    try {
                        PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT username FROM users WHERE id = ? LIMIT 1");
                        try {
                            preparedStatementPrepareStatement.setInt(1, this.renterId);
                            ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                            try {
                                if (resultSetExecuteQuery.next()) {
                                    this.renterName = resultSetExecuteQuery.getString("username");
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
        }
    }

    public InteractionRentableSpace(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.renterName = Emulator.PREVIEW;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        if (getExtradata().isEmpty()) {
            return false;
        }
        Habbo habbo = room.getHabbo(roomUnit);
        if (habbo == null || habbo.getHabboInfo().getId() == room.getId()) {
            return true;
        }
        return this.endTimestamp > Emulator.getIntUnixTimestamp() && this.renterId > 0 && this.renterId == habbo.getHabboInfo().getId();
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        sendRentWidget(gameClient.getHabbo());
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void serializeExtradata(ServerMessage serverMessage) {
        if (getExtradata().isEmpty()) {
            setExtradata("0:0");
        }
        serverMessage.appendInt(Integer.valueOf(1 + (isLimited() ? 256 : 0)));
        if (isRented()) {
            serverMessage.appendInt((Integer) 1);
            serverMessage.appendString("renterId");
            serverMessage.appendString(this.renterId + Emulator.PREVIEW);
        } else {
            serverMessage.appendInt((Integer) 0);
        }
        super.serializeExtradata(serverMessage);
    }

    public void rent(Habbo habbo) {
        if (!isRented() && !habbo.getHabboStats().isRentingSpace() && habbo.getHabboInfo().getCredits() >= rentCost() && habbo.getHabboStats().getClubExpireTimestamp() >= Emulator.getIntUnixTimestamp()) {
            setRenterId(habbo.getHabboInfo().getId());
            setRenterName(habbo.getHabboInfo().getUsername());
            setEndTimestamp(Emulator.getIntUnixTimestamp() + 604800);
            habbo.getHabboStats().setRentedItemId(getId());
            habbo.getHabboStats().setRentedTimeEnd(this.endTimestamp);
            needsUpdate(true);
            run();
        }
    }

    public void endRent() {
        setEndTimestamp(0);
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId());
        if (room == null) {
            return;
        }
        Rectangle rectangle = RoomLayout.getRectangle(getX(), getY(), getBaseItem().getWidth(), getBaseItem().getLength(), getRotation());
        THashSet tHashSet = new THashSet();
        for (int i = rectangle.x; i < ((double) rectangle.x) + rectangle.getWidth(); i++) {
            for (int i2 = rectangle.y; i2 < ((double) rectangle.y) + rectangle.getHeight(); i2++) {
                tHashSet.addAll(room.getItemsAt(i, i2, getZ()));
            }
        }
        TObjectHashIterator it = tHashSet.iterator();
        while (it.hasNext()) {
            HabboItem habboItem = (HabboItem) it.next();
            if (habboItem.getUserId() == this.renterId) {
                room.pickUpItem(habboItem, null);
            }
        }
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(this.renterId);
        if (habbo != null) {
            habbo.getHabboStats().setRentedItemId(0);
            habbo.getHabboStats().setRentedTimeEnd(0);
        } else {
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE users_settings SET rent_space_id = ?, rent_space_endtime = ? WHERE user_id = ? LIMIT 1");
                    try {
                        preparedStatementPrepareStatement.setInt(1, 0);
                        preparedStatementPrepareStatement.setInt(2, 0);
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
        setRenterId(0);
        setRenterName(Emulator.PREVIEW);
        needsUpdate(true);
        run();
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public String getExtradata() {
        return this.renterId + ":" + this.endTimestamp;
    }

    public int getRenterId() {
        return this.renterId;
    }

    public void setRenterId(int i) {
        this.renterId = i;
    }

    public String getRenterName() {
        return this.renterName;
    }

    public void setRenterName(String str) {
        this.renterName = str;
    }

    public int getEndTimestamp() {
        return this.endTimestamp;
    }

    public void setEndTimestamp(int i) {
        this.endTimestamp = i;
    }

    public boolean isRented() {
        return this.endTimestamp > Emulator.getIntUnixTimestamp();
    }

    public int rentCost() {
        String[] strArrSplit = getBaseItem().getName().replace("hblooza_spacerent", Emulator.PREVIEW).split("x");
        if (strArrSplit.length == 2) {
            return 10 * Integer.valueOf(strArrSplit[0]).intValue() * Integer.valueOf(strArrSplit[1]).intValue();
        }
        return 1337;
    }

    public int getRentErrorCode(Habbo habbo) {
        if (isRented() && this.renterId != habbo.getHabboInfo().getId()) {
            return 100;
        }
        if (habbo.getHabboStats().isRentingSpace() && habbo.getHabboStats().getRentedItemId() != getId()) {
            return RentableSpaceInfoComposer.CAN_RENT_ONLY_ONE_SPACE;
        }
        if (habbo.getHabboStats().getClubExpireTimestamp() < Emulator.getIntUnixTimestamp()) {
            return RentableSpaceInfoComposer.CANT_RENT_NO_HABBO_CLUB;
        }
        if (rentCost() > habbo.getHabboInfo().getCredits()) {
            return RentableSpaceInfoComposer.NOT_ENOUGH_CREDITS;
        }
        return 0;
    }

    public void sendRentWidget(Habbo habbo) {
        habbo.getClient().sendResponse(new RentableSpaceInfoComposer(habbo, this, getRentErrorCode(habbo)));
    }
}
