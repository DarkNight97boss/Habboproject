package com.eu.habbo.habbohotel.hotelview;

import com.eu.habbo.Emulator;
import gnu.trove.map.hash.THashMap;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HallOfFame {
   private static final Logger LOGGER = LoggerFactory.getLogger(HallOfFame.class);
   private final THashMap<Integer, HallOfFameWinner> winners = new THashMap();
   private String competitionName;

   public HallOfFame() {
      this.setCompetitionName("xmasRoomComp");
      this.reload();
   }

   public void reload() {
      this.winners.clear();
      synchronized (this.winners) {
         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               Statement statement = connection.createStatement();

               try {
                  ResultSet set = statement.executeQuery(Emulator.getConfig().getValue("hotelview.halloffame.query"));

                  try {
                     while (set.next()) {
                        HallOfFameWinner winner = new HallOfFameWinner(set);
                        this.winners.put(winner.getId(), winner);
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
   }

   public THashMap<Integer, HallOfFameWinner> getWinners() {
      return this.winners;
   }

   public String getCompetitionName() {
      return this.competitionName;
   }

   void setCompetitionName(String name) {
      this.competitionName = name;
   }
}
