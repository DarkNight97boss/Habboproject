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

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/polls/PollManager.class */
public class PollManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PollManager.class);
    private final THashMap<Integer, Poll> activePolls = new THashMap<>();

    public PollManager() {
        loadPolls();
    }

    public static boolean donePoll(Habbo habbo, int i) {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT NULL FROM polls_answers WHERE poll_id = ? AND user_id = ? LIMIT 1");
                try {
                    preparedStatementPrepareStatement.setInt(1, i);
                    preparedStatementPrepareStatement.setInt(2, habbo.getHabboInfo().getId());
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    try {
                        if (resultSetExecuteQuery.isBeforeFirst()) {
                            if (resultSetExecuteQuery != null) {
                                resultSetExecuteQuery.close();
                            }
                            if (preparedStatementPrepareStatement != null) {
                                preparedStatementPrepareStatement.close();
                            }
                            if (connection != null) {
                                connection.close();
                            }
                            return true;
                        }
                        if (resultSetExecuteQuery != null) {
                            resultSetExecuteQuery.close();
                        }
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
                        }
                        if (connection != null) {
                            connection.close();
                        }
                        return false;
                    } catch (Throwable th) {
                        if (resultSetExecuteQuery != null) {
                            try {
                                resultSetExecuteQuery.close();
                            } catch (Throwable th2) {
                                th.addSuppressed(th2);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th4) {
                            th3.addSuppressed(th4);
                        }
                    }
                    throw th3;
                }
            } catch (Throwable th5) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Throwable th6) {
                        th5.addSuppressed(th6);
                    }
                }
                throw th5;
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
            return false;
        }
    }

    public void loadPolls() {
        Connection connection;
        synchronized (this.activePolls) {
            this.activePolls.clear();
            try {
                connection = Emulator.getDatabase().getDataSource().getConnection();
            } catch (SQLException e) {
                LOGGER.error("Caught SQL exception", e);
            }
            try {
                Statement statementCreateStatement = connection.createStatement();
                try {
                    ResultSet resultSetExecuteQuery = statementCreateStatement.executeQuery("SELECT * FROM polls");
                    while (resultSetExecuteQuery.next()) {
                        try {
                            this.activePolls.put(Integer.valueOf(resultSetExecuteQuery.getInt("id")), new Poll(resultSetExecuteQuery));
                        } finally {
                        }
                    }
                    if (resultSetExecuteQuery != null) {
                        resultSetExecuteQuery.close();
                    }
                    resultSetExecuteQuery = statementCreateStatement.executeQuery("SELECT * FROM polls_questions ORDER BY parent_id, `order` ASC");
                    while (resultSetExecuteQuery.next()) {
                        try {
                            Poll poll = getPoll(resultSetExecuteQuery.getInt("poll_id"));
                            if (poll != null) {
                                PollQuestion pollQuestion = new PollQuestion(resultSetExecuteQuery);
                                if (resultSetExecuteQuery.getInt("parent_id") <= 0) {
                                    poll.addQuestion(pollQuestion);
                                } else {
                                    PollQuestion question = poll.getQuestion(resultSetExecuteQuery.getInt("parent_id"));
                                    if (question != null) {
                                        question.addSubQuestion(pollQuestion);
                                    }
                                }
                                poll.lastQuestionId = pollQuestion.id;
                            }
                        } finally {
                        }
                    }
                    if (resultSetExecuteQuery != null) {
                        resultSetExecuteQuery.close();
                    }
                    if (statementCreateStatement != null) {
                        statementCreateStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Throwable th) {
                    if (statementCreateStatement != null) {
                        try {
                            statementCreateStatement.close();
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Throwable th4) {
                        th3.addSuppressed(th4);
                    }
                }
                throw th3;
            }
        }
    }

    public Poll getPoll(int i) {
        return (Poll) this.activePolls.get(Integer.valueOf(i));
    }
}
