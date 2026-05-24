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

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/SaveScoreForTeam.class */
public class SaveScoreForTeam implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(SaveScoreForTeam.class);
    public final GameTeam team;
    public final Game game;

    public SaveScoreForTeam(GameTeam gameTeam, Game game) {
        this.team = gameTeam;
        this.game = game;
    }

    @Override // java.lang.Runnable
    public void run() {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO room_game_scores (room_id, game_start_timestamp, game_name, user_id, team_id, score, team_score) VALUES (?, ?, ?, ?, ?, ?, ?)");
                try {
                    TObjectHashIterator it = this.team.getMembers().iterator();
                    while (it.hasNext()) {
                        GamePlayer gamePlayer = (GamePlayer) it.next();
                        preparedStatementPrepareStatement.setInt(1, this.game.getRoom().getId());
                        preparedStatementPrepareStatement.setInt(2, this.game.getStartTime());
                        preparedStatementPrepareStatement.setString(3, this.game.getClass().getName());
                        preparedStatementPrepareStatement.setInt(4, gamePlayer.getHabbo().getHabboInfo().getId());
                        preparedStatementPrepareStatement.setInt(5, gamePlayer.getTeamColor().type);
                        preparedStatementPrepareStatement.setInt(6, gamePlayer.getScore());
                        preparedStatementPrepareStatement.setInt(7, this.team.getTeamScore());
                        preparedStatementPrepareStatement.addBatch();
                    }
                    preparedStatementPrepareStatement.executeBatch();
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Throwable th) {
                    if (preparedStatementPrepareStatement != null) {
                        try {
                            preparedStatementPrepareStatement.close();
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                    }
                    throw th;
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }
}
