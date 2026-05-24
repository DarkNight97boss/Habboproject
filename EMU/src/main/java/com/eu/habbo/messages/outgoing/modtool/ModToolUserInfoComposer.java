package com.eu.habbo.messages.outgoing.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ModToolSanctionItem;
import com.eu.habbo.habbohotel.modtool.ModToolSanctionLevelItem;
import com.eu.habbo.habbohotel.modtool.ModToolSanctions;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import gnu.trove.map.hash.THashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModToolUserInfoComposer extends MessageComposer {
   private static final Logger LOGGER = LoggerFactory.getLogger(ModToolUserInfoComposer.class);
   private final ResultSet set;

   public ModToolUserInfoComposer(ResultSet set) {
      this.set = set;
   }

   @Override
   protected ServerMessage composeInternal() {
      this.response.init(2866);

      try {
         int totalBans = 0;

         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) AS amount FROM bans WHERE user_id = ?");

               try {
                  statement.setInt(1, this.set.getInt("user_id"));

                  try {
                     ResultSet set = statement.executeQuery();

                     try {
                        if (set.next()) {
                           totalBans = set.getInt("amount");
                        }
                     } catch (Throwable var10) {
                        if (set != null) {
                           try {
                              set.close();
                           } catch (Throwable var9) {
                              var10.addSuppressed(var9);
                           }
                        }

                        throw var10;
                     }

                     if (set != null) {
                        set.close();
                     }
                  } catch (SQLException e) {
                     LOGGER.error("Caught SQL exception", e);
                  }
               } catch (Throwable var12) {
                  if (statement != null) {
                     try {
                        statement.close();
                     } catch (Throwable var8) {
                        var12.addSuppressed(var8);
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
                  } catch (Throwable var7) {
                     var13.addSuppressed(var7);
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

         this.response.appendInt(this.set.getInt("user_id"));
         this.response.appendString(this.set.getString("username"));
         this.response.appendString(this.set.getString("look"));
         this.response.appendInt((Emulator.getIntUnixTimestamp() - this.set.getInt("account_created")) / 60);
         this.response.appendInt((this.set.getInt("online") == 1 ? 0 : Emulator.getIntUnixTimestamp() - this.set.getInt("last_online")) / 60);
         this.response.appendBoolean(this.set.getInt("online") == 1);
         this.response.appendInt(this.set.getInt("cfh_send"));
         this.response.appendInt(this.set.getInt("cfh_abusive"));
         this.response.appendInt(this.set.getInt("cfh_warnings"));
         this.response.appendInt(totalBans);
         this.response.appendInt(this.set.getInt("tradelock_amount"));
         this.response.appendString("");
         this.response.appendString("");
         this.response.appendInt(this.set.getInt("user_id"));
         this.response.appendInt(0);
         this.response.appendString(this.set.getBoolean("hide_mail") ? "" : this.set.getString("mail"));
         this.response.appendString("Rank (" + this.set.getInt("rank_id") + "): " + this.set.getString("rank_name"));
         ModToolSanctions modToolSanctions = Emulator.getGameEnvironment().getModToolSanctions();
         if (Emulator.getConfig().getBoolean("hotel.sanctions.enabled")) {
            THashMap<Integer, ArrayList<ModToolSanctionItem>> modToolSanctionItemsHashMap = Emulator.getGameEnvironment()
               .getModToolSanctions()
               .getSanctions(this.set.getInt("user_id"));
            ArrayList<ModToolSanctionItem> modToolSanctionItems = (ArrayList<ModToolSanctionItem>)modToolSanctionItemsHashMap.get(this.set.getInt("user_id"));
            if (modToolSanctionItems != null && modToolSanctionItems.size() > 0) {
               ModToolSanctionItem item = modToolSanctionItems.get(modToolSanctionItems.size() - 1);
               ModToolSanctionLevelItem modToolSanctionLevelItem = modToolSanctions.getSanctionLevelItem(item.sanctionLevel);
               this.response.appendString(modToolSanctions.getSanctionType(modToolSanctionLevelItem));
               this.response.appendInt(31);
            }
         }

         return this.response;
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
         return null;
      }
   }
}
