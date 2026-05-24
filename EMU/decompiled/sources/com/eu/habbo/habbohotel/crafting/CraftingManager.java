package com.eu.habbo.habbohotel.crafting;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import gnu.trove.map.hash.THashMap;
import gnu.trove.procedure.TObjectProcedure;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/crafting/CraftingManager.class */
public class CraftingManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(CraftingManager.class);
    private final THashMap<Item, CraftingAltar> altars = new THashMap<>();

    public CraftingManager() {
        reload();
    }

    public void reload() {
        dispose();
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM crafting_altars_recipes INNER JOIN crafting_recipes ON crafting_altars_recipes.recipe_id = crafting_recipes.id INNER JOIN crafting_recipes_ingredients ON crafting_recipes.id = crafting_recipes_ingredients.recipe_id WHERE crafting_recipes.enabled = ? ORDER BY altar_id ASC");
                try {
                    preparedStatementPrepareStatement.setString(1, "1");
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            Item item = Emulator.getGameEnvironment().getItemManager().getItem(resultSetExecuteQuery.getInt("altar_id"));
                            if (item != null) {
                                if (!this.altars.containsKey(item)) {
                                    this.altars.put(item, new CraftingAltar(item));
                                }
                                CraftingAltar craftingAltar = (CraftingAltar) this.altars.get(item);
                                if (craftingAltar != null) {
                                    CraftingRecipe recipe = craftingAltar.getRecipe(resultSetExecuteQuery.getInt("crafting_recipes_ingredients.recipe_id"));
                                    if (recipe == null) {
                                        recipe = new CraftingRecipe(resultSetExecuteQuery);
                                        craftingAltar.addRecipe(recipe);
                                    }
                                    Item item2 = Emulator.getGameEnvironment().getItemManager().getItem(resultSetExecuteQuery.getInt("crafting_recipes_ingredients.item_id"));
                                    if (item2 != null) {
                                        recipe.addIngredient(item2, resultSetExecuteQuery.getInt("crafting_recipes_ingredients.amount"));
                                        craftingAltar.addIngredient(item2);
                                    } else {
                                        LOGGER.error("Unknown ingredient item " + resultSetExecuteQuery.getInt("crafting_recipes_ingredients.item_id"));
                                    }
                                }
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

    public int getRecipesWithItemCount(final Item item) {
        final int[] iArr = {0};
        synchronized (this.altars) {
            this.altars.forEachValue(new TObjectProcedure<CraftingAltar>() { // from class: com.eu.habbo.habbohotel.crafting.CraftingManager.1
                public boolean execute(CraftingAltar craftingAltar) {
                    if (!craftingAltar.hasIngredient(item)) {
                        return true;
                    }
                    int[] iArr2 = iArr;
                    iArr2[0] = iArr2[0] + 1;
                    return true;
                }
            });
        }
        return iArr[0];
    }

    public CraftingRecipe getRecipe(String str) {
        Iterator it = this.altars.values().iterator();
        while (it.hasNext()) {
            CraftingRecipe recipe = ((CraftingAltar) it.next()).getRecipe(str);
            if (recipe != null) {
                return recipe;
            }
        }
        return null;
    }

    public CraftingAltar getAltar(Item item) {
        return (CraftingAltar) this.altars.get(item);
    }

    public void dispose() {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("UPDATE crafting_recipes SET remaining = ? WHERE id = ? LIMIT 1");
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            Iterator it = this.altars.values().iterator();
            while (it.hasNext()) {
                for (CraftingRecipe craftingRecipe : ((CraftingAltar) it.next()).getRecipes()) {
                    if (craftingRecipe.isLimited()) {
                        preparedStatementPrepareStatement.setInt(1, craftingRecipe.getRemaining());
                        preparedStatementPrepareStatement.setInt(2, craftingRecipe.getId());
                        preparedStatementPrepareStatement.addBatch();
                    }
                }
            }
            preparedStatementPrepareStatement.executeBatch();
            if (preparedStatementPrepareStatement != null) {
                preparedStatementPrepareStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
            this.altars.clear();
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
    }
}
