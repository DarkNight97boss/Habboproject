package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.messages.ClientMessage;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.rooms.items.ItemStateComposer;
import gnu.trove.map.hash.TLongLongHashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionWired.class */
public abstract class InteractionWired extends InteractionDefault {
    private static final Logger LOGGER = LoggerFactory.getLogger(InteractionWired.class);
    private long cooldown;
    private TLongLongHashMap userExecutionCache;

    InteractionWired(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.userExecutionCache = new TLongLongHashMap(3);
        setExtradata("0");
    }

    InteractionWired(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.userExecutionCache = new TLongLongHashMap(3);
        setExtradata("0");
    }

    public abstract boolean execute(RoomUnit roomUnit, Room room, Object[] objArr);

    public abstract String getWiredData();

    public abstract void serializeWiredData(ServerMessage serverMessage, Room room);

    public abstract void loadWiredData(ResultSet resultSet, Room room) throws SQLException;

    @Override // com.eu.habbo.habbohotel.users.HabboItem, java.lang.Runnable
    public void run() {
        if (needsUpdate()) {
            String wiredData = getWiredData();
            if (wiredData == null) {
                wiredData = Emulator.PREVIEW;
            }
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE items SET wired_data = ? WHERE id = ?");
                    try {
                        if (getRoomId() != 0) {
                            preparedStatementPrepareStatement.setString(1, wiredData);
                        } else {
                            preparedStatementPrepareStatement.setString(1, Emulator.PREVIEW);
                        }
                        preparedStatementPrepareStatement.setInt(2, getId());
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
        super.run();
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPickUp(Room room) {
        onPickUp();
    }

    public abstract void onPickUp();

    public void activateBox(Room room) {
        activateBox(room, (RoomUnit) null, 0L);
    }

    public void activateBox(Room room, RoomUnit roomUnit, long j) {
        setExtradata(getExtradata().equals("1") ? "0" : "1");
        room.sendComposer(new ItemStateComposer(this).compose());
        if (roomUnit != null) {
            addUserExecutionCache(roomUnit.getId(), j);
        }
    }

    protected long requiredCooldown() {
        return 50L;
    }

    public boolean canExecute(long j) {
        return j - this.cooldown >= requiredCooldown();
    }

    public void setCooldown(long j) {
        this.cooldown = j;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem
    public boolean allowWiredResetState() {
        return false;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isUsable() {
        return true;
    }

    public boolean userCanExecute(int i, long j) {
        return i == -1 || !this.userExecutionCache.containsKey((long) i) || j - this.userExecutionCache.get((long) i) >= 100;
    }

    public void clearUserExecutionCache() {
        this.userExecutionCache.clear();
    }

    public void addUserExecutionCache(int i, long j) {
        this.userExecutionCache.put(i, j);
    }

    public static WiredSettings readSettings(ClientMessage clientMessage, boolean z) {
        int iIntValue = clientMessage.readInt().intValue();
        int[] iArr = new int[iIntValue];
        for (int i = 0; i < iIntValue; i++) {
            iArr[i] = clientMessage.readInt().intValue();
        }
        String string = clientMessage.readString();
        int iIntValue2 = clientMessage.readInt().intValue();
        int[] iArr2 = new int[iIntValue2];
        for (int i2 = 0; i2 < iIntValue2; i2++) {
            iArr2[i2] = clientMessage.readInt().intValue();
        }
        WiredSettings wiredSettings = new WiredSettings(iArr, string, iArr2, -1);
        if (z) {
            wiredSettings.setDelay(clientMessage.readInt().intValue());
        }
        wiredSettings.setStuffTypeSelectionCode(clientMessage.readInt().intValue());
        return wiredSettings;
    }
}
