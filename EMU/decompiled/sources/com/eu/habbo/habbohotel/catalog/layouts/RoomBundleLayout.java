package com.eu.habbo.habbohotel.catalog.layouts;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.catalog.CatalogItem;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomManager;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;
import com.eu.habbo.messages.outgoing.navigator.CanCreateRoomComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.procedure.TObjectProcedure;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/catalog/layouts/RoomBundleLayout.class */
public class RoomBundleLayout extends SingleBundle {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoomBundleLayout.class);
    public int roomId;
    public Room room;
    private int lastUpdate;
    private boolean loaded;

    public RoomBundleLayout(ResultSet resultSet) throws SQLException {
        super(resultSet);
        this.lastUpdate = 0;
        this.loaded = false;
        this.roomId = resultSet.getInt("room_id");
    }

    @Override // com.eu.habbo.habbohotel.catalog.CatalogPage
    public TIntObjectMap<CatalogItem> getCatalogItems() {
        if (Emulator.getIntUnixTimestamp() - this.lastUpdate < 120) {
            this.lastUpdate = Emulator.getIntUnixTimestamp();
            return super.getCatalogItems();
        }
        if (this.room == null) {
            if (this.roomId > 0) {
                this.room = Emulator.getGameEnvironment().getRoomManager().loadRoom(this.roomId);
                if (this.room != null) {
                    this.room.preventUnloading = true;
                }
            } else {
                LOGGER.error("No room id specified for room bundle " + getPageName() + "(" + getId() + ")");
            }
        }
        if (this.room == null) {
            return super.getCatalogItems();
        }
        final CatalogItem[] catalogItemArr = {null};
        super.getCatalogItems().forEachValue(new TObjectProcedure<CatalogItem>() { // from class: com.eu.habbo.habbohotel.catalog.layouts.RoomBundleLayout.1
            public boolean execute(CatalogItem catalogItem) {
                if (catalogItem == null) {
                    return true;
                }
                catalogItemArr[0] = catalogItem;
                return false;
            }
        });
        if (this.room.isPreLoaded()) {
            this.room.loadData();
            this.room.preventUncaching = true;
            this.room.preventUnloading = true;
        }
        if (catalogItemArr[0] != null) {
            catalogItemArr[0].getBundle().clear();
            THashMap tHashMap = new THashMap();
            TObjectHashIterator it = this.room.getFloorItems().iterator();
            while (it.hasNext()) {
                HabboItem habboItem = (HabboItem) it.next();
                if (!tHashMap.contains(habboItem.getBaseItem())) {
                    tHashMap.put(habboItem.getBaseItem(), 0);
                }
                tHashMap.put(habboItem.getBaseItem(), Integer.valueOf(((Integer) tHashMap.get(habboItem.getBaseItem())).intValue() + 1));
            }
            TObjectHashIterator it2 = this.room.getWallItems().iterator();
            while (it2.hasNext()) {
                HabboItem habboItem2 = (HabboItem) it2.next();
                if (!tHashMap.contains(habboItem2.getBaseItem())) {
                    tHashMap.put(habboItem2.getBaseItem(), 0);
                }
                tHashMap.put(habboItem2.getBaseItem(), Integer.valueOf(((Integer) tHashMap.get(habboItem2.getBaseItem())).intValue() + 1));
            }
            if (!catalogItemArr[0].getExtradata().isEmpty()) {
                tHashMap.put(Emulator.getGameEnvironment().getItemManager().getItem(Integer.valueOf(catalogItemArr[0].getExtradata()).intValue()), 1);
            }
            StringBuilder sb = new StringBuilder();
            for (Map.Entry entry : tHashMap.entrySet()) {
                sb.append(((Item) entry.getKey()).getId()).append(":").append(entry.getValue()).append(";");
            }
            catalogItemArr[0].setItemId(sb.toString());
            catalogItemArr[0].loadBundle();
        }
        return super.getCatalogItems();
    }

    public void loadItems(Room room) {
        if (this.room != null) {
            this.room.preventUnloading = false;
        }
        this.room = room;
        this.room.preventUnloading = true;
        getCatalogItems();
        this.loaded = true;
    }

    public void buyRoom(Habbo habbo) {
        buyRoom(habbo, habbo.getHabboInfo().getId(), habbo.getHabboInfo().getUsername());
    }

    public void buyRoom(Habbo habbo, int i, String str) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        PreparedStatement preparedStatementPrepareStatement2;
        if (!this.loaded) {
            loadItems(Emulator.getGameEnvironment().getRoomManager().loadRoom(this.roomId));
        }
        if (habbo != null) {
            int size = Emulator.getGameEnvironment().getRoomManager().getRoomsForHabbo(habbo).size();
            int i2 = habbo.getHabboStats().hasActiveClub() ? RoomManager.MAXIMUM_ROOMS_HC : RoomManager.MAXIMUM_ROOMS_USER;
            if (size >= i2) {
                habbo.getClient().sendResponse(new CanCreateRoomComposer(size, i2));
                return;
            }
        }
        if (this.room == null) {
            return;
        }
        this.room.save();
        TObjectHashIterator it = this.room.getFloorItems().iterator();
        while (it.hasNext()) {
            ((HabboItem) it.next()).run();
        }
        TObjectHashIterator it2 = this.room.getWallItems().iterator();
        while (it2.hasNext()) {
            ((HabboItem) it2.next()).run();
        }
        getCatalogItems();
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO rooms (owner_id, owner_name, name, description, model, password, state, users_max, category, paper_floor, paper_wall, paper_landscape, thickness_wall, thickness_floor, moodlight_data, override_model)  (SELECT ?, ?, name, description, model, password, state, users_max, category, paper_floor, paper_wall, paper_landscape, thickness_wall, thickness_floor, moodlight_data, override_model FROM rooms WHERE id = ?)", 1);
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setInt(1, i);
            preparedStatementPrepareStatement.setString(2, str);
            preparedStatementPrepareStatement.setInt(3, this.room.getId());
            preparedStatementPrepareStatement.execute();
            ResultSet generatedKeys = preparedStatementPrepareStatement.getGeneratedKeys();
            try {
                i = generatedKeys.next() ? generatedKeys.getInt(1) : 0;
                if (generatedKeys != null) {
                    generatedKeys.close();
                }
                if (preparedStatementPrepareStatement != null) {
                    preparedStatementPrepareStatement.close();
                }
                if (i == 0) {
                    if (connection != null) {
                        connection.close();
                        return;
                    }
                    return;
                }
                PreparedStatement preparedStatementPrepareStatement3 = connection.prepareStatement("INSERT INTO items (user_id, room_id, item_id, wall_pos, x, y, z, rot, extra_data, wired_data, limited_data, guild_id) (SELECT ?, ?, item_id, wall_pos, x, y, z, rot, extra_data, wired_data, ?, ? FROM items WHERE room_id = ?)", 1);
                try {
                    preparedStatementPrepareStatement3.setInt(1, i);
                    preparedStatementPrepareStatement3.setInt(2, i);
                    preparedStatementPrepareStatement3.setString(3, "0:0");
                    preparedStatementPrepareStatement3.setInt(4, 0);
                    preparedStatementPrepareStatement3.setInt(5, this.room.getId());
                    preparedStatementPrepareStatement3.execute();
                    if (preparedStatementPrepareStatement3 != null) {
                        preparedStatementPrepareStatement3.close();
                    }
                    if (this.room.hasCustomLayout()) {
                        try {
                            preparedStatementPrepareStatement2 = connection.prepareStatement("INSERT INTO room_models_custom (id, name, door_x, door_y, door_dir, heightmap) (SELECT ?, ?, door_x, door_y, door_dir, heightmap FROM room_models_custom WHERE id = ? LIMIT 1)", 1);
                            try {
                                preparedStatementPrepareStatement2.setInt(1, i);
                                preparedStatementPrepareStatement2.setString(2, "custom_" + i);
                                preparedStatementPrepareStatement2.setInt(3, this.room.getId());
                                preparedStatementPrepareStatement2.execute();
                                if (preparedStatementPrepareStatement2 != null) {
                                    preparedStatementPrepareStatement2.close();
                                }
                            } finally {
                            }
                        } catch (SQLException e2) {
                            LOGGER.error("Caught SQL exception", e2);
                        }
                    }
                    if (Emulator.getConfig().getBoolean("bundle.bots.enabled")) {
                        preparedStatementPrepareStatement2 = connection.prepareStatement("INSERT INTO bots (user_id, room_id, name, motto, figure, gender, x, y, z, chat_lines, chat_auto, chat_random, chat_delay, dance, type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", 1);
                        try {
                            synchronized (this.room.getCurrentBots()) {
                                preparedStatementPrepareStatement2.setInt(1, i);
                                preparedStatementPrepareStatement2.setInt(2, i);
                                for (Bot bot : this.room.getCurrentBots().valueCollection()) {
                                    preparedStatementPrepareStatement2.setString(3, bot.getName());
                                    preparedStatementPrepareStatement2.setString(4, bot.getMotto());
                                    preparedStatementPrepareStatement2.setString(5, bot.getFigure());
                                    preparedStatementPrepareStatement2.setString(6, bot.getGender().name());
                                    preparedStatementPrepareStatement2.setInt(7, bot.getRoomUnit().getX());
                                    preparedStatementPrepareStatement2.setInt(8, bot.getRoomUnit().getY());
                                    preparedStatementPrepareStatement2.setDouble(9, bot.getRoomUnit().getZ());
                                    StringBuilder sb = new StringBuilder();
                                    Iterator<String> it3 = bot.getChatLines().iterator();
                                    while (it3.hasNext()) {
                                        sb.append(it3.next()).append("\r");
                                    }
                                    preparedStatementPrepareStatement2.setString(10, sb.toString());
                                    preparedStatementPrepareStatement2.setString(11, bot.isChatAuto() ? "1" : "0");
                                    preparedStatementPrepareStatement2.setString(12, bot.isChatRandom() ? "1" : "0");
                                    preparedStatementPrepareStatement2.setInt(13, bot.getChatDelay());
                                    preparedStatementPrepareStatement2.setInt(14, bot.getRoomUnit().getDanceType().getType());
                                    preparedStatementPrepareStatement2.setString(15, bot.getType());
                                    preparedStatementPrepareStatement2.addBatch();
                                }
                            }
                            preparedStatementPrepareStatement2.executeBatch();
                            if (preparedStatementPrepareStatement2 != null) {
                                preparedStatementPrepareStatement2.close();
                            }
                        } finally {
                        }
                    }
                    if (connection != null) {
                        connection.close();
                    }
                    Room roomLoadRoom = Emulator.getGameEnvironment().getRoomManager().loadRoom(i);
                    roomLoadRoom.setWallHeight(this.room.getWallHeight());
                    roomLoadRoom.setFloorSize(this.room.getFloorSize());
                    roomLoadRoom.setWallPaint(this.room.getWallPaint());
                    roomLoadRoom.setFloorPaint(this.room.getFloorPaint());
                    roomLoadRoom.setScore(0);
                    roomLoadRoom.setNeedsUpdate(true);
                    THashMap tHashMap = new THashMap();
                    tHashMap.put("ROOMNAME", roomLoadRoom.getName());
                    tHashMap.put("ROOMID", roomLoadRoom.getId() + Emulator.PREVIEW);
                    tHashMap.put("OWNER", roomLoadRoom.getOwnerName());
                    tHashMap.put("image", "${image.library.url}/notifications/room_bundle_" + getId() + ".png");
                    if (habbo != null) {
                        habbo.getClient().sendResponse(new BubbleAlertComposer(BubbleAlertKeys.PURCHASING_ROOM.key, (THashMap<String, String>) tHashMap));
                    }
                } finally {
                    if (preparedStatementPrepareStatement3 != null) {
                        try {
                            preparedStatementPrepareStatement3.close();
                        } catch (Throwable th) {
                            th.addSuppressed(th);
                        }
                    }
                }
            } catch (Throwable th2) {
                if (generatedKeys != null) {
                    try {
                        generatedKeys.close();
                    } catch (Throwable th3) {
                        th2.addSuppressed(th3);
                    }
                }
                throw th2;
            }
        } finally {
            if (preparedStatementPrepareStatement != null) {
                try {
                    preparedStatementPrepareStatement.close();
                } catch (Throwable th4) {
                    th.addSuppressed(th4);
                }
            }
        }
    }
}
