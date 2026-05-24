package com.eu.habbo.habbohotel.pets;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.pets.InteractionNest;
import com.eu.habbo.habbohotel.items.interactions.pets.InteractionPetDrink;
import com.eu.habbo.habbohotel.items.interactions.pets.InteractionPetFood;
import com.eu.habbo.habbohotel.items.interactions.pets.InteractionPetToy;
import com.eu.habbo.habbohotel.users.HabboItem;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/pets/PetData.class */
public class PetData implements Comparable<PetData> {
    public static final String BLINK = "eyb";
    public static final String SPEAK = "spk";
    public static final String EAT = "eat";
    public static final String PLAYFUL = "pla";
    public static final List<Item> generalDrinkItems = new ArrayList();
    public static final List<Item> generalFoodItems = new ArrayList();
    public static final List<Item> generalNestItems = new ArrayList();
    public static final List<Item> generalToyItems = new ArrayList();
    public static final THashMap<PetVocalsType, THashSet<PetVocal>> generalPetVocals = new THashMap<>();
    public String[] actionsHappy;
    public String[] actionsTired;
    public String[] actionsRandom;
    public THashMap<PetVocalsType, THashSet<PetVocal>> petVocals;
    public boolean canSwim;
    private int type;
    private String name;
    private List<PetCommand> petCommands;
    private List<Item> nestItems;
    private List<Item> foodItems;
    private List<Item> drinkItems;
    private List<Item> toyItems;
    private int offspringType;

    public PetData(ResultSet resultSet) throws SQLException {
        load(resultSet);
    }

    public void load(ResultSet resultSet) throws SQLException {
        this.type = resultSet.getInt("pet_type");
        this.name = resultSet.getString("pet_name");
        this.offspringType = resultSet.getInt("offspring_type");
        this.actionsHappy = resultSet.getString("happy_actions").split(";");
        this.actionsTired = resultSet.getString("tired_actions").split(";");
        this.actionsRandom = resultSet.getString("random_actions").split(";");
        this.canSwim = resultSet.getString("can_swim").equalsIgnoreCase("1");
        reset();
    }

    public List<PetCommand> getPetCommands() {
        return this.petCommands;
    }

    public void setPetCommands(List<PetCommand> list) {
        this.petCommands = list;
    }

    public int getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

    public int getOffspringType() {
        return this.offspringType;
    }

    public void addNest(Item item) {
        if (item != null) {
            this.nestItems.add(item);
        }
    }

    public List<Item> getNests() {
        return this.nestItems;
    }

    public boolean haveNest(HabboItem habboItem) {
        return haveNest(habboItem.getBaseItem());
    }

    boolean haveNest(Item item) {
        return generalNestItems.contains(item) || this.nestItems.contains(item);
    }

    public HabboItem randomNest(THashSet<InteractionNest> tHashSet) {
        ArrayList arrayList = new ArrayList();
        TObjectHashIterator it = tHashSet.iterator();
        while (it.hasNext()) {
            InteractionNest interactionNest = (InteractionNest) it.next();
            if (haveNest(interactionNest)) {
                arrayList.add(interactionNest);
            }
        }
        if (arrayList.isEmpty()) {
            return null;
        }
        Collections.shuffle(arrayList);
        return (HabboItem) arrayList.get(0);
    }

    public void addFoodItem(Item item) {
        this.foodItems.add(item);
    }

    public List<Item> getFoodItems() {
        return this.foodItems;
    }

    public boolean haveFoodItem(HabboItem habboItem) {
        return haveFoodItem(habboItem.getBaseItem());
    }

    boolean haveFoodItem(Item item) {
        return this.foodItems.contains(item) || generalFoodItems.contains(item);
    }

    public HabboItem randomFoodItem(THashSet<InteractionPetFood> tHashSet) {
        ArrayList arrayList = new ArrayList();
        TObjectHashIterator it = tHashSet.iterator();
        while (it.hasNext()) {
            InteractionPetFood interactionPetFood = (InteractionPetFood) it.next();
            if (haveFoodItem(interactionPetFood)) {
                arrayList.add(interactionPetFood);
            }
        }
        if (arrayList.isEmpty()) {
            return null;
        }
        Collections.shuffle(arrayList);
        return (HabboItem) arrayList.get(0);
    }

    public void addDrinkItem(Item item) {
        this.drinkItems.add(item);
    }

    public List<Item> getDrinkItems() {
        return this.drinkItems;
    }

    public boolean haveDrinkItem(HabboItem habboItem) {
        return haveDrinkItem(habboItem.getBaseItem());
    }

    boolean haveDrinkItem(Item item) {
        return this.drinkItems.contains(item) || generalDrinkItems.contains(item);
    }

    public HabboItem randomDrinkItem(THashSet<InteractionPetDrink> tHashSet) {
        ArrayList arrayList = new ArrayList();
        TObjectHashIterator it = tHashSet.iterator();
        while (it.hasNext()) {
            InteractionPetDrink interactionPetDrink = (InteractionPetDrink) it.next();
            if (haveDrinkItem(interactionPetDrink)) {
                arrayList.add(interactionPetDrink);
            }
        }
        if (arrayList.isEmpty()) {
            return null;
        }
        Collections.shuffle(arrayList);
        return (HabboItem) arrayList.get(0);
    }

    public void addToyItem(Item item) {
        this.toyItems.add(item);
    }

    public List<Item> getToyItems() {
        return this.toyItems;
    }

    public boolean haveToyItem(HabboItem habboItem) {
        return haveToyItem(habboItem.getBaseItem());
    }

    public boolean haveToyItem(Item item) {
        return this.toyItems.contains(item) || generalToyItems.contains(item);
    }

    public HabboItem randomToyItem(THashSet<InteractionPetToy> tHashSet) {
        ArrayList arrayList = new ArrayList();
        TObjectHashIterator it = tHashSet.iterator();
        while (it.hasNext()) {
            InteractionPetToy interactionPetToy = (InteractionPetToy) it.next();
            if (haveToyItem(interactionPetToy)) {
                arrayList.add(interactionPetToy);
            }
        }
        if (arrayList.isEmpty()) {
            return null;
        }
        Collections.shuffle(arrayList);
        return (HabboItem) arrayList.get(0);
    }

    public PetVocal randomVocal(PetVocalsType petVocalsType) {
        ArrayList arrayList = new ArrayList();
        if (this.petVocals.get(petVocalsType) != null) {
            arrayList.addAll((Collection) this.petVocals.get(petVocalsType));
        }
        if (generalPetVocals.get(petVocalsType) != null) {
            arrayList.addAll((Collection) generalPetVocals.get(petVocalsType));
        }
        if (arrayList.isEmpty()) {
            return null;
        }
        return (PetVocal) arrayList.get(Emulator.getRandom().nextInt(arrayList.size()));
    }

    @Override // java.lang.Comparable
    public int compareTo(PetData petData) {
        return getType() - petData.getType();
    }

    public void reset() {
        this.petCommands = new ArrayList();
        this.nestItems = new ArrayList();
        this.foodItems = new ArrayList();
        this.drinkItems = new ArrayList();
        this.toyItems = new ArrayList();
        this.petVocals = new THashMap<>();
        for (PetVocalsType petVocalsType : PetVocalsType.values()) {
            this.petVocals.put(petVocalsType, new THashSet());
        }
        if (generalPetVocals.isEmpty()) {
            for (PetVocalsType petVocalsType2 : PetVocalsType.values()) {
                generalPetVocals.put(petVocalsType2, new THashSet());
            }
        }
    }
}
