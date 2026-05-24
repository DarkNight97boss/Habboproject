package com.eu.habbo.habbohotel.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.inventory.AddHabboItemComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserStatusComposer;
import com.eu.habbo.messages.outgoing.trading.TradeAcceptedComposer;
import com.eu.habbo.messages.outgoing.trading.TradeCloseWindowComposer;
import com.eu.habbo.messages.outgoing.trading.TradeClosedComposer;
import com.eu.habbo.messages.outgoing.trading.TradeCompleteComposer;
import com.eu.habbo.messages.outgoing.trading.TradeStartComposer;
import com.eu.habbo.messages.outgoing.trading.TradeUpdateComposer;
import com.eu.habbo.messages.outgoing.trading.TradingWaitingConfirmComposer;
import com.eu.habbo.plugin.events.trading.TradeConfirmEvent;
import com.eu.habbo.threading.runnables.QueryDeleteHabboItem;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoomTrade {
   private static final Logger LOGGER = LoggerFactory.getLogger(RoomTrade.class);
   public static boolean TRADING_ENABLED = true;
   public static boolean TRADING_REQUIRES_PERK = true;
   private final List<RoomTradeUser> users = new ArrayList<>();
   private final Room room;
   private boolean tradeCompleted = false;

   public RoomTrade(Habbo userOne, Habbo userTwo, Room room) {
      this.users.add(new RoomTradeUser(userOne));
      this.users.add(new RoomTradeUser(userTwo));
      this.room = room;
   }

   public void start() {
      this.initializeTradeStatus();
      this.openTrade();
   }

   protected void initializeTradeStatus() {
      for (RoomTradeUser roomTradeUser : this.users) {
         if (!roomTradeUser.getHabbo().getRoomUnit().hasStatus(RoomUnitStatus.TRADING)) {
            roomTradeUser.getHabbo().getRoomUnit().setStatus(RoomUnitStatus.TRADING, "");
            if (!roomTradeUser.getHabbo().getRoomUnit().isWalking()) {
               this.room.sendComposer(new RoomUserStatusComposer(roomTradeUser.getHabbo().getRoomUnit()).compose());
            }
         }
      }
   }

   protected void openTrade() {
      this.sendMessageToUsers(new TradeStartComposer(this));
   }

   public void offerItem(Habbo habbo, HabboItem item) {
      RoomTradeUser user = this.getRoomTradeUserForHabbo(habbo);
      if (!user.getItems().contains(item)) {
         habbo.getInventory().getItemsComponent().removeHabboItem(item);
         user.getItems().add(item);
         this.clearAccepted();
         this.updateWindow();
      }
   }

   public void offerMultipleItems(Habbo habbo, THashSet<HabboItem> items) {
      RoomTradeUser user = this.getRoomTradeUserForHabbo(habbo);
      TObjectHashIterator var4 = items.iterator();

      while (var4.hasNext()) {
         HabboItem item = (HabboItem)var4.next();
         if (!user.getItems().contains(item)) {
            habbo.getInventory().getItemsComponent().removeHabboItem(item);
            user.getItems().add(item);
         }
      }

      this.clearAccepted();
      this.updateWindow();
   }

   public void removeItem(Habbo habbo, HabboItem item) {
      RoomTradeUser user = this.getRoomTradeUserForHabbo(habbo);
      if (user.getItems().contains(item)) {
         habbo.getInventory().getItemsComponent().addItem(item);
         user.getItems().remove(item);
         this.clearAccepted();
         this.updateWindow();
      }
   }

   public void accept(Habbo habbo, boolean value) {
      RoomTradeUser user = this.getRoomTradeUserForHabbo(habbo);
      user.setAccepted(value);
      this.sendMessageToUsers(new TradeAcceptedComposer(user));
      boolean accepted = true;

      for (RoomTradeUser roomTradeUser : this.users) {
         if (!roomTradeUser.getAccepted()) {
            accepted = false;
         }
      }

      if (accepted) {
         this.sendMessageToUsers(new TradingWaitingConfirmComposer());
      }
   }

   public void confirm(Habbo habbo) {
      RoomTradeUser user = this.getRoomTradeUserForHabbo(habbo);
      user.confirm();
      this.sendMessageToUsers(new TradeAcceptedComposer(user));
      boolean accepted = true;

      for (RoomTradeUser roomTradeUser : this.users) {
         if (!roomTradeUser.getConfirmed()) {
            accepted = false;
         }
      }

      if (accepted) {
         if (this.tradeItems()) {
            this.closeWindow();
            this.sendMessageToUsers(new TradeCompleteComposer());
         }

         this.room.stopTrade(this);
      }
   }

   boolean tradeItems() {
      for (RoomTradeUser roomTradeUser : this.users) {
         TObjectHashIterator tradeConfirmEventRegistered = roomTradeUser.getItems().iterator();

         while (tradeConfirmEventRegistered.hasNext()) {
            HabboItem item = (HabboItem)tradeConfirmEventRegistered.next();
            if (roomTradeUser.getHabbo().getInventory().getItemsComponent().getHabboItem(item.getId()) != null) {
               this.sendMessageToUsers(new TradeClosedComposer(roomTradeUser.getHabbo().getRoomUnit().getId(), 1));
               return false;
            }
         }
      }

      RoomTradeUser userOne = this.users.get(0);
      RoomTradeUser userTwo = this.users.get(1);
      boolean tradeConfirmEventRegistered = Emulator.getPluginManager().isRegistered(TradeConfirmEvent.class, true);
      TradeConfirmEvent tradeConfirmEvent = new TradeConfirmEvent(userOne, userTwo);
      if (tradeConfirmEventRegistered) {
         Emulator.getPluginManager().fireEvent(tradeConfirmEvent);
      }

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            int tradeId = 0;
            boolean logTrades = Emulator.getConfig().getBoolean("hotel.log.trades");
            if (logTrades) {
               PreparedStatement statement = connection.prepareStatement(
                  "INSERT INTO room_trade_log (user_one_id, user_two_id, user_one_ip, user_two_ip, timestamp, user_one_item_count, user_two_item_count) VALUES (?, ?, ?, ?, ?, ?, ?)",
                  1
               );

               try {
                  statement.setInt(1, userOne.getHabbo().getHabboInfo().getId());
                  statement.setInt(2, userTwo.getHabbo().getHabboInfo().getId());
                  statement.setString(3, userOne.getHabbo().getHabboInfo().getIpLogin());
                  statement.setString(4, userTwo.getHabbo().getHabboInfo().getIpLogin());
                  statement.setInt(5, Emulator.getIntUnixTimestamp());
                  statement.setInt(6, userOne.getItems().size());
                  statement.setInt(7, userTwo.getItems().size());
                  statement.executeUpdate();
                  ResultSet generatedKeys = statement.getGeneratedKeys();

                  try {
                     if (generatedKeys.next()) {
                        tradeId = generatedKeys.getInt(1);
                     }
                  } catch (Throwable var19) {
                     if (generatedKeys != null) {
                        try {
                           generatedKeys.close();
                        } catch (Throwable var18) {
                           var19.addSuppressed(var18);
                        }
                     }

                     throw var19;
                  }

                  if (generatedKeys != null) {
                     generatedKeys.close();
                  }
               } catch (Throwable var20) {
                  if (statement != null) {
                     try {
                        statement.close();
                     } catch (Throwable var17) {
                        var20.addSuppressed(var17);
                     }
                  }

                  throw var20;
               }

               if (statement != null) {
                  statement.close();
               }
            }

            int userOneId = userOne.getHabbo().getHabboInfo().getId();
            int userTwoId = userTwo.getHabbo().getHabboInfo().getId();
            PreparedStatement statement = connection.prepareStatement("UPDATE items SET user_id = ? WHERE id = ? LIMIT 1");

            try {
               PreparedStatement stmt = connection.prepareStatement("INSERT INTO room_trade_log_items (id, item_id, user_id) VALUES (?, ?, ?)");

               try {
                  TObjectHashIterator item = userOne.getItems().iterator();

                  while (item.hasNext()) {
                     HabboItem itemx = (HabboItem)item.next();
                     itemx.setUserId(userTwoId);
                     statement.setInt(1, userTwoId);
                     statement.setInt(2, itemx.getId());
                     statement.addBatch();
                     if (logTrades) {
                        stmt.setInt(1, tradeId);
                        stmt.setInt(2, itemx.getId());
                        stmt.setInt(3, userOneId);
                        stmt.addBatch();
                     }
                  }

                  item = userTwo.getItems().iterator();

                  while (item.hasNext()) {
                     HabboItem itemx = (HabboItem)item.next();
                     itemx.setUserId(userOneId);
                     statement.setInt(1, userOneId);
                     statement.setInt(2, itemx.getId());
                     statement.addBatch();
                     if (logTrades) {
                        stmt.setInt(1, tradeId);
                        stmt.setInt(2, itemx.getId());
                        stmt.setInt(3, userTwoId);
                        stmt.addBatch();
                     }
                  }

                  if (logTrades) {
                     stmt.executeBatch();
                  }
               } catch (Throwable var21) {
                  if (stmt != null) {
                     try {
                        stmt.close();
                     } catch (Throwable var16) {
                        var21.addSuppressed(var16);
                     }
                  }

                  throw var21;
               }

               if (stmt != null) {
                  stmt.close();
               }

               statement.executeBatch();
            } catch (Throwable var22) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var15) {
                     var22.addSuppressed(var15);
                  }
               }

               throw var22;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var23) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var14) {
                  var23.addSuppressed(var14);
               }
            }

            throw var23;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }

      THashSet<HabboItem> itemsUserOne = new THashSet(userOne.getItems());
      THashSet<HabboItem> itemsUserTwo = new THashSet(userTwo.getItems());
      userOne.clearItems();
      userTwo.clearItems();
      int creditsForUserTwo = 0;
      THashSet<HabboItem> creditFurniUserOne = new THashSet();
      TObjectHashIterator var35 = itemsUserOne.iterator();

      while (var35.hasNext()) {
         HabboItem item = (HabboItem)var35.next();
         int worth = getCreditsByItem(item);
         if (worth > 0) {
            creditsForUserTwo += worth;
            creditFurniUserOne.add(item);
            new QueryDeleteHabboItem(item).run();
         }
      }

      itemsUserOne.removeAll(creditFurniUserOne);
      int creditsForUserOne = 0;
      THashSet<HabboItem> creditFurniUserTwo = new THashSet();
      TObjectHashIterator var40 = itemsUserTwo.iterator();

      while (var40.hasNext()) {
         HabboItem item = (HabboItem)var40.next();
         int worth = getCreditsByItem(item);
         if (worth > 0) {
            creditsForUserOne += worth;
            creditFurniUserTwo.add(item);
            new QueryDeleteHabboItem(item).run();
         }
      }

      itemsUserTwo.removeAll(creditFurniUserTwo);
      userOne.getHabbo().giveCredits(creditsForUserOne);
      userTwo.getHabbo().giveCredits(creditsForUserTwo);
      userOne.getHabbo().getInventory().getItemsComponent().addItems(itemsUserTwo);
      userTwo.getHabbo().getInventory().getItemsComponent().addItems(itemsUserOne);
      userOne.getHabbo().getClient().sendResponse(new AddHabboItemComposer(itemsUserTwo));
      userTwo.getHabbo().getClient().sendResponse(new AddHabboItemComposer(itemsUserOne));
      userOne.getHabbo().getClient().sendResponse(new InventoryRefreshComposer());
      userTwo.getHabbo().getClient().sendResponse(new InventoryRefreshComposer());
      return true;
   }

   protected void clearAccepted() {
      for (RoomTradeUser user : this.users) {
         user.setAccepted(false);
      }
   }

   protected void updateWindow() {
      this.sendMessageToUsers(new TradeUpdateComposer(this));
   }

   private void returnItems() {
      for (RoomTradeUser user : this.users) {
         user.putItemsIntoInventory();
      }
   }

   private void closeWindow() {
      this.removeStatusses();
      this.sendMessageToUsers(new TradeCloseWindowComposer());
   }

   public void stopTrade(Habbo habbo) {
      this.removeStatusses();
      this.clearAccepted();
      this.returnItems();

      for (RoomTradeUser user : this.users) {
         user.clearItems();
      }

      this.updateWindow();
      this.sendMessageToUsers(new TradeClosedComposer(habbo.getHabboInfo().getId(), 0));
      this.room.stopTrade(this);
   }

   private void removeStatusses() {
      for (RoomTradeUser user : this.users) {
         Habbo habbo = user.getHabbo();
         if (habbo != null) {
            habbo.getRoomUnit().removeStatus(RoomUnitStatus.TRADING);
            this.room.sendComposer(new RoomUserStatusComposer(habbo.getRoomUnit()).compose());
         }
      }
   }

   public RoomTradeUser getRoomTradeUserForHabbo(Habbo habbo) {
      for (RoomTradeUser roomTradeUser : this.users) {
         if (roomTradeUser.getHabbo() == habbo) {
            return roomTradeUser;
         }
      }

      return null;
   }

   public void sendMessageToUsers(MessageComposer message) {
      for (RoomTradeUser roomTradeUser : this.users) {
         roomTradeUser.getHabbo().getClient().sendResponse(message);
      }
   }

   public List<RoomTradeUser> getRoomTradeUsers() {
      return this.users;
   }

   public static int getCreditsByItem(HabboItem item) {
      if (!Emulator.getConfig().getBoolean("redeem.currency.trade")) {
         return 0;
      }

      if (!item.getBaseItem().getName().startsWith("CF_") && !item.getBaseItem().getName().startsWith("CFC_")) {
         return 0;
      }

      try {
         return Integer.valueOf(item.getBaseItem().getName().split("_")[1]);
      } catch (Exception e) {
         return 0;
      }
   }
}
