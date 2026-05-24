package com.eu.habbo.habbohotel.campaign.calendar;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.users.subscriptions.Subscription;
import com.eu.habbo.messages.outgoing.inventory.AddHabboItemComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/campaign/calendar/CalendarRewardObject.class */
public class CalendarRewardObject {
    private static final Logger LOGGER = LoggerFactory.getLogger(CalendarRewardObject.class);
    private final int id;
    private final String productName;
    private final String customImage;
    private final int credits;
    private final int pixels;
    private final int points;
    private final int pointsType;
    private final String badge;
    private final int itemId;
    private final String subscription_type;
    private final int subscription_days;

    public CalendarRewardObject(ResultSet resultSet) throws SQLException {
        this.id = resultSet.getInt("id");
        this.productName = resultSet.getString("product_name");
        this.customImage = resultSet.getString("custom_image");
        this.credits = resultSet.getInt("credits");
        this.pixels = resultSet.getInt("pixels");
        this.points = resultSet.getInt("points");
        this.pointsType = resultSet.getInt("points_type");
        this.badge = resultSet.getString("badge");
        this.itemId = resultSet.getInt("item_id");
        this.subscription_type = resultSet.getString("subscription_type");
        this.subscription_days = resultSet.getInt("subscription_days");
    }

    public void give(Habbo habbo) {
        Item item;
        if (this.credits > 0) {
            habbo.giveCredits(this.credits);
        }
        if (this.pixels > 0) {
            habbo.givePixels((int) (((double) this.pixels) * (habbo.getHabboStats().hasActiveClub() ? CalendarManager.HC_MODIFIER : 1.0d)));
        }
        if (this.points > 0) {
            habbo.givePoints(this.pointsType, this.points);
        }
        if (!this.badge.isEmpty()) {
            habbo.addBadge(this.badge);
        }
        if (this.subscription_type != null && !this.subscription_type.isEmpty()) {
            if (Subscription.HABBO_CLUB.equals(this.subscription_type)) {
                habbo.getHabboStats().createSubscription(Subscription.HABBO_CLUB, this.subscription_days * 86400);
            } else {
                habbo.getHabboStats().createSubscription(this.subscription_type, this.subscription_days * 86400);
            }
        }
        if (this.itemId <= 0 || (item = getItem()) == null) {
            return;
        }
        HabboItem habboItemCreateItem = Emulator.getGameEnvironment().getItemManager().createItem(habbo.getHabboInfo().getId(), item, 0, 0, Emulator.PREVIEW);
        habbo.getInventory().getItemsComponent().addItem(habboItemCreateItem);
        habbo.getClient().sendResponse(new AddHabboItemComposer(habboItemCreateItem));
        habbo.getClient().sendResponse(new InventoryRefreshComposer());
    }

    public int getId() {
        return this.id;
    }

    public String getCustomImage() {
        return this.customImage;
    }

    public int getCredits() {
        return this.credits;
    }

    public int getPixels() {
        return this.pixels;
    }

    public int getPoints() {
        return this.points;
    }

    public int getPointsType() {
        return this.pointsType;
    }

    public String getProductName() {
        return this.productName;
    }

    public String getSubscriptionType() {
        return this.subscription_type;
    }

    public int getSubscriptionDays() {
        return this.subscription_days;
    }

    public String getBadge() {
        return this.badge;
    }

    public Item getItem() {
        return Emulator.getGameEnvironment().getItemManager().getItem(this.itemId);
    }
}
