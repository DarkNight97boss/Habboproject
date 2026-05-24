package com.eu.habbo.habbohotel.crafting;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import gnu.trove.map.hash.THashMap;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/crafting/CraftingRecipe.class */
public class CraftingRecipe {
    private final int id;
    private final String name;
    private final Item reward;
    private final boolean secret;
    private final String achievement;
    private final boolean limited;
    private final THashMap<Item, Integer> ingredients = new THashMap<>();
    private int remaining;

    public CraftingRecipe(ResultSet resultSet) throws SQLException {
        this.id = resultSet.getInt("id");
        this.name = resultSet.getString("product_name");
        this.reward = Emulator.getGameEnvironment().getItemManager().getItem(resultSet.getInt("reward"));
        this.secret = resultSet.getString("secret").equals("1");
        this.achievement = resultSet.getString("achievement");
        this.limited = resultSet.getString("limited").equals("1");
        this.remaining = resultSet.getInt("remaining");
    }

    public boolean canBeCrafted() {
        return !this.limited || this.remaining > 0;
    }

    public synchronized boolean decrease() {
        if (this.remaining <= 0) {
            return false;
        }
        this.remaining--;
        return true;
    }

    public void addIngredient(Item item, int i) {
        this.ingredients.put(item, Integer.valueOf(i));
    }

    public int getAmountNeeded(Item item) {
        return ((Integer) this.ingredients.get(item)).intValue();
    }

    public boolean hasIngredient(Item item) {
        return this.ingredients.containsKey(item);
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Item getReward() {
        return this.reward;
    }

    public boolean isSecret() {
        return this.secret;
    }

    public String getAchievement() {
        return this.achievement;
    }

    public boolean isLimited() {
        return this.limited;
    }

    public THashMap<Item, Integer> getIngredients() {
        return this.ingredients;
    }

    public int getRemaining() {
        return this.remaining;
    }
}
