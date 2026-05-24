package com.eu.habbo.habbohotel.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.cache.HabboOfferPurchase;
import com.eu.habbo.messages.ServerMessage;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/catalog/TargetOffer.class */
public class TargetOffer {
    public static int ACTIVE_TARGET_OFFER_ID = 0;
    private final int id;
    private final int catalogItem;
    private final String identifier;
    private final int priceInCredits;
    private final int priceInActivityPoints;
    private final int activityPointsType;
    private final int purchaseLimit;
    private final int expirationTime;
    private final String title;
    private final String description;
    private final String imageUrl;
    private final String icon;
    private final String[] vars;

    public TargetOffer(ResultSet resultSet) throws SQLException {
        this.id = resultSet.getInt("id");
        this.identifier = resultSet.getString("offer_code");
        this.priceInCredits = resultSet.getInt("credits");
        this.priceInActivityPoints = resultSet.getInt("points");
        this.activityPointsType = resultSet.getInt("points_type");
        this.title = resultSet.getString("title");
        this.description = resultSet.getString("description");
        this.imageUrl = resultSet.getString("image");
        this.icon = resultSet.getString("icon");
        this.purchaseLimit = resultSet.getInt("purchase_limit");
        this.expirationTime = resultSet.getInt("end_timestamp");
        this.vars = resultSet.getString("vars").split(";");
        this.catalogItem = resultSet.getInt("catalog_item");
    }

    public void serialize(ServerMessage serverMessage, HabboOfferPurchase habboOfferPurchase) {
        serverMessage.appendInt(Integer.valueOf(habboOfferPurchase.getState()));
        serverMessage.appendInt(Integer.valueOf(this.id));
        serverMessage.appendString(this.identifier);
        serverMessage.appendString(this.identifier);
        serverMessage.appendInt(Integer.valueOf(this.priceInCredits));
        serverMessage.appendInt(Integer.valueOf(this.priceInActivityPoints));
        serverMessage.appendInt(Integer.valueOf(this.activityPointsType));
        serverMessage.appendInt(Integer.valueOf(Math.max(this.purchaseLimit - habboOfferPurchase.getAmount(), 0)));
        serverMessage.appendInt(Integer.valueOf(Math.max(this.expirationTime - Emulator.getIntUnixTimestamp(), 0)));
        serverMessage.appendString(this.title);
        serverMessage.appendString(this.description);
        serverMessage.appendString(this.imageUrl);
        serverMessage.appendString(this.icon);
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(this.vars.length));
        for (String str : this.vars) {
            serverMessage.appendString(str);
        }
    }

    public int getId() {
        return this.id;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public int getPriceInCredits() {
        return this.priceInCredits;
    }

    public int getPriceInActivityPoints() {
        return this.priceInActivityPoints;
    }

    public int getActivityPointsType() {
        return this.activityPointsType;
    }

    public int getPurchaseLimit() {
        return this.purchaseLimit;
    }

    public int getExpirationTime() {
        return this.expirationTime;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public String getImageUrl() {
        return this.imageUrl;
    }

    public String getIcon() {
        return this.icon;
    }

    public String[] getVars() {
        return this.vars;
    }

    public int getCatalogItem() {
        return this.catalogItem;
    }
}
