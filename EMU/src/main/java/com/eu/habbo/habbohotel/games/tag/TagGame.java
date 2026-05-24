package com.eu.habbo.habbohotel.games.tag;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.games.Game;
import com.eu.habbo.habbohotel.games.GamePlayer;
import com.eu.habbo.habbohotel.games.GameTeam;
import com.eu.habbo.habbohotel.games.GameTeamColors;
import com.eu.habbo.habbohotel.items.interactions.games.tag.InteractionTagField;
import com.eu.habbo.habbohotel.items.interactions.games.tag.InteractionTagPole;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboGender;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.events.roomunit.RoomUnitLookAtPointEvent;
import com.eu.habbo.plugin.events.users.UserTakeStepEvent;
import com.eu.habbo.threading.runnables.HabboItemNewState;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import java.util.Map.Entry;

public abstract class TagGame extends Game {
   public THashMap<Habbo, InteractionTagPole> taggers = new THashMap();

   public TagGame(Class<? extends GameTeam> gameTeamClazz, Class<? extends GamePlayer> gamePlayerClazz, Room room) {
      super(gameTeamClazz, gamePlayerClazz, room, false);
   }

   @EventHandler
   public static void onUserLookAtPoint(RoomUnitLookAtPointEvent event) {
      if (event.room != null && event.roomUnit != null && event.location != null) {
         if (RoomLayout.tilesAdjecent(event.roomUnit.getCurrentLocation(), event.location)) {
            Habbo habbo = event.room.getHabbo(event.roomUnit);
            if (habbo != null && habbo.getHabboInfo().getCurrentGame() != null && TagGame.class.isAssignableFrom(habbo.getHabboInfo().getCurrentGame())) {
               TagGame game = (TagGame)event.room.getGame(habbo.getHabboInfo().getCurrentGame());
               if (game != null && game.isTagger(habbo)) {
                  TObjectHashIterator var3 = event.room.getHabbosAt(event.location).iterator();

                  while (var3.hasNext()) {
                     Habbo tagged = (Habbo)var3.next();
                     if (tagged != habbo
                        && tagged.getHabboInfo().getCurrentGame() != null
                        && tagged.getHabboInfo().getCurrentGame() == habbo.getHabboInfo().getCurrentGame()) {
                        game.tagged(event.room, habbo, tagged);
                        break;
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public static void onUserWalkEvent(UserTakeStepEvent event) {
      if (event.habbo.getHabboInfo().getCurrentGame() != null && TagGame.class.isAssignableFrom(event.habbo.getHabboInfo().getCurrentGame())) {
         THashSet<HabboItem> items = event.habbo.getHabboInfo().getCurrentRoom().getItemsAt(event.toLocation);
         TagGame game = (TagGame)event.habbo.getHabboInfo().getCurrentRoom().getGame(event.habbo.getHabboInfo().getCurrentGame());
         if (game != null) {
            TObjectHashIterator var3 = items.iterator();

            while (var3.hasNext()) {
               HabboItem item = (HabboItem)var3.next();
               if (item instanceof InteractionTagField && ((InteractionTagField)item).gameClazz == event.habbo.getHabboInfo().getCurrentGame()) {
                  if (game.taggers.isEmpty()) {
                     game.tagged(event.habbo.getHabboInfo().getCurrentRoom(), null, event.habbo);
                  }

                  return;
               }
            }

            game.removeHabbo(event.habbo);
         }
      }
   }

   public abstract Class<? extends InteractionTagPole> getTagPole();

   public abstract int getMaleEffect();

   public abstract int getMaleTaggerEffect();

   public abstract int getFemaleEffect();

   public abstract int getFemaleTaggerEffect();

   public void tagged(Room room, Habbo tagger, Habbo tagged) {
      if (!this.taggers.containsKey(tagged)) {
         THashSet<HabboItem> poles = room.getRoomSpecialTypes().getItemsOfType(this.getTagPole());
         InteractionTagPole pole = (InteractionTagPole)this.taggers.get(tagger);
         room.giveEffect(tagged, this.getTaggedEffect(tagged), -1);
         if (poles.size() > this.taggers.size()) {
            for (Entry<Habbo, InteractionTagPole> set : this.taggers.entrySet()) {
               poles.remove(set.getValue());
            }

            TObjectHashIterator var8 = poles.iterator();

            while (var8.hasNext()) {
               HabboItem item = (HabboItem)var8.next();
               tagged.getHabboInfo().getCurrentRoom().giveEffect(tagged, this.getTaggedEffect(tagged), -1);
               this.taggers.put(tagged, (InteractionTagPole)item);
            }
         } else {
            if (tagger != null) {
               room.giveEffect(tagger, this.getEffect(tagger), -1);
               this.taggers.remove(tagger);
            }

            this.taggers.put(tagged, pole);
         }

         if (pole != null) {
            pole.setExtradata("1");
            room.updateItemState(pole);
            Emulator.getThreading().run(new HabboItemNewState(pole, room, "0"), 1000L);
         }
      }
   }

   @Override
   public synchronized boolean addHabbo(Habbo habbo, GameTeamColors teamColor) {
      super.addHabbo(habbo, GameTeamColors.RED);
      if (this.getTagPole() != null) {
         THashSet<HabboItem> poles = habbo.getHabboInfo().getCurrentRoom().getRoomSpecialTypes().getItemsOfType(this.getTagPole());
         if (poles.size() > this.taggers.size()) {
            for (Entry<Habbo, InteractionTagPole> set : this.taggers.entrySet()) {
               poles.remove(set.getValue());
            }

            TObjectHashIterator<HabboItem> iterator = poles.iterator();
            if (iterator.hasNext()) {
               HabboItem item = (HabboItem)iterator.next();
               habbo.getHabboInfo().getCurrentRoom().giveEffect(habbo, this.getEffect(habbo), -1);
               this.room.scheduledTasks.add((Runnable)() -> habbo.getHabboInfo().getCurrentRoom().giveEffect(habbo, this.getTaggedEffect(habbo), -1));
               this.taggers.put(habbo, (InteractionTagPole)item);
               return true;
            }
         }
      } else if (this.taggers.isEmpty()) {
         habbo.getHabboInfo().getCurrentRoom().giveEffect(habbo, this.getEffect(habbo), -1);
         this.room.scheduledTasks.add((Runnable)() -> habbo.getHabboInfo().getCurrentRoom().giveEffect(habbo, this.getTaggedEffect(habbo), -1));
         this.taggers.put(habbo, null);
         return true;
      }

      habbo.getHabboInfo().getCurrentRoom().giveEffect(habbo, this.getEffect(habbo), -1);
      return true;
   }

   @Override
   public synchronized void removeHabbo(Habbo habbo) {
      super.removeHabbo(habbo);
      this.taggers.remove(habbo);
      habbo.getHabboInfo().getCurrentRoom().giveEffect(habbo, 0, -1);
   }

   @Override
   public void initialise() {
   }

   @Override
   public void run() {
   }

   public int getEffect(Habbo habbo) {
      return habbo.getHabboInfo().getGender().equals(HabboGender.M) ? this.getMaleEffect() : this.getFemaleEffect();
   }

   public int getTaggedEffect(Habbo habbo) {
      return habbo.getHabboInfo().getGender().equals(HabboGender.M) ? this.getMaleTaggerEffect() : this.getFemaleTaggerEffect();
   }

   public boolean isTagger(Habbo habbo) {
      return this.taggers.containsKey(habbo);
   }
}
