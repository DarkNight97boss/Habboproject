package com.eu.habbo.messages.incoming.polls;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.polls.Poll;
import com.eu.habbo.messages.incoming.MessageHandler;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CancelPollEvent extends MessageHandler {
   private static final Logger LOGGER = LoggerFactory.getLogger(CancelPollEvent.class);

   @Override
   public void handle() throws Exception {
      int pollId = this.packet.readInt();
      Poll poll = Emulator.getGameEnvironment().getPollManager().getPoll(pollId);
      if (poll != null) {
         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               PreparedStatement statement = connection.prepareStatement(
                  "INSERT INTO polls_answers (poll_id, user_id, question_id, answer) VALUES (?, ?, ?, ?)"
               );

               try {
                  statement.setInt(1, pollId);
                  statement.setInt(2, this.client.getHabbo().getHabboInfo().getId());
                  statement.setInt(3, 0);
                  statement.setString(4, "");
                  statement.execute();
               } catch (Throwable var9) {
                  if (statement != null) {
                     try {
                        statement.close();
                     } catch (Throwable var8) {
                        var9.addSuppressed(var8);
                     }
                  }

                  throw var9;
               }

               if (statement != null) {
                  statement.close();
               }
            } catch (Throwable var10) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var7) {
                     var10.addSuppressed(var7);
                  }
               }

               throw var10;
            }

            if (connection != null) {
               connection.close();
            }
         } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
         }
      }
   }
}
