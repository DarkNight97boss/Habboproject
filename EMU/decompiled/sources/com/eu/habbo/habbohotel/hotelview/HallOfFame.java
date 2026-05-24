package com.eu.habbo.habbohotel.hotelview;

import com.eu.habbo.Emulator;
import gnu.trove.map.hash.THashMap;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/hotelview/HallOfFame.class */
public class HallOfFame {
    private static final Logger LOGGER = LoggerFactory.getLogger(HallOfFame.class);
    private final THashMap<Integer, HallOfFameWinner> winners = new THashMap<>();
    private String competitionName;

    public HallOfFame() {
        setCompetitionName("xmasRoomComp");
        reload();
    }

    public void reload() {
        this.winners.clear();
        synchronized (this.winners) {
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    Statement statementCreateStatement = connection.createStatement();
                    try {
                        ResultSet resultSetExecuteQuery = statementCreateStatement.executeQuery(Emulator.getConfig().getValue("hotelview.halloffame.query"));
                        while (resultSetExecuteQuery.next()) {
                            try {
                                HallOfFameWinner hallOfFameWinner = new HallOfFameWinner(resultSetExecuteQuery);
                                this.winners.put(Integer.valueOf(hallOfFameWinner.getId()), hallOfFameWinner);
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
                    } catch (Throwable th3) {
                        if (statementCreateStatement != null) {
                            try {
                                statementCreateStatement.close();
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
            }
        }
    }

    public THashMap<Integer, HallOfFameWinner> getWinners() {
        return this.winners;
    }

    public String getCompetitionName() {
        return this.competitionName;
    }

    void setCompetitionName(String str) {
        this.competitionName = str;
    }
}
