package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.games.Game;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.wired.WiredSaveException;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectProcedure;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

public class WiredEffectGiveScore extends InteractionWiredEffect {
   public static final WiredEffectType type = WiredEffectType.GIVE_SCORE;
   private int score;
   private int count;
   private TObjectIntMap<Entry<Integer, Integer>> data = new TObjectIntHashMap();

   public WiredEffectGiveScore(ResultSet set, Item baseItem) throws SQLException {
      super(set, baseItem);
   }

   public WiredEffectGiveScore(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
      super(id, userId, item, extradata, limitedStack, limitedSells);
   }

   @Override
   public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
      Habbo habbo = room.getHabbo(roomUnit);
      if (habbo != null && habbo.getHabboInfo().getCurrentGame() != null) {
         Game game = room.getGame(habbo.getHabboInfo().getCurrentGame());
         if (game == null) {
            return false;
         }

         int gameStartTime = game.getStartTime();
         TObjectIntMap<Entry<Integer, Integer>> dataClone = new TObjectIntHashMap(this.data);
         TObjectIntIterator<Entry<Integer, Integer>> iterator = dataClone.iterator();
         int i = dataClone.size();

         while (i-- > 0) {
            iterator.advance();
            Entry<Integer, Integer> map = (Entry<Integer, Integer>)iterator.key();
            if (map.getValue() == habbo.getHabboInfo().getId()) {
               if (map.getKey() == gameStartTime) {
                  if (iterator.value() < this.count) {
                     iterator.setValue(iterator.value() + 1);
                     habbo.getHabboInfo().getGamePlayer().addScore(this.score, true);
                     return true;
                  }
               } else {
                  iterator.remove();
               }
            }
         }

         try {
            this.data.put(new SimpleEntry<>(gameStartTime, habbo.getHabboInfo().getId()), 1);
         } catch (IllegalArgumentException var11) {
         }

         if (habbo.getHabboInfo().getGamePlayer() != null) {
            habbo.getHabboInfo().getGamePlayer().addScore(this.score, true);
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public String getWiredData() {
      return WiredHandler.getGsonBuilder().create().toJson(new WiredEffectGiveScore.JsonData(this.score, this.count, this.getDelay()));
   }

   @Override
   public void loadWiredData(ResultSet set, Room room) throws SQLException {
      String wiredData = set.getString("wired_data");
      if (wiredData.startsWith("{")) {
         WiredEffectGiveScore.JsonData data = (WiredEffectGiveScore.JsonData)WiredHandler.getGsonBuilder()
            .create()
            .fromJson(wiredData, WiredEffectGiveScore.JsonData.class);
         this.score = data.score;
         this.count = data.count;
         this.setDelay(data.delay);
      } else {
         String[] data = wiredData.split(";");
         if (data.length == 3) {
            this.score = Integer.valueOf(data[0]);
            this.count = Integer.valueOf(data[1]);
            this.setDelay(Integer.valueOf(data[2]));
         }

         this.needsUpdate(true);
      }
   }

   @Override
   public void onPickUp() {
      this.score = 0;
      this.count = 0;
      this.setDelay(0);
   }

   @Override
   public WiredEffectType getType() {
      return type;
   }

   @Override
   public void serializeWiredData(ServerMessage message, Room room) {
      message.appendBoolean(false);
      message.appendInt(5);
      message.appendInt(0);
      message.appendInt(this.getBaseItem().getSpriteId());
      message.appendInt(this.getId());
      message.appendString("");
      message.appendInt(2);
      message.appendInt(this.score);
      message.appendInt(this.count);
      message.appendInt(0);
      message.appendInt(this.getType().code);
      message.appendInt(this.getDelay());
      if (this.requiresTriggeringUser()) {
         final List<Integer> invalidTriggers = new ArrayList<>();
         room.getRoomSpecialTypes().getTriggers(this.getX(), this.getY()).forEach(new TObjectProcedure<InteractionWiredTrigger>() {
            public boolean execute(InteractionWiredTrigger object) {
               if (!object.isTriggeredByRoomUnit()) {
                  invalidTriggers.add(object.getBaseItem().getSpriteId());
               }

               return true;
            }
         });
         message.appendInt(invalidTriggers.size());

         for (Integer i : invalidTriggers) {
            message.appendInt(i);
         }
      } else {
         message.appendInt(0);
      }
   }

   @Override
   public boolean saveData(WiredSettings settings, GameClient gameClient) throws WiredSaveException {
      if (settings.getIntParams().length < 2) {
         throw new WiredSaveException("Invalid data");
      }

      int score = settings.getIntParams()[0];
      if (score >= 1 && score <= 100) {
         int timesPerGame = settings.getIntParams()[1];
         if (timesPerGame >= 1 && timesPerGame <= 10) {
            int delay = settings.getDelay();
            if (delay > Emulator.getConfig().getInt("hotel.wired.max_delay", 20)) {
               throw new WiredSaveException("Delay too long");
            }

            this.score = score;
            this.count = timesPerGame;
            this.setDelay(delay);
            return true;
         } else {
            throw new WiredSaveException("Times per game is invalid");
         }
      } else {
         throw new WiredSaveException("Score is invalid");
      }
   }

   @Override
   public boolean requiresTriggeringUser() {
      return true;
   }

   static class JsonData {
      int score;
      int count;
      int delay;

      public JsonData(int score, int count, int delay) {
         this.score = score;
         this.count = count;
         this.delay = delay;
      }
   }
}
