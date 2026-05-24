package com.eu.habbo.habbohotel.users.inventory;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInventory;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.plugin.events.inventory.InventoryItemAddedEvent;
import com.eu.habbo.plugin.events.inventory.InventoryItemRemovedEvent;
import com.eu.habbo.plugin.events.inventory.InventoryItemsAddedEvent;
import gnu.trove.TCollections;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.hash.THashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/users/inventory/ItemsComponent.class */
public class ItemsComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemsComponent.class);
    private final TIntObjectMap<HabboItem> items = TCollections.synchronizedMap(new TIntObjectHashMap());
    private final HabboInventory inventory;

    public ItemsComponent(HabboInventory habboInventory, Habbo habbo) {
        this.inventory = habboInventory;
        this.items.putAll(loadItems(habbo));
    }

    public static THashMap<Integer, HabboItem> loadItems(Habbo habbo) {
        Connection connection;
        THashMap<Integer, HabboItem> tHashMap = new THashMap<>();
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM items WHERE room_id = ? AND user_id = ?");
            try {
                preparedStatementPrepareStatement.setInt(1, 0);
                preparedStatementPrepareStatement.setInt(2, habbo.getHabboInfo().getId());
                ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                while (resultSetExecuteQuery.next()) {
                    try {
                        try {
                            HabboItem habboItemLoadHabboItem = Emulator.getGameEnvironment().getItemManager().loadHabboItem(resultSetExecuteQuery);
                            if (habboItemLoadHabboItem != null) {
                                tHashMap.put(Integer.valueOf(resultSetExecuteQuery.getInt("id")), habboItemLoadHabboItem);
                            } else {
                                LOGGER.error("Failed to load HabboItem: " + resultSetExecuteQuery.getInt("id"));
                            }
                        } catch (SQLException e2) {
                            LOGGER.error("Caught SQL exception", e2);
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
                return tHashMap;
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
    }

    public void addItem(HabboItem habboItem) {
        if (habboItem == null) {
            return;
        }
        InventoryItemAddedEvent inventoryItemAddedEvent = new InventoryItemAddedEvent(this.inventory, habboItem);
        if (((InventoryItemAddedEvent) Emulator.getPluginManager().fireEvent(inventoryItemAddedEvent)).isCancelled()) {
            return;
        }
        synchronized (this.items) {
            this.items.put(inventoryItemAddedEvent.item.getId(), inventoryItemAddedEvent.item);
        }
    }

    public void addItems(THashSet<HabboItem> tHashSet) {
        InventoryItemsAddedEvent inventoryItemsAddedEvent = new InventoryItemsAddedEvent(this.inventory, tHashSet);
        if (((InventoryItemsAddedEvent) Emulator.getPluginManager().fireEvent(inventoryItemsAddedEvent)).isCancelled()) {
            return;
        }
        synchronized (this.items) {
            TObjectHashIterator it = inventoryItemsAddedEvent.items.iterator();
            while (it.hasNext()) {
                HabboItem habboItem = (HabboItem) it.next();
                if (habboItem != null) {
                    this.items.put(habboItem.getId(), habboItem);
                }
            }
        }
    }

    public HabboItem getHabboItem(int i) {
        return (HabboItem) this.items.get(Math.abs(i));
    }

    public HabboItem getAndRemoveHabboItem(final Item item) {
        final HabboItem[] habboItemArr = {null};
        synchronized (this.items) {
            this.items.forEachValue(new TObjectProcedure<HabboItem>() { // from class: com.eu.habbo.habbohotel.users.inventory.ItemsComponent.1
                public boolean execute(HabboItem habboItem) {
                    if (habboItem.getBaseItem() != item) {
                        return true;
                    }
                    habboItemArr[0] = habboItem;
                    return false;
                }
            });
        }
        removeHabboItem(habboItemArr[0]);
        return habboItemArr[0];
    }

    public void removeHabboItem(int i) {
        this.items.remove(i);
    }

    public void removeHabboItem(HabboItem habboItem) {
        InventoryItemRemovedEvent inventoryItemRemovedEvent = new InventoryItemRemovedEvent(this.inventory, habboItem);
        if (((InventoryItemRemovedEvent) Emulator.getPluginManager().fireEvent(inventoryItemRemovedEvent)).isCancelled()) {
            return;
        }
        synchronized (this.items) {
            this.items.remove(inventoryItemRemovedEvent.item.getId());
        }
    }

    public TIntObjectMap<HabboItem> getItems() {
        return this.items;
    }

    public THashSet<HabboItem> getItemsAsValueCollection() {
        THashSet<HabboItem> tHashSet = new THashSet<>();
        tHashSet.addAll(this.items.valueCollection());
        return tHashSet;
    }

    public int itemCount() {
        return this.items.size();
    }

    public void dispose() {
        synchronized (this.items) {
            TIntObjectIterator it = this.items.iterator();
            if (it == null) {
                LOGGER.error("Items is NULL!");
                return;
            }
            if (!this.items.isEmpty()) {
                int size = this.items.size();
                while (true) {
                    int i = size;
                    size--;
                    if (i <= 0) {
                        break;
                    }
                    try {
                        it.advance();
                        if (((HabboItem) it.value()).needsUpdate()) {
                            Emulator.getThreading().run((Runnable) it.value());
                        }
                    } catch (NoSuchElementException e) {
                    }
                }
            }
            this.items.clear();
        }
    }
}
