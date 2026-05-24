package com.eu.habbo.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/core/DatabaseLoggable.class */
public interface DatabaseLoggable {
    String getQuery();

    void log(PreparedStatement preparedStatement) throws SQLException;
}
