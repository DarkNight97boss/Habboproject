package com.eu.habbo.habbohotel.crafting;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.users.Habbo;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/crafting/CraftingAltar.class */
public class CraftingAltar {
    private final Item baseItem;
    private final THashSet<Item> ingredients = new THashSet<>(1);
    private final THashMap<Integer, CraftingRecipe> recipes = new THashMap<>(1);

    public CraftingAltar(Item item) {
        this.baseItem = item;
    }

    public void addIngredient(Item item) {
        this.ingredients.add(item);
    }

    public boolean hasIngredient(Item item) {
        return this.ingredients.contains(item);
    }

    public Map<CraftingRecipe, Boolean> matchRecipes(Map<Item, Integer> map) {
        THashMap tHashMap = new THashMap(Math.max(1, this.recipes.size() / 3));
        for (Map.Entry entry : this.recipes.entrySet()) {
            boolean z = true;
            if (!((CraftingRecipe) entry.getValue()).isLimited() || ((CraftingRecipe) entry.getValue()).canBeCrafted()) {
                boolean z2 = map.size() == ((CraftingRecipe) entry.getValue()).getIngredients().size();
                for (Map.Entry<Item, Integer> entry2 : map.entrySet()) {
                    if (z) {
                        if (((CraftingRecipe) entry.getValue()).getIngredients().containsKey(entry2.getKey())) {
                            if (!((Integer) ((CraftingRecipe) entry.getValue()).getIngredients().get(entry2.getKey())).equals(entry2.getValue())) {
                                z2 = false;
                                if (((Integer) ((CraftingRecipe) entry.getValue()).getIngredients().get(entry2.getKey())).intValue() > entry2.getValue().intValue()) {
                                }
                            }
                        }
                        z = false;
                    }
                }
                if (z) {
                    tHashMap.put((CraftingRecipe) entry.getValue(), Boolean.valueOf(z2));
                }
            }
        }
        return tHashMap;
    }

    public void addRecipe(CraftingRecipe craftingRecipe) {
        this.recipes.put(Integer.valueOf(craftingRecipe.getId()), craftingRecipe);
    }

    public CraftingRecipe getRecipe(int i) {
        return (CraftingRecipe) this.recipes.get(Integer.valueOf(i));
    }

    public CraftingRecipe getRecipe(String str) {
        for (Map.Entry entry : this.recipes.entrySet()) {
            if (((CraftingRecipe) entry.getValue()).getName().equals(str)) {
                return (CraftingRecipe) entry.getValue();
            }
        }
        return null;
    }

    public CraftingRecipe getRecipe(Map<Item, Integer> map) {
        Iterator it = this.recipes.entrySet().iterator();
        while (it.hasNext()) {
            CraftingRecipe craftingRecipe = (CraftingRecipe) ((Map.Entry) it.next()).getValue();
            for (Map.Entry entry : craftingRecipe.getIngredients().entrySet()) {
                if (!map.containsKey(entry.getKey()) || !map.get(entry.getKey()).equals(entry.getValue())) {
                    craftingRecipe = null;
                    break;
                }
            }
            if (craftingRecipe != null) {
                return craftingRecipe;
            }
        }
        return null;
    }

    public List<CraftingRecipe> getRecipesForHabbo(Habbo habbo) {
        ArrayList arrayList = new ArrayList();
        for (Map.Entry entry : this.recipes.entrySet()) {
            if (!((CraftingRecipe) entry.getValue()).isSecret() || habbo.getHabboStats().hasRecipe(((CraftingRecipe) entry.getValue()).getId())) {
                arrayList.add((CraftingRecipe) entry.getValue());
            }
        }
        return arrayList;
    }

    public Item getBaseItem() {
        return this.baseItem;
    }

    public Collection<Item> getIngredients() {
        return this.ingredients;
    }

    public Collection<CraftingRecipe> getRecipes() {
        return this.recipes.values();
    }
}
