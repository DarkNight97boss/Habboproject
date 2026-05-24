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
import java.util.List;
import java.util.Map.Entry;

public class RoomSpecialTypes {
   private final THashMap<Integer, InteractionBattleBanzaiTeleporter> banzaiTeleporters = new THashMap(0);
   private final THashMap<Integer, InteractionNest> nests = new THashMap(0);
   private final THashMap<Integer, InteractionPetDrink> petDrinks = new THashMap(0);
   private final THashMap<Integer, InteractionPetFood> petFoods = new THashMap(0);
   private final THashMap<Integer, InteractionPetToy> petToys = new THashMap(0);
   private final THashMap<Integer, InteractionRoller> rollers = new THashMap(0);
   private final THashMap<WiredTriggerType, THashSet<InteractionWiredTrigger>> wiredTriggers = new THashMap(0);
   private final THashMap<WiredEffectType, THashSet<InteractionWiredEffect>> wiredEffects = new THashMap(0);
   private final THashMap<WiredConditionType, THashSet<InteractionWiredCondition>> wiredConditions = new THashMap(0);
   private final THashMap<Integer, InteractionWiredExtra> wiredExtras = new THashMap(0);
   private final THashMap<Integer, InteractionGameScoreboard> gameScoreboards = new THashMap(0);
   private final THashMap<Integer, InteractionGameGate> gameGates = new THashMap(0);
   private final THashMap<Integer, InteractionGameTimer> gameTimers = new THashMap(0);
   private final THashMap<Integer, InteractionFreezeExitTile> freezeExitTile = new THashMap(0);
   private final THashMap<Integer, HabboItem> undefined = new THashMap(0);
   private final THashSet<ICycleable> cycleTasks = new THashSet(0);

   public InteractionBattleBanzaiTeleporter getBanzaiTeleporter(int itemId) {
      return (InteractionBattleBanzaiTeleporter)this.banzaiTeleporters.get(itemId);
   }

   public void addBanzaiTeleporter(InteractionBattleBanzaiTeleporter item) {
      this.banzaiTeleporters.put(item.getId(), item);
   }

   public void removeBanzaiTeleporter(InteractionBattleBanzaiTeleporter item) {
      this.banzaiTeleporters.remove(item.getId());
   }

   public THashSet<InteractionBattleBanzaiTeleporter> getBanzaiTeleporters() {
      synchronized (this.banzaiTeleporters) {
         THashSet<InteractionBattleBanzaiTeleporter> battleBanzaiTeleporters = new THashSet();
         battleBanzaiTeleporters.addAll(this.banzaiTeleporters.values());
         return battleBanzaiTeleporters;
      }
   }

   public InteractionBattleBanzaiTeleporter getRandomTeleporter(Item baseItem, InteractionBattleBanzaiTeleporter exclude) {
      List<InteractionBattleBanzaiTeleporter> teleporterList = new ArrayList<>();

      for (InteractionBattleBanzaiTeleporter teleporter : this.banzaiTeleporters.values()) {
         if (baseItem == null || teleporter.getBaseItem() == baseItem) {
            teleporterList.add(teleporter);
         }
      }

      teleporterList.remove(exclude);
      if (!teleporterList.isEmpty()) {
         Collections.shuffle(teleporterList);
         return teleporterList.get(0);
      } else {
         return null;
      }
   }

   public InteractionNest getNest(int itemId) {
      return (InteractionNest)this.nests.get(itemId);
   }

   public void addNest(InteractionNest item) {
      this.nests.put(item.getId(), item);
   }

   public void removeNest(InteractionNest item) {
      this.nests.remove(item.getId());
   }

   public THashSet<InteractionNest> getNests() {
      synchronized (this.nests) {
         THashSet<InteractionNest> nests = new THashSet();
         nests.addAll(this.nests.values());
         return nests;
      }
   }

   public InteractionPetDrink getPetDrink(int itemId) {
      return (InteractionPetDrink)this.petDrinks.get(itemId);
   }

   public void addPetDrink(InteractionPetDrink item) {
      this.petDrinks.put(item.getId(), item);
   }

   public void removePetDrink(InteractionPetDrink item) {
      this.petDrinks.remove(item.getId());
   }

   public THashSet<InteractionPetDrink> getPetDrinks() {
      synchronized (this.petDrinks) {
         THashSet<InteractionPetDrink> petDrinks = new THashSet();
         petDrinks.addAll(this.petDrinks.values());
         return petDrinks;
      }
   }

   public InteractionPetFood getPetFood(int itemId) {
      return (InteractionPetFood)this.petFoods.get(itemId);
   }

   public void addPetFood(InteractionPetFood item) {
      this.petFoods.put(item.getId(), item);
   }

   public void removePetFood(InteractionPetFood petFood) {
      this.petFoods.remove(petFood.getId());
   }

   public THashSet<InteractionPetFood> getPetFoods() {
      synchronized (this.petFoods) {
         THashSet<InteractionPetFood> petFoods = new THashSet();
         petFoods.addAll(this.petFoods.values());
         return petFoods;
      }
   }

   public InteractionPetToy getPetToy(int itemId) {
      return (InteractionPetToy)this.petToys.get(itemId);
   }

   public void addPetToy(InteractionPetToy item) {
      this.petToys.put(item.getId(), item);
   }

   public void removePetToy(InteractionPetToy petToy) {
      this.petToys.remove(petToy.getId());
   }

   public THashSet<InteractionPetToy> getPetToys() {
      synchronized (this.petToys) {
         THashSet<InteractionPetToy> petToys = new THashSet();
         petToys.addAll(this.petToys.values());
         return petToys;
      }
   }

   public InteractionRoller getRoller(int itemId) {
      synchronized (this.rollers) {
         return (InteractionRoller)this.rollers.get(itemId);
      }
   }

   public void addRoller(InteractionRoller item) {
      synchronized (this.rollers) {
         this.rollers.put(item.getId(), item);
      }
   }

   public void removeRoller(InteractionRoller roller) {
      synchronized (this.rollers) {
         this.rollers.remove(roller.getId());
      }
   }

   public THashMap<Integer, InteractionRoller> getRollers() {
      return this.rollers;
   }

   public InteractionWiredTrigger getTrigger(int itemId) {
      synchronized (this.wiredTriggers) {
         for (Entry<WiredTriggerType, THashSet<InteractionWiredTrigger>> map : this.wiredTriggers.entrySet()) {
            TObjectHashIterator var5 = map.getValue().iterator();

            while (var5.hasNext()) {
               InteractionWiredTrigger trigger = (InteractionWiredTrigger)var5.next();
               if (trigger.getId() == itemId) {
                  return trigger;
               }
            }
         }

         return null;
      }
   }

   public THashSet<InteractionWiredTrigger> getTriggers() {
      synchronized (this.wiredTriggers) {
         THashSet<InteractionWiredTrigger> triggers = new THashSet();

         for (Entry<WiredTriggerType, THashSet<InteractionWiredTrigger>> map : this.wiredTriggers.entrySet()) {
            triggers.addAll((Collection)map.getValue());
         }

         return triggers;
      }
   }

   public THashSet<InteractionWiredTrigger> getTriggers(WiredTriggerType type) {
      return (THashSet<InteractionWiredTrigger>)this.wiredTriggers.get(type);
   }

   public THashSet<InteractionWiredTrigger> getTriggers(int x, int y) {
      synchronized (this.wiredTriggers) {
         THashSet<InteractionWiredTrigger> triggers = new THashSet();

         for (Entry<WiredTriggerType, THashSet<InteractionWiredTrigger>> map : this.wiredTriggers.entrySet()) {
            TObjectHashIterator var7 = map.getValue().iterator();

            while (var7.hasNext()) {
               InteractionWiredTrigger trigger = (InteractionWiredTrigger)var7.next();
               if (trigger.getX() == x && trigger.getY() == y) {
                  triggers.add(trigger);
               }
            }
         }

         return triggers;
      }
   }

   public void addTrigger(InteractionWiredTrigger trigger) {
      synchronized (this.wiredTriggers) {
         if (!this.wiredTriggers.containsKey(trigger.getType())) {
            this.wiredTriggers.put(trigger.getType(), new THashSet());
         }

         ((THashSet)this.wiredTriggers.get(trigger.getType())).add(trigger);
      }
   }

   public void removeTrigger(InteractionWiredTrigger trigger) {
      synchronized (this.wiredTriggers) {
         ((THashSet)this.wiredTriggers.get(trigger.getType())).remove(trigger);
         if (((THashSet)this.wiredTriggers.get(trigger.getType())).isEmpty()) {
            this.wiredTriggers.remove(trigger.getType());
         }
      }
   }

   public InteractionWiredEffect getEffect(int itemId) {
      synchronized (this.wiredEffects) {
         for (Entry<WiredEffectType, THashSet<InteractionWiredEffect>> map : this.wiredEffects.entrySet()) {
            TObjectHashIterator var5 = map.getValue().iterator();

            while (var5.hasNext()) {
               InteractionWiredEffect effect = (InteractionWiredEffect)var5.next();
               if (effect.getId() == itemId) {
                  return effect;
               }
            }
         }

         return null;
      }
   }

   public THashSet<InteractionWiredEffect> getEffects() {
      synchronized (this.wiredEffects) {
         THashSet<InteractionWiredEffect> effects = new THashSet();

         for (Entry<WiredEffectType, THashSet<InteractionWiredEffect>> map : this.wiredEffects.entrySet()) {
            effects.addAll((Collection)map.getValue());
         }

         return effects;
      }
   }

   public THashSet<InteractionWiredEffect> getEffects(WiredEffectType type) {
      return (THashSet<InteractionWiredEffect>)this.wiredEffects.get(type);
   }

   public THashSet<InteractionWiredEffect> getEffects(int x, int y) {
      synchronized (this.wiredEffects) {
         THashSet<InteractionWiredEffect> effects = new THashSet();

         for (Entry<WiredEffectType, THashSet<InteractionWiredEffect>> map : this.wiredEffects.entrySet()) {
            TObjectHashIterator var7 = map.getValue().iterator();

            while (var7.hasNext()) {
               InteractionWiredEffect effect = (InteractionWiredEffect)var7.next();
               if (effect.getX() == x && effect.getY() == y) {
                  effects.add(effect);
               }
            }
         }

         return effects;
      }
   }

   public void addEffect(InteractionWiredEffect effect) {
      synchronized (this.wiredEffects) {
         if (!this.wiredEffects.containsKey(effect.getType())) {
            this.wiredEffects.put(effect.getType(), new THashSet());
         }

         ((THashSet)this.wiredEffects.get(effect.getType())).add(effect);
      }
   }

   public void removeEffect(InteractionWiredEffect effect) {
      synchronized (this.wiredEffects) {
         ((THashSet)this.wiredEffects.get(effect.getType())).remove(effect);
         if (((THashSet)this.wiredEffects.get(effect.getType())).isEmpty()) {
            this.wiredEffects.remove(effect.getType());
         }
      }
   }

   public InteractionWiredCondition getCondition(int itemId) {
      synchronized (this.wiredConditions) {
         for (Entry<WiredConditionType, THashSet<InteractionWiredCondition>> map : this.wiredConditions.entrySet()) {
            TObjectHashIterator var5 = map.getValue().iterator();

            while (var5.hasNext()) {
               InteractionWiredCondition condition = (InteractionWiredCondition)var5.next();
               if (condition.getId() == itemId) {
                  return condition;
               }
            }
         }

         return null;
      }
   }

   public THashSet<InteractionWiredCondition> getConditions() {
      synchronized (this.wiredConditions) {
         THashSet<InteractionWiredCondition> conditions = new THashSet();

         for (Entry<WiredConditionType, THashSet<InteractionWiredCondition>> map : this.wiredConditions.entrySet()) {
            conditions.addAll((Collection)map.getValue());
         }

         return conditions;
      }
   }

   public THashSet<InteractionWiredCondition> getConditions(WiredConditionType type) {
      synchronized (this.wiredConditions) {
         return (THashSet<InteractionWiredCondition>)this.wiredConditions.get(type);
      }
   }

   public THashSet<InteractionWiredCondition> getConditions(int x, int y) {
      synchronized (this.wiredConditions) {
         THashSet<InteractionWiredCondition> conditions = new THashSet();

         for (Entry<WiredConditionType, THashSet<InteractionWiredCondition>> map : this.wiredConditions.entrySet()) {
            TObjectHashIterator var7 = map.getValue().iterator();

            while (var7.hasNext()) {
               InteractionWiredCondition condition = (InteractionWiredCondition)var7.next();
               if (condition.getX() == x && condition.getY() == y) {
                  conditions.add(condition);
               }
            }
         }

         return conditions;
      }
   }

   public void addCondition(InteractionWiredCondition condition) {
      synchronized (this.wiredConditions) {
         if (!this.wiredConditions.containsKey(condition.getType())) {
            this.wiredConditions.put(condition.getType(), new THashSet());
         }

         ((THashSet)this.wiredConditions.get(condition.getType())).add(condition);
      }
   }

   public void removeCondition(InteractionWiredCondition condition) {
      synchronized (this.wiredConditions) {
         ((THashSet)this.wiredConditions.get(condition.getType())).remove(condition);
         if (((THashSet)this.wiredConditions.get(condition.getType())).isEmpty()) {
            this.wiredConditions.remove(condition.getType());
         }
      }
   }

   public THashSet<InteractionWiredExtra> getExtras() {
      synchronized (this.wiredExtras) {
         THashSet<InteractionWiredExtra> conditions = new THashSet();

         for (Entry<Integer, InteractionWiredExtra> map : this.wiredExtras.entrySet()) {
            conditions.add(map.getValue());
         }

         return conditions;
      }
   }

   public THashSet<InteractionWiredExtra> getExtras(int x, int y) {
      synchronized (this.wiredExtras) {
         THashSet<InteractionWiredExtra> extras = new THashSet();

         for (Entry<Integer, InteractionWiredExtra> map : this.wiredExtras.entrySet()) {
            if (map.getValue().getX() == x && map.getValue().getY() == y) {
               extras.add(map.getValue());
            }
         }

         return extras;
      }
   }

   public void addExtra(InteractionWiredExtra extra) {
      synchronized (this.wiredExtras) {
         this.wiredExtras.put(extra.getId(), extra);
      }
   }

   public void removeExtra(InteractionWiredExtra extra) {
      synchronized (this.wiredExtras) {
         this.wiredExtras.remove(extra.getId());
      }
   }

   public boolean hasExtraType(short x, short y, Class<? extends InteractionWiredExtra> type) {
      synchronized (this.wiredExtras) {
         for (Entry<Integer, InteractionWiredExtra> map : this.wiredExtras.entrySet()) {
            if (map.getValue().getX() == x && map.getValue().getY() == y && map.getValue().getClass().isAssignableFrom(type)) {
               return true;
            }
         }

         return false;
      }
   }

   public InteractionGameScoreboard getGameScorebord(int itemId) {
      return (InteractionGameScoreboard)this.gameScoreboards.get(itemId);
   }

   public void addGameScoreboard(InteractionGameScoreboard scoreboard) {
      this.gameScoreboards.put(scoreboard.getId(), scoreboard);
   }

   public void removeScoreboard(InteractionGameScoreboard scoreboard) {
      this.gameScoreboards.remove(scoreboard.getId());
   }

   public THashMap<Integer, InteractionFreezeScoreboard> getFreezeScoreboards() {
      synchronized (this.gameScoreboards) {
         THashMap<Integer, InteractionFreezeScoreboard> boards = new THashMap();

         for (Entry<Integer, InteractionGameScoreboard> set : this.gameScoreboards.entrySet()) {
            if (set.getValue() instanceof InteractionFreezeScoreboard) {
               boards.put(set.getValue().getId(), (InteractionFreezeScoreboard)set.getValue());
            }
         }

         return boards;
      }
   }

   public THashMap<Integer, InteractionFreezeScoreboard> getFreezeScoreboards(GameTeamColors teamColor) {
      synchronized (this.gameScoreboards) {
         THashMap<Integer, InteractionFreezeScoreboard> boards = new THashMap();

         for (Entry<Integer, InteractionGameScoreboard> set : this.gameScoreboards.entrySet()) {
            if (set.getValue() instanceof InteractionFreezeScoreboard && ((InteractionFreezeScoreboard)set.getValue()).teamColor.equals(teamColor)) {
               boards.put(set.getValue().getId(), (InteractionFreezeScoreboard)set.getValue());
            }
         }

         return boards;
      }
   }

   public THashMap<Integer, InteractionBattleBanzaiScoreboard> getBattleBanzaiScoreboards() {
      synchronized (this.gameScoreboards) {
         THashMap<Integer, InteractionBattleBanzaiScoreboard> boards = new THashMap();

         for (Entry<Integer, InteractionGameScoreboard> set : this.gameScoreboards.entrySet()) {
            if (set.getValue() instanceof InteractionBattleBanzaiScoreboard) {
               boards.put(set.getValue().getId(), (InteractionBattleBanzaiScoreboard)set.getValue());
            }
         }

         return boards;
      }
   }

   public THashMap<Integer, InteractionBattleBanzaiScoreboard> getBattleBanzaiScoreboards(GameTeamColors teamColor) {
      synchronized (this.gameScoreboards) {
         THashMap<Integer, InteractionBattleBanzaiScoreboard> boards = new THashMap();

         for (Entry<Integer, InteractionGameScoreboard> set : this.gameScoreboards.entrySet()) {
            if (set.getValue() instanceof InteractionBattleBanzaiScoreboard && ((InteractionBattleBanzaiScoreboard)set.getValue()).teamColor.equals(teamColor)) {
               boards.put(set.getValue().getId(), (InteractionBattleBanzaiScoreboard)set.getValue());
            }
         }

         return boards;
      }
   }

   public THashMap<Integer, InteractionFootballScoreboard> getFootballScoreboards() {
      synchronized (this.gameScoreboards) {
         THashMap<Integer, InteractionFootballScoreboard> boards = new THashMap();

         for (Entry<Integer, InteractionGameScoreboard> set : this.gameScoreboards.entrySet()) {
            if (set.getValue() instanceof InteractionFootballScoreboard) {
               boards.put(set.getValue().getId(), (InteractionFootballScoreboard)set.getValue());
            }
         }

         return boards;
      }
   }

   public THashMap<Integer, InteractionFootballScoreboard> getFootballScoreboards(GameTeamColors teamColor) {
      synchronized (this.gameScoreboards) {
         THashMap<Integer, InteractionFootballScoreboard> boards = new THashMap();

         for (Entry<Integer, InteractionGameScoreboard> set : this.gameScoreboards.entrySet()) {
            if (set.getValue() instanceof InteractionFootballScoreboard && ((InteractionFootballScoreboard)set.getValue()).teamColor.equals(teamColor)) {
               boards.put(set.getValue().getId(), (InteractionFootballScoreboard)set.getValue());
            }
         }

         return boards;
      }
   }

   public InteractionGameGate getGameGate(int itemId) {
      return (InteractionGameGate)this.gameGates.get(itemId);
   }

   public void addGameGate(InteractionGameGate gameGate) {
      this.gameGates.put(gameGate.getId(), gameGate);
   }

   public void removeGameGate(InteractionGameGate gameGate) {
      this.gameGates.remove(gameGate.getId());
   }

   public THashMap<Integer, InteractionFreezeGate> getFreezeGates() {
      synchronized (this.gameGates) {
         THashMap<Integer, InteractionFreezeGate> gates = new THashMap();

         for (Entry<Integer, InteractionGameGate> set : this.gameGates.entrySet()) {
            if (set.getValue() instanceof InteractionFreezeGate) {
               gates.put(set.getValue().getId(), (InteractionFreezeGate)set.getValue());
            }
         }

         return gates;
      }
   }

   public THashMap<Integer, InteractionBattleBanzaiGate> getBattleBanzaiGates() {
      synchronized (this.gameGates) {
         THashMap<Integer, InteractionBattleBanzaiGate> gates = new THashMap();

         for (Entry<Integer, InteractionGameGate> set : this.gameGates.entrySet()) {
            if (set.getValue() instanceof InteractionBattleBanzaiGate) {
               gates.put(set.getValue().getId(), (InteractionBattleBanzaiGate)set.getValue());
            }
         }

         return gates;
      }
   }

   public InteractionGameTimer getGameTimer(int itemId) {
      return (InteractionGameTimer)this.gameTimers.get(itemId);
   }

   public void addGameTimer(InteractionGameTimer gameTimer) {
      this.gameTimers.put(gameTimer.getId(), gameTimer);
   }

   public void removeGameTimer(InteractionGameTimer gameTimer) {
      this.gameTimers.remove(gameTimer.getId());
   }

   public THashMap<Integer, InteractionGameTimer> getGameTimers() {
      return this.gameTimers;
   }

   public InteractionFreezeExitTile getFreezeExitTile() {
      Iterator var1 = this.freezeExitTile.values().iterator();
      return var1.hasNext() ? (InteractionFreezeExitTile)var1.next() : null;
   }

   public InteractionFreezeExitTile getRandomFreezeExitTile() {
      synchronized (this.freezeExitTile) {
         return (InteractionFreezeExitTile)this.freezeExitTile.values().toArray()[Emulator.getRandom().nextInt(this.freezeExitTile.size())];
      }
   }

   public void addFreezeExitTile(InteractionFreezeExitTile freezeExitTile) {
      this.freezeExitTile.put(freezeExitTile.getId(), freezeExitTile);
   }

   public THashMap<Integer, InteractionFreezeExitTile> getFreezeExitTiles() {
      return this.freezeExitTile;
   }

   public void removeFreezeExitTile(InteractionFreezeExitTile freezeExitTile) {
      this.freezeExitTile.remove(freezeExitTile.getId());
   }

   public boolean hasFreezeExitTile() {
      return !this.freezeExitTile.isEmpty();
   }

   public void addUndefined(HabboItem item) {
      synchronized (this.undefined) {
         this.undefined.put(item.getId(), item);
      }
   }

   public void removeUndefined(HabboItem item) {
      synchronized (this.undefined) {
         this.undefined.remove(item.getId());
      }
   }

   public THashSet<HabboItem> getItemsOfType(Class<? extends HabboItem> type) {
      THashSet<HabboItem> items = new THashSet();
      synchronized (this.undefined) {
         for (HabboItem item : this.undefined.values()) {
            if (item.getClass() == type) {
               items.add(item);
            }
         }

         return items;
      }
   }

   public HabboItem getLowestItemsOfType(Class<? extends HabboItem> type) {
      HabboItem i = null;
      synchronized (this.undefined) {
         for (HabboItem item : this.undefined.values()) {
            if ((i == null || item.getZ() < i.getZ()) && item.getClass().isAssignableFrom(type)) {
               i = item;
            }
         }

         return i;
      }
   }

   public THashSet<ICycleable> getCycleTasks() {
      return this.cycleTasks;
   }

   public void addCycleTask(ICycleable task) {
      this.cycleTasks.add(task);
   }

   public void removeCycleTask(ICycleable task) {
      this.cycleTasks.remove(task);
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

   public Rectangle tentAt(RoomTile location) {
      TObjectHashIterator var2 = this.getItemsOfType(InteractionTent.class).iterator();

      while (var2.hasNext()) {
         HabboItem item = (HabboItem)var2.next();
         Rectangle rectangle = RoomLayout.getRectangle(
            item.getX(), item.getY(), item.getBaseItem().getWidth(), item.getBaseItem().getLength(), item.getRotation()
         );
         if (RoomLayout.tileInSquare(rectangle, location)) {
            return rectangle;
         }
      }

      return null;
   }
}
