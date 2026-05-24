package com.eu.habbo.habbohotel.hotelview;

import com.eu.habbo.Emulator;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/hotelview/NewsList.class */
public class NewsList {
    private static final Logger LOGGER = LoggerFactory.getLogger(NewsList.class);
    private final ArrayList<NewsWidget> newsWidgets = new ArrayList<>();

    public NewsList() {
        reload();
    }

    public void reload() {
        Connection connection;
        synchronized (this.newsWidgets) {
            this.newsWidgets.clear();
            try {
                connection = Emulator.getDatabase().getDataSource().getConnection();
            } catch (SQLException e) {
                LOGGER.error("Caught SQL exception", e);
            }
            try {
                Statement statementCreateStatement = connection.createStatement();
                try {
                    ResultSet resultSetExecuteQuery = statementCreateStatement.executeQuery("SELECT * FROM hotelview_news ORDER BY id DESC LIMIT 10");
                    while (resultSetExecuteQuery.next()) {
                        try {
                            this.newsWidgets.add(new NewsWidget(resultSetExecuteQuery));
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
        }
    }

    public ArrayList<NewsWidget> getNewsWidgets() {
        return this.newsWidgets;
    }
}
