package com.eu.habbo.habbohotel.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.games.GameTeamColors;
import com.eu.habbo.habbohotel.items.ICycleable;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionRoller;
import com.eu.habbo.habbohotel.items.interactions.InteractionTent;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredExtra;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.games.InteractionGameGate;
import com.eu.habbo.habbohotel.items.interactions.games.InteractionGameScoreboard;
import com.eu.habbo.habbohotel.items.interactions.games.InteractionGameTimer;
import com.eu.habbo.habbohotel.items.interactions.games.battlebanzai.InteractionBattleBanzaiTeleporter;
import com.eu.habbo.habbohotel.items.interactions.games.battlebanzai.gates.InteractionBattleBanzaiGate;
import com.eu.habbo.habbohotel.items.interactions.games.battlebanzai.scoreboards.InteractionBattleBanzaiScoreboard;
import com.eu.habbo.habbohotel.items.interactions.games.football.scoreboards.InteractionFootballScoreboard;
import com.eu.habbo.habbohotel.items.interactions.games.freeze.InteractionFreezeExitTile;
import com.eu.habbo.habbohotel.items.interactions.games.freeze.gates.InteractionFreezeGate;
import com.eu.habbo.habbohotel.items.interactions.games.freeze.scoreboards.InteractionFreezeScoreboard;
import com.eu.habbo.habbohotel.items.interactions.pets.InteractionNest;
import com.eu.habbo.habbohotel.items.interactions.pets.InteractionPetDrink;
import com.eu.habbo.habbohotel.items.interactions.pets.InteractionPetFood;
import com.eu.habbo.habbohotel.items.interactions.pets.InteractionPetToy;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredConditionType;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/rooms/RoomSpecialTypes.class */
public class RoomSpecialTypes {
    private final THashMap<Integer, InteractionBattleBanzaiTeleporter> banzaiTeleporters = new THashMap<>(0);
    private final THashMap<Integer, InteractionNest> nests = new THashMap<>(0);
    private final THashMap<Integer, InteractionPetDrink> petDrinks = new THashMap<>(0);
    private final THashMap<Integer, InteractionPetFood> petFoods = new THashMap<>(0);
    private final THashMap<Integer, InteractionPetToy> petToys = new THashMap<>(0);
    private final THashMap<Integer, InteractionRoller> rollers = new THashMap<>(0);
    private final THashMap<WiredTriggerType, THashSet<InteractionWiredTrigger>> wiredTriggers = new THashMap<>(0);
    private final THashMap<WiredEffectType, THashSet<InteractionWiredEffect>> wiredEffects = new THashMap<>(0);
    private final THashMap<WiredConditionType, THashSet<InteractionWiredCondition>> wiredConditions = new THashMap<>(0);
    private final THashMap<Integer, InteractionWiredExtra> wiredExtras = new THashMap<>(0);
    private final THashMap<Integer, InteractionGameScoreboard> gameScoreboards = new THashMap<>(0);
    private final THashMap<Integer, InteractionGameGate> gameGates = new THashMap<>(0);
    private final THashMap<Integer, InteractionGameTimer> gameTimers = new THashMap<>(0);
    private final THashMap<Integer, InteractionFreezeExitTile> freezeExitTile = new THashMap<>(0);
    private final THashMap<Integer, HabboItem> undefined = new THashMap<>(0);
    private final THashSet<ICycleable> cycleTasks = new THashSet<>(0);

    public InteractionBattleBanzaiTeleporter getBanzaiTeleporter(int i) {
        return (InteractionBattleBanzaiTeleporter) this.banzaiTeleporters.get(Integer.valueOf(i));
    }

    public void addBanzaiTeleporter(InteractionBattleBanzaiTeleporter interactionBattleBanzaiTeleporter) {
        this.banzaiTeleporters.put(Integer.valueOf(interactionBattleBanzaiTeleporter.getId()), interactionBattleBanzaiTeleporter);
    }

    public void removeBanzaiTeleporter(InteractionBattleBanzaiTeleporter interactionBattleBanzaiTeleporter) {
        this.banzaiTeleporters.remove(Integer.valueOf(interactionBattleBanzaiTeleporter.getId()));
    }

    public THashSet<InteractionBattleBanzaiTeleporter> getBanzaiTeleporters() {
        THashSet<InteractionBattleBanzaiTeleporter> tHashSet;
        synchronized (this.banzaiTeleporters) {
            tHashSet = new THashSet<>();
            tHashSet.addAll(this.banzaiTeleporters.values());
        }
        return tHashSet;
    }

    public InteractionBattleBanzaiTeleporter getRandomTeleporter(Item item, InteractionBattleBanzaiTeleporter interactionBattleBanzaiTeleporter) {
        ArrayList arrayList = new ArrayList();
        for (InteractionBattleBanzaiTeleporter interactionBattleBanzaiTeleporter2 : this.banzaiTeleporters.values()) {
            if (item == null || interactionBattleBanzaiTeleporter2.getBaseItem() == item) {
                arrayList.add(interactionBattleBanzaiTeleporter2);
            }
        }
        arrayList.remove(interactionBattleBanzaiTeleporter);
        if (arrayList.isEmpty()) {
            return null;
        }
        Collections.shuffle(arrayList);
        return (InteractionBattleBanzaiTeleporter) arrayList.get(0);
    }

    public InteractionNest getNest(int i) {
        return (InteractionNest) this.nests.get(Integer.valueOf(i));
    }

    public void addNest(InteractionNest interactionNest) {
        this.nests.put(Integer.valueOf(interactionNest.getId()), interactionNest);
    }

    public void removeNest(InteractionNest interactionNest) {
        this.nests.remove(Integer.valueOf(interactionNest.getId()));
    }

    public THashSet<InteractionNest> getNests() {
        THashSet<InteractionNest> tHashSet;
        synchronized (this.nests) {
            tHashSet = new THashSet<>();
            tHashSet.addAll(this.nests.values());
        }
        return tHashSet;
    }

    public InteractionPetDrink getPetDrink(int i) {
        return (InteractionPetDrink) this.petDrinks.get(Integer.valueOf(i));
    }

    public void addPetDrink(InteractionPetDrink interactionPetDrink) {
        this.petDrinks.put(Integer.valueOf(interactionPetDrink.getId()), interactionPetDrink);
    }

    public void removePetDrink(InteractionPetDrink interactionPetDrink) {
        this.petDrinks.remove(Integer.valueOf(interactionPetDrink.getId()));
    }

    public THashSet<InteractionPetDrink> getPetDrinks() {
        THashSet<InteractionPetDrink> tHashSet;
        synchronized (this.petDrinks) {
            tHashSet = new THashSet<>();
            tHashSet.addAll(this.petDrinks.values());
        }
        return tHashSet;
    }

    public InteractionPetFood getPetFood(int i) {
        return (InteractionPetFood) this.petFoods.get(Integer.valueOf(i));
    }

    public void addPetFood(InteractionPetFood interactionPetFood) {
        this.petFoods.put(Integer.valueOf(interactionPetFood.getId()), interactionPetFood);
    }

    public void removePetFood(InteractionPetFood interactionPetFood) {
        this.petFoods.remove(Integer.valueOf(interactionPetFood.getId()));
    }

    public THashSet<InteractionPetFood> getPetFoods() {
        THashSet<InteractionPetFood> tHashSet;
        synchronized (this.petFoods) {
            tHashSet = new THashSet<>();
            tHashSet.addAll(this.petFoods.values());
        }
        return tHashSet;
    }

    public InteractionPetToy getPetToy(int i) {
        return (InteractionPetToy) this.petToys.get(Integer.valueOf(i));
    }

    public void addPetToy(InteractionPetToy interactionPetToy) {
        this.petToys.put(Integer.valueOf(interactionPetToy.getId()), interactionPetToy);
    }

    public void removePetToy(InteractionPetToy interactionPetToy) {
        this.petToys.remove(Integer.valueOf(interactionPetToy.getId()));
    }

    public THashSet<InteractionPetToy> getPetToys() {
        THashSet<InteractionPetToy> tHashSet;
        synchronized (this.petToys) {
            tHashSet = new THashSet<>();
            tHashSet.addAll(this.petToys.values());
        }
        return tHashSet;
    }

    public InteractionRoller getRoller(int i) {
        InteractionRoller interactionRoller;
        synchronized (this.rollers) {
            interactionRoller = (InteractionRoller) this.rollers.get(Integer.valueOf(i));
        }
        return interactionRoller;
    }

    public void addRoller(InteractionRoller interactionRoller) {
        synchronized (this.rollers) {
            this.rollers.put(Integer.valueOf(interactionRoller.getId()), interactionRoller);
        }
    }

    public void removeRoller(InteractionRoller interactionRoller) {
        synchronized (this.rollers) {
            this.rollers.remove(Integer.valueOf(interactionRoller.getId()));
        }
    }

    public THashMap<Integer, InteractionRoller> getRollers() {
        return this.rollers;
    }

    public InteractionWiredTrigger getTrigger(int i) {
        synchronized (this.wiredTriggers) {
            Iterator it = this.wiredTriggers.entrySet().iterator();
            while (it.hasNext()) {
                TObjectHashIterator it2 = ((THashSet) ((Map.Entry) it.next()).getValue()).iterator();
                while (it2.hasNext()) {
                    InteractionWiredTrigger interactionWiredTrigger = (InteractionWiredTrigger) it2.next();
                    if (interactionWiredTrigger.getId() == i) {
                        return interactionWiredTrigger;
                    }
                }
            }
            return null;
        }
    }

    public THashSet<InteractionWiredTrigger> getTriggers() {
        THashSet<InteractionWiredTrigger> tHashSet;
        synchronized (this.wiredTriggers) {
            tHashSet = new THashSet<>();
            Iterator it = this.wiredTriggers.entrySet().iterator();
            while (it.hasNext()) {
                tHashSet.addAll((Collection) ((Map.Entry) it.next()).getValue());
            }
        }
        return tHashSet;
    }

    public THashSet<InteractionWiredTrigger> getTriggers(WiredTriggerType wiredTriggerType) {
        return (THashSet) this.wiredTriggers.get(wiredTriggerType);
    }

    public THashSet<InteractionWiredTrigger> getTriggers(int i, int i2) {
        THashSet<InteractionWiredTrigger> tHashSet;
        synchronized (this.wiredTriggers) {
            tHashSet = new THashSet<>();
            Iterator it = this.wiredTriggers.entrySet().iterator();
            while (it.hasNext()) {
                TObjectHashIterator it2 = ((THashSet) ((Map.Entry) it.next()).getValue()).iterator();
                while (it2.hasNext()) {
                    InteractionWiredTrigger interactionWiredTrigger = (InteractionWiredTrigger) it2.next();
                    if (interactionWiredTrigger.getX() == i && interactionWiredTrigger.getY() == i2) {
                        tHashSet.add(interactionWiredTrigger);
                    }
                }
            }
        }
        return tHashSet;
    }

    public void addTrigger(InteractionWiredTrigger interactionWiredTrigger) {
        synchronized (this.wiredTriggers) {
            if (!this.wiredTriggers.containsKey(interactionWiredTrigger.getType())) {
                this.wiredTriggers.put(interactionWiredTrigger.getType(), new THashSet());
            }
            ((THashSet) this.wiredTriggers.get(interactionWiredTrigger.getType())).add(interactionWiredTrigger);
        }
    }

    public void removeTrigger(InteractionWiredTrigger interactionWiredTrigger) {
        synchronized (this.wiredTriggers) {
            ((THashSet) this.wiredTriggers.get(interactionWiredTrigger.getType())).remove(interactionWiredTrigger);
            if (((THashSet) this.wiredTriggers.get(interactionWiredTrigger.getType())).isEmpty()) {
                this.wiredTriggers.remove(interactionWiredTrigger.getType());
            }
        }
    }

    public InteractionWiredEffect getEffect(int i) {
        synchronized (this.wiredEffects) {
            Iterator it = this.wiredEffects.entrySet().iterator();
            while (it.hasNext()) {
                TObjectHashIterator it2 = ((THashSet) ((Map.Entry) it.next()).getValue()).iterator();
                while (it2.hasNext()) {
                    InteractionWiredEffect interactionWiredEffect = (InteractionWiredEffect) it2.next();
                    if (interactionWiredEffect.getId() == i) {
                        return interactionWiredEffect;
                    }
                }
            }
            return null;
        }
    }

    public THashSet<InteractionWiredEffect> getEffects() {
        THashSet<InteractionWiredEffect> tHashSet;
        synchronized (this.wiredEffects) {
            tHashSet = new THashSet<>();
            Iterator it = this.wiredEffects.entrySet().iterator();
            while (it.hasNext()) {
                tHashSet.addAll((Collection) ((Map.Entry) it.next()).getValue());
            }
        }
        return tHashSet;
    }

    public THashSet<InteractionWiredEffect> getEffects(WiredEffectType wiredEffectType) {
        return (THashSet) this.wiredEffects.get(wiredEffectType);
    }

    public THashSet<InteractionWiredEffect> getEffects(int i, int i2) {
        THashSet<InteractionWiredEffect> tHashSet;
        synchronized (this.wiredEffects) {
            tHashSet = new THashSet<>();
            Iterator it = this.wiredEffects.entrySet().iterator();
            while (it.hasNext()) {
                TObjectHashIterator it2 = ((THashSet) ((Map.Entry) it.next()).getValue()).iterator();
                while (it2.hasNext()) {
                    InteractionWiredEffect interactionWiredEffect = (InteractionWiredEffect) it2.next();
                    if (interactionWiredEffect.getX() == i && interactionWiredEffect.getY() == i2) {
                        tHashSet.add(interactionWiredEffect);
                    }
                }
            }
        }
        return tHashSet;
    }

    public void addEffect(InteractionWiredEffect interactionWiredEffect) {
        synchronized (this.wiredEffects) {
            if (!this.wiredEffects.containsKey(interactionWiredEffect.getType())) {
                this.wiredEffects.put(interactionWiredEffect.getType(), new THashSet());
            }
            ((THashSet) this.wiredEffects.get(interactionWiredEffect.getType())).add(interactionWiredEffect);
        }
    }

    public void removeEffect(InteractionWiredEffect interactionWiredEffect) {
        synchronized (this.wiredEffects) {
            ((THashSet) this.wiredEffects.get(interactionWiredEffect.getType())).remove(interactionWiredEffect);
            if (((THashSet) this.wiredEffects.get(interactionWiredEffect.getType())).isEmpty()) {
                this.wiredEffects.remove(interactionWiredEffect.getType());
            }
        }
    }

    public InteractionWiredCondition getCondition(int i) {
        synchronized (this.wiredConditions) {
            Iterator it = this.wiredConditions.entrySet().iterator();
            while (it.hasNext()) {
                TObjectHashIterator it2 = ((THashSet) ((Map.Entry) it.next()).getValue()).iterator();
                while (it2.hasNext()) {
                    InteractionWiredCondition interactionWiredCondition = (InteractionWiredCondition) it2.next();
                    if (interactionWiredCondition.getId() == i) {
                        return interactionWiredCondition;
                    }
                }
            }
            return null;
        }
    }

    public THashSet<InteractionWiredCondition> getConditions() {
        THashSet<InteractionWiredCondition> tHashSet;
        synchronized (this.wiredConditions) {
            tHashSet = new THashSet<>();
            Iterator it = this.wiredConditions.entrySet().iterator();
            while (it.hasNext()) {
                tHashSet.addAll((Collection) ((Map.Entry) it.next()).getValue());
            }
        }
        return tHashSet;
    }

    public THashSet<InteractionWiredCondition> getConditions(WiredConditionType wiredConditionType) {
        THashSet<InteractionWiredCondition> tHashSet;
        synchronized (this.wiredConditions) {
            tHashSet = (THashSet) this.wiredConditions.get(wiredConditionType);
        }
        return tHashSet;
    }

    public THashSet<InteractionWiredCondition> getConditions(int i, int i2) {
        THashSet<InteractionWiredCondition> tHashSet;
        synchronized (this.wiredConditions) {
            tHashSet = new THashSet<>();
            Iterator it = this.wiredConditions.entrySet().iterator();
            while (it.hasNext()) {
                TObjectHashIterator it2 = ((THashSet) ((Map.Entry) it.next()).getValue()).iterator();
                while (it2.hasNext()) {
                    InteractionWiredCondition interactionWiredCondition = (InteractionWiredCondition) it2.next();
                    if (interactionWiredCondition.getX() == i && interactionWiredCondition.getY() == i2) {
                        tHashSet.add(interactionWiredCondition);
                    }
                }
            }
        }
        return tHashSet;
    }

    public void addCondition(InteractionWiredCondition interactionWiredCondition) {
        synchronized (this.wiredConditions) {
            if (!this.wiredConditions.containsKey(interactionWiredCondition.getType())) {
                this.wiredConditions.put(interactionWiredCondition.getType(), new THashSet());
            }
            ((THashSet) this.wiredConditions.get(interactionWiredCondition.getType())).add(interactionWiredCondition);
        }
    }

    public void removeCondition(InteractionWiredCondition interactionWiredCondition) {
        synchronized (this.wiredConditions) {
            ((THashSet) this.wiredConditions.get(interactionWiredCondition.getType())).remove(interactionWiredCondition);
            if (((THashSet) this.wiredConditions.get(interactionWiredCondition.getType())).isEmpty()) {
                this.wiredConditions.remove(interactionWiredCondition.getType());
            }
        }
    }

    public THashSet<InteractionWiredExtra> getExtras() {
        THashSet<InteractionWiredExtra> tHashSet;
        synchronized (this.wiredExtras) {
            tHashSet = new THashSet<>();
            Iterator it = this.wiredExtras.entrySet().iterator();
            while (it.hasNext()) {
                tHashSet.add((InteractionWiredExtra) ((Map.Entry) it.next()).getValue());
            }
        }
        return tHashSet;
    }

    public THashSet<InteractionWiredExtra> getExtras(int i, int i2) {
        THashSet<InteractionWiredExtra> tHashSet;
        synchronized (this.wiredExtras) {
            tHashSet = new THashSet<>();
            for (Map.Entry entry : this.wiredExtras.entrySet()) {
                if (((InteractionWiredExtra) entry.getValue()).getX() == i && ((InteractionWiredExtra) entry.getValue()).getY() == i2) {
                    tHashSet.add((InteractionWiredExtra) entry.getValue());
                }
            }
        }
        return tHashSet;
    }

    public void addExtra(InteractionWiredExtra interactionWiredExtra) {
        synchronized (this.wiredExtras) {
            this.wiredExtras.put(Integer.valueOf(interactionWiredExtra.getId()), interactionWiredExtra);
        }
    }

    public void removeExtra(InteractionWiredExtra interactionWiredExtra) {
        synchronized (this.wiredExtras) {
            this.wiredExtras.remove(Integer.valueOf(interactionWiredExtra.getId()));
        }
    }

    public boolean hasExtraType(short s, short s2, Class<? extends InteractionWiredExtra> cls) {
        synchronized (this.wiredExtras) {
            for (Map.Entry entry : this.wiredExtras.entrySet()) {
                if (((InteractionWiredExtra) entry.getValue()).getX() == s && ((InteractionWiredExtra) entry.getValue()).getY() == s2 && ((InteractionWiredExtra) entry.getValue()).getClass().isAssignableFrom(cls)) {
                    return true;
                }
            }
            return false;
        }
    }

    public InteractionGameScoreboard getGameScorebord(int i) {
        return (InteractionGameScoreboard) this.gameScoreboards.get(Integer.valueOf(i));
    }

    public void addGameScoreboard(InteractionGameScoreboard interactionGameScoreboard) {
        this.gameScoreboards.put(Integer.valueOf(interactionGameScoreboard.getId()), interactionGameScoreboard);
    }

    public void removeScoreboard(InteractionGameScoreboard interactionGameScoreboard) {
        this.gameScoreboards.remove(Integer.valueOf(interactionGameScoreboard.getId()));
    }

    public THashMap<Integer, InteractionFreezeScoreboard> getFreezeScoreboards() {
        THashMap<Integer, InteractionFreezeScoreboard> tHashMap;
        synchronized (this.gameScoreboards) {
            tHashMap = new THashMap<>();
            for (Map.Entry entry : this.gameScoreboards.entrySet()) {
                if (entry.getValue() instanceof InteractionFreezeScoreboard) {
                    tHashMap.put(Integer.valueOf(((InteractionGameScoreboard) entry.getValue()).getId()), (InteractionFreezeScoreboard) entry.getValue());
                }
            }
        }
        return tHashMap;
    }

    public THashMap<Integer, InteractionFreezeScoreboard> getFreezeScoreboards(GameTeamColors gameTeamColors) {
        THashMap<Integer, InteractionFreezeScoreboard> tHashMap;
        synchronized (this.gameScoreboards) {
            tHashMap = new THashMap<>();
            for (Map.Entry entry : this.gameScoreboards.entrySet()) {
                if ((entry.getValue() instanceof InteractionFreezeScoreboard) && ((InteractionFreezeScoreboard) entry.getValue()).teamColor.equals(gameTeamColors)) {
                    tHashMap.put(Integer.valueOf(((InteractionGameScoreboard) entry.getValue()).getId()), (InteractionFreezeScoreboard) entry.getValue());
                }
            }
        }
        return tHashMap;
    }

    public THashMap<Integer, InteractionBattleBanzaiScoreboard> getBattleBanzaiScoreboards() {
        THashMap<Integer, InteractionBattleBanzaiScoreboard> tHashMap;
        synchronized (this.gameScoreboards) {
            tHashMap = new THashMap<>();
            for (Map.Entry entry : this.gameScoreboards.entrySet()) {
                if (entry.getValue() instanceof InteractionBattleBanzaiScoreboard) {
                    tHashMap.put(Integer.valueOf(((InteractionGameScoreboard) entry.getValue()).getId()), (InteractionBattleBanzaiScoreboard) entry.getValue());
                }
            }
        }
        return tHashMap;
    }

    public THashMap<Integer, InteractionBattleBanzaiScoreboard> getBattleBanzaiScoreboards(GameTeamColors gameTeamColors) {
        THashMap<Integer, InteractionBattleBanzaiScoreboard> tHashMap;
        synchronized (this.gameScoreboards) {
            tHashMap = new THashMap<>();
            for (Map.Entry entry : this.gameScoreboards.entrySet()) {
                if ((entry.getValue() instanceof InteractionBattleBanzaiScoreboard) && ((InteractionBattleBanzaiScoreboard) entry.getValue()).teamColor.equals(gameTeamColors)) {
                    tHashMap.put(Integer.valueOf(((InteractionGameScoreboard) entry.getValue()).getId()), (InteractionBattleBanzaiScoreboard) entry.getValue());
                }
            }
        }
        return tHashMap;
    }

    public THashMap<Integer, InteractionFootballScoreboard> getFootballScoreboards() {
        THashMap<Integer, InteractionFootballScoreboard> tHashMap;
        synchronized (this.gameScoreboards) {
            tHashMap = new THashMap<>();
            for (Map.Entry entry : this.gameScoreboards.entrySet()) {
                if (entry.getValue() instanceof InteractionFootballScoreboard) {
                    tHashMap.put(Integer.valueOf(((InteractionGameScoreboard) entry.getValue()).getId()), (InteractionFootballScoreboard) entry.getValue());
                }
            }
        }
        return tHashMap;
    }

    public THashMap<Integer, InteractionFootballScoreboard> getFootballScoreboards(GameTeamColors gameTeamColors) {
        THashMap<Integer, InteractionFootballScoreboard> tHashMap;
        synchronized (this.gameScoreboards) {
            tHashMap = new THashMap<>();
            for (Map.Entry entry : this.gameScoreboards.entrySet()) {
                if ((entry.getValue() instanceof InteractionFootballScoreboard) && ((InteractionFootballScoreboard) entry.getValue()).teamColor.equals(gameTeamColors)) {
                    tHashMap.put(Integer.valueOf(((InteractionGameScoreboard) entry.getValue()).getId()), (InteractionFootballScoreboard) entry.getValue());
                }
            }
        }
        return tHashMap;
    }

    public InteractionGameGate getGameGate(int i) {
        return (InteractionGameGate) this.gameGates.get(Integer.valueOf(i));
    }

    public void addGameGate(InteractionGameGate interactionGameGate) {
        this.gameGates.put(Integer.valueOf(interactionGameGate.getId()), interactionGameGate);
    }

    public void removeGameGate(InteractionGameGate interactionGameGate) {
        this.gameGates.remove(Integer.valueOf(interactionGameGate.getId()));
    }

    public THashMap<Integer, InteractionFreezeGate> getFreezeGates() {
        THashMap<Integer, InteractionFreezeGate> tHashMap;
        synchronized (this.gameGates) {
            tHashMap = new THashMap<>();
            for (Map.Entry entry : this.gameGates.entrySet()) {
                if (entry.getValue() instanceof InteractionFreezeGate) {
                    tHashMap.put(Integer.valueOf(((InteractionGameGate) entry.getValue()).getId()), (InteractionFreezeGate) entry.getValue());
                }
            }
        }
        return tHashMap;
    }

    public THashMap<Integer, InteractionBattleBanzaiGate> getBattleBanzaiGates() {
        THashMap<Integer, InteractionBattleBanzaiGate> tHashMap;
        synchronized (this.gameGates) {
            tHashMap = new THashMap<>();
            for (Map.Entry entry : this.gameGates.entrySet()) {
                if (entry.getValue() instanceof InteractionBattleBanzaiGate) {
                    tHashMap.put(Integer.valueOf(((InteractionGameGate) entry.getValue()).getId()), (InteractionBattleBanzaiGate) entry.getValue());
                }
            }
        }
        return tHashMap;
    }

    public InteractionGameTimer getGameTimer(int i) {
        return (InteractionGameTimer) this.gameTimers.get(Integer.valueOf(i));
    }

    public void addGameTimer(InteractionGameTimer interactionGameTimer) {
        this.gameTimers.put(Integer.valueOf(interactionGameTimer.getId()), interactionGameTimer);
    }

    public void removeGameTimer(InteractionGameTimer interactionGameTimer) {
        this.gameTimers.remove(Integer.valueOf(interactionGameTimer.getId()));
    }

    public THashMap<Integer, InteractionGameTimer> getGameTimers() {
        return this.gameTimers;
    }

    public InteractionFreezeExitTile getFreezeExitTile() {
        Iterator it = this.freezeExitTile.values().iterator();
        if (it.hasNext()) {
            return (InteractionFreezeExitTile) it.next();
        }
        return null;
    }

    public InteractionFreezeExitTile getRandomFreezeExitTile() {
        InteractionFreezeExitTile interactionFreezeExitTile;
        synchronized (this.freezeExitTile) {
            interactionFreezeExitTile = (InteractionFreezeExitTile) this.freezeExitTile.values().toArray()[Emulator.getRandom().nextInt(this.freezeExitTile.size())];
        }
        return interactionFreezeExitTile;
    }

    public void addFreezeExitTile(InteractionFreezeExitTile interactionFreezeExitTile) {
        this.freezeExitTile.put(Integer.valueOf(interactionFreezeExitTile.getId()), interactionFreezeExitTile);
    }

    public THashMap<Integer, InteractionFreezeExitTile> getFreezeExitTiles() {
        return this.freezeExitTile;
    }

    public void removeFreezeExitTile(InteractionFreezeExitTile interactionFreezeExitTile) {
        this.freezeExitTile.remove(Integer.valueOf(interactionFreezeExitTile.getId()));
    }

    public boolean hasFreezeExitTile() {
        return !this.freezeExitTile.isEmpty();
    }

    public void addUndefined(HabboItem habboItem) {
        synchronized (this.undefined) {
            this.undefined.put(Integer.valueOf(habboItem.getId()), habboItem);
        }
    }

    public void removeUndefined(HabboItem habboItem) {
        synchronized (this.undefined) {
            this.undefined.remove(Integer.valueOf(habboItem.getId()));
        }
    }

    public THashSet<HabboItem> getItemsOfType(Class<? extends HabboItem> cls) {
        THashSet<HabboItem> tHashSet = new THashSet<>();
        synchronized (this.undefined) {
            for (HabboItem habboItem : this.undefined.values()) {
                if (habboItem.getClass() == cls) {
                    tHashSet.add(habboItem);
                }
            }
        }
        return tHashSet;
    }

    public HabboItem getLowestItemsOfType(Class<? extends HabboItem> cls) {
        HabboItem habboItem = null;
        synchronized (this.undefined) {
            for (HabboItem habboItem2 : this.undefined.values()) {
                if ((habboItem == null || habboItem2.getZ() < habboItem.getZ()) && habboItem2.getClass().isAssignableFrom(cls)) {
                    habboItem = habboItem2;
                }
            }
        }
        return habboItem;
    }

    public THashSet<ICycleable> getCycleTasks() {
        return this.cycleTasks;
    }

    public void addCycleTask(ICycleable iCycleable) {
        this.cycleTasks.add(iCycleable);
    }

    public void removeCycleTask(ICycleable iCycleable) {
        this.cycleTasks.remove(iCycleable);
    }

    public synchronized void dispose() {
        this.banzaiTeleporters.clear();
        this.nests.clear();
        this.petDrinks.clear();
        this.petFoods.clear();
        this.rollers.clear();
        this.wiredTriggers.clear();
        this.wiredEffects.clear();
        this.wiredConditions.clear();
        this.gameScoreboards.clear();
        this.gameGates.clear();
        this.gameTimers.clear();
        this.freezeExitTile.clear();
        this.undefined.clear();
        this.cycleTasks.clear();
    }

    public Rectangle tentAt(RoomTile roomTile) {
        TObjectHashIterator it = getItemsOfType(InteractionTent.class).iterator();
        while (it.hasNext()) {
            HabboItem habboItem = (HabboItem) it.next();
            Rectangle rectangle = RoomLayout.getRectangle(habboItem.getX(), habboItem.getY(), habboItem.getBaseItem().getWidth(), habboItem.getBaseItem().getLength(), habboItem.getRotation());
            if (RoomLayout.tileInSquare(rectangle, roomTile)) {
                return rectangle;
            }
        }
        return null;
    }
}
