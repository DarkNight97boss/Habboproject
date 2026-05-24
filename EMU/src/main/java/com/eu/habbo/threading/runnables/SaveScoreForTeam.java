package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.games.Game;
import com.eu.habbo.habbohotel.games.GamePlayer;
import com.eu.habbo.habbohotel.games.GameTeam;
import gnu.trove.iterator.hash.TObjectHashIterator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaveScoreForTeam implements Runnable {
   private static final Logger LOGGER = LoggerFactory.getLogger(SaveScoreForTeam.class);
   public final GameTeam team;
   public final Game game;

   public SaveScoreForTeam(GameTeam team, Game game) {
      this.team = team;
      this.game = game;
   }

   @Override
   public void run() {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "INSERT INTO room_game_scores (room_id, game_start_timestamp, game_name, user_id, team_id, score, team_score) VALUES (?, ?, ?, ?, ?, ?, ?)"
            );

            try {
               TObjectHashIterator var3 = this.team.getMembers().iterator();

               while (var3.hasNext()) {
                  GamePlayer player = (GamePlayer)var3.next();
                  statement.setInt(1, this.game.getRoom().getId());
                  statement.setInt(2, this.game.getStartTime());
                  statement.setString(3, this.game.getClass().getName());
                  statement.setInt(4, player.getHabbo().getHabboInfo().getId());
                  statement.setInt(5, player.getTeamColor().type);
                  statement.setInt(6, player.getScore());
                  statement.setInt(7, this.team.getTeamScore());
                  statement.addBatch();
               }

               statement.executeBatch();
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
}
