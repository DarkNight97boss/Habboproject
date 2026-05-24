package com.eu.habbo.habbohotel.catalog;

import com.eu.habbo.messages.ISerialize;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.TCollections;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/catalog/CatalogPage.class */
public abstract class CatalogPage implements Comparable<CatalogPage>, ISerialize {
    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogPage.class);
    protected final TIntArrayList offerIds = new TIntArrayList();
    protected final THashMap<Integer, CatalogPage> childPages = new THashMap<>();
    private final TIntObjectMap<CatalogItem> catalogItems = TCollections.synchronizedMap(new TIntObjectHashMap());
    private final ArrayList<Integer> included = new ArrayList<>();
    protected int id;
    protected int parentId;
    protected int rank;
    protected String caption;
    protected String pageName;
    protected int iconColor;
    protected int iconImage;
    protected int orderNum;
    protected boolean visible;
    protected boolean enabled;
    protected boolean clubOnly;
    protected String layout;
    protected String headerImage;
    protected String teaserImage;
    protected String specialImage;
    protected String textOne;
    protected String textTwo;
    protected String textDetails;
    protected String textTeaser;

    public CatalogPage() {
    }

    public CatalogPage(ResultSet resultSet) throws SQLException {
        if (resultSet == null) {
            return;
        }
        this.id = resultSet.getInt("id");
        this.parentId = resultSet.getInt("parent_id");
        this.rank = resultSet.getInt("min_rank");
        this.caption = resultSet.getString("caption");
        this.pageName = resultSet.getString("caption_save");
        this.iconColor = resultSet.getInt("icon_color");
        this.iconImage = resultSet.getInt("icon_image");
        this.orderNum = resultSet.getInt("order_num");
        this.visible = resultSet.getBoolean("visible");
        this.enabled = resultSet.getBoolean("enabled");
        this.clubOnly = resultSet.getBoolean("club_only");
        this.layout = resultSet.getString("page_layout");
        this.headerImage = resultSet.getString("page_headline");
        this.teaserImage = resultSet.getString("page_teaser");
        this.specialImage = resultSet.getString("page_special");
        this.textOne = resultSet.getString("page_text1");
        this.textTwo = resultSet.getString("page_text2");
        this.textDetails = resultSet.getString("page_text_details");
        this.textTeaser = resultSet.getString("page_text_teaser");
        if (resultSet.getString("includes").isEmpty()) {
            return;
        }
        for (String str : resultSet.getString("includes").split(";")) {
            try {
                this.included.add(Integer.valueOf(str));
            } catch (Exception e) {
                LOGGER.error("Caught exception", e);
                LOGGER.error("Failed to parse includes column value of (" + str + ") for catalog page (" + this.id + ")");
            }
        }
    }

    public int getId() {
        return this.id;
    }

    public int getParentId() {
        return this.parentId;
    }

    public int getRank() {
        return this.rank;
    }

    public void setRank(int i) {
        this.rank = i;
    }

    public String getCaption() {
        return this.caption;
    }

    public String getPageName() {
        return this.pageName;
    }

    public int getIconColor() {
        return this.iconColor;
    }

    public int getIconImage() {
        return this.iconImage;
    }

    public int getOrderNum() {
        return this.orderNum;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isClubOnly() {
        return this.clubOnly;
    }

    public String getLayout() {
        return this.layout;
    }

    public String getHeaderImage() {
        return this.headerImage;
    }

    public String getTeaserImage() {
        return this.teaserImage;
    }

    public String getSpecialImage() {
        return this.specialImage;
    }

    public String getTextOne() {
        return this.textOne;
    }

    public String getTextTwo() {
        return this.textTwo;
    }

    public String getTextDetails() {
        return this.textDetails;
    }

    public String getTextTeaser() {
        return this.textTeaser;
    }

    public TIntArrayList getOfferIds() {
        return this.offerIds;
    }

    public void addOfferId(int i) {
        this.offerIds.add(i);
    }

    public void addItem(CatalogItem catalogItem) {
        this.catalogItems.put(catalogItem.getId(), catalogItem);
    }

    public TIntObjectMap<CatalogItem> getCatalogItems() {
        return this.catalogItems;
    }

    public CatalogItem getCatalogItem(int i) {
        return (CatalogItem) this.catalogItems.get(i);
    }

    public ArrayList<Integer> getIncluded() {
        return this.included;
    }

    public THashMap<Integer, CatalogPage> getChildPages() {
        return this.childPages;
    }

    public void addChildPage(CatalogPage catalogPage) {
        this.childPages.put(Integer.valueOf(catalogPage.getId()), catalogPage);
        if (catalogPage.getRank() < getRank()) {
            catalogPage.setRank(getRank());
        }
    }

    @Override // java.lang.Comparable
    public int compareTo(CatalogPage catalogPage) {
        return getOrderNum() - catalogPage.getOrderNum();
    }

    @Override // com.eu.habbo.messages.ISerialize
    public abstract void serialize(ServerMessage serverMessage);
}
