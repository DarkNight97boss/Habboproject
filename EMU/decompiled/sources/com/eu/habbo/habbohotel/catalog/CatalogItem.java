package com.eu.habbo.habbohotel.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.FurnitureType;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.messages.ISerialize;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/catalog/CatalogItem.class */
public class CatalogItem implements ISerialize, Runnable, Comparable<CatalogItem> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogItem.class);
    int id;
    int limitedStack;
    private int pageId;
    private String itemId;
    private String name;
    private int credits;
    private int points;
    private short pointsType;
    private int amount;
    private boolean allowGift = false;
    private int limitedSells;
    private String extradata;
    private boolean clubOnly;
    private boolean haveOffer;
    private int offerId;
    private boolean needsUpdate;
    private int orderNumber;
    private HashMap<Integer, Integer> bundle;

    public CatalogItem(ResultSet resultSet) throws SQLException {
        load(resultSet);
        this.needsUpdate = false;
    }

    public static boolean haveOffer(CatalogItem catalogItem) {
        if (!catalogItem.haveOffer || catalogItem.getAmount() != 1 || catalogItem.isLimited() || catalogItem.bundle.size() > 1 || catalogItem.getName().toLowerCase().startsWith("cf_") || catalogItem.getName().toLowerCase().startsWith("cfc_")) {
            return false;
        }
        TObjectHashIterator it = catalogItem.getBaseItems().iterator();
        while (it.hasNext()) {
            Item item = (Item) it.next();
            if (item.getName().toLowerCase().startsWith("cf_") || item.getName().toLowerCase().startsWith("cfc_") || item.getName().toLowerCase().startsWith("rentable_bot")) {
                return false;
            }
        }
        return !catalogItem.getName().toLowerCase().startsWith("rentable_bot_");
    }

    public void update(ResultSet resultSet) throws SQLException {
        load(resultSet);
    }

    private void load(ResultSet resultSet) throws SQLException {
        this.id = resultSet.getInt("id");
        this.pageId = resultSet.getInt("page_id");
        this.itemId = resultSet.getString("item_Ids");
        this.name = resultSet.getString("catalog_name");
        this.credits = resultSet.getInt("cost_credits");
        this.points = resultSet.getInt("cost_points");
        this.pointsType = resultSet.getShort("points_type");
        this.amount = resultSet.getInt("amount");
        this.limitedStack = resultSet.getInt("limited_stack");
        this.limitedSells = resultSet.getInt("limited_sells");
        this.extradata = resultSet.getString("extradata");
        this.clubOnly = resultSet.getBoolean("club_only");
        this.haveOffer = resultSet.getBoolean("have_offer");
        this.offerId = resultSet.getInt("offer_id");
        this.orderNumber = resultSet.getInt("order_number");
        this.bundle = new HashMap<>();
        loadBundle();
    }

    public int getId() {
        return this.id;
    }

    public int getPageId() {
        return this.pageId;
    }

    public void setPageId(int i) {
        this.pageId = i;
    }

    public String getItemId() {
        return this.itemId;
    }

    public void setItemId(String str) {
        this.itemId = str;
    }

    public String getName() {
        return this.name;
    }

    public int getCredits() {
        return this.credits;
    }

    public int getPoints() {
        return this.points;
    }

    public int getPointsType() {
        return this.pointsType;
    }

    public int getAmount() {
        return this.amount;
    }

    public int getLimitedStack() {
        return this.limitedStack;
    }

    public int getLimitedSells() {
        CatalogLimitedConfiguration limitedConfig = Emulator.getGameEnvironment().getCatalogManager().getLimitedConfig(this);
        return limitedConfig != null ? this.limitedStack - limitedConfig.available() : this.limitedStack;
    }

    public String getExtradata() {
        return this.extradata;
    }

    public boolean isClubOnly() {
        return this.clubOnly;
    }

    public boolean isHaveOffer() {
        return this.haveOffer;
    }

    public int getOfferId() {
        return this.offerId;
    }

    public boolean isLimited() {
        return this.limitedStack > 0;
    }

    private int getOrderNumber() {
        return this.orderNumber;
    }

    public void setNeedsUpdate(boolean z) {
        this.needsUpdate = z;
    }

    public synchronized void sellRare() {
        this.limitedSells++;
        this.needsUpdate = true;
        if (this.limitedSells == this.limitedStack) {
            Emulator.getGameEnvironment().getCatalogManager().moveCatalogItem(this, Emulator.getConfig().getInt("catalog.ltd.page.soldout"));
        }
        Emulator.getThreading().run(this);
    }

    public THashSet<Item> getBaseItems() {
        Item item;
        THashSet<Item> tHashSet = new THashSet<>();
        if (!this.itemId.isEmpty()) {
            String[] strArrSplit = this.itemId.split(";");
            int length = strArrSplit.length;
            for (int i = 0; i < length; i++) {
                String str = strArrSplit[i];
                if (!str.isEmpty()) {
                    if (str.contains(":")) {
                        str = str.split(":")[0];
                    }
                    try {
                        int i2 = Integer.parseInt(str);
                        if (i2 > 0 && (item = Emulator.getGameEnvironment().getItemManager().getItem(i2)) != null) {
                            tHashSet.add(item);
                        }
                    } catch (Exception e) {
                        LOGGER.info("Invalid value (" + str + ") for items_base column for catalog_item id (" + this.id + "). Value must be integer or of the format of integer:amount;integer:amount");
                    }
                }
            }
        }
        return tHashSet;
    }

    public int getItemAmount(int i) {
        return this.bundle.containsKey(Integer.valueOf(i)) ? this.bundle.get(Integer.valueOf(i)).intValue() : this.amount;
    }

    public HashMap<Integer, Integer> getBundle() {
        return this.bundle;
    }

    public void loadBundle() {
        if (!this.itemId.contains(";")) {
            try {
                Item item = Emulator.getGameEnvironment().getItemManager().getItem(Integer.valueOf(this.itemId).intValue());
                if (item != null) {
                    this.allowGift = item.allowGift();
                }
                return;
            } catch (Exception e) {
                return;
            }
        }
        try {
            for (String str : this.itemId.split(";")) {
                if (str.contains(":")) {
                    String[] strArrSplit = str.split(":");
                    if (strArrSplit.length > 1 && Integer.parseInt(strArrSplit[0]) > 0 && Integer.parseInt(strArrSplit[1]) > 0) {
                        this.bundle.put(Integer.valueOf(Integer.parseInt(strArrSplit[0])), Integer.valueOf(Integer.parseInt(strArrSplit[1])));
                    }
                } else if (!str.isEmpty()) {
                    this.bundle.put(Integer.valueOf(Integer.parseInt(str)), 1);
                }
            }
        } catch (Exception e2) {
            LOGGER.debug("Failed to load " + this.itemId);
            LOGGER.error("Caught exception", e2);
        }
    }

    @Override // com.eu.habbo.messages.ISerialize
    public void serialize(ServerMessage serverMessage) {
        serverMessage.appendInt(Integer.valueOf(getId()));
        serverMessage.appendString(getName());
        serverMessage.appendBoolean(false);
        serverMessage.appendInt(Integer.valueOf(getCredits()));
        serverMessage.appendInt(Integer.valueOf(getPoints()));
        serverMessage.appendInt(Integer.valueOf(getPointsType()));
        serverMessage.appendBoolean(Boolean.valueOf(this.allowGift));
        THashSet<Item> baseItems = getBaseItems();
        serverMessage.appendInt(Integer.valueOf(baseItems.size()));
        TObjectHashIterator it = baseItems.iterator();
        while (it.hasNext()) {
            Item item = (Item) it.next();
            serverMessage.appendString(item.getType().code.toLowerCase());
            if (item.getType() == FurnitureType.BADGE) {
                serverMessage.appendString(item.getName());
            } else {
                serverMessage.appendInt(Integer.valueOf(item.getSpriteId()));
                if (getName().contains("wallpaper_single") || getName().contains("floor_single") || getName().contains("landscape_single")) {
                    serverMessage.appendString(getName().split("_")[2]);
                } else if (item.getName().contains("bot") && item.getType() == FurnitureType.ROBOT) {
                    boolean z = false;
                    String[] strArrSplit = getExtradata().split(";");
                    int length = strArrSplit.length;
                    int i = 0;
                    while (true) {
                        if (i >= length) {
                            break;
                        }
                        String str = strArrSplit[i];
                        if (str.startsWith("figure:")) {
                            z = true;
                            serverMessage.appendString(str.replace("figure:", Emulator.PREVIEW));
                            break;
                        }
                        i++;
                    }
                    if (!z) {
                        serverMessage.appendString(getExtradata());
                    }
                } else if (item.getType() == FurnitureType.ROBOT || item.getName().equalsIgnoreCase("poster") || getName().startsWith("SONG ")) {
                    serverMessage.appendString(getExtradata());
                } else {
                    serverMessage.appendString(Emulator.PREVIEW);
                }
                serverMessage.appendInt(Integer.valueOf(getItemAmount(item.getId())));
                serverMessage.appendBoolean(Boolean.valueOf(isLimited()));
                if (isLimited()) {
                    serverMessage.appendInt(Integer.valueOf(getLimitedStack()));
                    serverMessage.appendInt(Integer.valueOf(getLimitedStack() - getLimitedSells()));
                }
            }
        }
        serverMessage.appendInt(Boolean.valueOf(this.clubOnly));
        serverMessage.appendBoolean(Boolean.valueOf(haveOffer(this)));
        serverMessage.appendBoolean(false);
        serverMessage.appendString(this.name + ".png");
    }

    @Override // java.lang.Runnable
    public void run() {
        Connection connection;
        if (this.needsUpdate) {
            try {
                connection = Emulator.getDatabase().getDataSource().getConnection();
            } catch (SQLException e) {
                LOGGER.error("Caught SQL exception", e);
            }
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE catalog_items SET limited_sells = ?, page_id = ? WHERE id = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, getLimitedSells());
                    preparedStatementPrepareStatement.setInt(2, this.pageId);
                    preparedStatementPrepareStatement.setInt(3, getId());
                    preparedStatementPrepareStatement.execute();
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

    @Override // java.lang.Comparable
    public int compareTo(CatalogItem catalogItem) {
        return CatalogManager.SORT_USING_ORDERNUM ? getOrderNumber() - catalogItem.getOrderNumber() : getId() - catalogItem.getId();
    }
}
