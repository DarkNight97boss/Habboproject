package com.eu.habbo.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface DatabaseLoggable {
   String getQuery();

   void log(PreparedStatement var1) throws SQLException;
}
