package com.eu.habbo.habbohotel.polls;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import gnu.trove.map.hash.THashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollManager {
   private static final Logger LOGGER = LoggerFactory.getLogger(PollManager.class);
   private final THashMap<Integer, Poll> activePolls = new THashMap();

   public PollManager() {
      this.loadPolls();
   }

   public static boolean donePoll(Habbo habbo, int pollId) {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         boolean var5;
         label109: {
            try {
               PreparedStatement statement;
               label102: {
                  statement = connection.prepareStatement("SELECT NULL FROM polls_answers WHERE poll_id = ? AND user_id = ? LIMIT 1");

                  try {
                     statement.setInt(1, pollId);
                     statement.setInt(2, habbo.getHabboInfo().getId());
                     ResultSet set = statement.executeQuery();

                     label84: {
                        try {
                           if (set.isBeforeFirst()) {
                              var5 = true;
                              break label84;
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
                        break label102;
                     }

                     if (set != null) {
                        set.close();
                     }
                  } catch (Throwable var11) {
                     if (statement != null) {
                        try {
                           statement.close();
                        } catch (Throwable var8) {
                           var11.addSuppressed(var8);
                        }
                     }

                     throw var11;
                  }

                  if (statement != null) {
                     statement.close();
                  }
                  break label109;
               }

               if (statement != null) {
                  statement.close();
               }
            } catch (Throwable var12) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var7) {
                     var12.addSuppressed(var7);
                  }
               }

               throw var12;
            }

            if (connection != null) {
               connection.close();
            }

            return false;
         }

         if (connection != null) {
            connection.close();
         }

         return var5;
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
         return false;
      }
   }

   public void loadPolls() {
      synchronized (this.activePolls) {
         this.activePolls.clear();

         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               Statement statement = connection.createStatement();

               try {
                  ResultSet set = statement.executeQuery("SELECT * FROM polls");

                  try {
                     while (set.next()) {
                        this.activePolls.put(set.getInt("id"), new Poll(set));
                     }
                  } catch (Throwable var14) {
                     if (set != null) {
                        try {
                           set.close();
                        } catch (Throwable var12) {
                           var14.addSuppressed(var12);
                        }
                     }

                     throw var14;
                  }

                  if (set != null) {
                     set.close();
                  }

                  set = statement.executeQuery("SELECT * FROM polls_questions ORDER BY parent_id, `order` ASC");

                  try {
                     while (set.next()) {
                        Poll poll = this.getPoll(set.getInt("poll_id"));
                        if (poll != null) {
                           PollQuestion question = new PollQuestion(set);
                           if (set.getInt("parent_id") <= 0) {
                              poll.addQuestion(question);
                           } else {
                              PollQuestion parentQuestion = poll.getQuestion(set.getInt("parent_id"));
                              if (parentQuestion != null) {
                                 parentQuestion.addSubQuestion(question);
                              }
                           }

                           poll.lastQuestionId = question.id;
                        }
                     }
                  } catch (Throwable var13) {
                     if (set != null) {
                        try {
                           set.close();
                        } catch (Throwable var11) {
                           var13.addSuppressed(var11);
                        }
                     }

                     throw var13;
                  }

                  if (set != null) {
                     set.close();
                  }
               } catch (Throwable var15) {
                  if (statement != null) {
                     try {
                        statement.close();
                     } catch (Throwable var10) {
                        var15.addSuppressed(var10);
                     }
                  }

                  throw var15;
               }

               if (statement != null) {
                  statement.close();
               }
            } catch (Throwable var16) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var9) {
                     var16.addSuppressed(var9);
                  }
               }

               throw var16;
            }

            if (connection != null) {
               connection.close();
            }
         } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
         }
      }
   }

   public Poll getPoll(int pollId) {
      return (Poll)this.activePolls.get(pollId);
   }
}
