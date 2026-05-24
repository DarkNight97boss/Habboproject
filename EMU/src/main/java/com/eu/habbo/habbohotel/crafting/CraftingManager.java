package com.eu.habbo.habbohotel.crafting;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import gnu.trove.map.hash.THashMap;
import gnu.trove.procedure.TObjectProcedure;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CraftingManager {
   private static final Logger LOGGER = LoggerFactory.getLogger(CraftingManager.class);
   private final THashMap<Item, CraftingAltar> altars = new THashMap();

   public CraftingManager() {
      this.reload();
   }

   public void reload() {
      this.dispose();

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "SELECT * FROM crafting_altars_recipes INNER JOIN crafting_recipes ON crafting_altars_recipes.recipe_id = crafting_recipes.id INNER JOIN crafting_recipes_ingredients ON crafting_recipes.id = crafting_recipes_ingredients.recipe_id WHERE crafting_recipes.enabled = ? ORDER BY altar_id ASC"
            );

            try {
               statement.setString(1, "1");
               ResultSet set = statement.executeQuery();

               try {
                  while (set.next()) {
                     Item item = Emulator.getGameEnvironment().getItemManager().getItem(set.getInt("altar_id"));
                     if (item != null) {
                        if (!this.altars.containsKey(item)) {
                           this.altars.put(item, new CraftingAltar(item));
                        }

                        CraftingAltar altar = (CraftingAltar)this.altars.get(item);
                        if (altar != null) {
                           CraftingRecipe recipe = altar.getRecipe(set.getInt("crafting_recipes_ingredients.recipe_id"));
                           if (recipe == null) {
                              recipe = new CraftingRecipe(set);
                              altar.addRecipe(recipe);
                           }

                           Item ingredientItem = Emulator.getGameEnvironment().getItemManager().getItem(set.getInt("crafting_recipes_ingredients.item_id"));
                           if (ingredientItem != null) {
                              recipe.addIngredient(ingredientItem, set.getInt("crafting_recipes_ingredients.amount"));
                              altar.addIngredient(ingredientItem);
                           } else {
                              LOGGER.error("Unknown ingredient item " + set.getInt("crafting_recipes_ingredients.item_id"));
                           }
                        }
                     }
                  }
               } catch (Throwable var11) {
                  if (set != null) {
                     try {
                        set.close();
                     } catch (Throwable var10) {
                        var11.addSuppressed(var10);
                     }
                  }

                  throw var11;
               }

               if (set != null) {
                  set.close();
               }
            } catch (Throwable var12) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var9) {
                     var12.addSuppressed(var9);
                  }
               }

               throw var12;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var13) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var8) {
                  var13.addSuppressed(var8);
               }
            }

            throw var13;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }
   }

   public int getRecipesWithItemCount(final Item item) {
      final int[] i = new int[]{0};
      synchronized (this.altars) {
         this.altars.forEachValue(new TObjectProcedure<CraftingAltar>() {
            public boolean execute(CraftingAltar altar) {
               if (altar.hasIngredient(item)) {
                  i[0]++;
               }

               return true;
            }
         });
      }

      return i[0];
   }

   public CraftingRecipe getRecipe(String recipeName) {
      for (CraftingAltar altar : this.altars.values()) {
         CraftingRecipe recipe = altar.getRecipe(recipeName);
         if (recipe != null) {
            return recipe;
         }
      }

      return null;
   }

   public CraftingAltar getAltar(Item item) {
      return (CraftingAltar)this.altars.get(item);
   }

   public void dispose() {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("UPDATE crafting_recipes SET remaining = ? WHERE id = ? LIMIT 1");

            try {
               for (CraftingAltar altar : this.altars.values()) {
                  for (CraftingRecipe recipe : altar.getRecipes()) {
                     if (recipe.isLimited()) {
                        statement.setInt(1, recipe.getRemaining());
                        statement.setInt(2, recipe.getId());
                        statement.addBatch();
                     }
                  }
               }

               statement.executeBatch();
            } catch (Throwable var9) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var8) {
                     var9.addSuppressed(var8);
                  }
               }

               throw var9;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var10) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var7) {
                  var10.addSuppressed(var7);
               }
            }

            throw var10;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }

      this.altars.clear();
   }
}
