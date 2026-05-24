package com.eu.habbo.habbohotel.wired;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.CatalogItem;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredExtra;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredTriggerReset;
import com.eu.habbo.habbohotel.items.interactions.wired.effects.WiredEffectGiveReward;
import com.eu.habbo.habbohotel.items.interactions.wired.effects.WiredEffectTriggerStacks;
import com.eu.habbo.habbohotel.items.interactions.wired.extra.WiredExtraRandom;
import com.eu.habbo.habbohotel.items.interactions.wired.extra.WiredExtraUnseen;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboBadge;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.users.HabboStats;
import com.eu.habbo.messages.outgoing.catalog.PurchaseOKComposer;
import com.eu.habbo.messages.outgoing.inventory.AddHabboItemComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import com.eu.habbo.messages.outgoing.users.AddUserBadgeComposer;
import com.eu.habbo.messages.outgoing.wired.WiredRewardAlertComposer;
import com.eu.habbo.plugin.events.furniture.wired.WiredConditionFailedEvent;
import com.eu.habbo.plugin.events.furniture.wired.WiredStackExecutedEvent;
import com.eu.habbo.plugin.events.furniture.wired.WiredStackTriggeredEvent;
import com.eu.habbo.plugin.events.users.UserWiredRewardReceived;
import com.google.gson.GsonBuilder;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WiredHandler {
   private static final Logger LOGGER = LoggerFactory.getLogger(WiredHandler.class);
   public static int MAXIMUM_FURNI_SELECTION = 5;
   public static int TELEPORT_DELAY = 500;
   private static GsonBuilder gsonBuilder = null;

   public static boolean handle(WiredTriggerType triggerType, RoomUnit roomUnit, Room room, Object[] stuff) {
      if (triggerType == WiredTriggerType.CUSTOM) {
         return false;
      }

      boolean talked = false;
      if (!Emulator.isReady) {
         return false;
      }

      if (room == null) {
         return false;
      }

      if (!room.isLoaded()) {
         return false;
      }

      if (room.getRoomSpecialTypes() == null) {
         return false;
      }

      THashSet<InteractionWiredTrigger> triggers = room.getRoomSpecialTypes().getTriggers(triggerType);
      if (triggers != null && !triggers.isEmpty()) {
         long millis = System.currentTimeMillis();
         THashSet<InteractionWiredEffect> effectsToExecute = new THashSet();
         List<RoomTile> triggeredTiles = new ArrayList<>();
         TObjectHashIterator var10 = triggers.iterator();

         while (var10.hasNext()) {
            InteractionWiredTrigger trigger = (InteractionWiredTrigger)var10.next();
            RoomTile tile = room.getLayout().getTile(trigger.getX(), trigger.getY());
            if (!triggeredTiles.contains(tile)) {
               THashSet<InteractionWiredEffect> tEffectsToExecute = new THashSet();
               if (handle(trigger, roomUnit, room, stuff, tEffectsToExecute)) {
                  effectsToExecute.addAll(tEffectsToExecute);
                  if (triggerType.equals(WiredTriggerType.SAY_SOMETHING)) {
                     talked = true;
                  }

                  triggeredTiles.add(tile);
               }
            }
         }

         var10 = effectsToExecute.iterator();

         while (var10.hasNext()) {
            InteractionWiredEffect effect = (InteractionWiredEffect)var10.next();
            triggerEffect(effect, roomUnit, room, stuff, millis);
         }

         return talked;
      } else {
         return false;
      }
   }

   public static boolean handleCustomTrigger(Class<? extends InteractionWiredTrigger> triggerType, RoomUnit roomUnit, Room room, Object[] stuff) {
      if (!Emulator.isReady) {
         return false;
      }

      if (room == null) {
         return false;
      }

      if (!room.isLoaded()) {
         return false;
      }

      if (room.getRoomSpecialTypes() == null) {
         return false;
      }

      THashSet<InteractionWiredTrigger> triggers = room.getRoomSpecialTypes().getTriggers(WiredTriggerType.CUSTOM);
      if (triggers != null && !triggers.isEmpty()) {
         long millis = System.currentTimeMillis();
         THashSet<InteractionWiredEffect> effectsToExecute = new THashSet();
         List<RoomTile> triggeredTiles = new ArrayList<>();
         TObjectHashIterator var9 = triggers.iterator();

         while (var9.hasNext()) {
            InteractionWiredTrigger trigger = (InteractionWiredTrigger)var9.next();
            if (trigger.getClass() == triggerType) {
               RoomTile tile = room.getLayout().getTile(trigger.getX(), trigger.getY());
               if (!triggeredTiles.contains(tile)) {
                  THashSet<InteractionWiredEffect> tEffectsToExecute = new THashSet();
                  if (handle(trigger, roomUnit, room, stuff, tEffectsToExecute)) {
                     effectsToExecute.addAll(tEffectsToExecute);
                     triggeredTiles.add(tile);
                  }
               }
            }
         }

         var9 = effectsToExecute.iterator();

         while (var9.hasNext()) {
            InteractionWiredEffect effect = (InteractionWiredEffect)var9.next();
            triggerEffect(effect, roomUnit, room, stuff, millis);
         }

         return effectsToExecute.size() > 0;
      } else {
         return false;
      }
   }

   public static boolean handle(InteractionWiredTrigger trigger, RoomUnit roomUnit, Room room, Object[] stuff) {
      long millis = System.currentTimeMillis();
      THashSet<InteractionWiredEffect> effectsToExecute = new THashSet();
      if (!handle(trigger, roomUnit, room, stuff, effectsToExecute)) {
         return false;
      }

      TObjectHashIterator var7 = effectsToExecute.iterator();

      while (var7.hasNext()) {
         InteractionWiredEffect effect = (InteractionWiredEffect)var7.next();
         triggerEffect(effect, roomUnit, room, stuff, millis);
      }

      return true;
   }

   public static boolean handle(
      InteractionWiredTrigger trigger, RoomUnit roomUnit, Room room, Object[] stuff, THashSet<InteractionWiredEffect> effectsToExecute
   ) {
      long millis = System.currentTimeMillis();
      int roomUnitId = roomUnit != null ? roomUnit.getId() : -1;
      if (Emulator.isReady
         && (
            Emulator.getConfig().getBoolean("wired.custom.enabled", false)
                  && (trigger.canExecute(millis) || roomUnitId > -1)
                  && trigger.userCanExecute(roomUnitId, millis)
               || !Emulator.getConfig().getBoolean("wired.custom.enabled", false) && trigger.canExecute(millis)
         )
         && trigger.execute(roomUnit, room, stuff)) {
         trigger.activateBox(room, roomUnit, millis);
         THashSet<InteractionWiredCondition> conditions = room.getRoomSpecialTypes().getConditions(trigger.getX(), trigger.getY());
         THashSet<InteractionWiredEffect> effects = room.getRoomSpecialTypes().getEffects(trigger.getX(), trigger.getY());
         if (Emulator.getPluginManager().fireEvent(new WiredStackTriggeredEvent(room, roomUnit, trigger, effects, conditions)).isCancelled()) {
            return false;
         }

         if (!conditions.isEmpty()) {
            ArrayList<WiredConditionType> matchedConditions = new ArrayList<>(conditions.size());
            TObjectHashIterator hasExtraUnseen = conditions.iterator();

            while (hasExtraUnseen.hasNext()) {
               InteractionWiredCondition searchMatched = (InteractionWiredCondition)hasExtraUnseen.next();
               if (!matchedConditions.contains(searchMatched.getType())
                  && searchMatched.operator() == WiredConditionOperator.OR
                  && searchMatched.execute(roomUnit, room, stuff)) {
                  matchedConditions.add(searchMatched.getType());
               }
            }

            hasExtraUnseen = conditions.iterator();

            while (hasExtraUnseen.hasNext()) {
               InteractionWiredCondition condition = (InteractionWiredCondition)hasExtraUnseen.next();
               if ((condition.operator() != WiredConditionOperator.OR || !matchedConditions.contains(condition.getType()))
                  && (condition.operator() != WiredConditionOperator.AND || !condition.execute(roomUnit, room, stuff))
                  && !Emulator.getPluginManager().fireEvent(new WiredConditionFailedEvent(room, roomUnit, trigger, condition)).isCancelled()) {
                  return false;
               }
            }
         }

         trigger.setCooldown(millis);
         boolean hasExtraRandom = room.getRoomSpecialTypes().hasExtraType(trigger.getX(), trigger.getY(), WiredExtraRandom.class);
         boolean hasExtraUnseen = room.getRoomSpecialTypes().hasExtraType(trigger.getX(), trigger.getY(), WiredExtraUnseen.class);
         THashSet<InteractionWiredExtra> extras = room.getRoomSpecialTypes().getExtras(trigger.getX(), trigger.getY());
         TObjectHashIterator effectList = extras.iterator();

         while (effectList.hasNext()) {
            InteractionWiredExtra extra = (InteractionWiredExtra)effectList.next();
            extra.activateBox(room, roomUnit, millis);
         }

         List<InteractionWiredEffect> effectListx = new ArrayList<>(effects);
         if (hasExtraRandom || hasExtraUnseen) {
            Collections.shuffle(effectListx);
         }

         if (hasExtraUnseen) {
            TObjectHashIterator var23 = room.getRoomSpecialTypes().getExtras(trigger.getX(), trigger.getY()).iterator();

            while (var23.hasNext()) {
               InteractionWiredExtra extra = (InteractionWiredExtra)var23.next();
               if (extra instanceof WiredExtraUnseen) {
                  extra.setExtradata(extra.getExtradata().equals("1") ? "0" : "1");
                  InteractionWiredEffect effect = ((WiredExtraUnseen)extra).getUnseenEffect(effectListx);
                  effectsToExecute.add(effect);
                  break;
               }
            }
         } else {
            for (InteractionWiredEffect effect : effectListx) {
               boolean executed = effectsToExecute.add(effect);
               if (hasExtraRandom && executed) {
                  break;
               }
            }
         }

         return !Emulator.getPluginManager().fireEvent(new WiredStackExecutedEvent(room, roomUnit, trigger, effects, conditions)).isCancelled();
      } else {
         return false;
      }
   }

   private static boolean triggerEffect(InteractionWiredEffect effect, RoomUnit roomUnit, Room room, Object[] stuff, long millis) {
      boolean executed = false;
      if (effect != null
         && (
            effect.canExecute(millis)
               || roomUnit != null
                  && effect.requiresTriggeringUser()
                  && Emulator.getConfig().getBoolean("wired.custom.enabled", false)
                  && effect.userCanExecute(roomUnit.getId(), millis)
         )) {
         executed = true;
         if (!effect.requiresTriggeringUser() || roomUnit != null && effect.requiresTriggeringUser()) {
            Emulator.getThreading().run(() -> {
               if (room.isLoaded()) {
                  try {
                     if (!effect.execute(roomUnit, room, stuff)) {
                        return;
                     }

                     effect.setCooldown(millis);
                  } catch (Exception e) {
                     LOGGER.error("Caught exception", e);
                  }

                  effect.activateBox(room, roomUnit, millis);
               }
            }, effect.getDelay() * 500);
         }
      }

      return executed;
   }

   public static GsonBuilder getGsonBuilder() {
      if (gsonBuilder == null) {
         gsonBuilder = new GsonBuilder();
      }

      return gsonBuilder;
   }

   public static boolean executeEffectsAtTiles(THashSet<RoomTile> tiles, RoomUnit roomUnit, Room room, Object[] stuff) {
      TObjectHashIterator var4 = tiles.iterator();

      while (var4.hasNext()) {
         RoomTile tile = (RoomTile)var4.next();
         if (room != null) {
            THashSet<HabboItem> items = room.getItemsAt(tile);
            long millis = room.getCycleTimestamp();
            TObjectHashIterator var9 = items.iterator();

            while (var9.hasNext()) {
               HabboItem item = (HabboItem)var9.next();
               if (item instanceof InteractionWiredEffect && !(item instanceof WiredEffectTriggerStacks)) {
                  triggerEffect((InteractionWiredEffect)item, roomUnit, room, stuff, millis);
                  ((InteractionWiredEffect)item).setCooldown(millis);
               }
            }
         }
      }

      return true;
   }

   public static void dropRewards(int wiredId) {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM wired_rewards_given WHERE wired_item = ?");

            try {
               statement.setInt(1, wiredId);
               statement.execute();
            } catch (Throwable var7) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var8) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var5) {
                  var8.addSuppressed(var5);
               }
            }

            throw var8;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }
   }

   private static void giveReward(Habbo habbo, WiredEffectGiveReward wiredBox, WiredGiveRewardItem reward) {
      if (wiredBox.limit > 0) {
         wiredBox.given++;
      }

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "INSERT INTO wired_rewards_given (wired_item, user_id, reward_id, timestamp) VALUES ( ?, ?, ?, ?)"
            );

            try {
               statement.setInt(1, wiredBox.getId());
               statement.setInt(2, habbo.getHabboInfo().getId());
               statement.setInt(3, reward.id);
               statement.setInt(4, Emulator.getIntUnixTimestamp());
               statement.execute();
            } catch (Throwable var11) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var10) {
                     var11.addSuppressed(var10);
                  }
               }

               throw var11;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var12) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var9) {
                  var12.addSuppressed(var9);
               }
            }

            throw var12;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }

      if (reward.badge) {
         UserWiredRewardReceived rewardReceived = new UserWiredRewardReceived(habbo, wiredBox, "badge", reward.data);
         if (Emulator.getPluginManager().fireEvent(rewardReceived).isCancelled()) {
            return;
         }

         if (rewardReceived.value.isEmpty()) {
            return;
         }

         if (habbo.getInventory().getBadgesComponent().hasBadge(rewardReceived.value)) {
            return;
         }

         HabboBadge badge = new HabboBadge(0, rewardReceived.value, 0, habbo);
         Emulator.getThreading().run(badge);
         habbo.getInventory().getBadgesComponent().addBadge(badge);
         habbo.getClient().sendResponse(new AddUserBadgeComposer(badge));
         habbo.getClient().sendResponse(new WiredRewardAlertComposer(7));
      } else {
         String[] data = reward.data.split("#");
         if (data.length == 2) {
            UserWiredRewardReceived rewardReceived = new UserWiredRewardReceived(habbo, wiredBox, data[0], data[1]);
            if (Emulator.getPluginManager().fireEvent(rewardReceived).isCancelled()) {
               return;
            }

            if (rewardReceived.value.isEmpty()) {
               return;
            }

            if (rewardReceived.type.equalsIgnoreCase("credits")) {
               int credits = Integer.valueOf(rewardReceived.value);
               habbo.giveCredits(credits);
            } else if (rewardReceived.type.equalsIgnoreCase("pixels")) {
               int pixels = Integer.valueOf(rewardReceived.value);
               habbo.givePixels(pixels);
            } else if (rewardReceived.type.startsWith("points")) {
               int points = Integer.valueOf(rewardReceived.value);
               int type = 5;

               try {
                  type = Integer.valueOf(rewardReceived.type.replace("points", ""));
               } catch (Exception var8) {
               }

               habbo.givePoints(type, points);
            } else if (rewardReceived.type.equalsIgnoreCase("furni")) {
               Item baseItem = Emulator.getGameEnvironment().getItemManager().getItem(Integer.valueOf(rewardReceived.value));
               if (baseItem != null) {
                  HabboItem item = Emulator.getGameEnvironment().getItemManager().createItem(habbo.getHabboInfo().getId(), baseItem, 0, 0, "");
                  if (item != null) {
                     habbo.getClient().sendResponse(new AddHabboItemComposer(item));
                     habbo.getClient().getHabbo().getInventory().getItemsComponent().addItem(item);
                     habbo.getClient().sendResponse(new PurchaseOKComposer(null));
                     habbo.getClient().sendResponse(new InventoryRefreshComposer());
                     habbo.getClient().sendResponse(new WiredRewardAlertComposer(6));
                  }
               }
            } else if (rewardReceived.type.equalsIgnoreCase("respect")) {
               HabboStats var10000 = habbo.getHabboStats();
               var10000.respectPointsReceived = var10000.respectPointsReceived + Integer.valueOf(rewardReceived.value);
            } else if (rewardReceived.type.equalsIgnoreCase("cata")) {
               CatalogItem item = Emulator.getGameEnvironment().getCatalogManager().getCatalogItem(Integer.valueOf(rewardReceived.value));
               if (item != null) {
                  Emulator.getGameEnvironment().getCatalogManager().purchaseItem(null, item, habbo, 1, "", true);
               }

               habbo.getClient().sendResponse(new WiredRewardAlertComposer(6));
            }
         }
      }
   }

   public static boolean getReward(Habbo habbo, WiredEffectGiveReward wiredBox) {
      if (wiredBox.limit > 0 && wiredBox.limit - wiredBox.given == 0) {
         habbo.getClient().sendResponse(new WiredRewardAlertComposer(0));
         return false;
      }

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         boolean var9;
         label311: {
            boolean item;
            label312: {
               int randomNumber;
               label313: {
                  label314: {
                     label315: {
                        label316: {
                           label338: {
                              try {
                                 PreparedStatement statement;
                                 label318: {
                                    label319: {
                                       label320: {
                                          label321: {
                                             label322: {
                                                label323: {
                                                   label324: {
                                                      statement = connection.prepareStatement(
                                                         "SELECT COUNT(*) as row_count, wired_rewards_given.* FROM wired_rewards_given WHERE user_id = ? AND wired_item = ? ORDER BY timestamp DESC LIMIT ?",
                                                         1004,
                                                         1007
                                                      );

                                                      try {
                                                         label325: {
                                                            statement.setInt(1, habbo.getHabboInfo().getId());
                                                            statement.setInt(2, wiredBox.getId());
                                                            statement.setInt(3, wiredBox.rewardItems.size());
                                                            ResultSet set = statement.executeQuery();

                                                            label279: {
                                                               label278: {
                                                                  label277: {
                                                                     label276: {
                                                                        label275: {
                                                                           label274: {
                                                                              label273: {
                                                                                 try {
                                                                                    if (set.first()) {
                                                                                       if (set.getInt("row_count") >= 1 && wiredBox.rewardTime == 0) {
                                                                                          habbo.getClient().sendResponse(new WiredRewardAlertComposer(1));
                                                                                          randomNumber = 0;
                                                                                          break label273;
                                                                                       }

                                                                                       set.beforeFirst();
                                                                                       if (set.next()) {
                                                                                          if (wiredBox.rewardTime == 3
                                                                                             && Emulator.getIntUnixTimestamp() - set.getInt("timestamp") <= 60) {
                                                                                             habbo.getClient().sendResponse(new WiredRewardAlertComposer(8));
                                                                                             randomNumber = 0;
                                                                                             break label274;
                                                                                          }

                                                                                          if (wiredBox.uniqueRewards
                                                                                             && set.getInt("row_count") == wiredBox.rewardItems.size()) {
                                                                                             habbo.getClient().sendResponse(new WiredRewardAlertComposer(5));
                                                                                             randomNumber = 0;
                                                                                             break label275;
                                                                                          }

                                                                                          if (wiredBox.rewardTime == 2
                                                                                             && Emulator.getIntUnixTimestamp() - set.getInt("timestamp")
                                                                                                < 3600 * wiredBox.limitationInterval) {
                                                                                             habbo.getClient().sendResponse(new WiredRewardAlertComposer(3));
                                                                                             randomNumber = 0;
                                                                                             break label276;
                                                                                          }

                                                                                          if (wiredBox.rewardTime == 1
                                                                                             && Emulator.getIntUnixTimestamp() - set.getInt("timestamp")
                                                                                                < 86400 * wiredBox.limitationInterval) {
                                                                                             habbo.getClient().sendResponse(new WiredRewardAlertComposer(2));
                                                                                             randomNumber = 0;
                                                                                             break label277;
                                                                                          }
                                                                                       }

                                                                                       if (!wiredBox.uniqueRewards) {
                                                                                          randomNumber = Emulator.getRandom().nextInt(101);
                                                                                          int count = 0;
                                                                                          TObjectHashIterator var24 = wiredBox.rewardItems.iterator();

                                                                                          while (var24.hasNext()) {
                                                                                             WiredGiveRewardItem itemx = (WiredGiveRewardItem)var24.next();
                                                                                             if (randomNumber >= count
                                                                                                && randomNumber <= count + itemx.probability) {
                                                                                                giveReward(habbo, wiredBox, itemx);
                                                                                                var9 = true;
                                                                                                break label279;
                                                                                             }

                                                                                             count += itemx.probability;
                                                                                          }
                                                                                       } else {
                                                                                          TObjectHashIterator randomNumberx = wiredBox.rewardItems.iterator();

                                                                                          while (randomNumberx.hasNext()) {
                                                                                             WiredGiveRewardItem itemx = (WiredGiveRewardItem)randomNumberx.next();
                                                                                             set.beforeFirst();
                                                                                             boolean found = false;

                                                                                             while (set.next()) {
                                                                                                if (set.getInt("reward_id") == itemx.id) {
                                                                                                   found = true;
                                                                                                }
                                                                                             }

                                                                                             if (!found) {
                                                                                                giveReward(habbo, wiredBox, itemx);
                                                                                                item = true;
                                                                                                break label278;
                                                                                             }
                                                                                          }
                                                                                       }
                                                                                    }
                                                                                 } catch (Throwable var13) {
                                                                                    if (set != null) {
                                                                                       try {
                                                                                          set.close();
                                                                                       } catch (Throwable var12) {
                                                                                          var13.addSuppressed(var12);
                                                                                       }
                                                                                    }

                                                                                    throw var13;
                                                                                 }

                                                                                 if (set != null) {
                                                                                    set.close();
                                                                                 }
                                                                                 break label318;
                                                                              }

                                                                              if (set != null) {
                                                                                 set.close();
                                                                              }
                                                                              break label325;
                                                                           }

                                                                           if (set != null) {
                                                                              set.close();
                                                                           }
                                                                           break label324;
                                                                        }

                                                                        if (set != null) {
                                                                           set.close();
                                                                        }
                                                                        break label323;
                                                                     }

                                                                     if (set != null) {
                                                                        set.close();
                                                                     }
                                                                     break label322;
                                                                  }

                                                                  if (set != null) {
                                                                     set.close();
                                                                  }
                                                                  break label321;
                                                               }

                                                               if (set != null) {
                                                                  set.close();
                                                               }
                                                               break label320;
                                                            }

                                                            if (set != null) {
                                                               set.close();
                                                            }
                                                            break label319;
                                                         }
                                                      } catch (Throwable var14) {
                                                         if (statement != null) {
                                                            try {
                                                               statement.close();
                                                            } catch (Throwable var11) {
                                                               var14.addSuppressed(var11);
                                                            }
                                                         }

                                                         throw var14;
                                                      }

                                                      if (statement != null) {
                                                         statement.close();
                                                      }
                                                      break label338;
                                                   }

                                                   if (statement != null) {
                                                      statement.close();
                                                   }
                                                   break label316;
                                                }

                                                if (statement != null) {
                                                   statement.close();
                                                }
                                                break label315;
                                             }

                                             if (statement != null) {
                                                statement.close();
                                             }
                                             break label314;
                                          }

                                          if (statement != null) {
                                             statement.close();
                                          }
                                          break label313;
                                       }

                                       if (statement != null) {
                                          statement.close();
                                       }
                                       break label312;
                                    }

                                    if (statement != null) {
                                       statement.close();
                                    }
                                    break label311;
                                 }

                                 if (statement != null) {
                                    statement.close();
                                 }
                              } catch (Throwable var15) {
                                 if (connection != null) {
                                    try {
                                       connection.close();
                                    } catch (Throwable var10) {
                                       var15.addSuppressed(var10);
                                    }
                                 }

                                 throw var15;
                              }

                              if (connection != null) {
                                 connection.close();
                              }

                              return false;
                           }

                           if (connection != null) {
                              connection.close();
                           }

                           return randomNumber != 0;
                        }

                        if (connection != null) {
                           connection.close();
                        }

                        return randomNumber != 0;
                     }

                     if (connection != null) {
                        connection.close();
                     }

                     return randomNumber != 0;
                  }

                  if (connection != null) {
                     connection.close();
                  }

                  return randomNumber != 0;
               }

               if (connection != null) {
                  connection.close();
               }

               return randomNumber != 0;
            }

            if (connection != null) {
               connection.close();
            }

            return item;
         }

         if (connection != null) {
            connection.close();
         }

         return var9;
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
         return false;
      }
   }

   public static void resetTimers(Room room) {
      if (room.isLoaded() && room.getRoomSpecialTypes() != null) {
         room.getRoomSpecialTypes()
            .getTriggers()
            .forEach(
               t -> {
                  if (t != null) {
                     if (t.getType() == WiredTriggerType.AT_GIVEN_TIME
                        || t.getType() == WiredTriggerType.PERIODICALLY
                        || t.getType() == WiredTriggerType.PERIODICALLY_LONG) {
                        ((WiredTriggerReset)t).resetTimer();
                     }
                  }
               }
            );
         room.setLastTimerReset(Emulator.getIntUnixTimestamp());
      }
   }
}
